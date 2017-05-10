package demo;

import image.ImageFrame;
import image.ImageLoader;
import io.ModelWrapper;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * This class uses the MNIST demo dataset.
 *
 * It is used to train the model and to evaluate the quality of the model:
 *
 *     boolean doApplyModel = true;  // only apply learned model to some data
 *     boolean doLearn = false;      // only learn model form t
 *
 * Results are stored on disk.
 */

public class ImageClassifierHDD {

    static final int numRows = 28; // The number of rows of a matrix.
    static final int numColumns = 28; // The number of columns of a matrix.

    static int batchSize = 128; // How many examples to fetch with each step.
    static int numEpochs = 15; // An epoch is a complete pass through a given dataset.

    public static int outputNum = 10; // Number of possible outcomes (e.g. labels 0 through 9).
    public static int rngSeed = 123; // This random-number generator applies a seed to ensure that the same initial weights are used when training. We’ll explain why this matters later.
    public static double rate = 0.006; // Learning rate ...

    private static Logger log = LoggerFactory.getLogger(ImageClassifierHDD.class);

    private static boolean doApplyModel = true;  // only apply learned model to some data
    private static boolean doLearn = true;  // only learn model form training data

    static String modelID = "default";

    static StringBuffer statsCollector = null;

    public static void main(String[] args) throws Exception {

        long t0 = System.currentTimeMillis();

        //--------------------------------------------------------
        // Parameter variation ...
        //
        modelID = initModelIDFromParameters();
        statsCollector = new StringBuffer();

        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);

        System.out.println(">>> Setup ... ");

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed) //include a random seed for reproducibility
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT) // use stochastic gradient descent as an optimization algorithm
                .iterations(1)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .learningRate(rate) //specify the learning rate
                .updater(Updater.NESTEROVS).momentum(0.98) //specify the rate of change of the learning rate.
                .regularization(true).l2(rate * 0.005) // regularize learning model
                .list()
                .layer(0, new DenseLayer.Builder() //create the first input layer.
                        .nIn(numRows * numColumns)
                        .nOut(500)
                        .build())
                .layer(1, new DenseLayer.Builder() //create the second input layer
                        .nIn(500)
                        .nOut(100)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD) //create hidden layer
                        .activation(Activation.SOFTMAX)
                        .nIn(100)
                        .nOut(outputNum)
                        .build())
                .pretrain(false).backprop(true) //use backpropagation to adjust weights
                .build();

        if (doLearn) {

            System.out.println(">>> Start learning ... ");


            MultiLayerNetwork model = new MultiLayerNetwork(conf);
            model.init();

            //print the score with every 1 iteration
            model.setListeners(new ScoreIterationListener(1));

            System.out.println(">>> Train model ...");

            long t1 = System.currentTimeMillis();

            for (int i = 0; i < numEpochs; i++) {

                System.out.println(">>> " + i + " ... ");
                /**
                 *  Learning means iteration over the dataset iterator which provides
                 *  the training data with labels.
                 */
                model.fit(mnistTrain);

            }

            long t2 = System.currentTimeMillis();

            long time = System.currentTimeMillis();

            ModelWrapper.saveModelOnLocalDisc( model, modelID, time);

        }

        if (doApplyModel) {

            MultiLayerNetwork loadedModel = ModelWrapper.getMultiLayerNetworkFromLocalDisc_latest( modelID );

            // Process one image ... in two formats ...
            String fnPGM = "./MNIST_images_pgm/3_01.pgm";
            String fnPNG = "./MNIST_images/3_01.png";

            File f1 = new File(fnPGM);
            File f2 = new File(fnPNG);

            System.out.println( f1.canRead() );
            System.out.println( f2.canRead() );

            BufferedImage bi1 = ImageIO.read( f1 );
            BufferedImage bi2 = ImageIO.read( f2 );

            ImageFrame.getImageFrame().compareImages(bi1,bi2);

            ImageLoader loader = new ImageLoader();

            // we have to convert an arbitrary PNG image to INDArray ... which is our feature vector!

            INDArray fm1 = null;

            // try to predict one label loaded from outside ...

            if( fm1 != null ) {

                INDArray output1 = loadedModel.output(fm1); //get the networks prediction

                System.out.println("dimensions : " + output1.length() + "\n");
                for (int i = 0; i < output1.length(); i++) {
                    System.out.print(output1.getInt(i) + " : ");
                }
            }

            System.out.println("\n>>> Re-Evaluate model....");

            Evaluation evil = new Evaluation(outputNum); //create an evaluation object with 10 possible classes

            while (mnistTest.hasNext()) {

                DataSet next = mnistTest.next();

                INDArray featureMatrix = next.getFeatureMatrix(); //get the networks prediction

                INDArray output = loadedModel.output( featureMatrix ); //get the networks prediction

                evil.eval(next.getLabels(), output); //check the prediction against the true class

            }

            System.out.println(evil.stats());

            statsCollector.append( "\n" + evil.stats() );

            // ADD CODE TO PERSIST THE MODEL Quality and Parameters to store (Model Filename as ID)
            // public static void updateModelEvaluationResult( String id, Object model ) {
        }

        double t1 = System.currentTimeMillis();

        System.out.println("****************Example finished********************");

        System.out.println("**************** " + ( (t1 - t0) / 1000 ) + " ********************");

    }

    public static String initModelIDFromParameters() {
        return outputNum + "_" + rngSeed + "_" + rate;
    }

}
