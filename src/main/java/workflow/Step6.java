package workflow;

import data.FileNameBasedLabeler;
import demo.ImageClassifierHBase;
import hbase.HBaseImageStore;

import java.io.File;
import java.io.FileFilter;


/**
 * Prepare a model store in HBase.
 *
 *
 *
 *
 */
public class Step6 {

    public static void main(String[] args ) {

        HBaseImageStore.tableName = "MNIST";
        HBaseImageStore.zk = "cdsw-mk8-1.vpc.cloudera.com";

        try {

            HBaseImageStore.init();

//            HBaseImageStore.initForceReset();

            HBaseImageStore.describe();

            System.exit(0);

            //
            // Ingest MNIST images ...
            //
            //File folder = new File( "MNIST_images_pgm" );
            //FileFilter filter = new PGMFileFilter();



            //
            // Ingest unlabeled images for testing the model ...
            //
            File folder1 = new File( "/GITHUB.cloudera.internal/NeuralCrawler/DATASETS/CAT_DOG_Kaggle/test1" );
            FileFilter filter1 = null;

            int limit1 = Integer.MAX_VALUE;
            //int limit1 = 10;
            HBaseImageStore.ingest_Image_DATA_to_HBase( folder1, filter1, limit1 );

            File exportFolder1 = new File( "EXPORT_raw" );
            HBaseImageStore.export_LABELED_IMAGES(exportFolder1);



            //
            // Ingest labeled images for training ...
            //
            File folder2 = new File( "/GITHUB.cloudera.internal/NeuralCrawler/DATASETS/CAT_DOG_Kaggle/train" );
            FileFilter filter2 = null;
            FileNameBasedLabeler labeler = new FileNameBasedLabeler();

            int limit2 = Integer.MAX_VALUE;
            // int limit2 = 10;
            HBaseImageStore.ingest_Labeled_Image_DATA_to_HBase( folder2, filter2, limit2, labeler );



            ImageClassifierHBase.main( args );



            File exportFolder2 = new File( "EXPORT_labeld" );
            HBaseImageStore.export_LABELED_IMAGES(exportFolder2);



            boolean t1 = HBaseImageStore.hasImageWithKey( "train___cat.100.jpg" );
            boolean t2 = HBaseImageStore.hasImageWithKey( "___train___cat.100.jpg" );
            System.out.println( "\nt1: (true)  => " + t1 );
            System.out.println( "t2: (false) => " + t2 );

            HBaseImageStore.describe();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

}
