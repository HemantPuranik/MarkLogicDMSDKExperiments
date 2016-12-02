package com.marklogic.datamovement.experiments;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hpuranik on 12/2/2016.
 */
public class CommandLineValues {
    @Option(name="-host",usage="MarkLogic Host Name.")
    private String hostName="localhost";

    @Option(name="-port",usage="REST Server Port Number.")
    private int portNum=8000;

    @Option(name="-usr",usage="MarkLogic User Name.")
    private String userName="admin";

    @Option(name="-pwd",usage="MarkLogic User Password.")
    private String password="admin";

    @Option(name="-database",usage="MarkLogic Database Name. (default: database associated with REST Server")
    private String dbName=null;

    @Option(name="-collection",usage="Collection Name to be used in collection query. Required.",required=true)
    private String collectionName=null;

    @Option(name="-max_partition",usage="Maximum number of documents per partition.")
    private int maxPartitionSize=10000;

    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>();

    private boolean cmdLineError = false;

    void readArgs(String[] args){
        CmdLineParser parser = new CmdLineParser(this);


        try {
            // parse the arguments.
            parser.parseArgument(args);


        } catch( CmdLineException e ) {
            //print the error message
            System.err.println(e.getMessage());
            // print the list of available options
            parser.printUsage(System.err);
            //set the flag indicating error in command line args
            cmdLineError = true;
            return;
        }

    }

    public void print(){
        System.out.println("-host is " + hostName);
        System.out.println("-port is " + portNum);
        System.out.println("-usr is " + userName);
        System.out.println("-pwd is " + password);
        System.out.println("-database is " + dbName);
        System.out.println("-collection is " + collectionName);
        System.out.println("-max_partition is " + maxPartitionSize);
    }

    public String getHostName() {
        return hostName;
    }

    public int getPortNum() {
        return portNum;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return dbName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public int getMaxPartitionSize() {
        return maxPartitionSize;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public boolean isCmdLineError() {
        return cmdLineError;
    }
}
