package hbase.admin;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;

public class ImageTabAdmin {

    public static final String imagesTabName = "imagesTab";

    public static final String cfRaw = "raw";
    public static final String cfLabels = "labels";

    public static void main(String[] args) throws IOException {
        
        // You need a configuration object to tell the client where to connect.
        // When you create a HBaseConfiguration, it reads in whatever you've set
        // into your hbase-site.xml and in hbase-default.xml, as long as these can
        // be found on the CLASSPATH
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.property.clientPort", "2181");  

        /**
         * Where is the Zookeeper server?
         * 
         */
        config.set("hbase.zookeeper.quorum", "cdsw-mk8-1.vpc.cloudera.com");

        String tabName = imagesTabName;

        /**
         * Vorsicht !!!
         * 
         * resetTable(  )
         * 
         * ... löscht eine bestehende TABELLE !!!
         * 
         */
        // resetTable(  config , tabName  );  
        
        
        initTable( config , tabName, false );

        // This instantiates an HTable object that connects you to
        // the "myLittleHBaseTable" table.
        HTable table = new HTable(config, tabName);
        
        int LIMIT = 10;


        System.out.println("\nRaw data SCAN");
        System.out.println(  "*************");
        Scan s = new Scan();
        s.addFamily(cfRaw.getBytes());
        ResultScanner scanner = table.getScanner(s);
        try {
            int c = 0;
            // Scanners return Result instances.
            // Now, for the actual iteration. One way is to use a while loop like so:
            for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {
                // print out the row we found and the columns we were looking for
                System.out.println("Found row: " + rr);
                c++;
                if ( c == LIMIT ) break;
            }

            // The other approach is to use a foreach loop. Scanners are iterable!
            // for (Result rr : scanner) {
            //   System.out.println("Found row: " + rr);
            // }
        } finally {
            // Make sure you close your scanners when you are done!
            // Thats why we have it inside a try/finally clause
            scanner.close();
        }
        
        
        System.out.println("\nLearned data SCAN");
        System.out.println(  "*****************");
        s = new Scan();
        s.addFamily(cfLabels.getBytes());

        scanner = table.getScanner(s);
        try {
            int c = 0;
            // Scanners return Result instances.
            // Now, for the actual iteration. One way is to use a while loop like so:
            for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {
                // print out the row we found and the columns we were looking for
                System.out.println("Found row: " + rr);
                c++;
                if ( c == LIMIT ) break;
            }

            // The other approach is to use a foreach loop. Scanners are iterable!
            // for (Result rr : scanner) {
            //   System.out.println("Found row: " + rr);
            // }
        } finally {
            // Make sure you close your scanners when you are done!
            // Thats why we have it inside a try/finally clause
            scanner.close();
        }

    }

    private static void resetTable(Configuration config, String name) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {

        System.out.println("\nRESET table");
        System.out.println(  "***********");

        HBaseAdmin hbase = new HBaseAdmin(config);
        hbase.disableTable(name);
        hbase.deleteTable(name);

    }

    public static void initTable(Configuration config, String name, boolean forceReset ) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {

        HBaseAdmin hbase = new HBaseAdmin(config);

        HTableDescriptor desc = new HTableDescriptor(name);
        HColumnDescriptor meta1 = new HColumnDescriptor(cfRaw.getBytes());
        HColumnDescriptor meta2 = new HColumnDescriptor(cfLabels.getBytes());

        meta1.setMaxVersions( Integer.MAX_VALUE );
        meta1.setTimeToLive( Integer.MAX_VALUE );

        meta2.setMaxVersions( Integer.MAX_VALUE );
        meta2.setTimeToLive( Integer.MAX_VALUE );

        desc.addFamily(meta1);
        desc.addFamily(meta2);

        if (hbase.tableExists(name)) {

            if ( forceReset ) {
                resetTable(config, name);
                hbase.createTable(desc);
            }

        }
        else {
            hbase.createTable(desc);
        };


    }
}
