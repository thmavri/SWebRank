/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;
/**
 *
 * @author Themis Mavridis
 */
import java.util.*;

/**
 *
 * @author themis
 */
public class NGD_total {

    /**
     *
     * @param ngd_arr
     * @param queries
     * @param ngd_threshold
     * @param i
     * @return
     */
    public int[] call(String[] ngd_arr,List<String> queries,Double ngd_threshold,int i, String config_path) {
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ NGD @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        //get all ngd scores for all the words comparing to the current query term
        NWD_Analysis ngd=new NWD_Analysis();
        Double[] ngd_scores=new Double[ngd_arr.length];
        System.out.println("into ngd total");
        for(int j=0;j<ngd_scores.length;j++)
        {
          int flag=0;
          //if a word is in the first keywords do not calculate a ngd score for it
          for(int k=0;k<queries.size();k++){
                if (ngd_arr[j].equalsIgnoreCase(queries.get(k))){flag=1;}
          }
          if (flag==0){ngd_scores[j]=ngd.NWD_score(queries.get(i),ngd_arr[j], config_path);}
          if (flag==1){ngd_scores[j]=Double.parseDouble("10000000000000000");}
        }
        //get the scores to a list
        List<Double> ngd_scores_list=Arrays.asList(ngd_scores);
        //create a hashmap in order to map the scores with the indexes
        IdentityHashMap<Double, Integer> originalIndices = new IdentityHashMap<Double, Integer>();
        //copy the original scores list
        for(int j=0; j<ngd_scores_list.size(); j++) {
            originalIndices.put(ngd_scores_list.get(j), j);
        }
        //sort the scores
        List<Double> sorted_ngd_scores = new ArrayList<Double>();
        sorted_ngd_scores.addAll(ngd_scores_list);
        Collections.sort(sorted_ngd_scores);
        //get the original indexes

        /*//if we want to take the top scores(for example top 10)
        int top_ngd=10;
        int[] origIndex=new int[10];
        for(int i3=0; i3<top_ngd; i3++) {
            Double score = sorted_ngd_scores.get(i3);
            / Lookup original index efficiently
            origIndex[i3] = originalIndices.get(score);

        }*/
        //if we have a threshold for ngd scores we follow the code below
        int y=0;
        int counter=0;
        while(y<sorted_ngd_scores.size()){
        if(sorted_ngd_scores.get(y).compareTo(ngd_threshold)<=0){
             counter++;
        }

             y++;
        }
        //we get the indexes and from them we get the terms of ngd_arr that are below the threshold
        //we submit every term to the search engines and we get the keys that LDA analysis returns
        int[] origIndex=new int[counter];
        for(int j=0;j<origIndex.length-1;j++)
        {   
                Double score = sorted_ngd_scores.get(j);
                origIndex[j] = originalIndices.get(score);
                
            
        }
        return origIndex;
        }
}

