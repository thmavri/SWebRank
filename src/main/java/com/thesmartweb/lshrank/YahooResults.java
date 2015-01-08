/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;

import java.io.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

import java.net.*;


import org.json.simple.parser.*;



import java.util.*;


/**
 *
 * @author Themis Mavridis
 */
public class YahooResults {
 public String[] Get(String quer,int yahoo_results_number,String example_dir){
     String chk="ok";
     String[] links=new String[yahoo_results_number];
     try {
            //counter is set in order to get the first something results of Yahoo
            //we connect through the Yahoo BOSS API
            //URL link_ur = new URL("http://boss.yahooapis.com/ysearch/web/v1/" + quer + "?appid=zrmigQ3V34FAyR9Nc4_EK91CP3Iw0Qa48QSgKqAvl2jLAo.rx97cmpgqW_ovGHvwjH.KgNQ-&format=json&count=" + yahoo_results_number);
            //APIconn apicon = new APIconn();
            //String line = apicon.connect(link_ur);
            quer=quer.replace("+","%20"); 
            YahooConn yc=new YahooConn();
            String line=yc.connect(quer);   
            if(!line.equalsIgnoreCase("fail")){
            //write the json-ticket text to a file
                //***********************textualmanipulation
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
 public Long Get_Results_Number(String quer) 
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
            String line=yc.connect(quer);
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
        }  catch (ParseException ex) {
            Logger.getLogger(YahooResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
         catch(java.lang.ArrayIndexOutOfBoundsException ex){
         Logger.getLogger(YahooResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
         catch(java.lang.NullPointerException ex){
         Logger.getLogger(YahooResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
    }



    }

