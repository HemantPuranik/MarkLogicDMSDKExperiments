package com.marklogic.datamovement.experiments;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.datamovement.DataMovementManager;
import com.marklogic.datamovement.ForestConfiguration;
import com.marklogic.datamovement.JobTicket;
import com.marklogic.datamovement.QueryHostBatcher;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hpuranik on 12/2/2016.
 */
public class MarkLogicQueryPartitioner {



    MarkLogicPartition[] getPartitions(String host,
                                       int port,
                                       String userName,
                                       String password,
                                       String databaseName,
                                       QueryDefinition query,
                                       int partitionSize){

        MarkLogicHostPartitions hostPartitions = new MarkLogicHostPartitions();
        DatabaseClient dbClient = DatabaseClientFactory.newClient(
                host,
                port,
                databaseName,
                new DatabaseClientFactory.DigestAuthContext(userName, password)
        );

        DataMovementManager movMgr = DataMovementManager.newInstance().withClient(dbClient);

        QueryHostBatcher uriBatcher = movMgr.newQueryHostBatcher(query)
                .withConsistentSnapshot()
                .withJobName("Query Partitioning")
                .withBatchSize(partitionSize)
                .onUrisReady((client, batch) -> {
                            //populate partition and add it to the map
                            MarkLogicPartition part = new MarkLogicPartition(batch.getJobBatchNumber(),
                                    batch.getItems(),
                                    batch.getForest().getHostName(),
                                    batch.getForest().getForestName(),
                                    client.getPort(),
                                    client.getDatabase(),
                                    client.getUser(),
                                    client.getPassword());
                            hostPartitions.addPartition(part);
                            System.out.println("added partition");
                        }
                )
                .onQueryFailure((client, exception) -> {
                            exception.printStackTrace();
                        }
                );

        JobTicket partitionTicket = movMgr.startJob(uriBatcher);
        uriBatcher.awaitCompletion();
        movMgr.stopJob(partitionTicket);

        MarkLogicPartition[] parts = hostPartitions.getDistributedPartitions();
        return parts;
    }

    public static void main(String[] args) throws IOException {

        CommandLineValues values = new CommandLineValues();
        values.readArgs(args);
        if(values.isCmdLineError()){
            System.exit(-1);
        }
        //Arguments are valid. Print arguments
        values.print();

        //construct a query - only collection query here based on collection name argument.
        StructuredQueryBuilder queryBuilder = new StructuredQueryBuilder();
        StructuredQueryDefinition query = queryBuilder.collection(values.getCollectionName());

        //get partitions
        MarkLogicQueryPartitioner queryPartitioner = new MarkLogicQueryPartitioner();
        MarkLogicPartition[] parts = queryPartitioner.getPartitions(
                values.getHostName(),
                values.getPortNum(),
                values.getUserName(),
                values.getPassword(),
                values.getDatabaseName(),
                query,
                values.getMaxPartitionSize()
        );

        //print partitions
        int totalURICount = 0;
        int partCount = parts.length;
        for(MarkLogicPartition part : parts){
            part.print();
            totalURICount += part.getUris().length;
        }
        System.out.println("Total URIs Partitioned: " + totalURICount);
        System.out.println("Number Of Partitions: " + partCount);
    }
}
