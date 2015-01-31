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

import java.util.List;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.json.simple.JSONObject;

/**
 *
 * @author themis
 */
public class Total_analysis {

    /**
     *
     */
    protected List<ArrayList<String>> array_wordLists = new ArrayList<>();

    /**
     *
     */
    protected List<String> wordList_total = new ArrayList<>();

    
    /**
     *
     */
    protected double F1;
    /**
     *
     * @param example_dir
     * @param enginechoice
     * @param queries
     * @param results_number
     * @param top_visible
     * @param mozMetrics
     * @param moz_threshold_option
     * @param moz_threshold
     * @param top_count_moz
     * @param ContentSemantics
     * @param SensebotConcepts
     * @param LSHrankSettings
     */
    public void perform(List<String> wordList_previous,int iteration_counter,String example_dir,String domain, List<Boolean> enginechoice,List<String> queries,int results_number, int top_visible, List<Boolean> mozMetrics, boolean moz_threshold_option, double moz_threshold, int top_count_moz, List<Boolean> ContentSemantics, int SensebotConcepts, List<Double> LSHrankSettings){
        //for every term of the query String[] it performs the search analysis function
        //which includes sumbission of the term to the search engines, getting the results according to the options selected
        //parsing the websites and getting the content and the running LDA on them and getting the top content
        for (int i=0;i<queries.size();i++){ 
            System.gc();
            System.gc();
            System.gc();
            List<String> wordList = new ArrayList<String>();
            //we call search analysis that is doing all the work needed and returns to us the wordlists
            Search_analysis sa = new Search_analysis();
            //the following string represents the directory for each query
            String example_directory = example_dir+queries.get(i) + "-query//";
            //we set the alpha variable of the LDA algorithm to the value that is said to be optimal in the paper of LDA, alpha
            double alpha = 50 / LSHrankSettings.get(1);
            //we call perform method of search analysis
            wordList = sa.perform(iteration_counter,example_directory, domain, enginechoice, queries.get(i), results_number, top_visible, LSHrankSettings, alpha,mozMetrics,top_count_moz,moz_threshold_option,moz_threshold,ContentSemantics, SensebotConcepts);
            
            //we add the wordlist to the vector of word list
            ArrayList<String> wordArrayList=new ArrayList<String>(wordList);
            array_wordLists.add(wordArrayList);
            //we add the wordlist and to the total wordlist
            wordList_total.addAll(wordList);
        }
        //we are going to check the convergence rate
        CheckConvergence cc = new CheckConvergence(); // here we check the convergence between the two wordLists, the new and the previous
        //the concergence percentage of this iteration
        F1 = cc.F1Calc(wordList_total, wordList_previous);
        Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
        Client client = node.client();
        JSONObject objEngineLevel = new JSONObject();
        objEngineLevel.put("RoundContent", wordList_total);
        objEngineLevel.put("Round", iteration_counter);
        objEngineLevel.put("ConvergenceF1", F1);
        String id=domain+"/"+iteration_counter;
        IndexRequest indexReq=new IndexRequest("lshrankgeneratedcontentperround","content",id);
        indexReq.source(objEngineLevel);
        IndexResponse indexRes = client.index(indexReq).actionGet();
        node.close();
        
     
     }
    
    /**
     *
     * @return
     */
    public double getF1()
    {
        return F1;
    }
    
    /**
     *
     * @return
     */
    public List<String> getwordList_total()
    {
        return wordList_total;
    }

    /**
     *
     * @return
     */
    public List<ArrayList<String>> getarray_wordLists()
    {
        return array_wordLists;
    }


    

}
