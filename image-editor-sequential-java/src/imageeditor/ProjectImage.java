/*Jordan Hunt
*CPS122
*Professor Bjork
*Project 1
*Due 2/11/15

 * ProjectImage.java
 *  Part of ImageEditor project - perform various operations on an image represented
 *  as a 2-dimensional array of pixel values.  Completion of the methods of this 
 *  class constitutes CPS122 Project 1
 *
 * Copyright (c) 2003, 2004, 2005, 2008, 2009, 2013 - Russell C. Bjork
 *
 */

package imageeditor; //Imports the imageeditor methods


import java.io.*;   //Imports various Java packages 
import java.awt.image.ColorModel;  //Imports various Java packages 
import java.util.Random;   //Imports various Java packages 

/*
Creates methods for a picture object. These methods work on either a Grayscale 
picture or on a color picture.

Methods include: Lighten, Darken, Negative, +Contrast, -Contrast, Horizontal and 
Vertical Flipping, Encryption/Decryption, Creation of a Histogram, Cropping to
half size, doubling the size, shifting the image horizontally or vertically, 
counterclockwize routation, filtering for blur, sharpen, or edge detection.

*/
public class ProjectImage
{
    /** Constructor
     *
     *  @param colorModel the color model to use for interpreting the
     *         pixel values
     *  @param pixels the data content of this image - a two-dimensional array
     *      having height rows, each containing width values.  If this is a
     *      grayscale image, then each element lies in the range 0 .. 255.  If
     *      this is a color image, then each element is a packed 24 bit color
     *      with alpha value
     */
    public ProjectImage(ColorModel colorModel, int [] [] pixels)
    { 
        this.colorModel = colorModel;
        this.pixels = pixels;
        this.height = pixels.length;
        this.width = pixels[0].length;
    }
    
    /**************************************************************************
     * Accessor for information about this image
     *************************************************************************/
     
    /** Find out if this class is capable of handling color images.  This method
     *  is made static so it can be called before an object of this class is
     *  created.
     *
     *  @return true if this class can handle color images.
     *
     *  If the return value is false, this class will assume that images are
     *  represented by grayscale values in the range 0 .. 255.  If the return
     *  value is true, this class can also handle images that are represented by
     *  packed color values, to be interpreted by colorModel.
     */
    public static boolean isColorCapable()
    {
        return true;
    }
    
    /**************************************************************************
     * Accessors for information about this image
     *************************************************************************/
     
    /** Get the pixels
     *
     *  @return the pixels for this image - represented as a 2 dimensional
     *          array of integers, to be interpreted according to the
     *          color model
     */
    public int [] [] getPixels()
    {
        return pixels;
    }
    
    /** Get the pixels of this image as a one-dimensional array of packed RGB
     *  values in the standard representation used internally by the Java
     *  image routines
     *
     *  @return the pixels of this image - represented as a 1 dimensional
     *          array of integers representing packed RGB values
     */
    public int [] getPixelsIntRGB()
    {
        int [] result = new int[height * width];
        for (int row = 0; row < height; row ++)
            for (int col = 0; col < width; col ++)
                result[row * width + col] = isColor()
                  ? pixels[row][col]
                  : pixels[row][col] * 0x10101; // Makes all three colors same
        return result;
    }

    /** Get the width of this image
     *
     *  @return the width of this image
     */
    public int getWidth()
    {
        return width;
    }
    
    /** Get the height of this image
     *
     *  @return the height of this image
     */
    public int getHeight()
    {
        return height;
    }
    
    /** Get the color model used by this image
     *
     *  @return the color model for this image
     */
    public ColorModel getColorModel()
    {
        return colorModel;
    }
    
    /** Check to see whether this image is color
     * 
     *  @return true if this image is color
     */
    public boolean isColor()
    {
        return ! (colorModel instanceof GrayScaleColorModel);
    }
    
    /**************************************************************************
     * Mutators to alter this image.  Some of these will alter the
     * image "in place", while others will change the width and/or height,
     * resulting in the creation of a new array of pixels.
     *************************************************************************/
     
    // CPS122 STUDENTS - ADD YOUR METHODS WITH APPROPRIATE COMMENTS IN THE
    // APPROPRIATE ORDER BELOW, THEN REMOVE THESE LINES.

