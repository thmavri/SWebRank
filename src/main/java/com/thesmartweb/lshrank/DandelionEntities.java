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

/**
 *
 * @author themis
 */
public class DandelionEntities {
    public static int ent_query_cnt=0;
    public static int cat_query_cnt=0;
    public void connect(String urlcheck, String quer) {  
  
        try {  
            ent_query_cnt=0;
            cat_query_cnt=0;
            int[] output = new int[2];
            String line="";
            String baseUrl = "https://api.dandelion.eu/datatxt/nex/v1?url=";
            String fullUrlStr = baseUrl + URLEncoder.encode(urlcheck, "UTF-8")+"&min_confidence=0.6&include=types%2Ccategories%2Clod";
            fullUrlStr =fullUrlStr +"&$app_id=59b43f94&$app_key=4374ae537a099afdca598c85a5cdaae7";
            URL link_ur = new URL(fullUrlStr);
            //we connect and then check the connection
            APIconn apicon = new APIconn();
            line = apicon.sslconnect(link_ur);
            if(!line.equalsIgnoreCase("fail")){
                JSONparsing jsonParser= new JSONparsing();
                //get the links in an array
                output= jsonParser.DandelionParsing(line, quer);
                ent_query_cnt=output[0];
                cat_query_cnt=output[1];
            }
        } catch (UnsupportedEncodingException | MalformedURLException e) {
        }  
    } 
    public int getEnt(){return ent_query_cnt;}
    public int getCat(){return cat_query_cnt;}
}

  
  

