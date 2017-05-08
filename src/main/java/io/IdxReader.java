package io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * As part of our demo (Phase 1) we need to import reference data (training and test dataset) from a known
 * source, the MNIST database.
 *
 * This program reads the binary database with 60.000 images and the related labels and writes a PNG file
 * to the outputPath.
 *
 * The known label becomes the first character of the exported file.
 */

public class IdxReader {

    public static String outputPath = "CBIR_Project/images/MNIST_Database_ARGB/";
    public static String dbFolder = "MNIST_images_raw/";



    public static void main(String[] args) {

        // TODO Auto-generated method stub
        FileInputStream inImage = null;
        FileInputStream inLabel = null;

        String inputImagePath = dbFolder + "/images-idx3-ubyte";
        String inputLabelPath = dbFolder + "/labels-idx1-ubyte";

        File folderOut = new File(outputPath);
        folderOut.mkdirs();

        int[] hashMap = new int[10];

        try {
            inImage = new FileInputStream(inputImagePath);
            inLabel = new FileInputStream(inputLabelPath);

            int magicNumberImages = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());
            int numberOfImages = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());
            int numberOfRows = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());
            int numberOfColumns = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());

            int magicNumberLabels = (inLabel.read() << 24) | (inLabel.read() << 16) | (inLabel.read() << 8) | (inLabel.read());
            int numberOfLabels = (inLabel.read() << 24) | (inLabel.read() << 16) | (inLabel.read() << 8) | (inLabel.read());

            BufferedImage image = new BufferedImage(numberOfColumns, numberOfRows, BufferedImage.TYPE_INT_ARGB);
            int numberOfPixels = numberOfRows * numberOfColumns;
            int[] imgPixels = new int[numberOfPixels];

            for (int i = 0; i < numberOfImages; i++) {

                if (i % 100 == 0) {
                    System.out.println("Number of images extracted: " + i);
                }

                for (int p = 0; p < numberOfPixels; p++) {
                    int gray = 255 - inImage.read();
                    imgPixels[p] = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
                }

                image.setRGB(0, 0, numberOfColumns, numberOfRows, imgPixels, 0, numberOfColumns);

                int label = inLabel.read();

                hashMap[label]++;


                File outputfile = new File(outputPath + label + "_0" + hashMap[label] + ".png");
                ImageIO.write(image, "png", outputfile);


                // Here we can also add the code for direct ingestion to Kudu table, using the
                // kudu.KuduImageStore.
                //
                // T.B.D.



            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (inImage != null) {
                try {
                    inImage.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (inLabel != null) {
                try {
                    inLabel.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}

