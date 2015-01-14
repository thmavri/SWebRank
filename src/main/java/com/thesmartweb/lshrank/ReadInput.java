/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;

/**
 *
 * @author Themis Mavridis
 */
import java.io.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author themis
 */
public class ReadInput {
           
    /**
     *
     */
    protected int results_number;//the number of results that are returned from each search engine
            
    /**
     *
     */
    protected Double moz_threshold;//if weant to have a threshold in moz
 
    /**
     *
     */
    protected boolean moz_threshold_option; 

    /**
     *
     */
    protected int SensebotConcepts;

    /**
     *
     */
    protected List<Boolean> mozMetrics;

    /**
     *
     */
    protected List<String> queries;

    /**
     *
     */
    protected List<Boolean> enginechoice; //Bing is in 1st place, Google is in 2nd place, Yahoo 3rd

    /**
     *
     */
    protected List<Boolean> ContentSemantics;

    /**
     *
     */
    protected List<Double> LSHrankSettings;

    /**
     *
     */
    public ReadInput() {
        this.SensebotConcepts = 0;
        this.moz_threshold_option = false;
        this.moz_threshold = 0.0;
        this.results_number = 0;
        this.queries = new LinkedList<String>();//better in add(E element)
        this.enginechoice= new ArrayList<Boolean>();//lower complexity in get
        this.ContentSemantics=new ArrayList<Boolean>();
        this.LSHrankSettings=new ArrayList<Double>();
        this.mozMetrics=new ArrayList<Boolean>();
    }

    /**
     *
     * @param Input
     * @return
     */
    public boolean perform(File Input){
    //function that reads the input file and returns a string with word "ok" if everything is fine, null if not

        FileInputStream inputStream=null;
        Scanner sc=null;
        try{
            inputStream=new FileInputStream(Input);
            sc=new Scanner(inputStream);
            if (sc.hasNextLine()) {
                int queries_number = Integer.parseInt(sc.nextLine().toString().split(":")[1].trim());
                int j=0;
                while(j<queries_number){
                    String temp=sc.nextLine().toString().split(":")[1].trim();
                    boolean add = queries.add(temp);
                    //queries.add(
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
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//1number of topics
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//2number of iterations
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//3number of top words
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//4number of probability threshold
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//5conversion limit
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//6ngd threshold
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//7combine limit
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
            }
            if (sc.hasNextLine()) {//8performance limit
                LSHrankSettings.add(Double.parseDouble(sc.nextLine().toString().split(":")[1].trim()));
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

   

}
