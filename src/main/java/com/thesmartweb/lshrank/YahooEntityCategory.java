package com.thesmartweb.lshrank;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author themis
 */


import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;import java.net.URLEncoder;  
 
  
  
/** 
 * Sample code to use Yahoo! Search BOSS 
 *  
 * Please include the following libraries  
 * 1. Apache Log4j 
 * 2. oAuth Signpost 
 *  
 * @author xyz 
 */  
public class YahooEntityCategory {

    /**
     *
     */
    public static int ent_query_cnt=0;

    /**
     *
     */
    public static int cat_query_cnt=0;
    /**
     *
     */
    public static int ent_query_cnt_whole=0;

    /**
     *
     */
    public static int cat_query_cnt_whole=0;
  
    /**
     *
     * @param urlcheck
     * @param quer
     * @return
     */
    public void connect(String urlcheck,String quer, boolean StemFlag) {  

        try {  
            cat_query_cnt=0;
            ent_query_cnt=0;
            String line="";
            String baseUrl = "http://query.yahooapis.com/v1/public/yql?q=";
            String query = "select * from contentanalysis.analyze where url='"+urlcheck+"'";
            String fullUrlStr = baseUrl + URLEncoder.encode(query, "UTF-8") + "&format=json";
            fullUrlStr=fullUrlStr.replace("+","%20");
            fullUrlStr=fullUrlStr.replace(" ","%20");
            URL link_ur = new URL(fullUrlStr);
            //we connect and then check the connection
            APIconn apicon = new APIconn();
            line = apicon.connect(link_ur);
            if(!line.equalsIgnoreCase("fail")){
                JSONparsing yejson= new JSONparsing();
                //get the links in an array
                yejson.YahooEntityJsonParsing(line, quer, StemFlag);
                ent_query_cnt=yejson.GetEntQuerCnt();
                cat_query_cnt=yejson.GetCatQuerCnt();
                ent_query_cnt_whole=yejson.GetEntQuerCntWhole();
                cat_query_cnt_whole=yejson.GetCatQuerCntWhole();
            }
        } catch (UnsupportedEncodingException | MalformedURLException e) {  
                
        }  
} 

    /**
     *
     * @return
     */
    public int GetEntQuerCnt(){
    return ent_query_cnt;
}

    /**
     *
     * @return
     */
    public int GetCatQuerCnt(){
    return cat_query_cnt;
}
    /**
     *
     * @return
     */
    public int GetEntQuerCntWhole(){
    return ent_query_cnt_whole;
}

    /**
     *
     * @return
     */
    public int GetCatQuerCntWhole(){
    return cat_query_cnt_whole;
}
  
} 
