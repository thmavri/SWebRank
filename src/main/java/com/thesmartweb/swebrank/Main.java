/* 
 * Copyright 2015 Themistoklis Mavridis <themis.mavridis@issel.ee.auth.gr>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesmartweb.swebrank;
/**
 * Main method of SWebRank. 
 * It receives the input in a txt file in a structure format.
 * Passes all the input variables to the total analysis class.
 * It receives the wordlist of every iteration for each query and creates a wordlist for every domain.
 * It get all the combinations and permutations of all the words in the wordlist.
 * Creates the new queries according to their Normalized Web Distance (using Bing Search API)
 * It compares the wordlist of every iteration with the previous one using Normalized Mutual Information
 * @author Themis Mavridis
 */

import java.io.*;
import java.util.*;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.json.simple.JSONObject;

/**
 * Main class of SWebRank that gets the settings and get the results of every iteration.
 * It calls the process to create new queries and to check if we converge.
 * @author themis
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        Path input_path=Paths.get("//mnt//var//DBs//inputsL//nba//");//input directory
        String output_parent_directory="//mnt//var//DBs//outputsConfL//nba//";//output directory
        String config_path="//mnt//var//DBs//config//";//input directory
        //---Disable apache log manually----
        //System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.NoOpLog");
        System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.Log4JLogger");
        //--------------Domain that is searched----------
        String domain="";
        //------------------search engine related options----------------------
        List<String> queries=null;
        int results_number = 0;//the number of results that are returned from each search engine
        List<Boolean> enginechoice = null;
        //list element #0. True/False Bing
        //list element #1. True/False Google
        //list element #2. True/False Yahoo!
        //list element #3. True/False Merged
        //-----------Moz options---------------------
        List<Boolean> mozMetrics = null;
        //The list is going to contain the moz related input in the following order
        //list element #1. True/False, True we use Moz API, false not
        //list element #2. True if we use Domain Authority
        //list element #3. True if we use External MozRank
        //list element #4. True if we use MozRank
        //list element #5. True if we use MozTrust
        //list element #6. True if we use Subdomain MozRank
        //list element #7. True if we use Page Authority
        //only one is used (the first to be set to true)
        boolean moz_threshold_option = false;//set to true we use the threshold
        Double moz_threshold = 0.0;//if we want to have a threshold in moz
        int top_count_moz = 0;//if we want to get the moz top-something results
        //---------------Semantic Analysis method----------------
        List<Boolean> ContentSemantics=null;
        int SensebotConcepts = 0;//define the amount of concepts that sensebot is going to recognize
        List<Double> SWebRankSettings=null;
        //------(string)directory is going to be used later-----
        String output_child_directory;
        //-------we get all the paths of the txt (input) files from the input directory-------
        DataManipulation getfiles=new DataManipulation();//class responsible for the extraction of paths
        Collection<File> inputs_files;//array to include the paths of the txt files
        inputs_files=getfiles.getinputfiles(input_path.toString(),"txt");//method to retrieve all the path of the input documents
        //------------read the txt files------------
        for (File input : inputs_files) {
            ReadInput ri=new ReadInput();//function to read the input
            boolean check_reading_input=ri.perform(input);
            if(check_reading_input){
                domain=ri.domain;
                //----------
                queries=ri.queries;    
                results_number=ri.results_number;
                enginechoice=ri.enginechoice;
                //------------
                mozMetrics=ri.mozMetrics;
                moz_threshold_option=ri.moz_threshold_option;
                moz_threshold=ri.moz_threshold.doubleValue();
                //---------------
                ContentSemantics=ri.ContentSemantics;
                SWebRankSettings=ri.SWebRankSettings;
            }                 
            int top_visible=0;//option to set the amount of results you can get in the merged search engine
            //------if we choose to use a Moz metric or Visibility score for our ranking, we need to set the results_number for the search engines to its max which is 50 
            //-----we set the top results number for moz or Visibility rank----
            if(mozMetrics.get(0)||enginechoice.get(3)){
              if(mozMetrics.get(0)){top_count_moz=results_number;}//if moz is true, top_count_moz gets the value of result number
              if(enginechoice.get(3)){top_visible=results_number;}//if merged engine is true, top_visible gets the value of result number
              results_number=50;//this is the max amount of results that you can get from the search engine APIs
            }
            //-----if we want to use Moz we should check first if it works
            if(mozMetrics.get(0)){
              Moz Moz = new Moz();
              //---if it works, moz remains true, otherwise it is set to false
              mozMetrics.add(0,Moz.check(config_path));
              //if it is false and we have chosen to use Visibility score with Moz, we reset back to the standard settings (ranking and not merged)
              //therefore, we reset the number of results from 50 to the top_count_moz which contained the original number of results
              if(!mozMetrics.get(0)){
                if(!enginechoice.get(3)){results_number=top_count_moz;}
              }
            }
            //----------we set the wordLists that we are going to use---------------------
            List<String> finalList = new ArrayList<String>();//finalList is going to contain all the content in the end
            Total_analysis ta = new Total_analysis();//we call total analysis
            int iteration_counter=0;//the iteration_counter is used in order to count the number of iterations of the algorithm and to be checked with perf_limit
            //this list of arraylists  is going to contain all the wordLists that are produced for every term of the String[] query,
            //in order to calculate the NGD scores between every term of the wordList and the term that was used as query in order to produce the spesific wordList
            List<ArrayList<String>> array_wordLists = new ArrayList<>();
            List<String> wordList_previous=new ArrayList<>();
            List<String> wordList_new=new ArrayList<>();
            double convergence=0;//we create the convergence percentage and initialize it
            String conv_percentages="";//string that contains all the convergence percentages
            DataManipulation wordsmanipulation=new DataManipulation();//method to manipulate various word data (String, list<String>, etc)
            do{ //if we run the algorithm for the 1st time we already have the query so we skip the loop below that produces the new array of query
                if(iteration_counter!=0){
                    wordList_previous = wordList_new;
                    //we add the previous wordList to the finalList
                    finalList=wordsmanipulation.AddAList(wordList_previous, finalList);
                    List<String> query_new_list_total = new ArrayList<>();
                    int iteration_previous=iteration_counter-1;
                    Combinations_Engine cn = new Combinations_Engine();//call the class to combine the terms produced
                    for(String query:queries){
                        List<String> ids=new ArrayList<>();
                        if(enginechoice.get(0)){
                            String id=domain+"/"+query+"/bing"+"/"+iteration_previous;
                            ids.add(id);
                        }
                        if(enginechoice.get(1)){
                            String id=domain+"/"+query+"/google"+"/"+iteration_previous;
                            ids.add(id);
                        }
                        if(enginechoice.get(2)){
                            String id=domain+"/"+query+"/yahoo"+"/"+iteration_previous;
                            ids.add(id);
                        }
                        ElasticGetWordList ESget=new ElasticGetWordList();//we call this class to get the wordlist from the Elastic Search
                        List<String> maxWords = ESget.getMaxWords(ids, SWebRankSettings.get(9).intValue(),config_path);//we are going to get a max amount of words
                        int query_index=queries.indexOf(query);
                        int size_query_new = SWebRankSettings.get(10).intValue();//the amount of new queries we are willing to create
                        //we create the new queries for every query of the previous round by combining the words produced from this query
                        List<String> query_new_list = cn.perform(maxWords, SWebRankSettings.get(7), queries, SWebRankSettings.get(6), query_index, size_query_new, config_path);
                        //we add the list of new queries to the total list that containas all the new queries
                        query_new_list_total.addAll(query_new_list);
                        System.out.println("query pointer=" + query_index + "");
                    }
                    //---------------------the following cleans a list from null and duplicates
                    query_new_list_total=wordsmanipulation.clearListString(query_new_list_total);
                    //--------------we create the new directory that our files are going to be saved 
                    String txt_directory=FilenameUtils.getBaseName(input.getName());
                    output_child_directory=output_parent_directory+txt_directory+"_level_"+iteration_counter+"//";
                    //----------------append the wordlist to a file------------------
                    wordsmanipulation.AppendWordList(query_new_list_total, output_child_directory+"queries_"+iteration_counter+".txt");
                    if(query_new_list_total.size()<1){break;}//if we don't create new queries we end the while loop
                    //total analysis' function is going to do all the work and return back what we need
                    ta = new Total_analysis();
                    ta.perform(wordList_previous,iteration_counter,output_child_directory,domain,enginechoice, query_new_list_total, results_number, top_visible, mozMetrics, moz_threshold_option, moz_threshold.doubleValue(), top_count_moz, ContentSemantics, SensebotConcepts, SWebRankSettings, config_path);
                    //we get the array of wordlists
                    array_wordLists=ta.getarray_wordLists();
                    //get the wordlist that includes all the new queries
                    wordList_new=ta.getwordList_total();
                    //---------------------the following cleans a list from null and duplicates-------------
                    wordList_new=wordsmanipulation.clearListString(wordList_new);
                    //----------------append the wordlist to a file--------------------
                    wordsmanipulation.AppendWordList(wordList_new, output_child_directory+ "wordList.txt");
                    //the concergence percentage of this iteration
                    convergence = ta.getConvergence();//we are going to use convergence score to check the convergence
                    //a string that contains all the convergence percentage for each round separated by \n character
                    conv_percentages = conv_percentages + "\n" + convergence;
                    //a file that is going to include the convergence percentages
                    wordsmanipulation.AppendString(conv_percentages, output_child_directory+ "convergence_percentage.txt");
                    //we add the new wordList to the finalList
                    finalList=wordsmanipulation.AddAList(wordList_new, finalList);
                    //we set the query array to be equal to the query new total that we have created
                    queries=query_new_list_total;
                    //we increment the iteration_counter in order to count the iterations of the algorithm and to use the perf_limit
                    iteration_counter++;
                }
                else{//the following source code is performed on the 1st run of the loop
                    //------------we extract the parent path of the file 
                    String txt_directory=FilenameUtils.getBaseName(input.getName());
                    //----------we create a string that is going to be used for the corresponding directory of outputs
                    output_child_directory=output_parent_directory+txt_directory+"_level_"+iteration_counter+"//";
                    //we call total analysis function performOld
                    ta.perform(wordList_new,iteration_counter,output_child_directory,domain, enginechoice, queries, results_number, top_visible, mozMetrics, moz_threshold_option, moz_threshold.doubleValue(), top_count_moz, ContentSemantics, SensebotConcepts, SWebRankSettings, config_path);
                    //we get the array of wordlists
                    array_wordLists=ta.getarray_wordLists();
                    //get the wordlist that includes all the new queries
                    wordList_new=ta.getwordList_total();
                    //---------------------the following cleans a list from null and duplicates
                    wordList_new=wordsmanipulation.clearListString(wordList_new);
                    //----------------append the wordlist to a file
                    wordsmanipulation.AppendWordList(wordList_new, output_child_directory+"wordList.txt");
                    //-----------------------------------------
                    iteration_counter++;//increase the iteration_counter that counts the iterations of the algorithm
                }
            }while(convergence<SWebRankSettings.get(5).doubleValue()&&iteration_counter<SWebRankSettings.get(8).intValue());//while the convergence percentage is below the limit and the iteration_counter below the performance limit
                if(iteration_counter==1){ finalList=wordsmanipulation.AddAList(wordList_new, finalList);}
                //--------------------content List----------------
                if (!finalList.isEmpty()) {
                    //---------------------the following cleans the final list from null and duplicates
                    finalList=wordsmanipulation.clearListString(finalList);
                    //write the keywords to a file
                    boolean flag_file = false;//boolean flag to declare successful write to file
                    flag_file=wordsmanipulation.AppendWordList(finalList, output_parent_directory+"total_content.txt");
                    if(!flag_file){
                        System.out.print("can not create the content file for: "+output_parent_directory+"total_content.txt");
                    }
                }
                //we are going to save the total content with its convergence on the ElasticSearch cluster in a separated index
                //Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
                //Client client = node.client();
                //get the elastic search indexes in a list
                List<String> elasticIndexes=ri.GetKeyFile(config_path, "elasticSearchIndexes");
                Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name","lshrankldacluster").build();
                Client client = new TransportClient(settings)
                    .addTransportAddress(new
                            InetSocketTransportAddress("localhost", 9300)
                    );
                JSONObject objEngineLevel = new JSONObject();
                objEngineLevel.put("TotalContent", finalList);//we save the total content
                objEngineLevel.put("Convergences", conv_percentages);//we save the convergence percentages
                IndexRequest indexReq=new IndexRequest(elasticIndexes.get(0),"content",domain);//we save also the domain 
                indexReq.source(objEngineLevel);
                IndexResponse indexRes = client.index(indexReq).actionGet();
                //node.close();
                client.close();
                //----------------------convergence percentages writing to file---------------
                //use the conv_percentages string
                if(conv_percentages.length()!=0){
                    boolean flag_file = false;//boolean flag to declare successful write to file
                    flag_file=wordsmanipulation.AppendString(conv_percentages, output_parent_directory+"convergence_percentages.txt");
                    if(!flag_file){
                        System.out.print("can not create the convergence file for: "+output_parent_directory+"convergence_percentages.txt");
                    }
                }
        }
    }
}


