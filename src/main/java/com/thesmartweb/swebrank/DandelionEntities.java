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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to deal with the semantic entities and concepts (categories) of Dandelion named Entity Extraction API
 * <a href="https://dandelion.eu/products/datatxt/nex/demo/?text=The+Mona+Lisa+is+a+16th+century+oil+painting+created+by+Leonardo.+It%27s+held+at+the+Louvre+in+Paris.&lang=auto&min_confidence=0.6&exec=true#results">check more</a>
 * @author Themistoklis Mavridis
 */
public class DandelionEntities {
    
    public static int ent_query_cnt=0;// the number of entities that contained a term of the query 
    public static int cat_query_cnt=0;// the number of categories that contained a term of the query
    public static int ent_query_cnt_whole=0;//the number of entities that contained the query as a whole
    public static int cat_query_cnt_whole=0;//the number of categories that contained  the query as a whole
    private List<String> entities;//the list to contain all the semantic entities
    private List<String> categories;//the list to contain all the semantic categories 
    /**
     * Method that recognizes the entities through Dandelion named Entity Extraction API of the content of a given URL
     * @param urlcheck the url to be annotated
     * @param quer the query term that which the url was a result of
     * @param StemFlag a flag to determine if we want to use stemming
     */
    public void connect(String urlcheck, String quer, boolean StemFlag, String config_path) {  
  
        try {  
            ent_query_cnt=0;
            cat_query_cnt=0;
            String line="";
            String baseUrl = "https://api.dandelion.eu/datatxt/nex/v1?url=";
            String fullUrlStr = baseUrl + URLEncoder.encode(urlcheck, "UTF-8")+"&min_confidence=0.30&include=types%2Ccategories%2Clod";
            String[] apiCreds = GetKeys(config_path);
            fullUrlStr =fullUrlStr +"&$app_id="+apiCreds[0]+"&$app_key="+apiCreds[1];
            URL link_ur = new URL(fullUrlStr);
            //we connect and then check the connection
            APIconn apicon = new APIconn();
            line = apicon.sslconnect(link_ur);
            if(!line.equalsIgnoreCase("fail")){
                JSONparsing jsonParser= new JSONparsing();
                //get the links in an array
                jsonParser.DandelionParsing(line, quer, StemFlag);
                ent_query_cnt=jsonParser.GetEntQuerCntDand();
                cat_query_cnt=jsonParser.GetCatQuerCntDand();
                ent_query_cnt_whole=jsonParser.GetEntQuerCntDandWhole();
                cat_query_cnt_whole=jsonParser.GetCatQuerCntDandWhole();
                entities = jsonParser.GetEntitiesDand();
                categories = jsonParser.GetCategoriesDand();
            }
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            Logger.getLogger(DandelionEntities.class.getName()).log(Level.SEVERE, null, ex);
        }  
    } 
    /**
     * Method to get the entities counter (partial query match)
     * @return entities counter
     */
    public int getEnt(){return ent_query_cnt;}
    /**
     * Method to get the categories counter (partial query match)
     * @return categories counter that have a partial query match
     */
    public int getCat(){return cat_query_cnt;}
    /**
     * Method to get the entities counter (whole query match)
     * @return entities counter that have whole query match
     */
    public int getEntWhole(){return ent_query_cnt_whole;}
    /**
     * Method to get the categories counter (whole query match)
     * @return categories counter that have whole query match
     */
    public int getCatWhole(){return cat_query_cnt_whole;}
    /**
     * Method to get the entities List
     * @return entities List
     */
    public List<String> GetEntitiesDand(){return entities;}
    /**
     * Method to get the categories List
     * @return categories List
     */
    public List<String> GetCategoriesDand(){return categories;}
    /**
     * Method to get the keys of Dandelion API 
     * @param config_path the directory to get the keys from
     * @return all the keys of Dandelion API
     */
    public String[] GetKeys(String config_path){
        ReadInput ri = new ReadInput();
        List<String> dandKeysList = ri.GetKeyFile(config_path, "dandelionkeys");
        String[] keys=new String[dandKeysList.size()];
        return keys;
    }
}