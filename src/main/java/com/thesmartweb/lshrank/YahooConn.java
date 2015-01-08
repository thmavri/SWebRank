package com.thesmartweb.lshrank;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author asymeon
 */

import java.io.IOException;  
import java.io.UnsupportedEncodingException;  
import java.net.URLEncoder;  
  
import org.apache.log4j.BasicConfigurator;  
import org.apache.log4j.Logger;  
  
import oauth.signpost.OAuthConsumer;  
import oauth.signpost.basic.DefaultOAuthConsumer;  
  
  
/** 
 * Sample code to use Yahoo! Search BOSS 
 *  
 * Please include the following libraries  
 * 1. Apache Log4j 
 * 2. oAuth Signpost 
 *  
 * @author xyz 
 */  
public class YahooConn {
  
private static final Logger log = Logger.getLogger(YahooConn.class);  
  
protected static String yahooServer = "http://query.yahooapis.com/v1/public/yql";  
  
// Please provide your consumer key here  
private static String consumer_key = "dj0yJmk9SlZGZml5ZzM0WlA5JmQ9WVdrOWVVOHlhMGM0TkhNbWNHbzlPVEUwTnpZMk1qWXkmcz1jb25zdW1lcnNlY3JldCZ4PTU0";  
  
// Please provide your consumer secret here  
private static String consumer_secret = "fa26f181ccff4e8e9977345113c89d83319e2fdf";  
  
/** The HTTP request object used for the connection */  
private static StHttpRequest httpRequest = new StHttpRequest();  
  
/** Encode Format */  
private static final String ENCODE_FORMAT = "UTF-8";  
  
/** Call Type */  
private static final String callType = "web";  
  
private static final int HTTP_STATUS_OK = 200;  
  
/** 
 *  
 * @return 
 */  
public String returnHttpData(String query)   
{  try{
  
  
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
} catch (Exception e) {  
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
 * @param args 
 */  
public String connect(String query) {  
  
  
BasicConfigurator.configure();  
  
try {  
   
YahooConn signPostTest = new YahooConn();  
  
String line=signPostTest.returnHttpData(query);  
return line;
} catch (Exception e) {  
log.info("Error", e);
String line="fail";
return line;
}  
}  
  
} 
