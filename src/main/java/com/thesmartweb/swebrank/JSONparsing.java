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
/**
 *
 * @author Themis Mavridis
 */
import java.io.IOException;
import static java.lang.String.valueOf;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.*;
import java.util.Iterator;

/**
 * Class for parsing JSON responses
 * @author themis
 */
public class JSONparsing {

    /**
     * The total links 
     */
    public static String[] links;

    /**
     * The links by yahoo or bing
     */
    public static String[] links_yahoo_bing;

    /**
     * The entries by yahoo or bing
     */
    public static Map.Entry[] entries_yahoo_bing;

    /**
     * The amount of semantic triples
     */
    public static int triple_cnt;

    /**
     * The amount of entities by Yahoo Content Analysis service that contained a word of a query
     */
    public static int ent_query_cnt=0;

    /**
     *The amount of categories by Yahoo Content Analysis service that contained a word of a query
     */
    public static int cat_query_cnt=0;
    
    /**
     * The amount of entities by Dandelion named Entity Extraction API that contained a word of a query
     */
    public static int ent_query_cnt_dand=0;
    /**
     * The amount of categories by Dandelion named Entity Extraction API that contained a word of a query
     */
    public static int cat_query_cnt_dand=0;
    /**
     * The amount of entities by Yahoo Content Analysis service that contained the whole query
     */
    public static int ent_query_cnt_whole=0;

    /**
     * The amount of categories by Yahoo Content Analysis service that contained the whole query
     */
    public static int cat_query_cnt_whole=0;
    
    /**
     * The amount of entities by Dandelion named Entity Extraction API that contained the whole query
     */
    public static int ent_query_cnt_dand_whole=0;
    /**
     * The amount of categories by Dandelion named Entity Extraction API that contained the whole query
     */
    public static int cat_query_cnt_dand_whole=0;
    
    JSONparsing(){links=new String[10];}//used for Google
    JSONparsing(int results_number){links_yahoo_bing=new String[results_number];}//used for Yahoo

