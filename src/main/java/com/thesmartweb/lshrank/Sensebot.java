/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 *
 * @author Administrator
 */
 
public class Sensebot {

    /**
     *
     */
    public HttpURLConnection httpCon;

    /**
     *
     * @param link_ur
     * @return
     */
    public String connect(URL link_ur) {
        try{
            SAXReader reader = new SAXReader();
            Document document = reader.read(link_ur);
            Element root = document.getRootElement();
            List<Node> content = root.content();
            String stringValue="";
            if (!(content.isEmpty())&&content.size()>1){
                Node get = content.get(1);
                stringValue = get.getStringValue();
                DataManipulation tp = new  DataManipulation();
                stringValue=tp.removeChars(stringValue).toLowerCase();
            }
            return stringValue;
        }catch (DocumentException ex) {
                Logger.getLogger(Sensebot.class.getName()).log(Level.SEVERE, null, ex);
                String output="";
                return output;
        } 
    
    }

    /**
     *
     * @param links
     * @param directory
     * @param SensebotConcepts
     * @return
     */
    public List<String> compute (String[] links,String directory,int SensebotConcepts){
       List<String> wordList=new ArrayList<String>();
       try{
           URL diff_url = null;
           String stringtosplit="";
           for(int i=0;i<links.length;i++){           
               if(!(links[i]==null)){
                   diff_url = new URL("http://api.sensebot.net/svc/extconcone.asmx/ExtractConcepts?userName=490b310f-0bdf-4092-afd5-148aa6d95115&numConcepts="+SensebotConcepts+"&artClass=&artLength=0&Lang=English&allURLs="+links[i]);
                   stringtosplit=connect(diff_url);
                   if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){       
                       stringtosplit=stringtosplit.replaceAll("[\\W&&[^\\s]]", "");
                       if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){
                           String[] tokenizedTerms=stringtosplit.split("\\W+");    //to get individual terms
                           for(int j=0;j<tokenizedTerms.length;j++){
                               if(!(tokenizedTerms[j]==null)&&(!(tokenizedTerms[j].equalsIgnoreCase("")))){
                                   wordList.add(tokenizedTerms[j]);
                               }    
                           }
                       }
                   }
               }
           }
           File file_words = new File(directory + "words.txt");
           FileUtils.writeLines(file_words,wordList);
           return wordList;
       }
       catch (MalformedURLException ex) {
           Logger.getLogger(Diffbot.class.getName()).log(Level.SEVERE, null, ex);
           return wordList;
       } catch (IOException ex) {
           Logger.getLogger(Diffbot.class.getName()).log(Level.SEVERE, null, ex);
           return wordList;
       }
   }
}
