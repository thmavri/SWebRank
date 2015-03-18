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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import org.json.simple.parser.*;
import java.util.*;
import org.json.simple.JSONArray;
/**
 * Class to get results from Bing
 * @author Themistoklis Mavridis
 */
public class BingResults {

    /**
     * Class to get the results for the Bing Search API
     * @param query the query to get the search results for
     * @param bing_results_number the number of results to get
     * @param example_dir the directory to save the json file that bing returns
     * @param config_path the path with the configuration file (api keys for bing)
     * @return an array of the urls of the results of bing for this query
     */
    public String[] Get(String query,int bing_results_number,String example_dir,String config_path ){
        String[] links=new String[bing_results_number];
        try {
                APIconn apicon = new APIconn();    
                String line="fail";
                query=query.replace("+", "%20");
                URL azure_url=new URL("https://api.datamarket.azure.com/Bing/SearchWeb/v1/Web?Query=%27"+query+"%27&$top="+bing_results_number+"&$format=JSON");
                line=apicon.azureconnect(azure_url,config_path);
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

    /**
     * Class to get the results number from Bing in order to use it in NWD
     * @param quer the query to get the results for
     * @param config_path the path with the configuration file (api keys for bing)
     * @return the number of the results for a certain query
     */
    public Long Get_Results_Number(String quer,String config_path) {   
        try {
            long results_number = 0;
            //we check if we have an extra useless + in the end
            String check_quer = quer.substring(quer.length() - 1, quer.length());
            char plus = "+".charAt(0);
            char check_plus = check_quer.charAt(0);
            if (check_plus == plus) {
                quer = quer.substring(0, quer.length() - 1);
            }
            quer=quer.replace("+", "%20");
            //we connect through the Bing search api
            URL azure_url=new URL("https://api.datamarket.azure.com/Bing/Search/Composite?Sources=%27web%27&Query=%27"+quer+"%27&$format=JSON");                       
            APIconn apicon = new APIconn();
            String line = apicon.azureconnect(azure_url,config_path);                 
            if((!line.equalsIgnoreCase("fail"))&&(!line.equalsIgnoreCase("insufficient"))&&(!line.equalsIgnoreCase("provided"))){
                //Create a parser of the json
                JSONParser parser = new JSONParser();
                //Create the map
                Map json = (Map) parser.parse(line);
                // Get a set of the entries
                Set set = json.entrySet();
                Object[] arr = set.toArray();
                Map.Entry entry = (Map.Entry) arr[0];
                //get to second level of yahoo json
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
                }
            }
            return results_number;
        } catch (MalformedURLException | ParseException | java.lang.ArrayIndexOutOfBoundsException | java.lang.NullPointerException | NumberFormatException ex) {
            Logger.getLogger(BingResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
    }
}
