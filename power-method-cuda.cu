/*
 * $Smake: nvcc -O2 -o %F %f wtime.c -I ../include -lhdf5
 *
 * Matrix-matrix product
 */

#include <cuda.h>
#include <hdf5.h>
#include <cstdio>
#include <cstdlib>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <cblas.h>
#include <cmath>
#include <iostream>
#include "wtime.h"



/// Check return values from HDF5 routines
#define CHKERR(status,name) if ( status < 0 ) \
     fprintf( stderr, "Error: nonzero status (%d) in %s\n", status, name )

// Compute index into single linear array for matrix element (i,j)
#define IDX(i,j,stride) ((i)*(stride)+(j)) // row major (c/c++ ordering)

//----------------------------------------------------------------------------
// Display matrix values on standard output

void dumpMatrix(
    double* a,      // in  - address of matrix data
    int rows,       // in  - number of rows in matrix
    int cols,       // in  - number of cols in matrix
    int stride      // in  - row length in memory (assuming C/C++ storage)
    )
{
    for ( int i = 0; i < rows; i++ )
    {
        for ( int j = 0; j < cols; j++ )
        {
            printf( " %8.2f", a[IDX(i,j,stride)] );
        }
        printf( "\n" );
    }
    printf( "\n" );
    fflush( stdout );
}

//----------------------------------------------------------------------------//
//----------------------------------------------------------------------------//
//					MATRIX OPERATIONS										  //
//----------------------------------------------------------------------------//
//----------------------------------------------------------------------------//

/*			- 					tested
 * Form vector product result = Ab
 * @param1 double* vector result
 * @param2 double* matrix to multiply
 * @param3 double* vector to multiply
 * @param4 int number of rows in matrix
 */
 __global__ void mat_vec_mult(double* result, double* A, double* b, int numRows)
 {
     int row = blockIdx.y * blockDim.y + threadIdx.y;
     int col = blockIdx.x * blockDim.x + threadIdx.x;
     double sum = 0.0;
     int tid = threadIdx.x + blockIdx.x * blockDim.x;
     if (tid < numRows * numRows) {
         for (int i = 0; i < numRows; i++) {
             sum += A[i * numRows + tid] * b[i];
         }
         result[row * numRows + col] = sum;
     }
 }

 /*
 * Form int product c = a^T * b  -  tested
*/
void vec_vec_mult( double* c, double* a, int rows, double* b)
{
	double sum = 0.0;
    int i;
	for (i = 0; i < rows; i++)
	{
		sum += a[i] * b[i];
	}
    *c = sum + 0.0;
}

/*
 * Form vector quotient c = a/||a||  -   tested
*/
void normalize( double* c, double* a, int rows )
{
	double magnitude = 0.0;
    for (int i = 0; i < rows; i++)
	{
		magnitude += pow(a[i], 2.0);
	}
    for (int i = 0; i < rows; i++)
	{
		c[i] = a[i] / sqrt(magnitude);
    }
}

//----------------------------------------------------------------------------
//----------------------------------------------------------------------------


