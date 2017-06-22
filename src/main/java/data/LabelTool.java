package data;

import com.google.gson.Gson;

/**
 * Created by kamir on 21.06.17.
 */
public class LabelTool {

    public static String getLabelsAsJSON(String[] categories, double[] weights) {

        LabelWithWeight[] ls = new LabelWithWeight[categories.length];

        for ( int i = 0; i < categories.length ; i++ ) {

            try {

                ls[i] = new LabelWithWeight( categories[i] , weights[i] );

            } catch (Exception ex) {

                ex.printStackTrace();

            }

        }

        Gson gson = new Gson();

        String json = gson.toJson( ls );

        return json;

    }
}


