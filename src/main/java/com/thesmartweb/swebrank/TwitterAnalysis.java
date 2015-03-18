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
import java.util.*;
import twitter4j.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Class for using Twitter related info through Twitter4j
 * @author Themistoklis Mavridis
 */
public class TwitterAnalysis {
    
    /**
     * Method to get tweets regarding a string 
     * @param query_string the string to search for
     * @param config_path the directory with the twitter api key
     * @return the tweets in a string
     */
    public String perform(String query_string, String config_path){    
        try {
            List<String> twitterkeys = GetKeys(config_path);
            //configuration builder in order to set the keys of twitter
            ConfigurationBuilder cb = new ConfigurationBuilder();
            String consumerkey=twitterkeys.get(0);
            String consumersecret = twitterkeys.get(1);
            String accesstoken = twitterkeys.get(2);
            String accesstokensecret = twitterkeys.get(3);
            cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerkey)
                .setOAuthConsumerSecret(consumersecret )
                .setOAuthAccessToken(accesstoken)
                .setOAuthAccessTokenSecret(accesstokensecret);
            TwitterFactory tf = new TwitterFactory(cb.build());
            AccessToken acc=new AccessToken(accesstoken,accesstokensecret);
            
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
    /**
     * Method to get the twitter keys
     * @param config_path the directory with twitter keys
     * @return the twitter keys in list
     */
     public List<String> GetKeys(String config_path){
        ReadInput ri = new ReadInput();
        List<String> twitterkeysList = ri.GetKeyFile(config_path, "twitterkeys");
        return twitterkeysList;
    }
}
