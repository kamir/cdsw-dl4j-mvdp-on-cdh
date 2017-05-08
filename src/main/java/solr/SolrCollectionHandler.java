package solr;

import data.LabeledImage;

public class SolrCollectionHandler {

    // SOLR SERVICE (from CFG)


    /**
     * Initialize a SOLR Server conncetion ...
     */
    public SolrCollectionHandler() {

    }

    String defaultCollection = "MNIST";
    String collection = defaultCollection;

    /**
     * Properties are mapped to fields (requires dynamic fields in the index, since fieldnames are combinations of
     * property names and context.
     *
     * @param image
     * @param collection
     */
    public void putLabeledPGMImage_to_COLLECTION(LabeledImage image, String collection) {
        this.collection = collection;

        // check accessability to SOLR ...

        // put item to collection ...
        // Document doc = SolrEntityWrapper.getDocumentForLabeledImage( image );

    }

}
