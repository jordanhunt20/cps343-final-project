/**
 *  ButtonPanel.java
 *
 *  Part of ProjectImage project - perform various operations on an image represented
 *  as a 2-dimensional array of pixel values.
 *
 *  This class represents the button Panel for the GUI.
 *
 *  Copyright (c) 2003, 2004, 2005, 2008, 2009, 2013, 2015 - Russell C. Bjork
 */

package imageeditor;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class ButtonPanel extends JPanel
{
    /** Constructor
     *
     *  @param gui the gui of which this is a part
     */
    ButtonPanel(final ImageEditorGUI gui)
    {
        super();
        setLayout(new GridLayout(3, 0));

        // Create the individual buttons

        JButton lightenButton = new HoldableButton("Lighten");
        add(lightenButton);
        lightenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().lighten();
                gui.redisplayImage(false);
            }
        });

        JButton darkenButton = new HoldableButton("Darken");
        add(darkenButton);
        darkenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().darken();
                gui.redisplayImage(false);
            }
        });

        JButton negativeButton = new JButton("Negative");
        add(negativeButton);
        negativeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().negative();
                gui.redisplayImage(false);
            }
        });

        JButton enhanceContrastButton = new HoldableButton("+ Contrast");
        add(enhanceContrastButton);
        enhanceContrastButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().enhanceContrast();
                gui.redisplayImage(false);
            }
        });

        JButton reduceContrastButton = new HoldableButton("- Contrast");
        add(reduceContrastButton);
        reduceContrastButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().reduceContrast();
                gui.redisplayImage(false);
            }
        });

        JButton flipHButton = new JButton("FlipH");
        add(flipHButton);
        flipHButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().flipHorizontally();
                gui.redisplayImage(false);
            }
        });

        JButton flipVButton = new JButton("FlipV");
        add(flipVButton);
        flipVButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().flipVertically();
                gui.redisplayImage(false);
            }
        });

        JButton encryptDecryptButton = new JButton("En/Decrypt");
        add(encryptDecryptButton);
        encryptDecryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                int seed;
                String keyString = JOptionPane.showInputDialog(
                    gui.getFrame(), "Key?", "", JOptionPane.QUESTION_MESSAGE);
                if (keyString != null)
                {
                    try
                    {
                        seed = Integer.parseInt(keyString);
                        if (seed <= 0)
                            throw new NumberFormatException();
                        gui.getImage().encryptDecrypt(seed);
                        gui.redisplayImage(false);
                    }
                    catch(NumberFormatException exception)
                    {
                        JOptionPane.showMessageDialog(gui.getFrame(),
                            "Key must be a positive integer",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton histogramButton = new JButton("Histogram");
        add(histogramButton);
        histogramButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
// STUDENTS: The following line calls the professor's implementation of
// the histogram option.
// If you write your own version, comment out the line that calls
// setHistogram with a null parameter (which uses the professor's version) and
// remove the comment symbol from the line that calls your version.
    //              gui.showHistogram(null);
            gui.showHistogram(gui.getImage().calculateHistogram());
            }
        });
        JButton halveButton = new JButton("Halve");
        add(halveButton);
        halveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().halve();
                gui.redisplayImage(true);
            }
        });

        JButton shiftLeftButton = new HoldableButton("< Shift");
        add(shiftLeftButton);
        shiftLeftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().shiftHorizontally(-1);
                gui.redisplayImage(false);
            }
        });


        JButton shiftRightButton = new HoldableButton("Shift >");
        add(shiftRightButton);
        shiftRightButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().shiftHorizontally(1);
                gui.redisplayImage(false);
            }
        });


        JButton shiftUpButton = new HoldableButton("Shift ^");
        add(shiftUpButton);
        shiftUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().shiftVertically(-1);
                gui.redisplayImage(false);
            }
        });

        JButton shiftDownButton = new HoldableButton("Shift v");
        add(shiftDownButton);
        shiftDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().shiftVertically(1);
                gui.redisplayImage(false);
            }
        });


        JButton rotateButton = new JButton("Rotate");
        add(rotateButton);
        rotateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                gui.getImage().rotate();
                gui.redisplayImage(true);
            }
        });


        JButton doubleButton = new JButton("Double");
        add(doubleButton);
        doubleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (gui.getImage() == null) return;
                ColorModel savedColorModel = gui.getImage().getColorModel();
                int [] [] savedPixels = gui.getImage().getPixels();
                try
                {
                    gui.getImage().doubleSize();
                    gui.redisplayImage(true);
                }
                catch(OutOfMemoryError exception)
                {
                    gui.setImage(new ProjectImage(savedColorModel,
                                                  savedPixels));
                    gui.redisplayImage(false);
                    JOptionPane.showMessageDialog(gui.getFrame(),
                        "Resulting image would be too big",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JButton blurButton = new JButton("Blur");
        add(blurButton);
        blurButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent E)
            {
                if (gui.getImage() == null) return;
                double [] [] filter = { { 1.0/16.0, 1.0/16.0, 1.0/16.0 },
                                        { 1.0/16.0, 1.0/2.0, 1.0/16.0 },
                                        { 1.0/16.0, 1.0/16.0, 1.0/16.0 }
                                     };
                gui.getImage().applyFilter(filter);
                gui.redisplayImage(false);
            }
        });


        JButton sharpenButton = new JButton("Sharpen");
        add(sharpenButton);
        sharpenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent E)
            {
                if (gui.getImage() == null) return;
                double [] [] filter = { { -0.1, -0.1, -0.1 },
                                        { -0.1, 1.8, -0.1 },
                                        { -0.1, -0.1, -0.1 }
                                     };
                gui.getImage().applyFilter(filter);
                gui.redisplayImage(false);
            }
        });


        JButton edgeButton = new JButton("Edges");
        add(edgeButton);
        edgeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent E)
            {
                if (gui.getImage() == null) return;
                double [] [] filter = { { -1, -1, -1, -1, -1 },
                                        { -1,  1,  1,  1, -1 },
                                        { -1,  1,  8,  1, -1 },
                                        { -1,  1,  1,  1, -1 },
                                        { -1, -1, -1, -1, -1 }
                                     };
                gui.getImage().applyFilter(filter);
                gui.redisplayImage(false);
            }
        });

    }
}
