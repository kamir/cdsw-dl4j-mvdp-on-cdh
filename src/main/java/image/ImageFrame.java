package image;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a simple image debugger frame.
 *
 * The number of images is limited to 'maxFrames=10' per JVM.
 *
 * @web http://java-buddy.blogspot.com/
 */
public class ImageFrame {

    public static int maxFrames = 10;
    public static int zFrames = 0;

    static ImageFrame frame = null;

    public static ImageFrame getImageFrame() {
        if ( frame == null )
            frame = new ImageFrame();

        return frame;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        ImageFrame frame = new ImageFrame();

        // String imgPath = "/GITHUB.cloudera.internal/cdsw-deeplearning4j-demo-02/MNIST_images_pgm/0_01.pgm";
        String imgPath = "/GITHUB.cloudera.internal/cdsw-deeplearning4j-demo-02/MNIST_images/0_01.png";

        frame.showSinlgeImage( imgPath );

    }

    public static void resetFrames() {

        for(ImageDebugFrame f : frames ) {

            f.dispose();

        }
        maxFrames = 10;
        zFrames = 0;
        frames = new Vector<ImageDebugFrame>();

    };

    static Vector<ImageDebugFrame> frames = new Vector<ImageDebugFrame>();


    public void showSinlgeImage(String path) throws IOException {

        File file = new File(path);

        System.out.println( file.getAbsolutePath() + "=>" + file.canRead() );

        if ( zFrames < maxFrames ) {

            BufferedImage bufferedImage = ImageIO.read( file );

            ImageDebugFrame f = new ImageDebugFrame(bufferedImage);
            frames.add( f );

            zFrames++;

            f.setVisible(true);

        }

    }


    public void compareImages(BufferedImage bi1, BufferedImage bi2) {

        ImageDebugFrame f = new ImageDebugFrame(bi1,bi2);
        frames.add( f );
        zFrames++;
        f.setVisible(true);

    }


    public static class ImageDebugFrame extends JFrame {

        BufferedImage bufferedImage = null;

        public ImageDebugFrame() {

            this.setTitle("image debug frame");

            this.setSize(300, 200);

            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            if (bufferedImage == null) {

                try {

                    bufferedImage = ImageIO.read(this.getClass().getResource("duke.png"));

                } catch (IOException ex) {
                    Logger.getLogger(ImageFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            JLabel jLabel = new JLabel(new ImageIcon(bufferedImage));

            JPanel jPanel = new JPanel();
            jPanel.add(jLabel);
            this.add(jPanel);

        }

        public ImageDebugFrame( BufferedImage i1, BufferedImage i2 ) {

            this.setTitle("image compare frame");

            this.setSize(300, 200);

            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JLabel jLabel1 = new JLabel(new ImageIcon(i1));
            JLabel jLabel2 = new JLabel(new ImageIcon(i2));

            JPanel jPanel = new JPanel();

            jPanel.add(jLabel1);
            jPanel.add(jLabel2);

            this.add(jPanel);

        }

        public ImageDebugFrame( BufferedImage i1 ) {

            System.out.println( i1 );

            this.setTitle("image preview frame");

            this.setSize(300, 200);

            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JLabel jLabel1 = new JLabel(new ImageIcon(i1));

            JPanel jPanel = new JPanel();

            jPanel.add(jLabel1);

            this.add(jPanel);

        }

    }


}