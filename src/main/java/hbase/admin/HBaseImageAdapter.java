/**
 *
 * High level adapter for storing and retrieving Time Series from HBase.
 *
 */
package hbase.admin;

import com.google.gson.Gson;
import data.LabelWithWeight;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kamir
 */
public class HBaseImageAdapter {

    static boolean debug = true;

    private HBaseImageAdapter() {

        // You need a configuration object to tell the client where to connect.
        // When you create a HBaseConfiguration, it reads in whatever you've set
        // into your hbase-site.xml and in hbase-default.xml, as long as these can
        // be found on the CLASSPATH

        config = HBaseConfiguration.create();

        config.set("hbase.zookeeper.quorum", defaultZookeeperIP);  // Here we are running zookeeper locally
        config.set("hbase.zookeeper.property.clientPort", "2181");  // Here we are running zookeeper locally

    }

    static Configuration config = null;
    static String defaultZookeeperIP = "cdsw-mk8-1.vpc.cloudera.com";

    static HBaseImageAdapter hba = null;

    static String tabName = ImageTabAdmin.imagesTabName;

    static public HTable table = null;

    public static HBaseImageAdapter init( boolean forceReset ) throws IOException {

        if (hba == null) {
            hba = new HBaseImageAdapter();

            ImageTabAdmin.initTable( config , tabName, forceReset );

            try {
                // This instantiates an HTable object that connects you to
                // the "myLittleHBaseTable" table.
                hba.table = new HTable(config, tabName);

                String k = "BOSCH-Bag-Stage-Image-Store: ";
                String v = "READY!";

                System.out.println("TAB-NAME:" + tabName);

                Put p1 = new Put(Bytes.toBytes(k));
                p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("sanity_test"), v.getBytes());

                table.put(p1);

                Get g = new Get( k.getBytes() );

                Result r = table.get(g);
                byte [] value = r.getValue( Bytes.toBytes(ImageTabAdmin.cfRaw), Bytes.toBytes("sanity_test") );
                String valueStr = Bytes.toString(value);

                System.out.println("SANITY TEST: " + k + " " + r);

                System.out.println("GET: " + valueStr);

            }
            catch (Exception ex) {
                Logger.getLogger(HBaseImageAdapter.class.getName()).log(Level.SEVERE, null, ex);
            }


        }
        return hba;
    }


    public static void putRawImagesAsPGM(String absolutePath, byte[] bytesPGM, String bytesPGM_as_base64) throws IOException {

        File path = new File( absolutePath );
        String bagFileName = path.getParentFile().getName();

        String rowKey = bagFileName + "___" + path.getName();

        Put p1 = new Put(Bytes.toBytes(rowKey));
        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesPGM"), bytesPGM);
        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesPGM_BASE64"),
                Bytes.toBytes(bytesPGM_as_base64));

        table.put(p1);

    }


    public void putRawImagesAsFileAndPGM(String absolutePath, byte[] bytesPGM, String bytesPGM_as_base64, byte[] bytesRAW, String bytesRAW_as_base64) throws IOException {

        File path = new File( absolutePath );
        String bagFileName = path.getParentFile().getName();

        String rowKey = bagFileName + "___" + path.getName();

        Put p1 = new Put(Bytes.toBytes(rowKey));
        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesPGM"), bytesPGM);
        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesPGM_BASE64"),
                Bytes.toBytes(bytesPGM_as_base64));

        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesRAW"), bytesRAW);
        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesRAW_BASE64"),
                Bytes.toBytes(bytesRAW_as_base64));

        table.put(p1);

    }


    public void putRawImagesAsFile(String absolutePath, byte[] bytesRAW, String bytesRAW_as_base64) throws IOException {

        File path = new File( absolutePath );
        String bagFileName = path.getParentFile().getName();

        String rowKey = bagFileName + "___" + path.getName();

        Put p1 = new Put(Bytes.toBytes(rowKey));

        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesRAW"), bytesRAW);
        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesRAW_BASE64"),
                Bytes.toBytes(bytesRAW_as_base64));

        table.put(p1);

    }

    public void putRawImagesAsFileWithLabel(String absolutePath, byte[] bytesRAW, String bytesRAW_as_base64, String label) throws IOException {

        File path = new File( absolutePath );
        String bagFileName = path.getParentFile().getName();

        String rowKey = bagFileName + "___" + path.getName();

        Put p1 = new Put(Bytes.toBytes(rowKey));

        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesRAW"), bytesRAW);
        p1.add(Bytes.toBytes(ImageTabAdmin.cfRaw),Bytes.toBytes("bytesRAW_BASE64"),
                Bytes.toBytes(bytesRAW_as_base64));

        p1.add(Bytes.toBytes(ImageTabAdmin.cfLabels),Bytes.toBytes("provided"), Bytes.toBytes( label ) );

        table.put(p1);

    }


    /**
     * The full row will be provided as a single JSON object.
     *
     * @param key
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String getImageRow(byte[] key) throws Exception {

        Get g = new Get(key);

        Result r = hba.table.get(g);

        // Iterate results and build a JSON Object ...


        String resultString = "NO IMPLEMENTED YET !!!";
        return resultString;

    }
    
    public static byte[] getImageKeyOnly(byte[] key) throws IOException, Exception {

        Get g = new Get(key);
        
        KeyOnlyFilter filter = new KeyOnlyFilter();
        g.setFilter(filter);
        
        Result r = hba.table.get(g);
        // byte[] value = r.getValue(Bytes.toBytes( TSTabAdmin.colFamNameE), Bytes.toBytes("raw"));
        int size = r.size();

        return Bytes.toBytes(size);

    }
    
    public boolean hasImageWithKey(byte[] k) throws IOException {

        Get g = new Get(k);
        g.setMaxVersions(1);
        g.setFilter( new KeyOnlyFilter() );

        Result r = hba.table.get(g);

        byte[] value = r.getRow();

        if ( value == null) return false;
        else return true;

    }

    public byte[] getImageWithKey(String k, String cf, String cn) {

        byte[] value = null;

        try {

            Get g = new Get(k.getBytes());

            g.addColumn( cf.getBytes(), cn.getBytes() );

            g.setMaxVersions(1);

            Result r = hba.table.get(g);

            value = r.getValue( Bytes.toBytes(ImageTabAdmin.cfRaw), Bytes.toBytes("bytesRAW") );


        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return value;

    }


    /**
     *
     * TODO all labels have to be extracted as graph structure.
     *
     * @param exportFolder
     * @throws IOException
     */
    public static void scanImagesAndLabels(File exportFolder) throws IOException {

        Scan s = new Scan();

        s.addColumn(Bytes.toBytes(ImageTabAdmin.cfRaw), Bytes.toBytes("bytesRAW"));
        s.addFamily( Bytes.toBytes(ImageTabAdmin.cfLabels) );

        File fGraph = new File( exportFolder.getAbsolutePath() + File.separator + "EL_image_label_graph.csv" );

        BufferedWriter bw = new BufferedWriter( new FileWriter( fGraph ) );
        bw.write("Source,Target,Weight,Type\n");

        ResultScanner scanner = table.getScanner(s);
        int i = 0;
        try {
            i++;
            // Scanners return Result instances.
            // Now, for the actual iteration. One way is to use a while loop like so:
            for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {

                // print out the row we found and the columns we were looking for
                System.out.println("[" + i + "] found rowkey: " + rr);

                File fImage = new File( exportFolder.getAbsolutePath() + File.separator + Bytes.toString( rr.getRow() ) );

                byte[] data = rr.getValue( Bytes.toBytes(ImageTabAdmin.cfRaw), Bytes.toBytes("bytesRAW") );

                // Link : (Source,Target,Probability)
                // Source,Target,Weight
                String source = Bytes.toString( rr.getRow() );
                List<Cell> providedLabel = rr.getColumnCells( Bytes.toBytes(ImageTabAdmin.cfLabels), Bytes.toBytes("provided") );
                System.out.println( providedLabel.size() + " provided labels found.");

                for( Cell cell : providedLabel ) {

                    String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
                    String value = Bytes.toString(CellUtil.cloneValue(cell));
                    System.out.printf("Qualifier : %s : Value : %s", qualifier, value);

                    String type = qualifier;

                    String target = "?";
                    String weight = "0.0";

                    Gson gson = new Gson();

                    LabelWithWeight[] ls = gson.fromJson( value , LabelWithWeight[].class );

                    for( int j = 0; j < ls.length; j++ ) {

                        target = ls[j].label;
                        weight = ls[j].weight+"";

                        System.out.println( " => " + source + "," + target + "," + weight + "," + type + "\n" );
                        bw.write( source + "," + target + "," + weight + "," + type + "\n");
                    }


                }







                List<Cell> learnedLabel = rr.getColumnCells( Bytes.toBytes(ImageTabAdmin.cfLabels), Bytes.toBytes("defaultAlgorithm") );

                System.out.println( learnedLabel.size() + " learned labels found (using: defaultAlgorithm).");

                for( Cell cell : learnedLabel ) {

                    String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));

                    String value = Bytes.toString(CellUtil.cloneValue(cell));

                    System.out.printf("Qualifier : %s : Value : %s", qualifier, value);

                    String type = qualifier;

                    String target = "?";
                    String weight = "0.0";

                    Gson gson = new Gson();

                    LabelWithWeight[] ls = gson.fromJson( value , LabelWithWeight[].class );

                    for( int j = 0; j < ls.length; j++ ) {

                        target = ls[j].label;
                        weight = ls[j].weight+"";

                        System.out.println( " => " + source + "," + target + "," + weight + "," + type + "\n" );
                        bw.write( source + "," + target + "," + weight + "," + type + "\n");
                    }


                }


                // frame.showSinlgeImage( data );

                ByteArrayInputStream in = new ByteArrayInputStream(data);

                FileOutputStream out = new FileOutputStream(fImage);

                IOUtils.copy(in, out);

                IOUtils.closeQuietly(in);

                IOUtils.closeQuietly(out);

            }

            // The other approach is to use a foreach loop. Scanners are iterable!
            // for (Result rr : scanner) {
            //   System.out.println("Found row: " + rr);
            // }
        } finally {
            // Make sure you close your scanners when you are done!
            // Thats why we have it inside a try/finally clause
            scanner.close();
            bw.flush();
            bw.close();
        }

    }


    public static int countAllImages() throws IOException {

        Scan s = new Scan();

        FilterList allFilters = new FilterList();
        allFilters.addFilter(new FirstKeyOnlyFilter());
        allFilters.addFilter(new KeyOnlyFilter());

        s.setFilter(allFilters);

        ResultScanner scanner = table.getScanner(s);

        int count = 0;

        long start = System.currentTimeMillis();

        try {
            for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {
                count += 1;
                if (count % 100000 == 0) System.out.println(count);
            }
        } finally {
            scanner.close();
        }

        long end = System.currentTimeMillis();

        long elapsedTime = end - start;

        System.out.println("Elapsed time was " + (elapsedTime/1000F));

        count = count - 1; // we have a row for the sanity check.

        return count;

    }

    public static void _putLabelToImageWithKey(String key, String cn, String s) {
        try {
            putLearnedLabel( key.getBytes() , cn, s );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void putLearnedLabel(byte[] key, String algorithm, String label ) throws IOException, Exception {

        if ( debug ) {
            System.out.println(">> key         " + new String( key ) );
            System.out.println(">> cf:cnv      " + ImageTabAdmin.cfLabels + ":" + algorithm );
            System.out.println(">> v           " + new String( label ) );
            System.out.println(">> hba         " + (hba == null) );
            System.out.println(">> table       " + (hba.table == null) );
        }

        Put p = new Put(key);

        p.add(Bytes.toBytes( ImageTabAdmin.cfLabels), Bytes.toBytes(algorithm), label.getBytes());

        hba.table.put(p);
        hba.table.flushCommits();

    }

    public void describeTable() {

        try {
            System.out.println( ">>> ImageStore table descriptor: \n" +
                                table.getTableDescriptor().toString() );
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String getLabelsAsJSON(String s) throws IOException {
        return getLabelsAsJSON( s, "defaultAlgorithm" );
    }

    public String getLabelsAsJSON(String s, String algorithm) throws IOException {

        Get g = new Get( s.getBytes() );

        g.addFamily( Bytes.toBytes(ImageTabAdmin.cfLabels) );

        Result rr = table.get( g );

        // print out the row we found and the columns we were looking for
        System.out.println("[OK] found row: " + rr);

        List<Cell> providedLabel = rr.getColumnCells( Bytes.toBytes(ImageTabAdmin.cfLabels), Bytes.toBytes("provided") );
        System.out.println( providedLabel.size() + " provided labels found.");

        LabelWithWeight[] labels1 = null;
        LabelWithWeight[] labels2 = null;

        for( Cell cell : providedLabel ) {
            String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
            String value = Bytes.toString(CellUtil.cloneValue(cell));
            System.out.printf("Qualifier : %s : Value : %s\n", qualifier, value);

            String type = qualifier;

            String target = "?";
            String weight = "0.0";

            Gson gson = new Gson();

            labels1 = gson.fromJson( value , LabelWithWeight[].class );

        }

        List<Cell> learnedLabels = rr.getColumnCells( Bytes.toBytes(ImageTabAdmin.cfLabels), Bytes.toBytes(algorithm) );

        System.out.println( learnedLabels.size() + " learned labels found (using: defaultAlgorithm).");

        for( Cell cell : learnedLabels ) {

            String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));

            String value = Bytes.toString(CellUtil.cloneValue(cell));

            System.out.printf("Qualifier : %s : Value : %s\n", qualifier, value);

            String type = qualifier;

            String target = "?";
            String weight = "0.0";

            Gson gson = new Gson();

            labels2 = gson.fromJson( value , LabelWithWeight[].class );
        }





        if ( labels1 != null && labels2 != null ) {

            int aLen = labels1.length;
            int bLen = labels2.length;
            LabelWithWeight[] c = new LabelWithWeight[aLen + bLen];
            System.arraycopy(labels1, 0, c, 0, aLen);
            System.arraycopy(labels2, 0, c, aLen, bLen);

            Gson g2 = new Gson();

            return g2.toJson(c);

        }

        if ( labels1 != null ) {
            Gson g2 = new Gson();

            return g2.toJson(labels1);

        }

        if ( labels2 != null ) {
            Gson g2 = new Gson();

            return g2.toJson(labels2);

        }

        return null;
    }
}
