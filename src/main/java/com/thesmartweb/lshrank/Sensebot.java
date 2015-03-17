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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
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
 * Class to deal with the various functionalities related to Sensebot
 * @author Administrator
 */
 
public class Sensebot {

    
    /**
     * Method that connects to the Sensebot url and gets the document using SAXReader
     * @param link_ur the link to read from
     * @return the response in a string
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
     * Method to get the top sensebot concepts recognized for given links
     * @param links the links to search for
     * @param directory the directory to save the results to
     * @param SensebotConcepts the amount of concepts to search for
     * @param config_path the path to find sensebot's username
     * @return a list with all the top sensebot concepts recognized for the given links
     */
    public List<String> compute (String[] links,String directory,int SensebotConcepts, String config_path){
       List<String> wordList=new ArrayList<>();
       try{
           URL diff_url = null;
           String stringtosplit="";
           String username = GetUserName(config_path);
           for (String link : links) {
               if (!(link == null)) {
                   diff_url = new URL("http://api.sensebot.net/svc/extconcone.asmx/ExtractConcepts?userName="+username+"&numConcepts="+SensebotConcepts+"&artClass=&artLength=0&Lang=English&allURLs=" + link);
                   stringtosplit=connect(diff_url);
                   if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){       
                       stringtosplit=stringtosplit.replaceAll("[\\W&&[^\\s]]", "");
                       if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){
                           String[] tokenizedTerms=stringtosplit.split("\\W+");    //to get individual terms
                           for (String tokenizedTerm : tokenizedTerms) {
                               if (!(tokenizedTerm == null) && (!(tokenizedTerm.equalsIgnoreCase("")))) {
                                   wordList.add(tokenizedTerm);
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
    /**
     * Method to get the userName of sensebot
     * @param config_path the path to find sensebot's username
     * @return Sensebot's username
     */
    public String GetUserName(String config_path){
        Path input_path=Paths.get(config_path);       
        DataManipulation getfiles=new DataManipulation();//class responsible for the extraction of paths
        Collection<File> inputs_files;//array to include the paths of the txt files
        inputs_files=getfiles.getinputfiles(input_path.toString(),"txt");//method to retrieve all the path of the input documents
        List<String> tokenList = new ArrayList<>();
        ReadInput ri = new ReadInput();
        for (File input : inputs_files) {
            if(input.getName().contains("sensebotUsername")){
                tokenList=ri.GetAPICredentials(input);
            }
        }
        if(tokenList.size()>0){
            return tokenList.get(0);
        }
        else{
            String output="";
            return output;
        }
    }
}
