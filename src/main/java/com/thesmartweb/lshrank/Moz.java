/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;

import java.util.*;
import com.seomoz.api.authentication.Authenticator;
import com.seomoz.api.service.URLMetricsService;
import com.seomoz.api.response.UrlResponse;
import com.google.gson.*;

/**
 * Class to get various info using Moz API
 * @author themis
 */
public class Moz {

    /**
     * Method that captures the various Moz metrics for the provided urls (with help of the sample here https://github.com/seomoz/SEOmozAPISamples
     * @param links the urls to analyze
     * @param top_count the amount of results to keep when we rerank the results according to their value of a specific Moz metric
     * @param moz_threshold the threshold to the Moz value to use
     * @param moz_threshold_option flag if we are going to use threshold in the Moz value or not
     * @param mozMetrics list that contains which metric to use for Moz //1st place is Page Authority,2nd external mozRank, 3rd, mozTrust, 4th DomainAuthority and 5th MozRank (it is the default)
     * @param config_path path that has the config files with the api keys and secret for Moz
     * @return
     */
    public String[] perform(String[] links,int top_count,Double moz_threshold,Boolean moz_threshold_option,List<Boolean> mozMetrics, String config_path){
        //=====short codes for the metrics 
        long upa=34359738368L;//page authority
        long pda=68719476736L;//domain authority
        long uemrp=1048576;//mozrank external equity
        long utrp=131072;//moztrust 
        long fmrp=32768;//mozrank subdomain
        long umrp=16384;//mozrank
        System.gc();
        System.out.println("into Moz");
        Double[] mozRanks= new Double[links.length];
        DataManipulation textualmanipulation=new DataManipulation();
        for(int i=0;i<links.length;i++){
            if(links[i]!=null){
                if(!textualmanipulation.StructuredFileCheck(links[i])){
                    try{
                        Thread.sleep(10000);
                        URLMetricsService urlMetricsservice;
                        urlMetricsservice = authenticate(config_path);
                        String objectURL =links[i].substring(0, links[i].length());
                        Gson gson = new Gson();
                        if(mozMetrics.get(1)){//Domain Authority
                            String response = urlMetricsservice.getUrlMetrics(objectURL,pda);
                            UrlResponse res = gson.fromJson(response, UrlResponse.class);
                            System.gc();
                            if(res!=null&&!(response.equalsIgnoreCase("{}"))){
                                String mozvalue_string=res.getPda();
                                mozRanks[i]=Double.parseDouble(mozvalue_string);
                            }
                                else{mozRanks[i]=Double.parseDouble("0");}
                        }
                        else if(mozMetrics.get(2)){//External MozRank
                            String response = urlMetricsservice.getUrlMetrics(objectURL,uemrp);
                            UrlResponse res = gson.fromJson(response, UrlResponse.class);
                            System.gc();
                            if(res!=null&&!(response.equalsIgnoreCase("{}"))){
                                String mozvalue_string=res.getUemrp();
                                mozRanks[i]=Double.parseDouble(mozvalue_string);
                            }
                            else{mozRanks[i]=Double.parseDouble("0");}
                        }
                        else if(mozMetrics.get(3)){//MozRank
                            String response = urlMetricsservice.getUrlMetrics(objectURL,umrp);
                            UrlResponse res = gson.fromJson(response, UrlResponse.class);
                            System.gc();
                            if(res!=null&&!(response.equalsIgnoreCase("{}"))){
                                String mozvalue_string=res.getUmrp();
                                mozRanks[i]=Double.parseDouble(mozvalue_string);
                            }
                            else{mozRanks[i]=Double.parseDouble("0");}
                        }
                        else if(mozMetrics.get(4)){//MozTrust
                            String response = urlMetricsservice.getUrlMetrics(objectURL,utrp);
                            UrlResponse res = gson.fromJson(response, UrlResponse.class);
                            System.gc();
                            if(res!=null&&!(response.equalsIgnoreCase("{}"))){
                                String mozvalue_string=res.getUtrp();
                                mozRanks[i]=Double.parseDouble(mozvalue_string);
                            }
                            else{mozRanks[i]=Double.parseDouble("0");}
                        }
                        else if(mozMetrics.get(5)){//Page Authority
                            String response = urlMetricsservice.getUrlMetrics(objectURL,upa);
                            UrlResponse res = gson.fromJson(response, UrlResponse.class);
                            System.gc();
                            if(res!=null&&!(response.equalsIgnoreCase("{}"))){
                                String mozvalue_string=res.getUpa();
                                mozRanks[i]=Double.parseDouble(mozvalue_string);
                            }
                            else{mozRanks[i]=Double.parseDouble("0");}
                        }
                        else if(mozMetrics.get(6)){//subdomain MozRank
                            String response = urlMetricsservice.getUrlMetrics(objectURL,fmrp);
                            UrlResponse res = gson.fromJson(response, UrlResponse.class);
                            System.gc();
                            if(res!=null&&!(response.equalsIgnoreCase("{}"))){
                                String mozvalue_string=res.getFmrp();
                                mozRanks[i]=Double.parseDouble(mozvalue_string);
                            }
                            else{mozRanks[i]=Double.parseDouble("0");}
                        }
                    }
                    catch (InterruptedException | JsonSyntaxException | NumberFormatException ex){
                        System.out.println("exception moz:" +ex.toString());
                        mozRanks[i]=Double.parseDouble("0");
                        String[] links_out=null;
                        return links_out;
                    }
                }
                else{
                    mozRanks[i]=Double.parseDouble("0");
                }
            }
        }
        try{//ranking of the urls according to their moz score
            //get the scores to a list
            System.out.println("I am goint to rank the scores of Moz");
            System.gc();
            List<Double> seomozRanks_scores_list=Arrays.asList(mozRanks);
            //create a hashmap in order to map the scores with the indexes
            System.gc();
            IdentityHashMap<Double, Integer> originalIndices = new IdentityHashMap<Double, Integer>();
            //copy the original scores list
            System.gc();
            for(int i=0; i<seomozRanks_scores_list.size(); i++){
                originalIndices.put(seomozRanks_scores_list.get(i), i);
                 System.gc();
            }
            //sort the scores
            List<Double> sorted_seomozRanks_scores = new ArrayList<Double>();
            System.gc();
            sorted_seomozRanks_scores.addAll(seomozRanks_scores_list);
            System.gc();
            sorted_seomozRanks_scores.removeAll(Collections.singleton(null));
            System.gc();
            if(!sorted_seomozRanks_scores.isEmpty()){
            Collections.sort(sorted_seomozRanks_scores,Collections.reverseOrder());}
            //get the original indexes
            //the max amount of results
            int[] origIndex=new int[150];
            if(!sorted_seomozRanks_scores.isEmpty()){
            //if we want to take the top scores(for example top 10)
                if(!moz_threshold_option){
                    origIndex=new int[top_count];
                    for(int i=0; i<top_count; i++){
                        Double score = sorted_seomozRanks_scores.get(i);
                         System.gc();
                        // Lookup original index efficiently
                         origIndex[i] = originalIndices.get(score);
                    }
                }
                //if we have a threshold
                else if(moz_threshold_option)
                {
                    int j=0;
                    int counter=0;
                    while(j<sorted_seomozRanks_scores.size()){
                        if(sorted_seomozRanks_scores.get(j).compareTo(moz_threshold)>=0){counter++;}
                        j++;
                    }
                    origIndex=new int[counter];
                    for(int k=0;k<origIndex.length-1;k++){
                        System.gc();
                        Double score = sorted_seomozRanks_scores.get(k);
                        origIndex[k] = originalIndices.get(score);
                    }
                }
            }
            String[] links_out=new String[origIndex.length];
            for(int jj=0;jj<origIndex.length;jj++){
                System.gc();
                links_out[jj]=links[origIndex[jj]];
            }
            System.gc();
            System.out.println("I have ranked the scores of moz");
            return links_out;
        }
        catch (Exception ex){
            System.out.println("exception moz list" +ex.toString());                 
            //Logger.getLogger(Moz.class.getName()).log(Level.SEVERE, null, ex);
            String[] links_out=null;
            return links_out;
        }      
}

