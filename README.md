# Implement a Minimal Viable Data Product Using Deeplearning4J

This project demonstrates data flows and interaction between core components of a data-product. Our example use-case is automatic classification of images using neural networks. 

The code examples show how: 
- to use DL4J in a Spark session / CDSW
- persist labeled training data in Kudu (in order to slice and dice the training set)
- execute a learned model in a Spark-shell session
- execute a learned model in a Spark-streaming job

This means: we go from learning to production, not perfectly robust, but end-2-end!

Let’s go and implement the data product. 

This is our todo-list:

1.	Ingest and convert raw images
2.	Train a model from labeled images
3.	Query for a specific training set
4.	Variation of model parameters 
5.	Evaluation of model quality
6.	Predict the class of unknown images

