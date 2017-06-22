package data;

import com.google.gson.Gson;

/**
 * Created by kamir on 21.06.17.
 */
public class FileNameBasedLabeler {

    public String getLabelsAsString(String name) {
        // cat.100.jpg

        if ( name == null ) return "NULL";

        System.out.println("Process: {" + name + "} >>> to get a label" );

        int max = name.indexOf( "." );

        String label = name.substring(0,max);
        double weight = 1.0;

        LabelWithWeight lww = new LabelWithWeight( label, weight );
        LabelWithWeight[ ] ls = new LabelWithWeight[1];
        ls[0] = lww;

        Gson gson = new Gson();

        return gson.toJson( ls );

    }

}