    /** Lighten the image
     */
    public void lighten()
    {
        for (int row = 0; row < height; row ++)
            for (int col = 0; col < width; col ++)
            {
                int red = colorModel.getRed(pixels[row][col]) + LIGHTEN_DARKEN_AMOUNT;
                int green = colorModel.getGreen(pixels[row][col]) + LIGHTEN_DARKEN_AMOUNT; 
                int blue = colorModel.getBlue(pixels[row][col]) + LIGHTEN_DARKEN_AMOUNT;
                int color = pack(red,green,blue);
                pixels[row][col] = color;
            }
    }
    /** Darken the image
     * /
     */
    public void darken()
    {
        for (int row = 0; row < height; row++)
            for(int col = 0; col < width; col ++)
            {
                int red = colorModel.getRed(pixels[row][col]) - LIGHTEN_DARKEN_AMOUNT;
                int green = colorModel.getGreen(pixels[row][col]) - LIGHTEN_DARKEN_AMOUNT; 
                int blue = colorModel.getBlue(pixels[row][col]) - LIGHTEN_DARKEN_AMOUNT;
                int color = pack(red,green,blue);
                pixels[row][col] = color;
            }
    }
    /**Each pixel is changed to the max possible brightness minus its current 
    brightness. In other words the image is changed to its negative. Or, if it 
    * is a color picture, it is changed to the negative of its RGB values.
    */
    public void negative()
    {
        for (int row = 0; row < height; row++)
            for(int col = 0; col < width; col ++)
            {
                int red = MAX_BRIGHTNESS - colorModel.getRed(pixels[row][col]);
                int green = MAX_BRIGHTNESS - colorModel.getGreen(pixels[row][col]); 
                int blue = MAX_BRIGHTNESS - colorModel.getBlue(pixels[row][col]);
                int color = pack(red,green,blue);
                pixels[row][col] = color;
            }
    }
    /**
     * Every pixel that is less bright than the average brightness is made less 
     * bright, and every pixel that is brighter than the average brightness is 
     * made more bright. The Contrast of the picture is enhanced. Also works for 
     * color pictures, but makes colors stand out instead of brightness.
     */
    public void enhanceContrast()
    {
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        int numberOfPixels = 0;
        for (int row = 0; row < height; row++)
            for(int col = 0; col < width; col ++)
            {
                totalRed += colorModel.getRed(pixels[row][col]);
                totalGreen += colorModel.getGreen(pixels[row][col]); 
                totalBlue += colorModel.getBlue(pixels[row][col]);
                numberOfPixels += 1;
            }
        int averageRed = totalRed / numberOfPixels;
        int averageGreen = totalGreen / numberOfPixels;
        int averageBlue = totalBlue / numberOfPixels;
        int newRed = 0;
        int newGreen = 0;
        int newBlue = 0;
        for (int row = 0; row < height; row++)
            for(int col = 0; col < width; col ++)
            {
                if (colorModel.getRed(pixels[row][col]) < averageRed)
                    newRed = colorModel.getRed(pixels[row][col]) - 1;
                else if (colorModel.getRed(pixels[row][col]) > averageRed)
                    newRed = colorModel.getRed(pixels[row][col]) + 1;
                if (colorModel.getGreen(pixels[row][col]) < averageGreen)
                    newGreen = colorModel.getGreen(pixels[row][col]) - 1;
                else if (colorModel.getGreen(pixels[row][col]) > averageGreen)
                    newGreen = colorModel.getGreen(pixels[row][col]) + 1;
                if (colorModel.getBlue(pixels[row][col]) < averageBlue)
                    newBlue = colorModel.getBlue(pixels[row][col]) - 1;
                else if (colorModel.getBlue(pixels[row][col]) > averageBlue)
                    newBlue = colorModel.getBlue(pixels[row][col]) + 1;
                pixels[row][col] = pack(newRed, newGreen, newBlue);
            }
    }
    /**
     * Every pixel that is more bright than the average brightness is made less 
     * bright, and every pixel that is less bright than the average brightness 
     * is made more bright. The Contrast of the picture is reduced. Also works for color pictures, but checks for color instead of brightness.
     */
    public void reduceContrast()
    {
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        int numberOfPixels = 0;
        for (int row = 0; row < height; row++)
            for(int col = 0; col < width; col ++)
            {
                totalRed += colorModel.getRed(pixels[row][col]);
                totalGreen += colorModel.getGreen(pixels[row][col]); 
                totalBlue += colorModel.getBlue(pixels[row][col]);
                numberOfPixels += 1;
            }
        int averageRed = totalRed / numberOfPixels;
        int averageGreen = totalGreen / numberOfPixels;
        int averageBlue = totalBlue / numberOfPixels;
        for (int row = 0; row < height; row++)
            for(int col = 0; col < width; col ++)
            {
                
                int newRed = colorModel.getRed(pixels[row][col]);
                int newGreen = colorModel.getGreen(pixels[row][col]);
                int newBlue = colorModel.getBlue(pixels[row][col]);
                if (colorModel.getRed(pixels[row][col]) > averageRed)
                    newRed = colorModel.getRed(pixels[row][col]) - 1;
                else if (colorModel.getRed(pixels[row][col]) < averageRed)
                    newRed = colorModel.getRed(pixels[row][col]) + 1;
                if (colorModel.getGreen(pixels[row][col]) > averageGreen)
                    newGreen = colorModel.getGreen(pixels[row][col]) - 1;
                else if (colorModel.getGreen(pixels[row][col]) < averageGreen)
                    newGreen = colorModel.getGreen(pixels[row][col]) + 1;
                if (colorModel.getBlue(pixels[row][col]) > averageBlue)
                    newBlue = colorModel.getBlue(pixels[row][col]) - 1;
                else if (colorModel.getBlue(pixels[row][col]) < averageBlue)
                    newBlue = colorModel.getBlue(pixels[row][col]) + 1;
                
                    
                pixels[row][col] = pack(newRed, newGreen, newBlue);
            }
    }
    
