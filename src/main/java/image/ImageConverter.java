package image;

import io.filefilters.PNGFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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

}