int main( int argc, char* argv[] )
{
	double tolerance = pow(10, -6.0); // default value
    long numIterations = 500; // default value
    long blockSize = 16; // default value

    double* a;             // pointer to matrix data
    hid_t file_id;         // HDF5 id for file
    hid_t dataspace_id;    // HDF5 id for dataspace in file
    hid_t dataset_id;      // HDF5 id for dataset in file
    hid_t memspace_id;     // HDF5 id for dataset in memory
    hsize_t* dims;         // matrix dimensions
    herr_t status;         // HDF5 return code
    int ndim;              // number of dimensions in HDF5 dataset

	// Process command line
	int c;

    const char* filename = argv[1];
    const char* path = "/A/value";

    bool quiet = false;

    // check for switches
    while ( ( c = getopt( argc, argv, "e:m:s:q" ) ) != -1 )
	{
	    switch( c )
		{
		case 'e':
		    tolerance = atof(optarg);
            if (tolerance <= 0)
            {
                fprintf(stderr, "tolerance must be positive\n");
                fprintf(stderr, "got: %f\n", tolerance);
                exit(EXIT_FAILURE);
            }
		    break;
		case 'm':
		    numIterations = atol(optarg);
	    	if (numIterations <= 0)
			{
			    fprintf(stderr, "number of iterations must be positive\n");
			    fprintf(stderr, "got: %ld\n", numIterations);
			    exit(EXIT_FAILURE);
			}
		    break;
        case 's':
            blockSize = atol( optarg );
            if ( blockSize <= 0 )
            {
                fprintf(stderr, "block size must be positive\n");
                fprintf(stderr, "got: %ld\n", blockSize);
                exit(EXIT_FAILURE);
            }
            break;
        case 'q':
            quiet = true;
            break;
		default:
		    fprintf(stderr, "default usage: %s [-e tolerance, -m numIterations -s blockSize, -q]\n", argv[0]);
		    return EXIT_FAILURE;
		}
	}

	//----------------------------------------------------------------------------//
	//----------------------------------------------------------------------------//
	//					READ MATRIX     										  //
	//----------------------------------------------------------------------------//
	//----------------------------------------------------------------------------//

	double startTime = wtime();

    // Open existing HDF5 file
    file_id = H5Fopen( filename, H5F_ACC_RDONLY, H5P_DEFAULT );
    if ( file_id < 0 ) exit( EXIT_FAILURE );

    // Open dataset in file
    dataset_id = H5Dopen( file_id, path, H5P_DEFAULT );
    if ( dataset_id < 0 ) exit( EXIT_FAILURE );

    // Determine dataset parameters
    dataspace_id = H5Dget_space( dataset_id );
    ndim = H5Sget_simple_extent_ndims( dataspace_id );
    dims = new hsize_t [ndim];

    // Get dimensions for dataset
    ndim = H5Sget_simple_extent_dims( dataspace_id, dims, NULL );
    if ( ndim != 2 )
    {
        fprintf( stderr, "Expected dataspace to be 2-dimensional " );
        fprintf( stderr, "but it appears to be %d-dimensional\n", ndim );
        exit( EXIT_FAILURE );
    }

    // Create memory dataspace
    memspace_id = H5Screate_simple(ndim, dims, NULL);
    if (memspace_id < 0) exit(EXIT_FAILURE);

    // Allocate memory for matrix and read data from file
    a = new double [dims[0] * dims[0]];
    status = H5Dread(dataset_id, H5T_NATIVE_DOUBLE, memspace_id,
                      dataspace_id, H5P_DEFAULT, a);
    CHKERR(status, "H5Dread()");

    // Close all remaining HDF5 objects
    CHKERR(H5Sclose(memspace_id), "H5Sclose()");
    CHKERR(H5Dclose(dataset_id), "H5Dclose()");
    CHKERR(H5Sclose(dataspace_id), "H5Sclose()");
    CHKERR(H5Fclose(file_id), "H5Fclose()");

	double endTime = wtime();
	double readTime = endTime - startTime;

    startTime = endTime;

//----------------------------------------------------------------------------
//----------------------------------------------------------------------------

    // Power Method Algorithm
	int cols = (int) dims[0];
    double x [cols]; // corresponding normalized eigenvector
    double y [cols]; // placeholder

    //initial eigenvector estimate (using y as a placeholder)
    for ( int i = 0; i < cols; i++) y[i] = 1.0;

    //normalize x (based on placeholder y)
    normalize(x, y, cols);

    // initialized to any value
    double lambda = 0.0;

    // make sure |lambda-lambda_0| > tolerance
    double lambda_0 = lambda + 2 * tolerance;

    // determine matrix size in bytes
    size_t matrix_size = cols * cols * sizeof(double);

    // determine vector size in bytes
    size_t vector_size = cols * sizeof(double);

    // set block size and number of blocks
    dim3 block_size(blockSize, blockSize);
  	dim3 num_blocks((cols - 1 + block_size.x) / block_size.x,
                   (cols - 1 + block_size.y) / block_size.y);

    // declare pointers to matrix and vectors in device memory and allocate memory
    double *a_d, *x_d, *y_d;
    cudaMalloc((void**) &a_d, matrix_size); // matrix
    cudaMalloc((void**) &x_d, vector_size); // eigenvalue
    cudaMalloc((void**) &y_d, vector_size); // placeholder

    // set a_d to matrix a, and copy it to device
    cudaMemcpy(a_d, a, matrix_size, cudaMemcpyHostToDevice);
    cudaMemcpy(x_d, x, vector_size, cudaMemcpyHostToDevice);

    long k = 0;
	while ((std::abs(lambda - lambda_0) >= tolerance) && k <= numIterations)
	{
         // compute next eigenvector estimate on device
        mat_vec_mult<<< num_blocks, block_size >>>(y_d, a_d, x_d, cols);

        // retrieve result from device and store on host
        cudaMemcpy(y, y_d, vector_size, cudaMemcpyDeviceToHost);
        // for(int i = 0; i < cols/2; i++)
        //     std::cout << y[i] << std::endl;
		lambda_0 = lambda; 						      	// previous eigenvalue estimate
 		vec_vec_mult(&lambda, x, cols, y);    			// compute new estimate
		normalize(x, y, cols );						// normalize eigenvector estimate

        // copy x from host to device
        cudaMemcpy(x_d, x, vector_size, cudaMemcpyHostToDevice);
		k++;
	}

    double executionTime = wtime() - startTime;

    if (quiet) {
        printf("\n%f %ld %f\n", lambda, k, readTime + executionTime);
    } else {
        printf("\nDominant Eigenvalue: %f\nRead Time: %f\nNumber Of Iterations: %ld\nExecution Time: %f\n", lambda, readTime, k, executionTime);
        printf("Number of Processes: %d\nTotal Time: %f\nNumber of Processes * Total Time: %f\nTime Per Loop: %f\n\n", 1, readTime + executionTime, readTime + executionTime, executionTime / (k + 0.0));
    }
//----------------------------------------------------------------------------
//----------------------------------------------------------------------------
    // Clean up and quit
    delete [] a;
    delete [] dims;
}
