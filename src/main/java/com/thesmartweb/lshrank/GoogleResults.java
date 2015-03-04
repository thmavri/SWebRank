/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * @author Themis Mavridis
 */

package com.thesmartweb.lshrank;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.simple.parser.*;
import org.json.simple.JSONArray;
import java.util.*;

//keys for the google API
//AIzaSyCGIdV0fatLZw_ltq7WdkiPfdv0t62_3C8&cx=004407321776517455750:p9yjzmshjac
//AIzaSyBdafvgCrExUWDdR6sr57Rl_x_FojxF8Dk&cx=004407321776517455750:p9yjzmshjac
//
/**
 *
 * @author Themis Mavridis
 */
public class GoogleResults {

    /**
     *
     * @param quer
     * @param google_results_number
     * @param example_dir
     * @return
     */
    public String[] Get(String quer,int google_results_number,String example_dir, String config_path){
        //counter is set
        int counter_limit=google_results_number+1;
        String chk="fail";
        String[] links_google_total=new String[google_results_number];//it contains all the links of the google, because the google api allows to get only 10 results each time
        int flag=0;
        DataManipulation textualmanipulation = new DataManipulation();
        APIconn apicon = new APIconn();
        for (int counter=1;counter<counter_limit;counter+=10){
            String[] links=new String[10];//10 is the default number that Google Search API returns
            String[] apikeys =new String[6];
            String[] cxs=new String[6];
            String[] keys;//keys is the combination of cxs and apikeys which are parameters for performing queries to Google Search API
            keys=GetKeys(config_path);
            int i=0;
            int flag_key=0;
            String key="";
            String line="";
            //-------------after this loop line will include the JSON response of the custom search api of google (we try with the different keys, due to account limitations)
            while(flag_key==0&&i<(cxs.length*apikeys.length)){
                key=keys[i];
                i++;
                URL link_ur = null;
                //-------we form the URL-----
                try {
                    link_ur = new URL("https://www.googleapis.com/customsearch/v1?key="+key+"&q=" + quer + "&alt=json" + "&start=" + counter);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(GoogleResults.class.getName()).log(Level.SEVERE, null, ex);
                    return links_google_total;
                }
                //------we attempt to connect to Google custom search API
                line = apicon.connect(link_ur);        
                if(!line.equalsIgnoreCase("fail")){
                    flag_key=1;
                    textualmanipulation.AppendString(line, example_dir + "google/" + "json" + counter + ".txt");
                    //initialize JSONparsing that parses the json and gets the links
                    JSONparsing gg = new JSONparsing();
                    //get the links in an array
                    links = gg.GoogleJsonParsing(line);
                    //insert the links from each loop to the array with all the google links
                    for(int jj=0;jj<links.length;jj++){
                        links_google_total[jj+counter-1]=links[jj];
                    }
                }
            }
        }
        return links_google_total;   
    }
 
    /**
     *
     * @param quer
     * @return
     */
    public Long Get_Results_Number(String quer)//it gets the results number for a specific query
    {   try {
            long results_number = 0;
            //we connect through the google api JSON custom search
            String check_quer = quer.substring(quer.length() - 1, quer.length());
            char plus = "+".charAt(0);
            char check_plus = check_quer.charAt(0);
            if (check_plus == plus) {
                quer = quer.substring(0, quer.length() - 1);
            }
            URL link_ur = new URL("https://www.googleapis.com/customsearch/v1?key=AIzaSyCGIdV0fatLZw_ltq7WdkiPfdv0t62_3C8&cx=004407321776517455750:p9yjzmshjac&q=" + quer + "&alt=json");
            APIconn apicon = new APIconn();
            String line = apicon.connect(link_ur);
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(line);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] obj = set.toArray();
            Map.Entry entry = (Map.Entry) obj[2];
            //get to the second level
            String you = entry.getValue().toString();
            json = (Map) parser.parse(you);
            set = json.entrySet();
            obj = set.toArray();
            entry = (Map.Entry) obj[obj.length-1];
            //get to the third level
            you = entry.getValue().toString();
            JSONArray json_arr = (JSONArray) parser.parse(you);
            obj = json_arr.toArray();
            you = obj[0].toString();
            //get to the fourth level
            json = (Map) parser.parse(you);
            set= json.entrySet();
            obj = set.toArray();
            entry = (Map.Entry) obj[4];
            results_number = (Long) entry.getValue();
            //results_number=(Long) entry3.getValue();
            return results_number;
        } catch (MalformedURLException ex) {
            Logger.getLogger(GoogleResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
             return results_number;
        } catch (ParseException ex) {
            Logger.getLogger(GoogleResults.class.getName()).log(Level.SEVERE, null, ex);
             long results_number = 0;
             return results_number;
        }
         catch(java.lang.ArrayIndexOutOfBoundsException ex){
         Logger.getLogger(GoogleResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
         catch(java.lang.NullPointerException ex){
         Logger.getLogger(GoogleResults.class.getName()).log(Level.SEVERE, null, ex);
            long results_number = 0;
            return results_number;
        }
    }
    
    /**
     *
     * @return
     */
    public String[] GetKeys(String config_path){
        ReadInput ri = new ReadInput();
        List<String> cxsList = ri.GetKeyFile(config_path, "google_cxs");
        List<String> apikeysList = ri.GetKeyFile(config_path, "google_apikeys");
        String[] cxs = cxsList.toArray(new String[cxsList.size()]);
        String[] apikeys = apikeysList.toArray(new String[apikeysList.size()]);
        String[] keys=new String[cxsList.size()*cxsList.size()];
        
        int k=0;
        for(int i=0;i<cxs.length;i++){
            for(int j=0;j<apikeys.length;j++){
                keys[k]=apikeys[i]+"&cx="+cxs[j];
                k++;
            }
        }
        return keys;
    }
}