    /**
     *
     * @return
     */
    public boolean check(String config_path){
        boolean moz=false;
        URLMetricsService urlMetricsservice; 
        urlMetricsservice = authenticate(config_path);
        String objectURL ="www.thesmartweb.eu";
        String response = urlMetricsservice.getUrlMetrics(objectURL);
        //if moz fails we set the moz option "false" and if we do not have merged option as true we set the results number=top_count_seomoz
        if(response.length()!=0){
            moz=true;
        }
        return moz;
    }

    /**
     *
     * @param config_path
     * @return
     */
    public URLMetricsService authenticate(String config_path){
        List<String> apikeys = GetKeys(config_path);
        //Add your accessID here
        String accessID="";
        String secretKey="";
        if(apikeys.size()==2){
            accessID = apikeys.get(0);
            //Add your secretKey here
            secretKey = apikeys.get(1);
        }
        System.setProperty("log4j.logger.org.apache.http","ERROR");
        Authenticator authenticator = new Authenticator();
        authenticator.setAccessID(accessID);
        authenticator.setSecretKey(secretKey);
        String SEOmozAPISign="http://lsapi.seomoz.com/linkscape/url-metrics/www.seomoz.org%2fblog?Cols=2048&Cols=16384&Cols=131072&Cols=1048576&Cols=34359738368&Cols=68719476736&Cols=131072&Cols=32768&AccessID=member-87c6a749b0&Expires=1353362399&Signature=rLWrFGFil%2Bt56DbIuZNgZhoNxew%3D";
        URLMetricsService urlMetricsService = new URLMetricsService(authenticator);
        return urlMetricsService;
    }
    public List<String> GetKeys(String config_path){
        ReadInput ri = new ReadInput();
        List<String> apikeysList=ri.GetKeyFile(config_path, "mozkeys");
        return apikeysList;
    }
}
