/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;
import java.util.*;
import twitter4j.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;



/**
 *
 * @author asymeon
 */
public class TwitterAnalysis {
public String perform(String query_string){    
try {
            //configuration builder in order to set the keys of twitter
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                .setOAuthConsumerKey("jVCbXgpQ5f3wslVL7JUMg")
                .setOAuthConsumerSecret("sxKLAYuaIPw8f3qmzlMTHIIxVuFpDBOEALMMFuMwFMM")
                .setOAuthAccessToken("31549851-uUoLosdDwOzM0JuSiAYAyEsONRrcQ6Dpvj01i7zqk")
                .setOAuthAccessTokenSecret("DBnaggNPKXYskl6SdJ2dKIGw9jvtBvbfy6YRdPg0UU");
            TwitterFactory tf = new TwitterFactory(cb.build());
            AccessToken acc=new AccessToken("31549851-uUoLosdDwOzM0JuSiAYAyEsONRrcQ6Dpvj01i7zqk","DBnaggNPKXYskl6SdJ2dKIGw9jvtBvbfy6YRdPg0UU");
            
            Twitter twitter = tf.getInstance(acc);
                   
            //query the twitter
            Query query = new Query(query_string);
            int rpp=100;
            query.count(rpp);
            query.setQuery(query_string);
            
            //----------get the tweets------------
             QueryResult result = twitter.search(query);
             List<Status> tweets = result.getTweets();
             
             RateLimitStatus rls=result.getRateLimitStatus();
             
             String tweet_txt="";
             for (Status tweet : tweets) {
                 tweet_txt=tweet_txt+" "+tweet.getText();
             }
             DataManipulation txtpro = new DataManipulation();
             Stopwords st = new Stopwords();
                tweet_txt=txtpro.removeChars(tweet_txt);
                tweet_txt=st.stop(tweet_txt);
                tweet_txt=txtpro.removeChars(tweet_txt);
             return tweet_txt;
        } 
        catch (TwitterException ex) {
              String tweet_txt="";
            Logger.getLogger(TwitterAnalysis.class.getName()).log(Level.SEVERE, null, ex);
             return tweet_txt="fail";
        }
    }
}
