package workflow;

import kudu.KuduImageStore;


/**
 * Prepare an image store in Kudu.
 *
 *
 */
public class Step2 {

    public static void main(String[] args ) {


        KuduImageStore.tableName = "MNIST";
        KuduImageStore.kuduMaster = "cdsw-mk8-1.vpc.cloudera.com";

        try {

            KuduImageStore.init();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

}
