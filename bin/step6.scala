// Initiate the spark shell and register custom serializers.
//
// spark-shell --jars cdsw-dl4j-demo-1.0-SNAPSHOT-bin.jar --conf spark.serializer=org.apache.spark.serializer.KryoSerializer --conf spark.kryo.registrator=org.nd4j.Nd4jRegistrator

// import dependencies.

import org.deeplearning4j.util.ModelSerializer
import org.apache.spark.{SparkFiles}
import java.util.Arrays
import java.io.File

// Add the weights file to the context

sc.addFile("file:///home/cloudera/bpann.zip")

// Read the image pbm.
val image = sc.wholeTextFiles("/user/cloudera/MNIST/1_01.pbm")

// Remove the metadata from the image content.
val output = image.map(x => x._2.replace("\n","").substring(7))

// Create a feature matrice. This is the matrix that represents the input neurons.
val featureMatrix = output.map(x => TestSpark.convertBinaryToFeatureMatrix(1,x.split("\\s+")))

// Extract the classifier output.
val classifications = featureMatrix.map(x => ModelSerializer.restoreMultiLayerNetwork(new File(SparkFiles.get("bpann.zip"))).output(x))

// Extract the final output.
classifications.map( x => TestSpark.winnerTakesAllDecision(x)).collect()

// Result is 1 a correct classification.
res8: Array[Int] = Array(1)