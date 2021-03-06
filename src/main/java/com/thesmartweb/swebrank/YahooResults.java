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

import java.io.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.json.simple.parser.*;
import java.util.*;

/**
 * Class for the Yahoo! Search API
 * @author Themistoklis Mavridis
 */
public class YahooResults {

    /**
     * Method to get the links for a specific query
     * @param quer the query
     * @param yahoo_results_number the number of results to return
     * @param example_dir a directory to save the yahoo search engine response
     * @param config_path the directory with yahoo! api keys
     * @return an array with the urls of the results
     */
    public String[] Get(String quer,int yahoo_results_number,String example_dir, String config_path){
        String[] links=new String[yahoo_results_number];
        try {
            quer=quer.replace("+","%20"); 
            YahooConn yc=new YahooConn();
            String line=yc.connect(quer, config_path);   
            if(!line.equalsIgnoreCase("fail")){
            //write the json-ticket text to a file
            File json_t = new File(example_dir + "yahoo/" + quer + "/json" + ".txt");
            FileUtils.writeStringToFile(json_t, line);
            //initialize JSONparsing
            JSONparsing gg = new JSONparsing(yahoo_results_number);
            //get the links in an array
            links = gg.YahooJsonParsing(line, yahoo_results_number);
            }
            return links;
        } catch (IOException ex) {
            Logger.getLogger(YahooResults.class.getName()).log(Level.SEVERE, null, ex);
            System.out.print("\n*********fail-yahoo results*********\n");
            return links;
        }
       
}
 
    /**
     * Method to get the results number of a specific query from Yahoo! search api
     * @param quer the query to search for
     * @param config_path the directory to get the api keys
     * @return the amount of results
     */
    public Long Get_Results_Number(String quer, String config_path) 
    {   try {
            long results_number = 0;
            //we connect through 
            String check_quer = quer.substring(quer.length() - 1, quer.length());
            char plus = "+".charAt(0);
            char check_plus = check_quer.charAt(0);
            if (check_plus == plus) {
                quer = quer.substring(0, quer.length() - 1);
            }
            quer=quer.replace("+","%20"); 
            //URL link_ur = new URL("http://boss.yahooapis.com/ysearch/web/v1/" + quer + "?appid=zrmigQ3V34FAyR9Nc4_EK91CP3Iw0Qa48QSgKqAvl2jLAo.rx97cmpgqW_ovGHvwjH.KgNQ-&format=json");
            //APIconn apicon = new APIconn();
            YahooConn yc=new YahooConn();
            String line=yc.connect(quer,config_path);
            //String line = apicon.connect(link_ur);
            if(!line.equalsIgnoreCase("fail")){
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(line);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] arr = set.toArray();
            Map.Entry entry = (Map.Entry) arr[0];
            //****get to second level of yahoo json
            String you = entry.getValue().toString();
            json = (Map) parser.parse(you);
            set = json.entrySet();
            arr = set.toArray();
            entry = (Map.Entry) arr[1];
            you = entry.getValue().toString();
            json = (Map) parser.parse(you);
            set = json.entrySet();
            arr = set.toArray();
            entry = (Map.Entry) arr[3];
            you = entry.getValue().toString();
            results_number = Long.parseLong(you);}
            return results_number;
        }  catch (ParseException | java.lang.ArrayIndexOutOfBoundsException | java.lang.NullPointerException ex) {
            Logger.getLogger(YahooResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
    }



    }

