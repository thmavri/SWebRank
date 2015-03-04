/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;
import java.net.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
/**
 *
 * @author Administrator
 */
public class Diffbot {

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
            String line="";
            httpCon = (HttpURLConnection) link_ur.openConnection();
            if (httpCon.getResponseCode() != 200) {
                line = "fail";
                return line;
                // throw new IOException(httpCon.getResponseMessage());
            }
            else{
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpCon.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                }
                JSONparsing jp=new JSONparsing();
                String output=jp.DiffbotParsing(line);
                return output;
            }
        }
        catch (IOException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            String line="fail";
            return line;
        }
    
    }

    /**
     *
     * @param links
     * @param directory
     * @param config_path
     * @return
     */
    public List<String> compute (String[] links,String directory, String config_path){
        List<String> wordList=null;
        try{
            URL diff_url = null;
            String stringtosplit="";
            String token = GetToken(config_path);
            for (String link : links) {
                if (!(link == null)) {
                    diff_url = new URL("http://api.diffbot.com/v2/article?token="+token+"&fields=tags,meta&url=" + link);
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
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                return wordList;
        } catch (IOException ex) {
            Logger.getLogger(Diffbot.class.getName()).log(Level.SEVERE, null, ex);
            return wordList;
        }
    }
    public String GetToken(String config_path){
        Path input_path=Paths.get(config_path);       
        DataManipulation getfiles=new DataManipulation();//class responsible for the extraction of paths
        Collection<File> inputs_files;//array to include the paths of the txt files
        inputs_files=getfiles.getinputfiles(input_path.toString(),"txt");//method to retrieve all the path of the input documents
        List<String> tokenList = new ArrayList<>();
        ReadInput ri = new ReadInput();
        for (File input : inputs_files) {
            if(input.getName().contains("diffbottoken")){
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
