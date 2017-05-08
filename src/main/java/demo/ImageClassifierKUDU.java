package demo;

import kudu.KuduImageClassifier;
import kudu.KuduImageStore;

import java.io.File;

/**
 * This is a stub for a image classifier tool, using Kudu as image store.
 *
 * Entry point to our "pet-project" around image classification model automation.
 */

public class ImageClassifierKUDU {

    public static void main( String[] args ) throws Exception {

        File dbFolder = new File( "MNIST_images_raw" );

        File baseFolderPNG = new File( "MNIST_images" );
        File baseFolderPGM = new File( "MNIST_images_pgm" );

        File baseFolder = new File( "MNIST_images" );

        /**
         * Prepare a connection to the Kudu-Service
         *
         *
         * Here you have to adjust the connection properties. Currently the "dev-cluster" is used.
         *
         */
        KuduImageStore.init();

        /**
         * Extract PNG files from binary db.
         */
        // kudu.KuduImageStore.convert_RAW_DB_TO_PNG(dbFolder, baseFolderPNG);

        /**
         * Convert PNG files to PGM files
         */
        // kudu.KuduImageStore.convert_PNG_TO_PGM(baseFolder, baseFolderPGM);

        /**
         * Store PGM files, converted to byte[] in Kudu.
         *
         * !!! Warning !!! This deletes the image table named MNIST if it already exists.
         */
        //kudu.KuduImageStore.ingest_MNIST_DATA_to_Kudu(baseFolderPGM);



        /**
         * Check the number of images, stored in Kudu.
         */
        System.out.println(">>> Inspect data in Kudu :");
        int z = KuduImageStore.getNrOfImages();
        System.out.println("z=" + z);


        System.out.println(">>> Classify images stored in Kudu :");

        KuduImageClassifier.classifyWithLatestModel( "default" );

//        kudu.KuduImageClassifier.classifyWithLatestModel( testData );

        System.out.println(">>> Done.");

        System.out.println("**************** Example finished ********************");

        System.exit(0);

    }

}
