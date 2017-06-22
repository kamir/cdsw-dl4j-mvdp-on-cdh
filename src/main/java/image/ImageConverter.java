package image;

import com.google.gson.Gson;
import io.filefilters.PNGFileFilter;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;

/**
 * Created by kamir on 09.05.17.
 */
public class ImageConverter {

    /**
     * For some reason I could not execute the convert command via ProcessBuilder.
     * We use a script instead to convert in the console.
     *
     * @param baseFolder
     * @param baseFolderPGM
     * @throws Exception
     */
    public static String convert_PNG_TO_PGM(File baseFolder, File baseFolderPGM, String scriptPath ) throws Exception {

        File file = new File( scriptPath + "/convert-png-to-pgm.sh" );

        System.out.println( "Create a converter script: " + file.getAbsolutePath() );

        BufferedWriter bw = new BufferedWriter( new FileWriter( scriptPath + "/convert-png-to-pgm.sh" ) );

        for( File f : baseFolder.listFiles( new PNGFileFilter() ) ) {
            String cmd = store_as_PGM( f.getAbsolutePath(), baseFolderPGM );
            bw.write( cmd + "\n");
        }

        bw.close();

        return file.getAbsolutePath();

    }

    /**
     * Reads the binary image into a BufferedImage and writes it to a different format ...   !!!
     *
     * Currently, the PGM export is not complete. We simple generate a call to the convert tool
     * and collect all those calls in a text file, which works as a shell script later.
     *
     * @param imageName
     * @return
     * @throws Exception
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

    public static BufferedImage getBufferedImage(INDArray array) {

        System.out.println("Array.rank(): " + array.rank());
        System.out.println("Size(-1): " + array.size(-1));
        System.out.println("Size(-2): " + array.size(-2));

        BufferedImage imageToRender = new BufferedImage(array.columns(),array.rows(),BufferedImage.TYPE_BYTE_GRAY);
        for( int x = 0; x < array.columns(); x++ ){
            for (int y = 0; y < array.rows(); y++ ) {
                System.out.println("x: " + (x) + " y: " + y);
                imageToRender.getRaster().setSample(x, y, 0, (int) (255 * array.getRow(y).getDouble(x)));
            }
        }

        return imageToRender;
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static BufferedImage toImage(INDArray matrix) {
        BufferedImage img = new BufferedImage(matrix.rows(), matrix.columns(), BufferedImage.TYPE_INT_ARGB);
        WritableRaster r = img.getRaster();
        int[] equiv = new int[matrix.length()];
        for (int i = 0; i < equiv.length; i++) {
            equiv[i] = (int) matrix.getScalar(i).element();
        }
        r.setDataElements(0, 0, matrix.rows(), matrix.columns(), equiv);
        return img;
    }


    public static String getImageMetadataAsJSON(byte[] imageBytes) throws Exception {

        //Parser method parameters
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();

        InputStream inputstream = new ByteArrayInputStream(imageBytes);

        ParseContext context = new ParseContext();

        parser.parse(inputstream, handler, metadata, context);

        // System.out.println(handler.toString());

        //getting the list of all meta data elements
        String[] metadataNames = metadata.names();

        for(String name : metadataNames) {
            System.out.println(name + "  :  " + metadata.get(name));
        }

        Gson gson = new Gson();

        return gson.toJson( metadata  );

    }

}
