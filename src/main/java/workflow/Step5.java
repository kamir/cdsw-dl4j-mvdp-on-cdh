package workflow;

import demo.ImageClassifierKUDU;


/**
 * Prepare a model store in Kudu.
 *
 *
 */
public class Step5 {

    public static void main(String[] args ) {

        try {

            ImageClassifierKUDU.main( args );

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

}
