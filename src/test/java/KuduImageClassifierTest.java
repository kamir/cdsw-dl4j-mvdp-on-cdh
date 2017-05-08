import kudu.KuduImageClassifier;
import kudu.KuduImageStore;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import java.io.File;
import java.io.IOException;

public class KuduImageClassifierTest {

    /**
     *
     * Simple test class for reading (A) image from HDD, (B) model from HDD
     * and predicting a label.
     *
     */
    public static void main(String args[]) throws IOException {

        /**
         *
         * Simple test, using PGM files from HDD.
         *
         */

        String fn = "MNIST_images_pgm/7_01.pgm";

        System.out.println(">>> Starting MNIST image classifiaction test ...");

        KuduImageStore kis = new KuduImageStore();
        byte[] image = kis.extractBytesFromPGM( fn );

        KuduImageClassifier classifier = new KuduImageClassifier();

        INDArray featureMatrix = classifier.convertBinaryToFeatureMatrix(1, image);

        System.out.println(">>> Feature Matrix (INDArray) : ");
        System.out.println(featureMatrix.toString());

        File f = new File("models/bpann.zip");
        MultiLayerNetwork loadedModel = ModelSerializer.restoreMultiLayerNetwork(f);

        INDArray output = loadedModel.output(featureMatrix); //get the networks prediction

        System.out.println(">>> Result (INDArray) : ");
        System.out.println(output);


        int labelIndex = classifier.winnerTakesAllDecision( output ); //get the networks prediction

        System.out.println(">>> Winner takes it all : ");
        System.out.println( labelIndex );

    }


    /**
     * Predict a labal for the bytes which represent an image in PGM format.
     *
     * See extraction code:
     *
     *    KuduImageStore kis = new KuduImageStore();
     *    byte[] image = kis.extractBytesFromPGM( fn );
     *
     * @param bytesPGM
     * @throws Exception
     */
    public static void testPrediction(byte[] bytesPGM) throws Exception {

        KuduImageClassifier classifier = new KuduImageClassifier();

        INDArray featureMatrix = classifier.convertBinaryToFeatureMatrix(1, bytesPGM);

        System.out.println(">>> Feature Matrix (INDArray) : ");
        System.out.println(featureMatrix.toString());

        File f = new File("models/bpann.zip");

        MultiLayerNetwork loadedModel = ModelSerializer.restoreMultiLayerNetwork(f);

        INDArray output = loadedModel.output(featureMatrix); //get the networks prediction

        System.out.println(">>> Result (INDArray) : ");
        System.out.println(output);


        int labelIndex = classifier.winnerTakesAllDecision( output ); //get the networks prediction

        System.out.println(">>> Winner takes it all : ");
        System.out.println( labelIndex );

    }
}
