package com.thesmartweb.lshrank;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to deal with the semantic entities and concepts (categories) of Dandelion named Entity Extraction API
 * @see https://dandelion.eu/products/datatxt/nex/demo/?text=The+Mona+Lisa+is+a+16th+century+oil+painting+created+by+Leonardo.+It%27s+held+at+the+Louvre+in+Paris.&lang=auto&min_confidence=0.6&exec=true#results
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
    public void connect(String urlcheck, String quer, boolean StemFlag) {  
  
        try {  
            ent_query_cnt=0;
            cat_query_cnt=0;
            String line="";
            String baseUrl = "https://api.dandelion.eu/datatxt/nex/v1?url=";
            String fullUrlStr = baseUrl + URLEncoder.encode(urlcheck, "UTF-8")+"&min_confidence=0.10&include=types%2Ccategories%2Clod";
            fullUrlStr =fullUrlStr +"&$app_id=59b43f94&$app_key=4374ae537a099afdca598c85a5cdaae7";
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
}