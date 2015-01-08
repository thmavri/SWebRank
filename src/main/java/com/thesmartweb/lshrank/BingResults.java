/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import org.json.simple.parser.*;
import java.util.*;
import org.json.simple.JSONArray;
/**
 *
 * @author Themis Mavridis
 *///
//"http://api.search.live.net/json.aspx?Appid=<7FC8F5CEC23A5A4B418E83457E2DC00DC189DAF8>&query="+quer+"&sources=web&web.count=4
public class BingResults {
public String[] Get(String query,int bing_results_number,String example_dir){
    String chk="ok";
    String[] links=new String[bing_results_number];
    try {
            APIconn apicon = new APIconn();    
            String line="fail";
            query=query.replace("+", "%20");
            URL azure_url=new URL("https://api.datamarket.azure.com/Bing/SearchWeb/v1/Web?Query=%27"+query+"%27&$top="+bing_results_number+"&$format=JSON");
            line=apicon.azureconnect(azure_url);
            if((!line.equalsIgnoreCase("fail"))&&(!line.equalsIgnoreCase("insufficient"))&&(!line.equalsIgnoreCase("provided"))){
                        
                //write the json-ticket text to a file
                DataManipulation textualmanipulation=new DataManipulation();
                textualmanipulation.AppendString(line, example_dir + "bing/" + query + "/json" + ".txt");
                //initialize JSONparsing
                JSONparsing gg = new JSONparsing(bing_results_number);
                //get the links in an array
                links = gg.BingAzureJsonParsing(line, bing_results_number);
            }
            return links;
    } 
    catch (IOException ex) {
        Logger.getLogger(BingResults.class.getName()).log(Level.SEVERE, null, ex);
        System.out.print("\n*********Failure in Bing results*********\n");
            return links;
    }
          
        
}
public Long Get_Results_Number(String quer)
    {   try {
            int azure_flag=1;
            long results_number = 0;
            //we connect through the Bing search api
            String check_quer = quer.substring(quer.length() - 1, quer.length());
            char plus = "+".charAt(0);
            char check_plus = check_quer.charAt(0);
            if (check_plus == plus) {
                quer = quer.substring(0, quer.length() - 1);
            }
            
            if(azure_flag==0){
                URL link_ur = new URL("http://api.search.live.net/json.aspx?Appid=7FC8F5CEC23A5A4B418E83457E2DC00DC189DAF8&Offset=100&query=" + quer + "&sources=web&");
                APIconn apicon = new APIconn();
                String line = apicon.connect(link_ur);
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
                entry = (Map.Entry) arr[0];
                you = entry.getValue().toString();
                //get to the third level
                json = (Map) parser.parse(you);
                set = json.entrySet();
                arr = set.toArray();
                entry = (Map.Entry) arr[2];
                you = entry.getValue().toString();
                results_number = Long.parseLong(you);}
                return results_number;
            }
            else{
            quer=quer.replace("+", "%20");
            URL azure_url=new URL("https://api.datamarket.azure.com/Bing/Search/Composite?Sources=%27web%27&Query=%27"+quer+"%27&$format=JSON");                       
            APIconn apicon = new APIconn();
            String line = apicon.azureconnect(azure_url);                 
                    if((!line.equalsIgnoreCase("fail"))&&(!line.equalsIgnoreCase("insufficient"))&&(!line.equalsIgnoreCase("provided"))){
                        //line=apicon.connect(azure_url);
                        //Create a parser
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
                        entry = (Map.Entry) arr[0];


                        you=entry.getValue().toString();
                        Object parse = parser.parse(you);
                        JSONArray json_new = (JSONArray) parse;
                        json = (Map) json_new.get(0);
                        set = json.entrySet();
                        arr = set.toArray();
                        Map.Entry[] entries_bing=null;
                        int k = 0;
                        Iterator new_iterator=set.iterator();
                        while(new_iterator.hasNext()){
                                Object next = new_iterator.next();
                                Map.Entry next_entry=(Map.Entry) next;
                                if(next_entry.getKey().toString().equalsIgnoreCase("WebTotal")){
                                    results_number=Long.parseLong(next_entry.getValue().toString());
                                }
                                int stop=0;
                        }
                    }
            
             return results_number;
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(BingResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        } catch (ParseException ex) {
            Logger.getLogger(BingResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
        catch(java.lang.ArrayIndexOutOfBoundsException ex){
         Logger.getLogger(BingResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }

         catch(java.lang.NullPointerException ex){
         Logger.getLogger(BingResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
         catch(Exception ex){
         Logger.getLogger(BingResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
    }
}
