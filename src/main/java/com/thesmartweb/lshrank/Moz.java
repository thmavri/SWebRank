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

import com.seomoz.api.authentication.Authenticator;

import com.seomoz.api.service.URLMetricsService;
import com.seomoz.api.response.UrlResponse;
import com.google.gson.*;
public class Moz {
public String[] perform(String[] links,int top_count,Double moz_threshold,Boolean moz_threshold_option,List<Boolean> mozMetrics){
    long upa=34359738368L;
    long pda=68719476736L;
    long uemrp=1048576;
    long utrp=131072;
    long fmrp=32768;
    long umrp=16384;//16384
    System.gc();
    System.out.println("into Moz");
    Double[] mozRanks= new Double[links.length];
    DataManipulation textualmanipulation=new DataManipulation();
    List<Boolean> MozMetric;//1st place is Page Authority,2nd external mozRank, 3rd, mozTrust, 4th DomainAuthority and 5th MozRank (it is the default
    for(int i=0;i<links.length;i++){
        if(links[i]!=null){
            if(!textualmanipulation.FileTypeAnalyzed(links[i])){
                try{
                    Thread.sleep(10000);
                    URLMetricsService urlMetricsservice;
                    urlMetricsservice = authenticate();
                    if(urlMetricsservice!=null){
                        String objectURL =links[i].substring(0, links[i].length());
                    }
                    //************************************************
                    String objectURL =links[i].substring(0, links[i].length());
                    //Add your accessID here
                    String accessID = "member-87c6a749b0";
                    //Add your secretKey here
                    String secretKey = "46fed510cc0c03a934b65ddc5ca54cfa";
                    System.setProperty("log4j.logger.org.apache.http","ERROR");
                    
                    Authenticator authenticator = new Authenticator();
                    authenticator.setAccessID(accessID);
                    authenticator.setSecretKey(secretKey);
                    System.out.println("I im going to try to get score for"+i+"url in seomoz");
//******************
                    String SEOmozAPISign="http://lsapi.seomoz.com/linkscape/url-metrics/www.seomoz.org%2fblog?Cols=2048&Cols=16384&Cols=131072&Cols=1048576&Cols=34359738368&Cols=68719476736&Cols=131072&Cols=32768&AccessID=member-87c6a749b0&Expires=1353362399&Signature=rLWrFGFil%2Bt56DbIuZNgZhoNxew%3D";
                    //objectURL="http://lsapi.seomoz.com/linkscape/url-metrics/"+links[i]+"?Cols=2048&Cols=16384&Cols=131072&Cols=1048576&Cols=34359738368&Cols=68719476736&Cols=131072&Cols=32768&AccessID=member-87c6a749b0&Expires=1353362399&Signature=rLWrFGFil%2Bt56DbIuZNgZhoNxew%3D";
                    System.out.println("I authenticated");
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
                catch (Exception ex){
                System.out.println("exception moz");                 
                //Logger.getLogger(Moz.class.getName()).log(Level.SEVERE, null, ex);
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
    try{
        //get the scores to a list
        System.out.println("I am goint to rank the scores of seomoz");
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
        //we get the indexes and from them we get the terms of ngd_arr that are below the threshold
        //we submit every term to the search engines and we get the keys that LDA analysis returns

        return links_out;
    }
    catch (Exception ex){
        System.out.println("exception moz list");                 
        //Logger.getLogger(Moz.class.getName()).log(Level.SEVERE, null, ex);
        String[] links_out=null;
        return links_out;
    }

                 
}
 public boolean check(){
    boolean moz=false;
    String accessID = "member-87c6a749b0";
    //Add your secretKey here
    String secretKey = "46fed510cc0c03a934b65ddc5ca54cfa";
    System.setProperty("log4j.logger.org.apache.http","ERROR");
    Authenticator authenticator = new Authenticator();
    authenticator.setAccessID(accessID);
    authenticator.setSecretKey(secretKey);
    String objectURL ="www.sei.org";
    URLMetricsService urlMetricsService = new URLMetricsService(authenticator);
    String response = urlMetricsService.getUrlMetrics(objectURL);
    //if moz fails we set the moz option "false" and if we do not have merged option as true we set the results number=top_count_seomoz
    if(response.length()!=0){
        moz=true;
    }
    return moz;
}
public URLMetricsService authenticate(){
    //Add your accessID here
    String accessID = "member-87c6a749b0";
    //Add your secretKey here
    String secretKey = "46fed510cc0c03a934b65ddc5ca54cfa";
    System.setProperty("log4j.logger.org.apache.http","ERROR");
    Authenticator authenticator = new Authenticator();
    authenticator.setAccessID(accessID);
    authenticator.setSecretKey(secretKey);
    String SEOmozAPISign="http://lsapi.seomoz.com/linkscape/url-metrics/www.seomoz.org%2fblog?Cols=2048&Cols=16384&Cols=131072&Cols=1048576&Cols=34359738368&Cols=68719476736&Cols=131072&Cols=32768&AccessID=member-87c6a749b0&Expires=1353362399&Signature=rLWrFGFil%2Bt56DbIuZNgZhoNxew%3D";
    URLMetricsService urlMetricsService = new URLMetricsService(authenticator);
    return urlMetricsService;
}
}
