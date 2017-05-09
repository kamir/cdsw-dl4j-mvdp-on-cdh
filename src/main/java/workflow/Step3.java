package workflow;

import kudu.KuduModelStore;


/**
 * Prepare a model store in Kudu.
 *
 *
 */
public class Step3 {

    public static void main(String[] args ) {


        KuduModelStore.tableName = "MNIST_models";
        KuduModelStore.kuduMaster = "cdsw-mk8-1.vpc.cloudera.com";

        try {

            KuduModelStore.init();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

}
