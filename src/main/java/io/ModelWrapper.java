package io;

/**
 * The learned weights of the NN represent the model approximation of a specific function, based on
 * well defined inputs.
 *
 * This class manages models in
 *
 * Created by kamir on 13.02.17.
 */

import io.filefilters.ModelFileFilter;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;

public class ModelWrapper {

    /**
     * Save a model with defined name and a time-stamp.
     *
     * @param model
     * @param time
     * @throws IOException
     */
    public static void saveModelOnLocalDisc(MultiLayerNetwork model, String modelID, long time) throws IOException {

        File locationToSave = new File("models/" + modelID + "_bpann-" + time + ".zip");
        boolean saveUpdater = true;
        ModelSerializer.writeModel(model, locationToSave, saveUpdater);

        locationToSave = new File("models/" + modelID + "_bpann.zip");
        ModelSerializer.writeModel(model, locationToSave, saveUpdater);

    }

    /**
     * The latest model is always stored without a timestamp. Historical models can be loaded if timestamp is provided.
     *
     * @return
     * @throws IOException
     */
    public static MultiLayerNetwork getMultiLayerNetworkFromLocalDisc_latest( String modelID ) throws IOException {

        File f = new File( "models/" + modelID + "_bpann.zip" );
        System.out.println( f.getAbsolutePath() );

        return ModelSerializer.restoreMultiLayerNetwork( f );
    }

    /**
     * Given a time-stamp we load a historical model.
     *
     * @return
     * @throws IOException
     */
    public static MultiLayerNetwork getMultiLayerNetworkFromLocalDisc_forTime(String modelID, long time) throws IOException {

        File f = new File( "models/" + modelID + "_bpann_" + time + ".zip" );
        System.out.println( f.getAbsolutePath() );


        return ModelSerializer.restoreMultiLayerNetwork( f );

    }

    public static boolean modelWithModelIDexists(String modelID, int i) {

        File locationToSave = new File("models");

        if ( !locationToSave.exists() ) {

            locationToSave.mkdirs();
            return false;

        }

        ModelFileFilter mff = new ModelFileFilter( modelID );
        File[] list = locationToSave.listFiles( mff );

        boolean r = false;

        if ( list != null )
            if ( list.length >= 1 ) r = true;

        System.out.println(">>> Check, if model for model-id [ " + modelID + " ] exists : (" + r + ") " + locationToSave.getAbsolutePath() + " contains " + list.length + " files with PATTERN=" + modelID );

        return r;
    }
}
