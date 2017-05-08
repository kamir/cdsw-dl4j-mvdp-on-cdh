package image;

import org.nd4j.linalg.api.ndarray.INDArray;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Transformers are used to translate between multiple data representations.
 *
 * Examples:
 *
 * (A)  PNG => feature vector
 * (B)  Solr-Document => feature vectore
 * (C)  File - Metadata => feature vector
 *
 * etc.
 *
 */
public class Transformers {


    public static void writeImage(INDArray array, File file) {

        System.out.println("Array.rank(): " + array.rank());
        System.out.println("Size(-1): " + array.size(-1));
        System.out.println("Size(-2): " + array.size(-2));

        BufferedImage imageToRender = new BufferedImage(array.columns(),array.rows(),BufferedImage.TYPE_BYTE_GRAY);
        for( int x = 0; x < array.columns(); x++ ){
            for (int y = 0; y < array.rows(); y++ ) {
                System.out.println("x: " + (x) + " y: " + y);
                imageToRender.getRaster().setSample(x, y, 0, (int) (255 * array.getRow(y).getDouble(x)));
            }
        }

        try {
            ImageIO.write(imageToRender, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static BufferedImage getBufferedImage(INDArray array) {

        System.out.println("Array.rank(): " + array.rank());
        System.out.println("Size(-1): " + array.size(-1));
        System.out.println("Size(-2): " + array.size(-2));

        BufferedImage imageToRender = new BufferedImage(array.columns(),array.rows(),BufferedImage.TYPE_BYTE_GRAY);
        for( int x = 0; x < array.columns(); x++ ){
            for (int y = 0; y < array.rows(); y++ ) {
                System.out.println("x: " + (x) + " y: " + y);
                imageToRender.getRaster().setSample(x, y, 0, (int) (255 * array.getRow(y).getDouble(x)));
            }
        }

        return imageToRender;
    }

}
