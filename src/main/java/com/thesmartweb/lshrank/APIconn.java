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
import org.apache.commons.codec.binary.Base64;
import javax.net.ssl.HttpsURLConnection;
/**
 *
 * @author Themis Mavridis
 */
public class APIconn {

    /**
     * variable that is used for http connections
     */
    public HttpURLConnection httpCon;

    /**
     * variable used for https connections
     */
    public HttpsURLConnection httpsCon;

    /**
     * Connects to an ssl url and GETs the response
     * @param link_ur
     * @return the response in a String
     */
    public String sslconnect(URL link_ur) {
        try {
            httpsCon=(HttpsURLConnection) link_ur.openConnection();
            System.out.println(httpsCon.getResponseCode());
            if (httpsCon.getResponseCode() != 200) {
                String line;
                line = "fail";
                return line;
            } else {
                String line;
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpsCon.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                }
                return line;
            } 
        } catch (IOException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            String line="fail";
            return line;
        }
}
    public String sslconnectAylien(URL link_ur) {
        try {
            httpsCon=(HttpsURLConnection) link_ur.openConnection();
            System.out.println(httpsCon.getResponseCode());
            if (httpsCon.getResponseCode() != 200) {
                String line;
                line = "fail";
                return line;
            } else {
                String line;
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpsCon.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                }
                return line;
            } 
        } catch (IOException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            String line="fail";
            return line;
        }
}
    
    
    /**
     * Connects to an http url and GETs the response
     * @param link_ur
     * @return the response in a string
     */
    public String connect(URL link_ur) {
        try {
            httpCon = (HttpURLConnection) link_ur.openConnection();
            if (httpCon.getResponseCode() != 200) {
                String line;
                line = "fail";
                return line;
                // throw new IOException(httpCon.getResponseMessage());
            } else {
                String line;
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpCon.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                }
                return line;
            }
        } catch (IOException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            String line="fail";
            return line;
        }

}

    /**
     * Checks if a connection to a url gets response code 200
     * @param link
     * @return a string that contains "ok-conn" if we have response code 200, "fail-conn" if sth else
     */
    public String check_conn(String link){
        try {
            link=link.trim();
            String line="fail-conn";
            if(link.startsWith("http")){
                URL link_ur=new URL(link);
                line="DNS-error";
                if(link.startsWith("http:")){
                    httpCon = (HttpURLConnection) link_ur.openConnection();
                    httpCon.setDefaultUseCaches(false);
                    httpCon.setReadTimeout(20000);
                    httpCon.setDoInput(true);
                    httpCon.connect();
                    line="fail-conn";
                    try{
                        int responseCode=httpCon.getResponseCode();
                        if (responseCode==200){line="ok-conn";}
                    }
                    catch (Exception e){
                        System.out.println(link);
                        System.gc();
                        System.gc();
                        System.gc();
                        httpCon=null;
                        line="fail-conn";
                        return line;
                    }
                }
                else if (link.startsWith("https")){
                    httpsCon = (HttpsURLConnection) link_ur.openConnection();
                    httpsCon.setDefaultUseCaches(false);
                    httpsCon.setReadTimeout(20000);
                    httpsCon.setDoInput(true);
                    httpsCon.connect();
                    //httpCon.connect();
                    line="fail-conn";
                    try{
                        int responseCode=httpsCon.getResponseCode();
                        if (responseCode==200){line="ok-conn";}
                    }
                    catch (Exception e){
                        System.out.println(link);
                        System.gc();
                        System.gc();
                        System.gc();
                        httpsCon=null;
                        line="fail-conn";
                        return line;
                    }
                }
            }
            return line;
        } catch (MalformedURLException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            return "fail-conn";
        } catch (IOException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            return "fail-conn";
        } 
        
}

    /**
     * It is used to connect to Azure Marketplace for Bing's search API
     * @param link_ur
     * @return the response of the Search API of Bing
     */
    public String azureconnect(URL link_ur, String config_path){
        String string_link_ur=link_ur.toString();
        String line="fail";
        if(string_link_ur.substring(23,28).equalsIgnoreCase("azure")){
            HttpsURLConnection[] httpsConn=new HttpsURLConnection[32];
            //String accountKey = "dvr6F3vxbj/LG4TOWzvrOHWOKP3/vGJAwm1bpMaBg+Y=";
            String[] accKeys = GetBingKeys(config_path);//contains the various bing search api keys
            int i=-1;
            int respp=0;
            do{//we are going to try all the keys
                try {
                    i++;
                    httpsConn[i]=(HttpsURLConnection) link_ur.openConnection();
                    byte[] accountKeyBytes = Base64.encodeBase64((accKeys[i]+":"+accKeys[i]).getBytes());
                    String accountKeyEnc = new String(accountKeyBytes);
                    httpsConn[i].setRequestProperty("Authorization","Basic "+accountKeyEnc);
                    respp=httpsConn[i].getResponseCode();
                } catch (IOException ex) {
                    Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
                    return line;
                }
            }
            while(respp!=200&&i<accKeys.length-1);
            int j=i;
            if(j>0){
                for(int k=0;k<j;k++){
                    httpsConn[k].disconnect();//we close all the other connections
                }
            }
            if (respp != 200){//we are going to capture some significant responses by Bing Search API in order to know our status
                 if(respp!=503){
                     try {
                         String responseMessage = httpsConn[i].getResponseMessage();
                         if(responseMessage.startsWith("Insufficient")){
                             line="insufficient";
                         }
                         if(responseMessage.contains("provided")){
                             line="provided";
                         }
                     } catch (IOException ex) {
                         Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
                         return line;
                     }
                 }
                 return line;
           } 
           else {//if we receive code 200, we are ok to read the response
                BufferedReader rd = null;
                try {
                    rd = new BufferedReader(new InputStreamReader(httpsConn[i].getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    line="";
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                    rd.close();
                    return line;
                } catch (IOException ex) {
                    Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        if(rd!=null){
                            rd.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
                        return line;
                    }
                }
            }                
        }
        return line;
}
    
    public String[] GetBingKeys(String config_path){
        Path input_path=Paths.get(config_path);       
        DataManipulation getfiles=new DataManipulation();//class responsible for the extraction of paths
        Collection<File> inputs_files;//array to include the paths of the txt files
        inputs_files=getfiles.getinputfiles(input_path.toString(),"txt");//method to retrieve all the path of the input documents
        List<String> bingkeysList = new ArrayList<>();
        ReadInput ri = new ReadInput();
        for (File input : inputs_files) {
            System.out.println(input.getName());
            if(input.getName().contains("bingkeys")){
                bingkeysList=ri.GetSEKeys(input);
            }
        }  
        String[] apikeys = bingkeysList.toArray(new String[bingkeysList.size()]);
        return apikeys;
    }

}
