/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;
import java.net.*;
import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
/**
 *
 * @author Administrator
 */
public class Diffbot {
    public HttpURLConnection httpCon;
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
            BufferedReader rd = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                line = sb.toString();
                rd.close();
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
    public List<String> compute (String[] links,String directory){
        List<String> wordList=null;
        try{
            URL diff_url = null;
            String stringtosplit="";
            for(int i=0;i<links.length;i++){
                if(!(links[i]==null)){
                    diff_url = new URL("http://api.diffbot.com/v2/article?token=da0fe9efcdd79ea32e76278fddf6ab2d&fields=tags,meta&url="+links[i]);
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
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                return wordList;
        } catch (IOException ex) {
            Logger.getLogger(Diffbot.class.getName()).log(Level.SEVERE, null, ex);
            return wordList;
        }
    }
 }
