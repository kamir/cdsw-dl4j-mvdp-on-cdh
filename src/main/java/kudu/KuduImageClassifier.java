package kudu;

import io.ModelWrapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.kudu.client.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  The Image classifier takes all images from a folder and imports the to a KUDU table.
 *
 *  During ingestion, we also write the "known-label" based on a metadata tag and the predicted value to the table.
 *
 *  For simplification we use the first character of thenfilename as tag. A better approach will use Apache Tika
 *  to really parse the image and extract a tag.
 *
 */
public class KuduImageClassifier {




    /**
     * Use the model, provided my the io.ModelWrapper to classify unclassified images in a table.
     */
    public static void classifyWithLatestModel(String modelID ) throws Exception {

        MultiLayerNetwork restored = ModelWrapper.getMultiLayerNetworkFromLocalDisc_latest( modelID );

        classifyWithModel(restored, Integer.MAX_VALUE);

    }



    /**
     * Use the model, provided my the io.ModelWrapper to classify unclassified images in a table.
     *
     * @param restoredModel
     */
    public static void classifyWithModel( MultiLayerNetwork restoredModel, int z ) throws Exception {

        List<String> projectColumns = new ArrayList<>(1);
        projectColumns.add("IMAGE_AS_PGM");
        projectColumns.add("ID");

        KuduSession session = KuduImageStore.kuduClient.newSession();

        KuduTable table = KuduImageStore.getImageTable();

        KuduScanner scanner = KuduImageStore.kuduClient.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
                .build();


        int c = 0;

        while ( scanner.hasMoreRows() ) {

            RowResultIterator results = scanner.nextRows();

            while (results.hasNext()) {

                RowResult result = results.next();

                String id = result.getString("ID");

                String bytesAsNase64 = result.getString("IMAGE_AS_PGM");

                byte[] bytes = Base64.decodeBase64( bytesAsNase64.getBytes() );


                INDArray featureMatrix = convertBinaryToFeatureMatrix(1, bytes);

                System.out.println(">>> Feature Matrix (INDArray) : ");
                // System.out.println(featureMatrix.toString());

                INDArray output = restoredModel.output(featureMatrix);

                System.out.println(">>> Result (INDArray) : ");
                System.out.println(output);

                int labelIndex = winnerTakesAllDecision( output ); //get the networks prediction

                System.out.println(">>> Winner takes it all : ");

                System.out.println( labelIndex + " # " + id );

                KuduImageStore.updateClassificationResult(id, labelIndex);

            }

        }
        System.out.println();
    }


    /**
     *
     * Find the maximum value in the output vector. This index represents the final label.
     *
     * Individual weights can be interpreted as "label probabilities".
     *
     * @param output
     * @return
     */
    public static int winnerTakesAllDecision(INDArray output) {

        float max = 0.0f;

        int winner = -1;

        for( int i = 0; i < output.length(); i++ ) {

            if(max < output.getFloat(i)) {
                max = output.getFloat(i);
                winner = i;
            }

        }

        return winner;

    }









    /**
     *
     * Convert the ... DataBufferByte ... to an INDArray ...
     *
     *
     * T.B.D.
     * This might be wrapped by a Base64 encoding!!!
     *
     *
     * @param numExamples
     * @param img
     * @return
     * @throws IOException
     */
    public static INDArray convertBinaryToFeatureMatrix(int numExamples, byte[] img) throws IOException {

        float[][] featureData = new float[numExamples][0];

        boolean binarize = true;

        float[] featureVec = new float[img.length];
        featureData[0] = featureVec;

        for (int j = 0; j < img.length; j++) {
            float v = ((int) img[j]); //byte is loaded as signed -> convert to unsigned
            if (binarize) {
                if (v > 30.0f) {
                    featureVec[j] = 1.0f;
                }
                else {
                    featureVec[j] = 0.0f;
                }
            } else {
                featureVec[j] = v / 255.0f;
            }
        }

        featureData = Arrays.copyOfRange(featureData,0,1);

        INDArray features = Nd4j.create(featureData);

        return features;

    }








}