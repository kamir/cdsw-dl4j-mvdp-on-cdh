package data;

import java.util.Hashtable;

/**
 * This class is a high-level representation of a labeled image.
 *
 * Combined with entity wrappers it allows storing it in multiple "big-data" storage components.
 */
public class LabeledImage {

    /**
     * Key value pairs describe this particular image.
     *
     * Typically, the KVPs are extracted by Apache Tika.
     */
    public Hashtable<String,String> extractedFacts;

    /**
     * Individual learning algorithms can be used for classification. Algorithms differ in quality and purpose. It can
     * help to use a different learning approach or just slightly different parameters, just to achieve higher precision,
     * or even to solve a totally different problem.
     *
     * Example: Instead of learning on one 4-class model one could also use two algorithms, to learn two two-class models
     * from which the results are combined to a final result.
     */
    public Hashtable<String,Hashtable<String,String>> learnedFacts;

    public String getImagePGM_BASE64() {
        return imagePGM_BASE64;
    }

    public void setImagePGM_BASE64(String imagePGM_BASE64) {
        this.imagePGM_BASE64 = imagePGM_BASE64;
    }

    /**
     * The raw bytes are converted into a BASE64 String for "high-level" portability.
     */
    String imagePGM_BASE64;

}
