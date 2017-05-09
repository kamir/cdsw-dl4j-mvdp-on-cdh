package workflow;

import demo.ImageClassifierHDD;


/**
 * Prepare a model store in Kudu.
 *
 *
 */
public class Step4 {

    public static void main(String[] args ) {

        try {

            ImageClassifierHDD.main( args );

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

}
