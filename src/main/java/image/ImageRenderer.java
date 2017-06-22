package image;

import org.datavec.image.loader.ImageLoader;

import org.nd4j.linalg.api.ndarray.INDArray;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Render ndarrays as images
 *
 * @author Adam Gibson
 */
public class ImageRenderer {


    /**
     * NDArrays are used by Deeplearning4J.
     *
     * In order to inspect such an array, we render it as a BufferedImage and write it into a file to local disc.
     *
     * @param image
     * @param path
     * @throws IOException
     */
    public static void render(INDArray image, String path) throws IOException {

        BufferedImage imageToRender = null;

        if (image.rank() == 3) {
            ImageLoader loader = new ImageLoader(image.size(-1), image.size(-2), image.size(-3));
            imageToRender = new BufferedImage(image.size(-1), image.size(-2), BufferedImage.TYPE_3BYTE_BGR);
            loader.toBufferedImageRGB(image, imageToRender);
        }
        else if (image.rank() == 2) {
            imageToRender = new BufferedImage(image.size(-1), image.size(-2), BufferedImage.TYPE_BYTE_GRAY);
            for (int i = 0; i < image.length(); i++) {
                imageToRender.getRaster().setSample(i % image.size(-1), i / image.size(-2), 0, (int) (255 * image.getDouble(i)));
            }

        }

        ImageIO.write(imageToRender, "png", new File(path));

    }


}