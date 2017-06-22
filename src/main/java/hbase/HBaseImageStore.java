package hbase;

import data.FileNameBasedLabeler;
import hbase.admin.HBaseImageAdapter;
import hbase.admin.ImageTabAdmin;
import image.ImageConverter;
import io.IdxReader;
import io.filefilters.ImageFileFilter;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.hbase.util.Bytes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The HBaseImageStore class provides an API to access the HBaseImageAdapter which wraps around the HBase-table
 * which contains our image dataset.
 *
 * This dataset can be unbound.
 */
public class HBaseImageStore {



    static HBaseImageAdapter dal = null;

    public static String tableName = "MNIST";
    public static String zk = "cdsw-mk8-1.vpc.cloudera.com";

    public static void init() throws IOException {

        System.out.println(">> Use the " + tableName + " table in HBase (zk=" + zk + ")" );
        dal = HBaseImageAdapter.init( false );

    }

    public static void initForceReset() throws IOException {

        System.out.println(">> (Re)create the " + tableName + " table in HBase (zk=" + zk + ")" );
        dal = HBaseImageAdapter.init( true );

    }

    public static void describe() {

        System.out.println( "Nr. of persisted images                    : " + getNrOfImages() );
        System.out.println( "Nr. of labeled images (for training)       : " + getNrOfPreLabeledImages() );
        System.out.println( "Nr. of unlabeled images (for prediction)   : " + getNrOfNotLabledImages() );

        dal.describeTable();

    }

    private static int getNrOfPreLabeledImages() {
        return -1;
    }

    private static int getNrOfNotLabledImages() {
        return -1;
    }

