package com.marklogic.datamovement.experiments;

import java.io.Serializable;

/**
 * Created by hpuranik on 12/2/2016.
 */
public class MarkLogicPartition implements Serializable {

    private long index = -1;
    private String[] uris = null;
    private String host = null;
    private String forest = null;
    private int port = -1;
    private String database = null;
    private String usr = null;
    private String pwd = null;

    public MarkLogicPartition(long id,
                              String[] uriList,
                              String hostName,
                              String forestName,
                              int portNum,
                              String dbName,
                              String userName,
                              String password) {
        index = id;
        uris = new String[uriList.length];
        System.arraycopy(uriList, 0, uris, 0, uriList.length);
        host = hostName;
        forest = forestName;
        port = portNum;
        database = dbName;
        usr = userName;
        pwd = password;
    }

    public String getHost() {
        return host;
    }


    public long getIndex() {
        return index;
    }

    public String[] getUris() {
        return uris;
    }

    public String getForest() {
        return forest;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsr() {
        return usr;
    }

    public String getPwd() {
        return pwd;
    }

    public void print(){
        System.out.println("****Partition - " + this.getIndex() + " Start****");
        System.out.println("Database = " + this.getDatabase());
        System.out.println("Host = " + this.getHost());
        System.out.println("Port = " + this.getPort());
        System.out.println("Forest = " + this.getForest());
        System.out.println("URI Count = " + this.getUris().length);
        System.out.println("****Partition - " + this.getIndex() + " End****");
    }
}