    /**
     * Method to get the links from Google Search API (google gets every time only 10 results)
     * @param input the JSON response
     * @return an array of the urls of the results
     */
    public String[] GoogleJsonParsing(String input)  {
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create a map
            JSONObject json = (JSONObject) parser.parse(input);         
            //Get a set of the entries
            Set set = json.entrySet();
            //Create an iterator
            Iterator iterator = set.iterator();
            //Find the entry that contain the part of JSON that contains the link
            int i=0;
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                if(entry.getKey().toString().equalsIgnoreCase("items")){
                    JSONArray jsonarray = (JSONArray) entry.getValue();
                    //find the key=link entry which contains the link
                    Iterator iterator_jsonarray= jsonarray.iterator();
                    while(iterator_jsonarray.hasNext()){
                        JSONObject next = (JSONObject) iterator_jsonarray.next();
                        links[i] = next.get("link").toString();
                        i++;
                    }
                }
            }
            return links;
        } catch (ParseException ex) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, ex);
            return links;
        }
    }

    /**
     * Method to get the links from Yahoo Search API
     * @param input the JSON response
     * @param yahoo_result_number the number of results to get
     * @return an array of the urls of the results
     */
    public String[] YahooJsonParsing(String input,int yahoo_result_number){
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(input);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] arr = set.toArray();
            Map.Entry entry = (Map.Entry) arr[0];
            //****get to second level of yahoo jsonmap
            String you = entry.getValue().toString();
           json = (Map) parser.parse(you);
             set = json.entrySet();
             arr = set.toArray();
            entry = (Map.Entry) arr[1];
            //***get to third level of yahoo jsonmap
           you = entry.getValue().toString();
           
           json = (Map) parser.parse(you);
             set = json.entrySet();
             arr = set.toArray();
            entry = (Map.Entry) arr[0];
            you = entry.getValue().toString();
            JSONArray json_arr = (JSONArray) parser.parse(you);
            for (int j = 0; j < yahoo_result_number; j++) {
                Map json_new = (Map) json_arr.get(j);
                Set set_new = json_new.entrySet();
                Object[] arr_new = set_new.toArray();
                for (int k = 0; k < arr_new.length; k++) {
                    entries_yahoo_bing[k] = (Map.Entry) arr_new[k];
                }
                //find the entry that has label "link" in ordet to get the link
                for (int y = 0; y < arr_new.length; y++) {
                    if (entries_yahoo_bing[y].getKey().toString().equalsIgnoreCase("url")) {
                        links_yahoo_bing[j] = (String) entries_yahoo_bing[y].getValue().toString();
                    }
                }
                
            }
            return links_yahoo_bing;
        } catch (ParseException ex) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, ex);
            return links_yahoo_bing;
        }
}

    /**
     * Method to get Bing Search API results
     * @param input the JSON response
     * @param bing_result_number the results number
     * @return an array with the urls of the results
     */
    public String[] BingAzureJsonParsing(String input,int bing_result_number) {
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            JSONObject jsonmap = (JSONObject) parser.parse(input);
            // Get a set of the entries
            Set set = jsonmap.entrySet();
            Iterator iterator=set.iterator();
            int i=0;
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                if(entry.getKey().toString().equalsIgnoreCase("d")){
                    JSONObject jsonobject=(JSONObject) entry.getValue();
                    JSONArray jsonarray = (JSONArray) jsonobject.get("results");
                    Iterator jsonarrayiterator=jsonarray.listIterator();
                    while(jsonarrayiterator.hasNext()){
                        JSONObject linkobject= (JSONObject) jsonarrayiterator.next();
                        links_yahoo_bing[i]=linkobject.get("Url").toString();
                        i++;
                    }
                }
            }
            return links_yahoo_bing;
        }
        catch (ParseException ex) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, ex);
            return links_yahoo_bing;
        }
            
}

    /**
     * Method to get the semantic namespaces by SINDICE JSON response
     * @param input the JSON response by the Sindice API
     * @return a boolean array for all the namespaces
     */
    public boolean[] TripleParse(String input) {
        try {
            boolean[] namespaces=new boolean[39];
            if(input.length()>0){
                //Create a parser
                JSONParser parser = new JSONParser();
                //Create the map
                Map json = (Map) parser.parse(input);
                // Get a set of the entries
                Set set = json.entrySet();
                Object[] arr = set.toArray();
                int flagresults=0;
                int flagstatus=0;
                Map.Entry entry;
                for(int j=0;j<arr.length;j++){
                    entry = (Map.Entry) arr[j];
                    if(entry.getKey().toString().equalsIgnoreCase("extractorResults")){
                        flagresults=j;
                    }
                    if(entry.getKey().toString().equalsIgnoreCase("status")){
                        flagstatus=j;
                    }
                }
                Map.Entry entrystatus=(Map.Entry) arr[flagstatus];
                if(entrystatus.getValue().toString().equalsIgnoreCase("ok")){
                    entry=(Map.Entry) arr[flagresults];

                    String you = entry.getValue().toString();
                    json = (Map) parser.parse(you);
                    set = json.entrySet();
                    arr = set.toArray();

                    for(int j=0;j<arr.length;j++){
                        entry = (Map.Entry) arr[j];
                        if(entry.getKey().toString().equalsIgnoreCase("metadata")){
                            flagresults=j;
                        }
                    }
                    entry = (Map.Entry) arr[flagresults];
                    //****get to the third level of bing jsonmap
                    you = entry.getValue().toString();
                   json = (Map) parser.parse(you);
                    set = json.entrySet();
                    arr = set.toArray();

                    for(int j=0;j<arr.length;j++){
                        entry = (Map.Entry) arr[j];
                        if(entry.getKey().toString().equalsIgnoreCase("explicit")){
                            flagresults=j;
                        }
                    }
                    entry = (Map.Entry) arr[flagresults];
                    you = entry.getValue().toString();
                   json = (Map) parser.parse(you);
                    set = json.entrySet();
                    arr = set.toArray();

                    for(int j=0;j<arr.length;j++){
                        entry = (Map.Entry) arr[j];
                        if(entry.getKey().toString().equalsIgnoreCase("bindings")){
                            flagresults=j;
                        }
                    }
                    entry = (Map.Entry) arr[flagresults];
                    JSONArray entry_new=(JSONArray)entry.getValue();
                    for(int p=0;p<entry_new.size();p++){
                        json = (Map) entry_new.get(p);
                        set = json.entrySet();
                        arr = set.toArray();
                        for(int kj=0;kj<arr.length;kj++){
                            entry = (Map.Entry) arr[kj];
                            if(entry.getKey().toString().contains("p")){
                                JSONObject jo= (JSONObject) entry.getValue();
                                String next = jo.get("value").toString();
                                if(next.contains("http://purl.org/vocab/bio/0.1/")){
                                    namespaces[0]=true;
                                }

                                if(next.contains("http://purl.org/dc/elements/1.1/")){
                                    namespaces[1]=true;
                                }
                                if(next.contains("http://purl.org/coo/n")){
                                    namespaces[2]=true;
                                }
                                if(next.contains("http://web.resource.org/cc/")){
                                    namespaces[3]=true;
                                }
                                if(next.contains("http://diligentarguont.ontoware.org/2005/10/arguonto")){
                                    namespaces[4]=true;
                                }
                                if(next.contains("http://usefulinc.com/ns/doap")){
                                    namespaces[5]=true;
                                }
                                if(next.contains("http://xmlns.com/foaf/0.1/")){
                                    namespaces[6]=true;
                                }
                                if(next.contains("http://purl.org/goodrelations/")){
                                    namespaces[7]=true;
                                }
                                if(next.contains("http://purl.org/muto/core")){
                                    namespaces[8]=true;
                                }
                                if(next.contains("http://webns.net/mvcb/")){
                                    namespaces[9]=true;
                                }
                                if(next.contains("http://purl.org/ontology/mo/")){
                                    namespaces[10]=true;
                                }
                                if(next.contains("http://purl.org/innovation/ns")){
                                    namespaces[11]=true;
                                }
                                if(next.contains("http://openguid.net/rdf")){
                                    namespaces[12]=true;
                                }
                                if(next.contains("http://www.slamka.cz/ontologies/diagnostika.owl")){
                                    namespaces[13]=true;
                                }
                                if(next.contains("http://purl.org/ontology/po/")){
                                    namespaces[14]=true;
                                }
                                if(next.contains("http://purl.org/net/provenance/ns")){
                                    namespaces[15]=true;
                                }
                                if(next.contains("http://purl.org/rss/1.0/modules/syndication")){
                                    namespaces[16]=true;
                                }
                                if(next.contains("http://rdfs.org/sioc/ns")){
                                    namespaces[17]=true;
                                }
                                if(next.contains("http://madskills.com/public/xml/rss/module/trackback/")){
                                    namespaces[18]=true;
                                }
                                if(next.contains("http://rdfs.org/ns/void")){
                                    namespaces[19]=true;
                                }
                                if(next.contains("http://www.fzi.de/2008/wise/")){
                                    namespaces[20]=true;
                                }
                                if(next.contains("http://xmlns.com/wot/0.1")){
                                    namespaces[21]=true;
                                }
                                if(next.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns")){
                                    namespaces[22]=true;
                                }
                                if(next.contains("http://www.w3.org/")&next.contains("rdf-schema")){
                                    namespaces[23]=true;
                                }
                                if(next.contains("http://www.w3.org/")&next.contains("XMLSchema#")){
                                    namespaces[24]=true;
                                }
                                if(next.contains("http://www.w3.org")&&next.contains("owl")){
                                    namespaces[25]=true;
                                }
                                if(next.contains("http://purl.org/dc/terms/")){
                                    namespaces[26]=true;
                                }
                                if(next.contains("http://www.w3.org/")&&next.contains("vcard")){
                                    namespaces[27]=true;
                                }
                                if(next.contains("http://www.geonames.org/ontology")){
                                    namespaces[28]=true;
                                }
                                if(next.contains("http://search.yahoo.com/searchmonkey/commerce/")){
                                    namespaces[29]=true;
                                }
                                if(next.contains("http://search.yahoo.com/searchmonkey/media/")){
                                    namespaces[30]=true;
                                }
                                if(next.contains("http://cb.semsol.org/ns#")){
                                    namespaces[31]=true;
                                }
                                if(next.contains("http://blogs.yandex.ru/schema/foaf/")){
                                    namespaces[32]=true;
                                }
                                if(next.contains("http://www.w3.org/2003/01/geo/wgs84_pos#")){
                                    namespaces[33]=true;
                                }
                                if(next.contains("http://rdfs.org/sioc/ns#")){
                                    namespaces[34]=true;
                                }
                                if(next.contains("http://rdfs.org/sioc/types#")){
                                    namespaces[35]=true;
                                }
                                if(next.contains("http://smw.ontoware.org/2005/smw#")){
                                    namespaces[36]=true;
                                }
                                if(next.contains("http://purl.org/rss/1.0/")){
                                    namespaces[37]=true;
                                }
                                if(next.contains("http://www.w3.org/2004/12/q/contentlabel#")){
                                    namespaces[38]=true;
                                }
                            }
                        }
                    }
                }
            }
            return namespaces;
        }
        catch (ParseException ex) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, ex);
            boolean[] namespaces=new boolean[40];
            return namespaces;
        }
        catch (Exception x) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, x);
            boolean[] namespaces=new boolean[40];
            return namespaces;
        }

}

    /**
     * Method to parse the JSON response by Diffbot
     * @param input the JSON response of Diffbot
     * @return a String containing all the Diffbot tags
     */
    public String DiffbotParsing(String input){
        String output=""; 
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(input);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] arr = set.toArray();
            Map.Entry entry = (Map.Entry) arr[0];
            //****get to second level of  jsonmap to get the tags
            Object value = entry.getValue();
            String you = entry.getValue().toString();
            DataManipulation tp=new DataManipulation();
            output=tp.removeChars(you).toLowerCase();
            return output;
        }
          catch (ParseException ex) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, ex);
            output="fail";
             return output;
        }


    }
    private List<String> entities;//the list to contain all the semantic entities
    private List<String> categories;//the list to contain all the semantic categories
    private double ent_avg_yahoo_score;//the average score of the entities recognized
    private double cat_avg_yahoo_score;//the average score of the categories recognized
    
    /**
     * Method to get all the Entities and Categories (and the corresponding stats) by Yahoo Content Analysis API
     * @param input the JSON response by the Yahoo Content Analysis API
     * @param quer the query to count the stats for
     * @param StemFlag flag for stemming
     * @param score_threshold threshold for the entities score 
     */
    public void YahooEntityJsonParsing(String input, String quer,boolean StemFlag, double score_threshold){
        try {
            double threshold = score_threshold;//threshold for the scores of entities in yahoo
            ent_query_cnt=0;
            cat_query_cnt=0;
            entities = new ArrayList<>();//it is going to contain all the entities
            categories = new ArrayList<>();//it is going to contain all the categories
            ent_avg_yahoo_score=0.0;
            cat_avg_yahoo_score=0.0;
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(input);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] arr = set.toArray();
            Map.Entry entry = (Map.Entry) arr[0];
            //****get to second level of yahoo jsonmap
            String you = entry.getValue().toString();
            json = (Map) parser.parse(you);
            set = json.entrySet();
            arr = set.toArray();
            
            searchforresult:
            for (int kj=0;kj<arr.length;kj++){
                entry = (Map.Entry) arr[kj];
                if(entry.getKey().toString().contains("results")){ 
                    break searchforresult;
                }
            }
            
            //***get to third level of yahoo jsonmap
            //fix to search value = result
            if(entry.getValue()!=null){
                you = entry.getValue().toString();
                json = (Map) parser.parse(you);
                set = json.entrySet();
                arr = set.toArray();//here we have in arr[0] the categories related to the url and in arr[1] the entities related to
                //--we get the categories first
                for(int jk=0;jk<arr.length;jk++){
                    entry = (Map.Entry) arr[jk];
                    if(entry.getKey().toString().contains("yctCategories")){
                        you = entry.getValue().toString();
                        json = (Map) parser.parse(you);
                        set = json.entrySet();
                        Object[] arr_cat = set.toArray();
                        for (int ip=0;ip<arr_cat.length;ip++){
                            entry = (Map.Entry) arr_cat[ip];
                            if(entry.getKey().toString().contains("yctCategory")){
                                you = entry.getValue().toString();
                                if(you.startsWith("[")){
                                    JSONArray json_arr = (JSONArray) parser.parse(you);
                                    for(int ka=0;ka<json_arr.size();ka++){
                                            json = (Map) json_arr.get(ka);
                                            set = json.entrySet();
                                            arr_cat = set.toArray();
                                            double score=0.0;
                                            for(int kj=0;kj<arr_cat.length;kj++){
                                                entry = (Map.Entry) arr_cat[kj];
                                                if(entry.getKey().toString().contains("score")){
                                                    score = Double.parseDouble(entry.getValue().toString());
                                                    if(score>threshold){
                                                        cat_avg_yahoo_score=cat_avg_yahoo_score+score;
                                                    }
                                                }
                                                if(entry.getKey().toString().contains("content")&&score>threshold){
                                                    categories.add(entry.getValue().toString().toLowerCase());

                                                }
                                            }
                                    }
                                }
                                if(you.startsWith("{")){
                                    json = (Map) parser.parse(you);
                                    set = json.entrySet();
                                    arr_cat = set.toArray();
                                    double score=0.0;
                                    for(int ka=0;ka<arr_cat.length;ka++){
                                        entry = (Map.Entry) arr_cat[ka];
                                        if(entry.getKey().toString().contains("score")){
                                            score = Double.parseDouble(entry.getValue().toString());
                                            if(score>threshold){
                                                cat_avg_yahoo_score=cat_avg_yahoo_score+score;
                                            }
                                        }
                                        if(entry.getKey().toString().contains("content")&&score>threshold){
                                            String categoryString=entry.getValue().toString().toLowerCase();
                                            if(StemFlag){
                                                String[] splitEntity = categoryString.split(" ");
                                                categoryString="";
                                                StemmerSnow stemmer = new StemmerSnow();
                                                List<String> splitEntityList=stemmer.stem(Arrays.asList(splitEntity));
                                                StringBuilder sb = new StringBuilder();
                                                for(String s:splitEntityList){
                                                    sb.append(s.trim());
                                                    sb.append(" ");
                                                }
                                                categoryString = sb.toString().trim();
                                            }
                                            categories.add(categoryString);
                                        }
                                    }
                                }     
                            }
                        }
                    }
                    //--we get the entities now
                    if(entry.getKey().toString().contains("entities")){
                        you = entry.getValue().toString();
                        json = (Map) parser.parse(you);
                        set = json.entrySet();
                        Object[] arr_ent = set.toArray();
                        for (int ip=0;ip<arr_ent.length;ip++){
                            entry = (Map.Entry) arr_ent[ip];
                            if(entry.getKey().toString().contains("entity")){
                                you = entry.getValue().toString();
                                if(you.startsWith("[")){
                                    JSONArray json_arr = (JSONArray) parser.parse(you);
                                    for(int ka=0;ka<json_arr.size();ka++){
                                        json = (Map) json_arr.get(ka);
                                        set = json.entrySet();
                                        arr_ent = set.toArray();
                                        double score=0.0;
                                        for(int kj=0;kj<arr_ent.length;kj++){
                                            entry = (Map.Entry) arr_ent[kj];
                                            if(entry.getKey().toString().contains("score")){
                                                score = Double.parseDouble(entry.getValue().toString());
                                                if(score>threshold){
                                                    ent_avg_yahoo_score=ent_avg_yahoo_score+score;
                                                }
                                            }
                                            if(entry.getKey().toString().contains("text")&&score>threshold){
                                                you = entry.getValue().toString();
                                                json = (Map) parser.parse(you);
                                                set = json.entrySet();
                                                arr_ent = set.toArray();
                                                for(int kai=0;kai<arr_ent.length;kai++){
                                                    entry = (Map.Entry) arr_ent[kai];
                                                    if(entry.getKey().toString().contains("content")){
                                                        entities.add(entry.getValue().toString().toLowerCase()); 
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(you.startsWith("{")){
                                    json = (Map) parser.parse(you);
                                    set = json.entrySet();
                                    arr_ent = set.toArray();
                                    double score=0.0;
                                    for(int ka=0;ka<arr_ent.length;ka++){
                                        entry = (Map.Entry) arr_ent[ka];
                                        if(entry.getKey().toString().contains("score")){
                                            score = Double.parseDouble(entry.getValue().toString());
                                            if(score>threshold){
                                                ent_avg_yahoo_score=ent_avg_yahoo_score+score;
                                            }
                                        }
                                        if(entry.getKey().toString().contains("text")&&score>threshold){
                                            you = entry.getValue().toString();
                                            json = (Map) parser.parse(you);
                                            set = json.entrySet();
                                            arr_ent = set.toArray();
                                            for(int kai=0;kai<arr_ent.length;kai++){
                                                entry = (Map.Entry) arr_ent[kai];
                                                if(entry.getKey().toString().contains("content")){
                                                    String entityString =entry.getValue().toString().toLowerCase();
                                                    if(StemFlag){
                                                        String[] splitEntity = entityString.split(" ");
                                                        entityString="";
                                                        StemmerSnow stemmer = new StemmerSnow();
                                                        List<String> splitEntityList=stemmer.stem(Arrays.asList(splitEntity));
                                                        StringBuilder sb = new StringBuilder();
                                                        for(String s:splitEntityList){
                                                            sb.append(s.trim());
                                                            sb.append(" ");
                                                        }
                                                        entityString = sb.toString().trim();
                                                    }
                                                    entities.add(entityString);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ent_query_cnt=0;
            ent_query_cnt_whole=0;
            cat_query_cnt_whole=0;
            cat_query_cnt_whole=0;
            quer =quer.toLowerCase();
            String[] split = quer.split("\\+");
            if(StemFlag){
                List<String> splitQuery = Arrays.asList(split);
                StemmerSnow stemmer = new StemmerSnow();
                splitQuery = stemmer.stem(splitQuery);
                split = splitQuery.toArray(new String[splitQuery.size()]);
            }
            int ent_count=0;
            for(String s:entities){
                ent_count=0;
                for(String splitStr:split){
                    if(s.contains(splitStr)){
                        ent_query_cnt++;
                        ent_count++;
                    }
                }
                if(ent_count==split.length){
                    ent_query_cnt_whole++;
                }
            }
            int cat_count=0;
            for(String s:categories){
                cat_count=0;
                for(String splitStr:split){
                    if(s.contains(splitStr)){
                        cat_query_cnt++;
                        cat_count++;
                    }
                }
                if(cat_count==split.length){
                    cat_query_cnt_whole++;
                }
            }
            ent_avg_yahoo_score = ent_avg_yahoo_score/ (double) entities.size();
            cat_avg_yahoo_score = cat_avg_yahoo_score/ (double) categories.size();
        } catch (ParseException ex) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, ex);
        }
}

    /**
     * Method to get the entities counter with partial match query
     * @return entities counter with partial match query
     */
    public int GetEntQuerCnt(){
    return ent_query_cnt;
}

    /**
     * Method to get the categories counter with partial match query
     * @return categories counter with partial match query
     */
    public int GetCatQuerCnt(){
    return cat_query_cnt;
    
}
    /**
     * Method to get the entities counter containing the whole query
     * @return entities counter containing the whole query
     */
    public int GetEntQuerCntWhole(){
    return ent_query_cnt_whole;
}

    /**
     * Method to get the categories counter containing the whole query
     * @return categories counter containing the whole query
     */
    public int GetCatQuerCntWhole(){
    return cat_query_cnt_whole;
    
}
    /**
     * Method to get the entities List
     * @return entities List
     */
    public List<String> GetEntitiesYahoo(){return entities;}
    /**
     * Method to get the categories List
     * @return categories List
     */
    public List<String> GetCategoriesYahoo(){return categories;}
    /**
     * Method to get the entities average score
     * @return entities score of entities recognized
     */
    public double GetEntitiesScoreYahoo(){return ent_avg_yahoo_score;}
    /**
     * Method to get the categories average score
     * @return average score of categories recognized
     */
    public double GetCategoriesScoreYahoo(){return cat_avg_yahoo_score;}
    
    /**
     * Get meta info for a Youtube link
     * @param ventry the id of the Youtube video
     * @return a String with all the meta info about the youtube video
     */
    public String GetYoutubeDetails(String ventry) {
        try {
            String apikey = "AIzaSyDLm-MfYHcbTHQO1S8ROX2rpvsqd5oYSRI";
            String output = "";
            URL link_ur = new URL("https://www.googleapis.com/youtube/v3/videos?id="+ventry+"&key=" + apikey+"&part=snippet");
            APIconn apicon = new APIconn();
            String line = apicon.connect(link_ur);
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(line);
            // Get a set of the entries
            Set set = json.entrySet();
            Iterator iterator=set.iterator();
            Map.Entry entry = null;
            boolean flagfound = false;
            while(iterator.hasNext()&&!flagfound){
                entry= (Map.Entry) iterator.next();
                if(entry.getKey().toString().equalsIgnoreCase("items")){
                    flagfound=true;
                }
            }
            JSONArray jsonarray=(JSONArray) entry.getValue();
            Iterator iteratorarray = jsonarray.iterator();
            flagfound=false;
            JSONObject get =null;
            while(iteratorarray.hasNext()&&!flagfound){
                JSONObject next = (JSONObject) iteratorarray.next();
                if(next.containsKey("snippet")){
                     get = (JSONObject) next.get("snippet");
                     flagfound=true;
                }
            }
            String description="";
            String title="";
            if(flagfound){
                if(get.containsKey("description")){
                    description=get.get("description").toString();
                }
                if(get.containsKey("title")){
                    title=get.get("title").toString();
                }
                    output = description + " " + title;
            }
            Stopwords stopwords = new Stopwords();
            output = stopwords.stop(output);
            return output;
        } catch (IOException | ArrayIndexOutOfBoundsException | ParseException ex) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, ex);
            String output = null;
            return output;
        } 
    }
    /**
     * Method to get all the Entities and Categories (and the corresponding stats) by Dandelion named Entity Extraction API
     * @param input the JSON response by the Yahoo Dandelion named Entity Extraction API
     * @param quer the query to count the stats for
     * @param StemFlag flag for stemming
     * @return 
     */
    private List<String> entitiesDand = new ArrayList<>();//contain all the entities of Dandelion API
    private List<String> categoriesDand = new ArrayList<>();//contain all the categories of Dandelion API
    private double ent_avg_dand_score=0.0;
    public void DandelionParsing(String input, String query, boolean StemFlag){ 
        try {
            ent_avg_dand_score=0.0;
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            Object parse = parser.parse(input);
            Map json = (Map) parser.parse(input);
            Set entrySet = json.entrySet();
            Iterator iterator=entrySet.iterator();
            Map.Entry entry = null;
            boolean flagfound = false;
            //we are going to search if we have semantic annotations
            while(iterator.hasNext()&&!flagfound){
                entry= (Map.Entry) iterator.next();
                if(entry.getKey().toString().equalsIgnoreCase("annotations")){
                    flagfound=true;
                }
            }
            if(flagfound){
                //if we have annotations we get the value
                JSONArray jsonarray=(JSONArray) entry.getValue();
                Iterator iteratorarray = jsonarray.iterator();
                flagfound=false;
                JSONObject get =null;
                while(iteratorarray.hasNext()&&!flagfound){
                    JSONObject next = (JSONObject) iteratorarray.next();
                    if(next.containsKey("label")){
                        String entityString =next.get("label").toString().toLowerCase();
                        if(StemFlag){
                            String[] splitEntity = entityString.split(" ");
                            entityString="";
                            StemmerSnow stemmer = new StemmerSnow();
                            List<String> splitEntityList=stemmer.stem(Arrays.asList(splitEntity));
                            StringBuilder sb = new StringBuilder();
                            for(String s:splitEntityList){
                                sb.append(s.trim());
                                sb.append(" ");
                            }
                            entityString = sb.toString().trim();
                        }
                        entitiesDand.add(entityString);
                    }
                    if(next.containsKey("categories")){
                        jsonarray = (JSONArray) next.get("categories");
                        for(int i=0;i<jsonarray.size();i++){
                            String categoryString =jsonarray.get(i).toString().toLowerCase();
                            if(StemFlag){
                                String[] splitEntity = categoryString.split(" ");
                                categoryString="";
                                StemmerSnow stemmer = new StemmerSnow();
                                List<String> splitEntityList=stemmer.stem(Arrays.asList(splitEntity));
                                StringBuilder sb = new StringBuilder();
                                for(String s:splitEntityList){
                                    sb.append(s.trim());
                                    sb.append(" ");
                                }
                                categoryString = sb.toString().trim();
                            }
                            categoriesDand.add(categoryString);
                        }
                    }
                    if(next.containsKey("confidence")){
                        ent_avg_dand_score = ent_avg_dand_score + Double.parseDouble(next.get("confidence").toString());
                    }
                }
                ent_avg_dand_score = ent_avg_dand_score/(double)entitiesDand.size();
                ent_query_cnt_dand=0;
                cat_query_cnt_dand=0;
                ent_query_cnt_dand_whole=0;
                cat_query_cnt_dand_whole=0;
                query =query.toLowerCase();
                String[] split = query.split("\\+");
                if(StemFlag){
                    List<String> splitQuery = Arrays.asList(split);
                    StemmerSnow stemmer = new StemmerSnow();
                    splitQuery = stemmer.stem(splitQuery);
                    split = splitQuery.toArray(new String[splitQuery.size()]);
                }
                int ent_count=0;
                for(String s:entitiesDand){
                    ent_count=0;
                    for(String splitStr:split){
                        if(s.contains(splitStr)){
                            ent_query_cnt_dand++;
                            ent_count++;
                        }
                    }
                    if(ent_count==split.length){
                        ent_query_cnt_dand_whole++;
                    }
                }
                int cat_count=0;
                for(String s:categoriesDand){
                    cat_count=0;
                    for(String splitStr:split){
                        if(s.contains(splitStr)){
                            cat_query_cnt_dand++;
                            cat_count++;
                        }
                    }
                    if(cat_count==split.length){
                        cat_query_cnt_dand_whole++;
                    }
                }
                
            }
        }
          catch (ParseException ex) {
            Logger.getLogger(JSONparsing.class.getName()).log(Level.SEVERE, null, ex);
            
        }


    }
    /**
     * Method to return the entities counter (partial query match)
     * @return the entities counter (partial query match)
     */
    public int GetEntQuerCntDand(){
    return ent_query_cnt_dand;
}

    /**
     * Method to return the categories counter (partial query match)
     * @return the categories counter (partial query match)
     */
    public int GetCatQuerCntDand(){
    return cat_query_cnt_dand;
    
}
    /**
     * Method to return the the entities counter with whole query match
     * @return the entities counter with whole query match
     */
    public int GetEntQuerCntDandWhole(){
    return ent_query_cnt_dand_whole;
}

    /**
     * Method to return the categories counter with whole query match
     * @return the categories counter with whole query match
     */
    public int GetCatQuerCntDandWhole(){
    return cat_query_cnt_dand_whole;
    
}
    /**
     * Method to return the entities  by Dandelion API
     * @return the entities by Dandelion API
     */
    public List<String> GetEntitiesDand(){
    return entitiesDand;
}
    /**
     * Method to return the categories by Dandelion API
     * @return the categories by Dandelion API
     */
      public List<String> GetCategoriesDand(){
    return categoriesDand;
}
      /**
     * Method to return the entities average score by Dandelion API
     * @return the entities average score by Dandelion API
     */
      public double GetEntitiesScoreDand(){
    return ent_avg_dand_score;
}
}



