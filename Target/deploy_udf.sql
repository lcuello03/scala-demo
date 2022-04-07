-- Step 1: Create a new stage if necessary
CREATE STAGE IF NOT EXISTS DB1.SCALA_DEMO.UDFS;

-- Step 2: PUT the Scala Library JAR in the stage
PUT file:///home/BlackDiamond/.resources/scala-library-2.12.15.jar
    @DB1.SCALA_DEMO.UDFS
    AUTO_COMPRESS = FALSE
    OVERWRITE = TRUE;

-- Step 3: PUT the JAR with the UDF code in the Stage
PUT file:///home/BlackDiamond/hello-world/target/scala-2.12/hello-world_2.12-1.0.jar
    @DB1.SCALA_DEMO.UDFS
    AUTO_COMPRESS = FALSE
    OVERWRITE = TRUE;

-- Step 4: Create UDF
CREATE OR REPLACE procedure DB1.SCALA_DEMO.COMPANYTYPE()
RETURNS STRING
LANGUAGE SCALA
runtime_version=2.12
PACKAGES = ('com.snowflake:snowpark:latest')
IMPORTS = ('@DB1.SCALA_DEMO.UDFS/hello-world_2.12-1.0.jar', '@DB1.SCALA_DEMO.UDFS/scala-library-2.12.15.jar')
HANDLER = 'Sample.Sample.companyType';

-- Step 5: Create UDF Function
CREATE OR REPLACE FUNCTION DB1.SCALA_DEMO.linearRegression(X ARRAY, Y ARRAY, valueToPredict DOUBLE)
RETURNS DOUBLE
LANGUAGE JAVA
IMPORTS = ('@DB1.SCALA_DEMO.UDFS/hello-world_2.12-1.0.jar', '@DB1.SCALA_DEMO.UDFS/scala-library-2.12.15.jar')
HANDLER = 'Regressions.Regressions.linearRegression';