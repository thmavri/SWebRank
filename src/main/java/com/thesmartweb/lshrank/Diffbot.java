/* 
 * Copyright 2015 themis.
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
 * class for diffbot article apiu usage
 * @author Themistoklis Mavridis
 */
public class Diffbot {

    /**
     * url connection
     */
    public HttpURLConnection httpCon;

    /**
     * Method to get the words recognized by Diffbot as important in given urls
     * @param links the urls to analyzes
     * @param directory the directory to save the output
     * @param config_path the configuration path to get the diffbot key
     * @return a list of the words
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
                    APIconn apiconn = new APIconn();
                    String line = apiconn.connect(diff_url);
                    JSONparsing jp=new JSONparsing();
                    stringtosplit=jp.DiffbotParsing(line);
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
    /**
     * Method to the token of diffbot
     * @param config_path the configuration path to get the diffbot key
     * @return the token in a string
     */
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
