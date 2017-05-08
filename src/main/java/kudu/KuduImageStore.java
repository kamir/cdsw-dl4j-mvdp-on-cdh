package kudu;

import io.IdxReader;
import io.filefilters.PGMFileFilter;
import io.filefilters.PNGFileFilter;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Kudu-Image store class provides an API to access the Kudu-table which contains our image dataset.
 *
 * This dataset can be an unbound
 *
 *
 */
public class KuduImageStore {



    public static KuduClient kuduClient = null;



    /**
     * Our Kudu image store is defined by the following settings:
     */
    public static String tableName = "MNIST";

    //public static String kuduMaster = "quickstart.cloudera";
    public static String kuduMaster = "cdsw-mk8-1.vpc.cloudera.com";

    public static ArrayList<ColumnSchema> columnList = null;
    public static Schema schema = null;

    public static void init() {

        System.out.println(">> Create the  MNIST table in Kudu");

        kuduClient = new KuduClient.KuduClientBuilder(kuduMaster).build();

        columnList = new ArrayList<>();
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("ID", Type.STRING).key(true).build());
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("IMAGE_AS_PNG", Type.STRING).key(false).build());  // BASE64 encoded
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("IMAGE_AS_PGM", Type.STRING).key(false).build());  // BASE64 encoded
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("PREDICTION", Type.INT32).key(false).build());
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("KNOWNLABEL", Type.INT32).key(false).build());

        schema = new Schema(columnList);

        List<String> partColumns = new ArrayList<>();
        partColumns.add("ID");

        try {
            if ( ! kuduClient.tableExists(tableName) ) {
                CreateTableOptions options = new CreateTableOptions().addHashPartitions(partColumns, 4).setNumReplicas(3);
                kuduClient.createTable(tableName, schema, options);
                System.out.println( "Success: " + kuduClient.tableExists(tableName));
            }
        } catch (KuduException e) {
            e.printStackTrace();
        }


    }



    /** Count all available images **/
    public static int getNrOfImages() throws KuduException {

        List<String> projectColumns = new ArrayList<>(1);
        projectColumns.add("ID");

        KuduSession session = KuduImageStore.kuduClient.newSession();

        KuduTable table = KuduImageStore.getImageTable();

        KuduScanner scanner = KuduImageStore.kuduClient.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
                .build();

        int z = 0;

        while ( scanner.hasMoreRows() ) {
            RowResultIterator results = scanner.nextRows();
            while (results.hasNext()) {

                z++;
                RowResult result = results.next();
            }
        }

        return z;

    }


    /**
     *
     * @param id
     * @param label
     */
    public static void updateClassificationResult( String id, int label ) {

        try {

            KuduSession session = KuduImageStore.kuduClient.newSession();

            KuduTable table = KuduImageStore.getImageTable();

            Update update = table.newUpdate();

            PartialRow row = update.getRow();

            row.addString("ID", id);
            row.addInt("PREDICTION", label);

            session.apply(update);

            session.flush();

        }
        catch( Exception ex ) {

            ex.printStackTrace();

        }

    }


    /**
     * For some reason I could not execute the convert command via ProcessBuilder.
     * I use a script instead to convert in the console.
     *
     * @param baseFolderRAW
     * @param baseFolderPNG
     * @throws Exception
     */
    public static void convert_RAW_DB_TO_PNG(File baseFolderRAW, File baseFolderPNG) throws Exception {

        IdxReader.dbFolder = baseFolderRAW.getAbsolutePath();
        IdxReader.outputPath = baseFolderPNG.getAbsolutePath();

        IdxReader.main( null );

    }




    /**
     * For some reason I could not execute the convert command via ProcessBuilder.
     * We use a script instead to convert in the console.
     *
     * @param baseFolder
     * @param baseFolderPGM
     * @throws Exception
     */
    public static void convert_PNG_TO_PGM(File baseFolder, File baseFolderPGM) throws Exception {

        BufferedWriter bw = new BufferedWriter( new FileWriter( "script.sh" ) );

        for( File f : baseFolder.listFiles( new PNGFileFilter() ) ) {

            String cmd = store_as_PGM( f.getAbsolutePath(), baseFolderPGM );

            bw.write( cmd + "\n");

        }

        bw.close();

    }

    /**
     * Import images from the folder "baseFolderPGM" using some binary conversions.
     */
    public static void ingest_MNIST_DATA_to_Kudu(File baseFolderPGM) {

        /**
         *
         * Clean up before we ingest new data ...
         *
         */
        try {
            if (kuduClient.tableExists("MNIST")) {
                System.out.println("DELETE table for MNIST data in Kudu");
                kuduClient.deleteTable("MNIST");
            }
        } catch (KuduException e) {
            e.printStackTrace();
        }


        /**
         *
         * Define the rigth table properties in Kudu.
         *
         * Here is the place for more optimization techniques, applied to the
         * data storage and data access layer.
         */

        try {

            CreateTableOptions options = new CreateTableOptions();

            ArrayList<String> hashPartition = new ArrayList<String>() {{add("ID");}};

            options.setNumReplicas(1);
            options.addHashPartitions(new ArrayList<String>(hashPartition),2);

            System.out.println("CREATE table for MNIST data in Kudu");

            kuduClient.createTable("MNIST", schema, options);

        } catch (KuduException e) {
            e.printStackTrace();
        }

        /** get an handle to the table **/
        KuduTable table = getImageTable();

        /** create a new session **/
        KuduSession session = kuduClient.newSession();

        try {

            /** iterate on the given folder and read only PGM files ...**/
            for( File f : baseFolderPGM.listFiles( new PGMFileFilter() ) ) {


                byte bytesPNG[] = new byte[0];

                byte bytesPGM[] = new byte[0];

                String bytesPNG_as_BASE64 = "";

                String bytesPGM_as_BASE64 = "";

                try {

                    // "./MNIST/MNIST_Database_ARGB/1_07.pgm"
                    // bytesPGM = extractBytesFromPGM( f.getAbsolutePath() );

                    // bytesPGM_as_BASE64 = Base64.encodeBase64String( bytesPGM );

                    // KuduImageClassifierTest.testPrediction( bytesPGM );

                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                Insert insert = table.newInsert();

                String labelKnown = f.getName().substring(0,1);

                PartialRow row = insert.getRow();
                row.addString(0, f.getAbsolutePath() );
                row.addString(1, bytesPNG_as_BASE64);
                row.addString(2, bytesPGM_as_BASE64);
                row.addInt(3, -1);
                row.addInt(4, Integer.parseInt(labelKnown) );

                try {

                    session.apply(insert);

                } catch (KuduException e) {
                    e.printStackTrace();
                }

                try {

                    session.flush();

                } catch (KuduException e) {
                    e.printStackTrace();
                }
            }
        } finally {

            try {

                session.close();

            } catch (KuduException e) {
                e.printStackTrace();
            }
        }

        System.out.println("-- finished loading MNIST files to Kudu from : " + baseFolderPGM.getAbsolutePath() );

    }

    /**
     * We use fixed table for doing some image classification exercises.
     *
     * @return
     */
    public static KuduTable getImageTable() {

        KuduTable table = null;

        try {

            table = kuduClient.openTable(tableName);

        }
        catch (KuduException e) {
            e.printStackTrace();
        }

        return table;
    }


    public static byte[] extractBytesFromPGM(String imageName) throws IOException {

        // open image
        File imgPath = new File(imageName);

        // THIS IS A PGM FILE
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        //ImageFrame.showSinlgeImage( imageName );

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

        System.out.println("nr of bytes in " + imgPath.getAbsolutePath() + " => " + data.getSize());

        byte[] img = data.getData();

        return (img);

    }


    /**
     * Reads the binary image into a BufferedImage and writes it to a different format ...   !!!
     *
     * Currently, the PGM export is not complete. We simple generate a call to the convert tool
     * and collect all those calls in a text file, which works as a shell script later.
     *
     * @param imageName
     * @return
     * @throws IOException
     */
    public static String store_as_PGM(String imageName, File targetFolder) throws Exception {

        // open image
        File imgPath = new File(imageName);

        File in = new File( imageName );
        File path = in.getParentFile();

        String stem = in.getName();
        String name = stem.substring(0, stem.length()-4 );

        Process p = new ProcessBuilder("/usr/local/bin/convert", "-colorspace gray", "-depth 8", imgPath.getAbsolutePath(), targetFolder.getAbsolutePath() + "/" + name +".pgm").start();
        int i = p.waitFor();

        return "/usr/local/bin/convert -colorspace gray -depth 8 " + imgPath.getAbsolutePath() + " " + targetFolder.getAbsolutePath() + "/" + name +".pgm";

    }


}
