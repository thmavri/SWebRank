/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import org.apache.commons.lang.StringUtils;

/**
 * Class for the various Sindice functionalities
 * @author Themistoklis Mavridis
 */
public class Sindice {
     int triplecount=0;//the amount of semantic triples 
     boolean[] namespaces;//the semantic namespaces recognized

    /**
     * Method that gets the amount of semantic triples and recognizes the semantic namespaces used
     * @param url the url that we are going to get the stats for
     * @return the amount of semantic triples
     */
    public int getsindicestats(String url){
        try {
            String chk;
            String line="";
            APIconn sind=new APIconn();
            chk=sind.check_conn(url);
            if(chk.equalsIgnoreCase("ok-conn")){
                URL link_ur =new URL("http://api.sindice.com/v2/live?url="+url+"&format=json");
                line=sind.connect(link_ur); 
            }
            if (!line.equalsIgnoreCase("fail")) {
                JSONparsing gg = new JSONparsing();
                String chck="\"o\":";
                triplecount=StringUtils.countMatches(line, chck);
                namespaces=gg.TripleParse(line);
                //triplecount=0;
            }
            return triplecount;
        } catch (IOException ex) {
            Logger.getLogger(Sindice.class.getName()).log(Level.SEVERE, null, ex);
            return triplecount;
        }
     }
}
