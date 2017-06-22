package demo;

import data.LabelTool;
import hbase.HBaseImageStore;

/**
 * This is a stub for a image classifier tool, using Kudu as image store.
 *
 * Entry point to our "pet-project" around image classification model automation.
 */

public class ImageClassifierHBase {

    public static void main( String[] args ) throws Exception {

        HBaseImageStore.tableName = "MNIST";
        HBaseImageStore.zk = "cdsw-mk8-1.vpc.cloudera.com";

        try {

            HBaseImageStore.init();
            // HBaseImageStore.initForceReset();

            HBaseImageStore.describe();

            System.out.println(">>> Classify images stored in HBase :");

            // DUMMY RESULT INJECTION:
            String[] categories = { "cats", "dogs", "mickey-mouse", "elephant" };
            double[] weights = { 0.98, 0.001, 0.001, 0.00001 };

            String labelsAsJSON = LabelTool.getLabelsAsJSON( categories, weights );

            HBaseImageStore.updateClassificationResult( "train___cat.10001.jpg", "defaultAlgorithm", labelsAsJSON );


//             HBaseImageClassifier.classifyWithLatestModel("default");


            System.out.println(">>> Done.");

            System.out.println("**************** Example finished ********************");

        }
        catch(Exception ex) {

            ex.printStackTrace();

        }

    }

}
