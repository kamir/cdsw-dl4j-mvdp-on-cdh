package data;

/**
 * The EntityWrapper converts an object into a record or document representation. It is comparable with
 * a data mapper in an ORM system, but not limitted to a particular SQL database.
 *
 * The EntityWrapper wraps around one object to represent it in multiple sources:
 *   - HBase
 *   - Kudu
 *   - SOLR
 *   - HDFS
 *
 * Depending on size and access patterns, multiple parallel representations can be used or transformations
 * between the different layers can be done.
 */
public interface EntityWrapper {



}
