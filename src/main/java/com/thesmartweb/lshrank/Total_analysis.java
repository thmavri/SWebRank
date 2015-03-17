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
 * Class of the main algorithm functionalities
 * @author Themistoklis Mavridis
 */
public class Total_analysis {

    /**
     * a list with all the wordlists produced
     */
    protected List<ArrayList<String>> array_wordLists = new ArrayList<>();

    /**
     * a list with all the words from all the wordlists
     */
    protected List<String> wordList_total = new ArrayList<>();

    
    /**
     * the f1 score
     */
    protected double F1;
    /**
     * Method to call search analysis for every query and to save the wordlists
     * @param wordList_previous the previous wordlist to check convergence
     * @param iteration_counter the iteration number
     * @param example_dir the directory to save the files
     * @param domain the domain we analyze
     * @param enginechoice the search engines chosen
     * @param queries the queries we search for
     * @param results_number the amount of results for each query
     * @param top_visible the amount of results if we use Visibility Score  (http://www.advancedwebranking.com/user-guide/html/en/ch08s06.html)
     * @param mozMetrics the metrics of Moz chosen
     * @param moz_threshold_option flag if we are going to use Moz threshold or not
     * @param moz_threshold the threshold to moz metrics
     * @param top_count_moz the amount of links to keep if we use Moz for evaluation
     * @param ContentSemantics get the choice of Content Semantic Analysis algorithm that we are going to use
     * @param SensebotConcepts the amount of concepts to be recognized if Sensebot is used
     * @param LSHrankSettings the settings for LDA and LSHrank in general (check the ReadInput Class)
     * @param config_path the configuration path to get all the api keys
     */
    public void perform(List<String> wordList_previous,int iteration_counter,String example_dir,String domain, List<Boolean> enginechoice,List<String> queries,int results_number, int top_visible, List<Boolean> mozMetrics, boolean moz_threshold_option, double moz_threshold, int top_count_moz, List<Boolean> ContentSemantics, int SensebotConcepts, List<Double> LSHrankSettings, String config_path){
        //for every term of the query String[] it performs the search analysis function
        //which includes sumbission of the term to the search engines, getting the results according to the options selected
        //parsing the websites and getting the content and the running LDA on them and getting the top content
        for (String query : queries) { 
            System.gc();
            System.gc();
            System.gc();
            List<String> wordList = new ArrayList<>();
            //we call search analysis that is doing all the work needed and returns to us the wordlists
            Search_analysis sa = new Search_analysis();
            //the following string represents the directory for each query
            String example_directory = example_dir + query + "-query//";
            //we set the alpha variable of the LDA algorithm to the value that is said to be optimal in the paper of LDA, alpha
            double alpha = 50 / LSHrankSettings.get(1);
            //we call perform method of search analysis
            wordList = sa.perform(iteration_counter, example_directory, domain, enginechoice, query, results_number, top_visible, LSHrankSettings, alpha, mozMetrics, top_count_moz, moz_threshold_option, moz_threshold, ContentSemantics, SensebotConcepts, config_path);
            //we add the wordlist to the vector of word list
            ArrayList<String> wordArrayList=new ArrayList<>(wordList);
            array_wordLists.add(wordArrayList);
            //we add the wordlist and to the total wordlist
            wordList_total.addAll(wordList);
        }
        //we are going to check the convergence rate
        CheckConvergence cc = new CheckConvergence(); // here we check the convergence between the two wordLists, the new and the previous
        //the concergence percentage of this iteration, we save it in Elastic Search
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
     * Getter of F1 score
     * @return f1 score
     */
    public double getF1()
    {
        return F1;
    }
    
    /**
     * Getter of the total wordlist
     * @return the list with all the words produced from all queries
     */
    public List<String> getwordList_total()
    {
        return wordList_total;
    }

    /**
     * Getter of all the wordlists for each query in separate
     * @return all the wordlists for each query
     */
    public List<ArrayList<String>> getarray_wordLists()
    {
        return array_wordLists;
    }


    

}
