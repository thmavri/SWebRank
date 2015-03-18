/* 
 * Copyright 2015 Themistoklis Mavridis <themis.mavridis@issel.ee.auth.gr>.
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
package com.thesmartweb.swebrank;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;  
import java.io.UnsupportedEncodingException;  
import java.net.URLEncoder; 
import java.util.List;
  
import org.apache.log4j.BasicConfigurator;  
import org.apache.log4j.Logger;  
  
import oauth.signpost.OAuthConsumer;  
import oauth.signpost.basic.DefaultOAuthConsumer;  
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
  
  
/** 
 * Sample code to use Yahoo! Search BOSS 
 *  
 * Please include the following libraries  
 * 1. Apache Log4j 
 * 2. oAuth Signpost 
 *  
 * @author Yahoo! 
 */  
public class YahooConn {
  
private static final Logger log = Logger.getLogger(YahooConn.class);  
  
    /**
     *
     */
protected static String yahooServer = "http://query.yahooapis.com/v1/public/yql";  
  
// Please provide your consumer key here  
private static String consumer_key ;  
  
// Please provide your consumer secret here  
private static String consumer_secret ;  
  
/** The HTTP request object used for the connection */  
private static StHttpRequest httpRequest = new StHttpRequest();  
  
/** Encode Format */  
private static final String ENCODE_FORMAT = "UTF-8";  
  
/** Call Type */  
private static final String callType = "web";  
  
private static final int HTTP_STATUS_OK = 200;  
  
/** 
 * Get the response by yahoo! on query
 * @param query the query to search for
 * @return the yahoo! response
 */  
public String returnHttpData(String query){  
    try{
        if(this.isConsumerKeyExists() && this.isConsumerSecretExists()) {  

            // Start with call Type  
            String params = callType;  

            // Add query  
            params = params.concat("?q="+query);  

            // Encode Query string before concatenating  
            params = params.concat(URLEncoder.encode("", "UTF-8").replace("+", "%20"));  
            params = params.replace(" ", "%20");
            params = params.replace("?", "%20"); 
            // Create final URL  
            String url = yahooServer + params;  

            // Create oAuth Consumer   
            OAuthConsumer consumer = new DefaultOAuthConsumer(consumer_key , consumer_secret);  

            // Set the HTTP request correctly  
            httpRequest.setOAuthConsumer(consumer);  


            //log.info("sending get request to" + URLDecoder.decode(url, ENCODE_FORMAT));  
            int responseCode = httpRequest.sendGetRequest(url);   

            // Send the request  
            if(responseCode == HTTP_STATUS_OK) {  
            //log.info("Response ");  
            } else {  
            log.error("Error in response due to status code = " + responseCode);

            String line="fail";
            return line; 
            }  
            //log.info(httpRequest.getResponseBody()); 
            String line = httpRequest.getResponseBody();
            return line; 

        } else {  
            log.error("Key/Secret does not exist"); 
            String line="fail";
            return line; 
        }  
    }
    catch(UnsupportedEncodingException e) {  
        log.error("Encoding/Decording error"); 
        String line="fail";
        return line; 
    } catch (IOException e) {  
        log.error("Error with HTTP IO", e); 
        String line="fail";
        return line; 
    } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {  
        log.error(httpRequest.getResponseBody(), e);  
        String line="fail";
        return line; 
    }  
}  
  
private String getSearchString() {  
    return "Yahoo";  
}  
  
private boolean isConsumerKeyExists() {  
    if(consumer_key.isEmpty()) {  
        log.error("Consumer Key is missing. Please provide the key");  
        return false;  
    }  
    return true;  
}  
  
private boolean isConsumerSecretExists() {  
    if(consumer_secret.isEmpty()) {  
        log.error("Consumer Secret is missing. Please provide the key");  
        return false;  
    }  
    return true;  
}  
/**
 * Method to connect to Yahoo! Search API
* @param query the query to search for
* @param config_path the path where to find the api keys
* @return  a string with the response
 */  
public String connect(String query, String config_path) {  
    BasicConfigurator.configure();  
    try {
        YahooConn signPostTest = new YahooConn();
        GetKeys(config_path);
        String line=signPostTest.returnHttpData(query);  
        return line;
    } catch (Exception e) {  
        log.info("Error", e);
        String line="fail";
        return line;
    }  
} 
/**
 * Gets the keys for Yahoo!
 * @param config_path  the path to search the api keys
 */
 public void GetKeys(String config_path){
        ReadInput ri = new ReadInput();
        List<String> apikeysList=ri.GetKeyFile(config_path, "yahookeys");
        if(apikeysList.size()==2){
            consumer_key = apikeysList.get(0);
            consumer_secret = apikeysList.get(1);
        }
    }
  
} 