    /** Count all available images **/
    public static int getNrOfImages() {
        try {
            return dal.countAllImages();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }



    public static void updateClassificationResult( String key, String cn, String l ) {

                dal._putLabelToImageWithKey( key, cn,  l );
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

        ImageConverter.convert_PNG_TO_PGM( baseFolder, baseFolderPGM, "./bin" );

    }

    /**
     * Import images from the folder "baseFolderPGM" using some binary conversions.
     */
    public static void ingest_MNIST_DATA_to_HBase(File baseFolderPGM, FileFilter fileFilter, int limit) {

        System.out.println(">>> Folder: " + baseFolderPGM.getAbsolutePath() );

        File[] files = baseFolderPGM.listFiles();

        System.out.println(">>> " + files.length + " files found.");

        int c = 0;

        try {


            /** iterate on the given folder and read only PGM files ...**/
            for( File f : baseFolderPGM.listFiles( fileFilter ) ) {

                if ( c >= limit ) break;
                c++;

                System.out.println( f.getAbsolutePath() );
                
                byte bytesPGM[] = new byte[0];
                byte bytesRAW[] = new byte[0];

                String bytesPGM_as_BASE64 = "";
                String bytesRAW_as_BASE64 = "";

                try {

                    // conversion of images into BASE64 encoding
                    bytesPGM = extractBytesFromPGM( f.getAbsolutePath() );
                    bytesRAW = readBytesFromFile( f.getAbsolutePath() );

                    bytesPGM_as_BASE64 = Base64.encodeBase64String( bytesPGM );
                    bytesRAW_as_BASE64 = Base64.encodeBase64String( bytesRAW );

                    dal.putRawImagesAsFileAndPGM( f.getAbsolutePath(), bytesPGM, bytesPGM_as_BASE64,  bytesRAW, bytesRAW_as_BASE64);

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        finally {
            System.out.println("--> finished loading MNIST files to HBase from : " + baseFolderPGM.getAbsolutePath() );
            System.out.println("--> c=" + c );
        }

    }



    public static void ingest_Image_DATA_to_HBase(File folder, FileFilter filter, int limit) {

        System.out.println(">>> Folder: " + folder.getAbsolutePath() );

        if ( filter == null ) {
            filter = new ImageFileFilter();
            System.out.println(">>> " + filter + " will be used to filter image files by extension.");
        }

        File[] files = folder.listFiles( filter );

        System.out.println(">>> " + files.length + " files found.");

        int c = 0;

        try {


            /** iterate on the given folder and read only PGM files ...**/
            for( File f : files  ) {

                if ( c >= limit ) break;
                c++;

                System.out.println( f.getAbsolutePath() );
                byte bytesRAW[] = new byte[0];
                String bytesRAW_as_BASE64 = "";

                try {

                    // conversion of images into BASE64 encoding
                    bytesRAW = readBytesFromFile( f.getAbsolutePath() );

                    bytesRAW_as_BASE64 = Base64.encodeBase64String( bytesRAW );

                    dal.putRawImagesAsFile( f.getAbsolutePath(), bytesRAW, bytesRAW_as_BASE64);

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        finally {
            System.out.println("--> finished loading MNIST files to HBase from : " + folder.getAbsolutePath() );
            System.out.println("--> c=" + c );
        }

    }

    public static void ingest_Labeled_Image_DATA_to_HBase(File folder2, FileFilter filter2, int limit2, FileNameBasedLabeler labeler) {

        System.out.println(">>> Folder: " + folder2.getAbsolutePath() );

        if ( filter2 == null ) {
            filter2 = new ImageFileFilter();
        }

        System.out.println(">>> " + filter2 + " will be used to filter image files by extension.");

        File[] files = folder2.listFiles( filter2 );

        System.out.println(">>> " + files.length + " files found.");
        System.out.println(">>> " + labeler + " will be used as image labeler." );

        int c = 0;

        try {


            /** iterate on the given folder and read only PGM files ...**/
            for( File f : files  ) {

                if ( c >= limit2 ) break;
                c++;

                System.out.println( f.getAbsolutePath() );
                byte bytesRAW[] = new byte[0];
                String bytesRAW_as_BASE64 = "";

                try {

                    // conversion of images into BASE64 encoding
                    bytesRAW = readBytesFromFile( f.getAbsolutePath() );

                    bytesRAW_as_BASE64 = Base64.encodeBase64String( bytesRAW );

                    String label = labeler.getLabelsAsString( f.getName() );

                    dal.putRawImagesAsFileWithLabel( f.getAbsolutePath(), bytesRAW, bytesRAW_as_BASE64, label );

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        finally {
            System.out.println("--> finished loading MNIST files to HBase from : " + folder2.getAbsolutePath() );
            System.out.println("--> c=" + c );
        }
    }



    public static byte[] readBytesFromFile(String imageName) throws IOException {

        Path fileLocation = Paths.get( imageName );
        byte[] data = Files.readAllBytes(fileLocation);
        return data;

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


    public static String getImageLabelsByKey(String s) {

        try {
            return dal.getLabelsAsJSON( s );
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



    public static String getImageLabelsByKeyAndAlgorithm(String s, String a) {

        try {
            return dal.getLabelsAsJSON( s, a );
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void export_LABELED_IMAGES(File exportFolder) {

        if ( !exportFolder.exists() ) {
            exportFolder.mkdirs();
        }

        try {
            dal.scanImagesAndLabels( exportFolder );
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static boolean hasImageWithKey(String s) throws IOException {

        return dal.hasImageWithKey(Bytes.toBytes( s ));

    }


    public static byte[] getImageByKey(String k) {

        String cf = ImageTabAdmin.cfRaw;
        String cn = "bytesRAW";

        return dal.getImageWithKey( k, cf, cn);

    }

    public static String getImageMetaDataByKey(String k) {

        String cf = ImageTabAdmin.cfRaw;
        String cn = "bytesRAW";
        byte[] data = dal.getImageWithKey( k, cf, cn);

        String md = null;

        try {

            md = ImageConverter.getImageMetadataAsJSON( data );

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return md;

    }

    public static String getImageRowByKey( String k ) {

        try {
            return dal.getImageRow( k.getBytes() );
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }


}