        /** 
     * Flips a picture horizontally
    */
    public void flipHorizontally()
    {
        int canvas [] [] = new int [height] [width];
        for(int row = 0; row < height; row++)
        {
            for(int col = 0; col < width; col++)
            {
                canvas[row][col] = pixels[row][col];
            }
        }
        for(int row = 0; row < height; row++)
        {
            for(int col = 0; col < width; col++)
            {
                pixels [row] [col] = canvas [row] [width - 1 - col]; 
            }
        }
    }

    
    /*
    Flips a picture vertically.
    */
    
    public void flipVertically()
    {
        int canvas [] [] = new int [height] [width];
        for(int row = 0; row < height; row++)
        {
            for(int col = 0; col < width; col++)
            {
                canvas[row][col] = pixels[row][col];
            }
        }
        for(int row = 0; row < height; row++)
        {
            for(int col = 0; col < width; col++)
            {
                pixels [row] [col] = canvas [height - 1 - row] [col]; 
            }
        }
    }   
    
    /*
    Encrypts a picture, then has the ability to decrypt it if given the same key.
    @param1 key is the key that will decrypt the picture after its encryption.
    */
    public void encryptDecrypt(int key)
    {
        Random randnum = new Random();
        randnum.setSeed(key);
        int colorEncrypter = 16777216; //works with color objects in place of (MAX_BRIGHTNESS + 1) for the parameter given to nextInt.
        for (int row = 0; row < height; row++)
        {
            
            for(int col = 0; col < width; col ++)
            {
                if (isColor())
                {
                    int random = randnum.nextInt(colorEncrypter);
                    pixels[row][col] = (pixels[row][col]) ^ random; 
                }
                else if (!isColor())
                {
                    int random = randnum.nextInt(MAX_BRIGHTNESS + 1);
                    pixels[row][col] = (pixels[row][col]) ^ random;
                }
            }
        }
    }
      
    /**
     * Calculate histogram for the image
     * @return 256-element array, with each element representing a count of 
     * the number of pixels in the image having that particular brightness or,
     * if using a color picture, color.
     */
    
    public int [] calculateHistogram()
    {
        int [] array = new int[256];
        for (int row = 0; row < height; row++)
        {
            
            for(int col = 0; col < width; col ++)
            {
                int red = colorModel.getRed(pixels[row][col]);
                int green = colorModel.getGreen(pixels[row][col]); 
                int blue = colorModel.getBlue(pixels[row][col]);
                int color = (red + green + blue) / 3;
                array[color]++;
            }
        }
        return array;
    }
    
