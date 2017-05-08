CREATE EXTERNAL TABLE mnist
STORED AS KUDU
TBLPROPERTIES (
  'kudu.table_name' = 'MNIST'
);



select count(PREDICTION),PREDICTION as all_prediction  from mnist group by prediction order by prediction;



select count(PREDICTION),PREDICTION as correct_prediction from mnist where prediction=knownlabel group by prediction order by prediction;



