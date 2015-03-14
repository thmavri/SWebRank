/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author themis
 */
public class DandelionEntities {
    public static int ent_query_cnt=0;
    public static int cat_query_cnt=0;
    public static int ent_query_cnt_whole=0;
    public static int cat_query_cnt_whole=0;
    private List<String> entities;
    private List<String> categories;
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
    public int getEnt(){return ent_query_cnt;}
    public int getCat(){return cat_query_cnt;}
    public int getEntWhole(){return ent_query_cnt_whole;}
    public int getCatWhole(){return cat_query_cnt_whole;}
     public List<String> GetEntitiesDand(){
    return entities;
}
    
      public List<String> GetCategoriesDand(){
    return categories;
}
}

  
  