    /** Scale the image by a factor of 0.5 in each dimension
     */
    public void halve()
    {
        // We need to build a new image in a separate array, and then make
        // it our current image
        
        int newWidth = width / 2;
        int newHeight = height / 2;
        int [] [] newPixels = new int [newHeight] [newWidth];
        int [] red = new int [pixels.length];
       
        
        // Each pixel in the new image is an average of a 2 x 2 square of pixels
        // in the original image
        
        for (int row = 0; row < newHeight; row ++)
            for (int col = 0; col < newWidth; col ++)
                newPixels[row][col] = averagePixels(
                        averagePixels(pixels[2*row][2*col], pixels[2*row+1][2*col]),
                        averagePixels(pixels[2*row][2*col+1], pixels[2*row+1][2*col+1]));
            
        // Now replace the current image with the one we just created
        
        width = newWidth;
        height = newHeight;
        pixels = newPixels;
    }
    /*
    Shifts a picture horizontally (left or right) one pixel. The pixel on the
    Extreme side reverts to the starting point.
    */
    public void shiftHorizontally(int direction)
    {
        int canvas [] [] = new int [height] [width];
        if (direction < 0) //shift left
        {
            for (int row = 0; row < height; row ++)
            {
                canvas[row][width - 1] = pixels[row][0];
                for (int col = 0; col < width - 1; col ++)
                {
                    canvas[row][col] = pixels[row][col + 1];
                }
            }
        }
        else if (direction > 0) //shift right
        {
            for (int row = 0; row < height; row ++)
            {
                canvas[row][0] = pixels[row][width - 1];
                for (int col = 1; col < width; col ++)
                {
                    canvas[row][col] = pixels[row][col - 1];
                }
            }
        }
        pixels = canvas;
    }
    
    /*
    Shifts a picture vertically (up or down) one pixel. The pixel on the
    Extreme side reverts to the starting point.
    */
    public void shiftVertically(int direction)
    {
        int canvas [] [] = new int [height] [width];
        if (direction < 0) //shift up
        {
            for (int col = 0; col < width; col ++)
            {
                canvas[height - 1][col] = pixels[0][col];
                for (int row = 0; row < height - 1; row ++)
                {
                    canvas[row][col] = pixels[row + 1][col];
                }
            }
        }
        else if (direction > 0) //shift down
        {
            for (int col = 0; col < width; col ++)
            {
                canvas[0][col] = pixels[height - 1][col];
                for (int row = 1; row < height; row ++)
                {
                    canvas[row][col] = pixels[row - 1][col];
                }
            }
        }
        pixels = canvas;
    }
    /*
    Rotates a picture clockwise once (90 degrees).
    */
    public void rotate()
    {
        int newHeight = width;
        int newWidth = height;
        int [][] canvas = new int [newHeight] [newWidth];
        for (int row = 0; row < height; row ++)
        {
            for (int col = 0; col < width; col ++)
            {
                canvas[col][newWidth - row - 1] = pixels[row][col];
            }
        }
        width = newWidth;
        height = newHeight;
        pixels = canvas;
    }
    /*
    Doubles the size of the image.
    */
    public void doubleSize()
    {
        int newWidth = (width * 2) - 1;
        int newHeight = (height * 2) - 1;
        int [] [] canvas = new int [newHeight] [newWidth];
        for (int row = 0; row < height - 1; row ++)
        {
            for (int col = 0; col < width - 1; col ++)
            {
                canvas[row * 2][col * 2] = (pixels[row][col]); // Original Array
                canvas[1 + (row * 2)][col * 2] = 
                        averagePixels((pixels[row][col]), (pixels[row + 1][col]));
                canvas[row * 2][col * 2 + 1] = 
                        averagePixels((pixels[row][col]), (pixels[row][col + 1]));
                canvas[1 + (row * 2)][1 + (col * 2)] = 
                        averagePixels((pixels[row][col]), (pixels[row + 1][col + 1]));
            }
        }
        width = newWidth;
        height = newHeight;
        pixels = canvas;
    }
    
    /**
     * Apply a filter to this image
     * @param filter a square array of doubles specifying the filter to 
     * apply- the number of rows and columns must be odd.
     */
    
