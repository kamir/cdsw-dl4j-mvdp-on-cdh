package workflow;

import image.ImageConverter;
import java.io.File;

/**
 * Convert example images.
 *
 *
 */
public class Step1 {

    public static void main(String[] args ) {

        String INPUTFOLDER = "./MNIST_images";
        String OUTPUTFOLDER = "./MNIST_images_PGM_2";
        String scriptPath = "./bin";

        File fIN = new File(INPUTFOLDER);
        File fOUT = new File(OUTPUTFOLDER);

        fOUT.mkdirs();

        try {

            String scriptToConvert =  ImageConverter.convert_PNG_TO_PGM( fIN, fOUT, scriptPath );
            ProcessBuilder pb = new ProcessBuilder();

            pb.command( scriptToConvert );

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

}
