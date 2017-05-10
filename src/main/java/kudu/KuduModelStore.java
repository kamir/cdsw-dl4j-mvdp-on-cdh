package kudu;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The Kudu-Image store class provides an API to access the Kudu-table which contains our image dataset.
 *
 * This dataset can be an unbound
 *
 *
 */
public class KuduModelStore {



    public static KuduClient kuduClient = null;



    /**
     * Our Kudu image store is defined by the following settings:
     */
    public static String tableName = "DLModels";

    public static String kuduMaster = "quickstart.cloudera";

    public static ArrayList<ColumnSchema> columnList = null;
    public static Schema schema = null;

    public static void init() {

        System.out.println("Setup model store in Kudu");

        kuduClient = new KuduClient.KuduClientBuilder(kuduMaster).build();

        columnList = new ArrayList<>();
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("ID", Type.STRING).key(true).build());
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("CONF", Type.STRING).key(false).build());
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("PARAMETERSET", Type.STRING).key(false).build());  // BASE64 encoded
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("TRAINING_SET", Type.STRING).key(false).build());  // BASE64 encoded
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("VALIDATION_SET", Type.STRING).key(false).build());  // BASE64 encoded
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("VALIDATION_PROFILE", Type.STRING).key(false).build());  // BASE64 encoded
        columnList.add(new ColumnSchema.ColumnSchemaBuilder("TRAINING_PROFILE", Type.STRING).key(false).build());  // BASE64 encoded

        columnList.add(new ColumnSchema.ColumnSchemaBuilder("ACCURACY", Type.DOUBLE).key(false).build());

        schema = new Schema(columnList);

    }



    /** Count all available models **/
    public static int getNrOfModels() throws KuduException {

        List<String> projectColumns = new ArrayList<>(1);
        projectColumns.add("ID");

        KuduSession session = KuduModelStore.kuduClient.newSession();

        KuduTable table = KuduModelStore.getModelTable();

        KuduScanner scanner = KuduModelStore.kuduClient.newScannerBuilder(table)
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
     * @param model
     */
    public static void updateModelEvaluationResult( String id, Object model ) {

        try {

            KuduSession session = KuduModelStore.kuduClient.newSession();

            KuduTable table = KuduModelStore.getModelTable();

            Update update = table.newUpdate();

            PartialRow row = update.getRow();

            row.addString("ID", id);

            session.apply(update);

            session.flush();

        }
        catch( Exception ex ) {

            ex.printStackTrace();

        }

    }








    /**
     * We use fixed table for doing some image classification exercises.
     *
     * @return
     */
    public static KuduTable getModelTable() {

        KuduTable table = null;

        try {

            table = kuduClient.openTable(tableName);

        }
        catch (KuduException e) {
            e.printStackTrace();
        }

        return table;
    }








}
