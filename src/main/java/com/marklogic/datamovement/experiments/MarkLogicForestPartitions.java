package com.marklogic.datamovement.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by hpuranik on 12/2/2016.
 */
public class MarkLogicForestPartitions {
    private HashMap<String, ArrayList<MarkLogicPartition>> forestParts = null;
    MarkLogicForestPartitions(){
        forestParts = new HashMap<>();
    }
    void addPartition(MarkLogicPartition part){
        String forestName = part.getForest();
        ArrayList<MarkLogicPartition> parts = forestParts.getOrDefault(forestName, null);
        if(parts == null){
            //forest encountered for the first time
            parts = new ArrayList<MarkLogicPartition>();
            forestParts.put(forestName, parts);
        }
        parts.add(part);
    }

    MarkLogicPartition getPart(String forestName, int index){
        return forestParts.get(forestName).get(index);
    }

    /* organize all the partitions within a forest in breadth first manner
       For example 3 forests and 3 partitions each
        F1 P1
        F2 P1
        F3 P1
        F1 P2
        F2 P2
        F3 P2
        F1 P3
        F2 P3
        F3 P3
     */
    ArrayList<MarkLogicPartition> getDistributedParitionList(){
        ArrayList<MarkLogicPartition> parts = new ArrayList<>();
        Set<String> forests = forestParts.keySet();
        int distro = 0;
        boolean more = true;
        while(more) {
            more = false;
            for (String forest : forests) {
                //for each forest get distro forest
                ArrayList<MarkLogicPartition> forestSplits = forestParts.get(forest);
                parts.add(forestSplits.get(distro));
                more = more || (forestSplits.size() > (distro+1));
            }
            distro +=1;
        }
        return parts;
    }



}
