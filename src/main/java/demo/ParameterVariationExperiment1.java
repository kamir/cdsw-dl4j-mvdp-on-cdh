package demo;

import io.ModelWrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Based on the ImageClassifierHDD program, we generate multiple models with different parameter settings.
 * Finally, we want to get more information about model stability.
 *
 * Assumption 1: We only change parameters of the core setup.
 *
 * Questions:
 * - we can also add noise to the raw data
 *   => When will the quality drop?
 *   => Is the quality change constant?
 * - we can randomly rotate the raw image
 *   => Can multiple layers help us to improve model quality?
 *
 */
public class ParameterVariationExperiment1 {

    /**
     * Those properties have to be changed systematically
     */
//    static int[] outputNums = { 10, 20, 128, 256}; // Number of possible outcomes (e.g. labels 0 through 9).
//    static int[] rngSeeds = { 123, 100, 200, 10, 5, 9, 400 }; // This random-number generator applies a seed to ensure that the same initial weights are used when training. We’ll explain why this matters later.
//    static double[] rates = { 0.0015, 0.003, 0.006, 0.012, 0.024}; // Learning rate ...

    static int[] outputNums = { 10 }; // Number of possible outcomes (e.g. labels 0 through 9)
    static int[] rngSeeds = { 100 }; // This random-number generator applies a seed to ensure that the same initial weights are used when training. We’ll explain why this matters later.
    static double[] rates = { 0.00001, 0.0001, 0.001, 0.01, 0.1, 1.0, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.25, 0.5, 0.75, 0.125, 0.15, 0.175}; // Learning rate ...

    public static void main( String[] ARGS ) throws Exception {

        int z = outputNums.length * rngSeeds.length * rates.length;
        int r = 1;
        double tEstimate = 129.152; // from single run of ImageClassifierHDD example

        System.out.println( "rounds : " + r  );
        System.out.println( "setups : " + z  );

        System.out.println( "total models to calculate : " + z * r );
        System.out.println( "total time estimated      : " + ( z * r * tEstimate ) / 60/60 );

        BufferedWriter br = new BufferedWriter( new FileWriter( new File( "exp_1.txt" ) ) );

        for( int outputNum : outputNums ) {
            ImageClassifierHDD.outputNum = outputNum;
            for( int rngSeed : rngSeeds ) {
                ImageClassifierHDD.rngSeed = rngSeed;
                for( double rate: rates ) {
                    ImageClassifierHDD.rate = rate;

                    String modelID = ImageClassifierHDD.initModelIDFromParameters();

                    try {

                        // If model for MODEL_ID exists, we can continue ...
                        //
                        // Create a Model-File-Filter and check if "mainpart of modelname" appears at least once
                        // in the modelfolder.
                        //
                        boolean modelExists = ModelWrapper.modelWithModelIDexists( modelID, 1 );
                        if( modelExists ) {
                            // nothing to do ...
                        }
                        else {
                            // learn using the current parameter set, defined via static properties of ImageClassifierHDD:

                            ImageClassifierHDD.main(null);

                            br.write( ImageClassifierHDD.statsCollector.toString() );
                            br.write( ImageClassifierHDD.modelID );

                            br.flush();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }

        }

        br.close();

    }

}
