package workflow;

import data.LabelTool;
import hbase.HBaseImageStore;
import image.ImageFrame;


/**
 * Prepare a model store in HBase.
 *
 *
 *
 *
 */
public class Step7 {

    public static void main(String[] args ) {

        HBaseImageStore.tableName = "MNIST";
        HBaseImageStore.zk = "cdsw-mk8-1.vpc.cloudera.com";

        try {

            HBaseImageStore.init();



//            HBaseImageStore.describe();



//             ImageClassifierHBase.main( args );


            System.out.println("Show a raw image in preview frame:");
            System.out.println("----------------------------------");

            byte[] RAWIMAGEDATA = HBaseImageStore.getImageByKey("train___cat.100.jpg");
            ImageFrame.getImageFrame().showSinlgeImage( RAWIMAGEDATA );

            System.out.println("Show full row as String:");
            System.out.println("------------------------");

            String full_row  = HBaseImageStore.getImageRowByKey("train___cat.100.jpg");
            System.out.println( full_row  );
            System.out.println();
            System.out.println();


            System.out.println("Show image metadata:");
            System.out.println("--------------------");

            String IMAGEMETADATA = HBaseImageStore.getImageMetaDataByKey("train___cat.100.jpg");
            System.out.println( IMAGEMETADATA  );
            System.out.println();

            System.out.println("DUMMY RESULT INJECTION:");
            System.out.println("-----------------------");
            String[] categories = { "cats", "dogs", "mickey-mouse", "elephant" };
            double[] weights = { 0.98, 0.001, 0.001, 0.00001 };

            String labelsAsJSON = LabelTool.getLabelsAsJSON( categories, weights );

            HBaseImageStore.updateClassificationResult( "train___cat.100.jpg", "defaultAlgorithm", labelsAsJSON );
            HBaseImageStore.updateClassificationResult( "train___cat.100.jpg", "coolNewAlgorithm", labelsAsJSON );



            System.out.println("Show image labels (learned labels and provided labels):");
            System.out.println("-----------------------------------------------------");

            String IMAGELABELS = HBaseImageStore.getImageLabelsByKey("train___cat.100.jpg");
            System.out.println( IMAGELABELS  );
            System.out.println();

            String IMAGELABELS2 = HBaseImageStore.getImageLabelsByKeyAndAlgorithm("train___cat.100.jpg", "coolNewAlgorithm" );
            System.out.println( IMAGELABELS2  );
            System.out.println();


        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

}
