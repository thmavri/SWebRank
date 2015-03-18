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

import java.io.BufferedReader;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.net.HttpURLConnection;  
import java.net.MalformedURLException;  
import java.net.URL; 
import org.apache.log4j.Logger;  
import oauth.signpost.OAuthConsumer;  
import oauth.signpost.exception.OAuthCommunicationException;  
import oauth.signpost.exception.OAuthExpectationFailedException;  
import oauth.signpost.exception.OAuthMessageSignerException;  
  
  
/** 
 * Simple HTTP Request implementation used for Yahoo! Search API
 * @author David Hardtke 
 * 
 */  
public class StHttpRequest {  
  
private static final Logger log = Logger.getLogger(StHttpRequest.class);  
  
    private String responseBody = "";  
     
    private OAuthConsumer consumer = null;  
  
    /** Default Constructor */  
    public StHttpRequest() { }  
     
   
    public StHttpRequest(OAuthConsumer consumer) {  
        this.consumer = consumer;  
    }  
  
    
    public HttpURLConnection getConnection(String url)   
    throws IOException,  
        OAuthMessageSignerException,  
        OAuthExpectationFailedException,   
        OAuthCommunicationException  
    {  
     try {  
             URL u = new URL(url);  
  
             HttpURLConnection uc = (HttpURLConnection) u.openConnection();  
               
             if (consumer != null) {  
                 try {  
                     //log.info("Signing the oAuth consumer");  
                     consumer.sign(uc);  
                       
                 } catch (OAuthMessageSignerException e) {  
                     log.error("Error signing the consumer", e);  
                     throw e;  
  
                 } catch (OAuthExpectationFailedException e) {  
                 log.error("Error signing the consumer", e);  
                 throw e;  
                   
                 } catch (OAuthCommunicationException e) {  
                 log.error("Error signing the consumer", e);  
                 throw e;  
                 }  
                 uc.connect();  
             }  
             return uc;  
     } catch (IOException e) {  
     log.error("Error signing the consumer", e);  
     throw e;  
     }  
    }  
      
   
    public int sendGetRequest(String url)   
    throws IOException,  
    OAuthMessageSignerException,  
    OAuthExpectationFailedException,   
    OAuthCommunicationException {  
      
      
        int responseCode = 500;  
        try {  
            HttpURLConnection uc = getConnection(url);  
              
            responseCode = uc.getResponseCode();  
              
            if(200 == responseCode || 401 == responseCode || 404 == responseCode){  
                BufferedReader rd = new BufferedReader(new InputStreamReader(responseCode==200?uc.getInputStream():uc.getErrorStream()));  
                StringBuffer sb = new StringBuffer();  
                String line;  
                while ((line = rd.readLine()) != null) {  
                    sb.append(line);  
                }  
                rd.close();  
                setResponseBody(sb.toString());  
            }  
         } catch (MalformedURLException ex) {  
            throw new IOException( url + " is not valid");  
        } catch (IOException ie) {  
            throw new IOException("IO Exception " + ie.getMessage());  
        }  
        return responseCode;  
    }  
  
  
    public String getResponseBody() {  
        return responseBody;  
    }  
  
    public void setResponseBody(String responseBody) {  
        if (null != responseBody) {  
            this.responseBody = responseBody;  
        }  
    }  
     
    
    public void setOAuthConsumer(OAuthConsumer consumer) {  
        this.consumer = consumer;  
    }  
}