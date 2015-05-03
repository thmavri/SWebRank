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
 * 
 *
 * @author Themis Mavridis
 */
import java.io.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import java.util.List;
import java.util.Scanner;

/**
 * Class to read the input files
 * @author themis
 */
public class ReadInput {
           
    /**
     * the number of results that are returned from each search engine
     */
    protected int results_number;
            
    /**
     * moz threshold
     */
    protected Double moz_threshold;
 
    /**
     * if we want to have a threshold in moz or not
     */
    protected boolean moz_threshold_option; 

    /**
     * amount of Sensebot Concept (if we choose to use Sensebot)
     */
    protected int SensebotConcepts;

    /**
     * List that has true 0 if we would like to use Moz
     * #1 Domain Authority
     * #2 External MozRank
     * #3 MozRank
     * #4 MozTrust
     * #5 Page Authority
     * #6 Subdomain MozRank
     */
    protected List<Boolean> mozMetrics;

    /**
     * List that contains the queries that are defined in the input files
     */
    protected List<String> queries;

    /**
     * Search engine choice, Bing is in 1st place, Google is in 2nd place, Yahoo 3rd
     */
    protected List<Boolean> enginechoice; 

    /**
     * List that contains which Semantic Analysis algorithm we choose
     * #1 Diffbotflag
     * #2 LDA
     * #3 Sensebotflag
     * #4 TF-IDF
     */
    protected List<Boolean> ContentSemantics;

    /**
     * List that contains then SWebRank settings
     * #0 beta
     * #1 number of topics
     * #2 number of iterations
     * #3 number of top words
     * #4 probability 
     * #5 nmi convergence limit
     * #6 nwd threshold
     * #7 combine limit
     * #8 performance limit
     * #9 new terms to combine from wordlist per query per round
     * #10 max new queries to generate per previous round query
     */
    protected List<Double> SWebRankSettings;

    /**
     * Domain of queries
     */
    
    protected String domain;
    
    
    /**
     * Initialize the values
     */
    
    public ReadInput() {
        this.SensebotConcepts = 0;
        this.moz_threshold_option = false;
        this.moz_threshold = 0.0;
        this.results_number = 0;
        this.queries = new LinkedList<String>();//better in add(E element)
        this.enginechoice= new ArrayList<Boolean>();//lower complexity in get
        this.ContentSemantics=new ArrayList<Boolean>();
        this.SWebRankSettings=new ArrayList<Double>();
        this.mozMetrics=new ArrayList<Boolean>();
        this.domain="";
        
    }

