/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.TextAPIException;
import com.aylien.textapi.parameters.*;
import com.aylien.textapi.responses.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author themis
 */
public class AylienEntities {
    public static int ent_query_cnt_ay=0;
    public static int cat_query_cnt_ay=0;
    public static int ent_query_cnt_ay_whole=0;
    public static int cat_query_cnt_ay_whole=0;
    public void connect(String urlcheck, String quer) {  
  
        try {
            TextAPIClient client = new TextAPIClient(
                    "914b03cc", "cfdc5351f4029d42cc690e2205524fc6");
            URL url = new URL(urlcheck);
            ConceptsParams.Builder builder = ConceptsParams.newBuilder();
            builder.setUrl(url);
            Concepts concepts = client.concepts(builder.build());
            //System.out.println(concepts.getText());
            List<String> conceptsText = new ArrayList<>();
            concepts.getConcepts().stream().forEach((c) -> {
                conceptsText.add(c.getUri().substring(28).toLowerCase());
            });
            quer=quer.toLowerCase();
            String[] split = quer.split("\\+");
            int cat_count=0;
            for(String s:conceptsText){
                cat_count=0;
                for(String splitStr:split){
                    if(s.contains(splitStr)){
                        cat_query_cnt_ay++;
                        cat_count++;
                    }
                }
                if(cat_count==split.length){
                    cat_query_cnt_ay_whole++;
                }
            }
            EntitiesParams.Builder builderEntities = EntitiesParams.newBuilder();
            builderEntities.setUrl(url);
            Entities entities = client.entities(builderEntities.build());
            List<String> entitiesText = new ArrayList<>();
            entities.getEntities().stream().forEach((c) -> {
                if(c.getType().equalsIgnoreCase("organization")||c.getType().equalsIgnoreCase("location")||c.getType().equalsIgnoreCase("products")||c.getType().equalsIgnoreCase("people")){
                    List<String> surfaceForms = c.getSurfaceForms();
                    surfaceForms.stream().forEach((s) -> {
                        entitiesText.add(s.toLowerCase());
                    });                    
                }
                System.out.println(c.getType());
                entitiesText.add(c.getType());
            });
            for(String splitStr:split){
                entitiesText.stream().forEach((s) -> {
                    if(s.contains(splitStr)){
                        ent_query_cnt_ay++;
                    }
                });
            }
            int ent_count=0;
            for(String s:entitiesText){
                ent_count=0;
                for(String splitStr:split){
                    if(s.contains(splitStr)){
                        ent_query_cnt_ay++;
                        ent_count++;
                    }
                }
                if(ent_count==split.length){
                    ent_query_cnt_ay_whole++;
                }
            }
            
        } catch (MalformedURLException | TextAPIException ex) {
            Logger.getLogger(AylienEntities.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    public int getEnt(){return ent_query_cnt_ay;}
    public int getCat(){return cat_query_cnt_ay;}
    public int getEntWhole(){return ent_query_cnt_ay_whole;}
    public int getCatWhole(){return cat_query_cnt_ay_whole;}
    
}