    public void applyFilter(double [][] filter)
    {
        int [][] canvas = new int [height][width]; //temporary canvas
        int lengthFilter = filter.length; //width or height of the filter
        double [] filterArray = new double[lengthFilter * lengthFilter]; //1D array of the values in filter.
        int filterArrayLength = filterArray.length; //Length of the 1D array of filter.
        double [] extraArray = new double[lengthFilter];
        int edge = (lengthFilter - 1) / 2; //The number of pixels on the edge we won't filter.
        

        //Creating a 1D array of the values from filter.
        for (int i = 0; i < lengthFilter; i++)
        {
            for(int j = 0; j < lengthFilter; j++)
            {
                extraArray[j] = filter[i][j];
            }
            for (int j = 0; j < lengthFilter; j++)
            {
                filterArray[j + (lengthFilter * i)] = extraArray[j];
            }
        }
        
        
        
        
        
        //Filtered Section
        
        for (int row = edge; row < height - edge; row ++)
        {
            for (int col = edge; col < width - edge; col ++)
            {
                int newValueRed = 0;
                int newValueGreen = 0;
                int newValueBlue = 0;
                int i = 0;
                for(int a = -1 * edge; a < 1 + edge; a++)
                {
                    for(int b = -1 * edge; b < 1 + edge; b++)
                    {
                        newValueRed += colorModel.getRed(pixels[row + a][col + b]) * filterArray[i];
                        newValueGreen += colorModel.getGreen(pixels[row + a][col + b]) * filterArray[i];
                        newValueBlue += colorModel.getBlue(pixels[row + a][col + b]) * filterArray[i];
                        i++;
                    }
                }
            
                canvas[row][col] = pack(newValueRed,newValueGreen,newValueBlue);
            }
        }
        
        
        
        //Edges
        for (int row = 0; row < height; row++) //Left and Right Edges
        {
            for(int col = 0; col < edge; col++)
            {
                canvas[row][col] = pixels[row][col]; //Left Edge
            }
            for(int col = width - edge; col < width; col++)
            {
                canvas[row][col] = pixels[row][col]; //Right Edge
            }
        }
        for (int col = 0; col < width; col++) //Top and Bottom Edges
        {
            for(int row = 0; row < edge; row++)
            {
                canvas[row][col] = pixels[row][col]; //Top Edge
            }
            for(int row = height - edge; row < height; row++)
            {
                canvas[row][col] = pixels[row][col]; //Bottom Edge
            }
        }
        
        pixels = canvas;
    }
      
    
    /* *************************************************************************
     * Utility methods for working with colorized images
     * ************************************************************************/
    
    
    
    /** If we are using a full color representation of the image, then pack 
     *  three colors into a single pixel, along with a 100% alpha value.
     *  If any color value is greater than the maximum or less than the minimum,
     *  it is forced to the maximum or minimum value (as the case may be)
     *  before packing.  
     *
     *  @param red the red value to pack
     *  @param green the green value to pack
     *  @param blue the blue value to pack
     *  @return the packed RGB representation for this pixel
     */
    private int pack(int red, int green, int blue)
    {
        if (red > MAX_BRIGHTNESS)
            red = MAX_BRIGHTNESS;
        else if (red < MIN_BRIGHTNESS)
            red = MIN_BRIGHTNESS;
            
        if (green > MAX_BRIGHTNESS)
            green = MAX_BRIGHTNESS;
        else if (green < MIN_BRIGHTNESS)
            green = MIN_BRIGHTNESS;
            
        if (blue > MAX_BRIGHTNESS)
            blue = MAX_BRIGHTNESS;
        else if (blue < MIN_BRIGHTNESS)
            blue = MIN_BRIGHTNESS;
            
        // If we are using a simple 8 bit gray-scale representation for the 
        // image, then all colors are the same and we can simply return any one
        // of them.  Otherwise, we must let the color model pack them correctly
        
        if (colorModel instanceof GrayScaleColorModel)
            return red;
        else
        {
            int [] components = { red, green, blue, 255 };
            return colorModel.getDataElement(components, 0);
        }
    }
    
    /** Average two pixels
     *
     *  @param pixel1 the first of the two pixels
     *  @param pixel2 the second of the two pixels
     *  @return a pixel which is equal to their average
     */
    private int averagePixels(int pixel1, int pixel2)
    {
        int red = (colorModel.getRed(pixel1) + colorModel.getRed(pixel2)) / 2;
        int green = (colorModel.getGreen(pixel1) + colorModel.getGreen(pixel2)) / 2;
        int blue = (colorModel.getBlue(pixel1) + colorModel.getBlue(pixel2)) / 2;
        
        return pack(red, green, blue);
    }
        
   
    
    // Image data
    
    private ColorModel colorModel;
    private int [] [] pixels;
    private int width;
    private int height;
    
    // Constants
    
    private static final int LIGHTEN_DARKEN_AMOUNT = 3;
    private static final int MAX_BRIGHTNESS = 255;
    private static final int MIN_BRIGHTNESS = 0;

    private int nextInt(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
    

