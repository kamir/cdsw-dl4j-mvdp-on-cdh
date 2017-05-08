package prep;

import io.filefilters.ImageFileFilter;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.File;
import java.io.FileInputStream;



/**
 * Created by kamir on 28.04.17.
 */
public class ImageTransformer {

    public static void main(String[] args) {


        File f = new File( "./brands_images_raw" );

        File[] files = f.listFiles();

        for( File f1 : files ) {

            if ( f1.isDirectory() ) {

                // count images per category
                preProcessImageCategory_round_01( f1 );

                // extract sizes
                double[] dimMax = preProcessImageCategory_round_02( f1 );
                // normalize and convert to data-matrix


            }

        }

    }


    private static void preProcessImageCategory_round_01( File file ) {

        File[] images = file.listFiles( new ImageFileFilter() );
        System.out.println( file.getName() + " => " + images.length );

    }


    private static double[] preProcessImageCategory_round_02( File file ) {

        double[] dim = new double[2];
        dim[0] = 0;
        dim[1] = 0;

        double[] dimMax = new double[2];
        dimMax[0] = 0;
        dimMax[1] = 0;

        try {



            // System.out.println( file.getAbsolutePath() );

            File[] images = file.listFiles(new ImageFileFilter());

            for (File image : images) {

                System.out.println( image.getAbsolutePath() );


                // TIKA PARSING to get the image size

                //Parser method parameters
                Parser parser = new AutoDetectParser();
                BodyContentHandler handler = new BodyContentHandler();
                Metadata metadata = new Metadata();

                FileInputStream inputstream = new FileInputStream(image);
                ParseContext context = new ParseContext();

                parser.parse(inputstream, handler, metadata, context);

                // System.out.println(handler.toString());

                //getting the list of all meta data elements
                String[] metadataNames = metadata.names();

                for(String name : metadataNames) {
                   System.out.println(name + "  :  " + metadata.get(name));
                }

                dim[0] = Integer.parseInt( metadata.get("width") );
                dim[1] = Integer.parseInt( metadata.get("height") );

                if ( dim[0] > dimMax[0] ) dimMax[0] = dim[0];
                if ( dim[1] > dimMax[1] ) dimMax[1] = dim[1];

                System.out.println( dimMax[0] + " X " + dimMax[1] + " => " + file.getAbsolutePath() );

            }
        }
        catch(Exception ex) {

            ex.printStackTrace();

        }
        return dimMax;

    }
}
