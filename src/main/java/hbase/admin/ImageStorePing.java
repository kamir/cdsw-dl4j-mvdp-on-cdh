package hbase.admin;


import java.io.IOException;


/**
 * Created by kamir on 20.06.17.
 */
public class ImageStorePing {

    /**
     *
     * A sanity check for our ImageStore.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        // HBaseTester.main(args);

        HBaseImageAdapter.init( false );

    }
}