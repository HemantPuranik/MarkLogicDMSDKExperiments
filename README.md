# MarkLogic Data Movement SDK Experiments
Experiments using MarkLogic 9 Data Movement SDK

## MarkLogic Query Partitioner ##

Data processing frameworks like [Spark](http://spark.apache.org/), [Hadoop MapReduce](http://hadoop.apache.org/), [Spring-Batch](http://projects.spring.io/spring-batch/) as well as open source and commercial ETL frameworks are designed to scale across multiple machines. While processing large volume of data, these frameworks partition the dataset across multiple machines within a cluster. These frameworks typically distribute the code across multiple machines as well so that each machine in the cluster processes the data that is locally available to the machine. For more information around partitioning concepts, please refer to [Spring-Batch Scaling and Parallel Processing](http://docs.spring.io/spring-batch/reference/html/scalability.html) or [Apache Spark RDD Partitions and Partitioning](https://jaceklaskowski.gitbooks.io/mastering-apache-spark/content/spark-rdd-partitions.html).

### Why MarkLogic Query Partitioner ###
 
Integration of MarkLogic in a highly scalable and efficient third party data processing platform or framework requires data queried from MarkLogic to be partitioned appropriately so that distributed computing capabilities of these frameworks can be fully utilized.
In a MarkLogic database, the data is typically distributed across multiple hosts within MarkLogic cluster and stored across multiple forests on each host. While data partitioning logic could be specific to data domain and can leverage specific range indexes, a default implementation of MarkLogic data partitioning should account for generic factors like host, forest, maximum partition size and so on.

[MarkLogicQueryPartitioner](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicQueryPartitioner.java) demonstrates how to partition query results from MarkLogic that can be handed over to a distributed cluster computing framework which will perform downstream processing of the query results like advanced analytics, machine learning and so on. Typically open source frameworks like Spring-Batch or Apache Spark have their own ecosystem and if a MarkLogic customers are already using one of these frameworks as a part of their overall architecture then they would prefer MarkLogic to fit nicely in that architecture.

Note that [MarkLogicQueryPartitioner](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicQueryPartitioner.java) simply demonstrates an approach to partitioning query results. This approach is agnostic to which framework you would integrate MarkLogic into. Actual implementation for a specific framework like Spring Batch or Apache Spark would be different. The approach demonstrated in MarkLogicQueryPartitioner needs to be adapted in the framework specific implementation. Hadoop MapReduce specific implementation is available within [MarkLogic Connector For Hadoop](http://developer.marklogic.com/products/hadoop).

### MarkLogic Query Partitioner using QuryHostBatcher###

The approach demonstrated in [MarkLogicQueryPartitioner](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicQueryPartitioner.java) leverages ML9 [QueryHostBatcher](https://github.com/marklogic/data-movement/blob/develop/data-movement/src/main/java/com/marklogic/datamovement/QueryHostBatcher.java). This is a limited use of QueryHostBatcher. QueryHostBatcher itself can orchestrate the overall data processing job. Nevertheless it already has all the MarkLogic specific smartness when it comes to partitioning the query results. [QueryHostBatcher](https://github.com/marklogic/data-movement/blob/develop/data-movement/src/main/java/com/marklogic/datamovement/QueryHostBatcher.java) batches the query results, takes MarkLogic cluster topology as well as failover capabilities into account. It guarantees that each batch of query results contains the documents that are located on the same host and same forest. 

[MarkLogicQueryPartitioner](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicQueryPartitioner.java) assumes that data partitioning is a one-time task in a long running data processing job. As a part of data partitioning process, the query is executed via [QueryHostBatcher](https://github.com/marklogic/data-movement/blob/develop/data-movement/src/main/java/com/marklogic/datamovement/QueryHostBatcher.java) which in turn returns the batches of URIs. These batches are used to create [MarkLogicPartition](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicPartition.java) objects. Once the query job is complete, [MarkLogicQueryPartitioner](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicQueryPartitioner.java) has an array of [MarkLogicPartition](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicPartition.java) objects. 

[MarkLogicPartition](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicPartition.java) objects are serializable since they need to be passed across multiples hosts and multiple JVM processes that would run in parallel. At the same time, these objects are bulkier than ideally desired as they store the URI of all the documents that belong in the partition. A better approach could be to construct MarkLogicPartition objects using metadata associated with query results. For example, refer to XQuery API [hadoop:get-splits](http://docs.marklogic.com/hadoop:get-splits). It can be used to create the lightweight MarkLogicPartition objects and actual query results will be populated as a part of distributed computing job across multiple JVM processes. Since [MarkLogicQueryPartitioner](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicQueryPartitioner.java) approach is based on [QueryHostBatcher](https://github.com/marklogic/data-movement/blob/develop/data-movement/src/main/java/com/marklogic/datamovement/QueryHostBatcher.java), at this time it simply stores the URIs of the documents in [MarkLogicPartition](https://github.com/HemantPuranik/MarkLogicDMSDKExperiments/blob/master/src/main/java/com/marklogic/datamovement/experiments/MarkLogicPartition.java).

### How to run MarkLogic Query Partitioner ###

#### Prerequisites ####

1. You have [MarkLogic 9 EA3 or above release](http://ea.marklogic.com/) installed and running.
2. You are familiar with [Data Movement SDK](http://ea.marklogic.com/features/data-integration/data-movement-sdk/) and you have setup maven based development environment for using MarkLogic 9 Java Client API and Data Movement SDK.  

#### Build ####

Clone this repository and run 

	mvn package

This will download all the dependencies from public repository and create the file /target/DataMovementExperiments-1.0-SNAPSHOT.jar.

#### Setup ####

1. Configure MarkLogic cluster, created a database and design the forest layout across multiple hosts. 
2. Using mlcp, load a relatively large number of documents and tag documents with one or more collection names such that a collection query will typically generate results from multiple forests across multiple hosts.
3. Optionally setup a REST server for the database. You can use the default REST server at port 8000 as well.

#### Usage ####

Navigate to the /target directory

	java -cp "DataMovementExperiments-1.0-SNAPSHOT.jar:lib/*" \
						com.marklogic.datamovement.experiments.MarkLogicQueryPartitioner \
						-host localhost -port 8000 -usr username -pwd password \
						-database myDataabse -collection myCollection -max_partition myMaximumPartitionSize
						
Successful execution will print the partition information, number of partitions and total number of URIs returned for the collection query.