    /**
     * Method to read SWebRank's settings
     * @param Input The file that contains the input settings to be read
     * @return True/False if everything was read correctly
     */
    public boolean perform(File Input){
    
        FileInputStream inputStream=null;
        Scanner sc=null;
        try{
            inputStream=new FileInputStream(Input);
            sc=new Scanner(inputStream);
            if (sc.hasNextLine()) {
                domain = sc.nextLine().toString().split(":")[1].trim();
            }
            if (sc.hasNextLine()) {
                int queries_number = Integer.parseInt(sc.nextLine().toString().split(":")[1].trim());
                int j=0;
                while(j<queries_number){
                    String temp=sc.nextLine().toString().split(":")[1].trim();
                    boolean add = queries.add(temp);
                    j++;
                }
            }

            if (sc.hasNextLine()) {
                results_number = Integer.parseInt(sc.nextLine().toString().split(":")[1].trim());
            }
            //-------------------
            if (sc.hasNextLine()) {//Bing
                enginechoice.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//Google
                enginechoice.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//Yahoo
                enginechoice.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//Merged Engine Results
                enginechoice.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            //--------------------
            if (sc.hasNextLine()) {//0moz
                mozMetrics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//1Domain Authority
                mozMetrics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//2External MozRank
                mozMetrics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//3MozRank
                mozMetrics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//4MozTrust
                mozMetrics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//5Page Authority
                mozMetrics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//6Subdomain_MozRank
                mozMetrics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {
                moz_threshold_option = Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim());
            }
            //------if we are going to use the threshold then we insert the value to the moz_threshold variable
            //------otherwise, we just skip it
            if (sc.hasNextLine()) {
                if(moz_threshold_option){
                    moz_threshold = Double.parseDouble(sc.nextLine().toString().split(":")[1].trim());
                }
                else{
                    sc.nextLine();
                    moz_threshold=-1.0;
                }
            }
            //-----the following is used to check if we have one and only one Moz option active
            if(mozMetrics.get(0)){
                int k=1;
                int true_position=-1;
                //we search for the option set to true
                while(true_position<0&&k<mozMetrics.size()){
                    if(mozMetrics.get(k)){
                        true_position=k;
                    }
                    k++;
                }
                //if the user has set multiple options set true, we are keeping the first one
                if(true_position>0){
                    int mozMetricsSize=mozMetrics.size();
                    for(k=true_position+1;k<mozMetricsSize;k++){
                        mozMetrics.set(k,false);
                    } 
                }
                //if the user has not set any option to true, we are not going to use Moz
                else{
                    mozMetrics.set(0, false);
                }
            }
            //-----------------------------
            if (sc.hasNextLine()) {//0Diffbotflag
                ContentSemantics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//1LDAflag
                ContentSemantics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//2Sensebotflag
                ContentSemantics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//3TFIDFflag
                ContentSemantics.add(Boolean.parseBoolean(sc.nextLine().toString().split(":")[1].trim()));
            }
            if(ContentSemantics.get(2).booleanValue()){
                if (sc.hasNextLine()) {
                    SensebotConcepts=Integer.parseInt(sc.nextLine().toString().split(":")[1].trim());
                }
            }
            else {
                sc.nextLine();
                SensebotConcepts=0;
            }
            
            //-----------------------------------
            //-----the following is used to check if we have one and only one Content Semantic option active
            int k=1;
            int true_position=-1;
            while(true_position<0&&k<ContentSemantics.size()){
                if(ContentSemantics.get(k)){
                    true_position=k;
                }
                k++;
            }
            if(true_position>0){
                int contentSemanticsSize=ContentSemantics.size();
                for(k=true_position+1;k<contentSemanticsSize;k++){
                    ContentSemantics.set(k,false);
                } 
            }
            //if the user has not set any option to true, we are not going to use LDA
            else{
                ContentSemantics.set(1,true);
            }
            //-----------------------------
            if (sc.hasNextLine()) {//0beta
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//1number of topics
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//2number of iterations
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//3number of top words
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//4number of probability threshold
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//5conversion limit
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//6ngd threshold
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//7combine limit
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//8performance limit
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//9amount of terms to get from each query from the wordlist of LDA to create the new queries per round
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//10amount of queries to create for each query using NWD
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//11amount of top topics to choose
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//12dbpedia spotlight confidence
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//13dbpedia spotlight support
                SWebRankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.ioException() !=null){
                return false;
            } 
        } catch (IOException ex) {
            Logger.getLogger(ReadInput.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            if (inputStream !=null){
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadInput.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
            if (sc !=null){
                sc.close();
                return true;
            }
        }
        return false;
    }
    /**
     * Method to get the api credentials in a list from a directory that contains multiple txt files
     * @param config_path the directory to read
     * @param name the name of the file that we would like to read
     * @return a list with the credentials
     */
     public List<String> GetKeyFile(String config_path,String name){
        Path input_path=Paths.get(config_path);       
        DataManipulation getfiles=new DataManipulation();//class responsible for the extraction of paths
        Collection<File> inputs_files;//array to include the paths of the txt files
        inputs_files=getfiles.getinputfiles(input_path.toString(),"txt");//method to retrieve all the path of the input documents
        List<String> apikeysList = new ArrayList<>();
        ReadInput ri = new ReadInput();
        for (File input : inputs_files) {
            if(input.getName().contains(name)){
                apikeysList=ri.GetAPICredentials(input);
            }
        }
        return apikeysList;
    }
     /**
      * Get the API credentials from a given file
      * @param Input the file to read
      * @return a List with the credentials in strings
      */
    public List<String> GetAPICredentials(File Input){
    
        FileInputStream inputStream=null;
        Scanner sc=null;
        List<String> output = new ArrayList<>();
        try{
            inputStream=new FileInputStream(Input);
            sc=new Scanner(inputStream);
            while (sc.hasNextLine()) {
                output.add(sc.nextLine().trim());
            }
        } catch (IOException ex) {
            Logger.getLogger(ReadInput.class.getName()).log(Level.SEVERE, null, ex);
            return output;
        } finally {
            if (inputStream !=null){
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadInput.class.getName()).log(Level.SEVERE, null, ex);
                    return output;
                }
            }
            if (sc !=null){
                sc.close();
            }
        }
        return output;
    }
   

}
