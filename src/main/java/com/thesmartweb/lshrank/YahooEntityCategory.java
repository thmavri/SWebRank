package com.thesmartweb.lshrank;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author themis
 */


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
    public String[] connect(String urlcheck,String quer) {  

        try {  
            cat_query_cnt=0;
            ent_query_cnt=0;
            String[] output=new String[2];
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
                output= yejson.YahooEntityJsonParsing(line, quer);
                ent_query_cnt=yejson.GetEntQuerCnt();
                cat_query_cnt=yejson.GetCatQuerCnt();
                ent_query_cnt_whole=yejson.GetEntQuerCntWhole();
                cat_query_cnt_whole=yejson.GetCatQuerCntWhole();
            }
            return output;
        } catch (Exception e) {  
                String[] output=new String[2];
                output[0]="fail";
                output[1]="fail";
                return output;
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
