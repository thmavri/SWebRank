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
package com.thesmartweb.lshrank;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;  
 
/** 
 *  Class related to the Yahoo! entities and categories
 * @author Themistoklis Mavridis
 */  
public class YahooEntityCategory {

    /**
     * the amount of terms of the query that occurred in an entity recognized
     */
    public static int ent_query_cnt=0;

    /**
     * the amount of terms of the query that occurred in a= category recognized
     */
    public static int cat_query_cnt=0;
    /**
     * the amount of queries that occurred in an entity recognized
     */
    public static int ent_query_cnt_whole=0;

    /**
     * the amount of queries that occurred in a category recognized
     */
    public static int cat_query_cnt_whole=0;
  
    /**
     * Method to get the entities and categories counts from Yahoo!
     * @param urlcheck the url to analyze
     * @param quer the query term to check for
     * @param StemFlag flag for stemming
     */
    public void connect(String urlcheck,String quer, boolean StemFlag) {  

        try {  
            cat_query_cnt=0;
            ent_query_cnt=0;
            ent_query_cnt_whole=0;
            cat_query_cnt_whole=0;
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
     * Getter of the entities counter (partial query match)
     * @return entities counter (partial query match)
     */
    public int GetEntQuerCnt(){
    return ent_query_cnt;
}

    /**
     * Getter of the categories counter (partial query match)
     * @return categories counter (partial query match)
     */
    public int GetCatQuerCnt(){
    return cat_query_cnt;
}
    /**
     * Getter of the entities counter (whole query match)
     * @return entities counter (whole query match)
     */
    public int GetEntQuerCntWhole(){
    return ent_query_cnt_whole;
}

    /**
     * Getter of the categories counter (whole query match)
     * @return categories counter (whole query match)
     */
    public int GetCatQuerCntWhole(){
    return cat_query_cnt_whole;
}
  
} 