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
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Class for analysis of all the queries through Search APIs and capturing of the result statistics
 * @author Themistoklis Mavridis
 */
public class Search_analysis {
    /**
     * Method to perform the queries to the search engines, get the links and get all the webpage and semantic stats for the links
     * @param iteration_counter The iteration number of the algorithm (to use it in the id for elasticsearch)
     * @param directory_save The directory we are going to several files
     * @param domain The domain that we are searching for (to use it in the id for elasticsearch)
     * @param enginechoice The search engines that were chosen to be used
     * @param quer the query we search for
     * @param results_number the results number that we are going to get from every search engine
     * @param top_visible the number of results if we use Visibility score
     * @param SWebRankSettings the settings for LDA and SwebRank in general (check the ReadInput Class)
     * @param alpha alpha value of LDA
     * @param mozMetrics the metrics of choice if Moz is going to be used
     * @param top_count_moz the amount of results if we use Moz
     * @param moz_threshold_option flag to show if we are going to use a threshold in Moz metrics or not
     * @param moz_threshold the moz threshold value
     * @param ContentSemantics get the choice of Content Semantic Analysis algorithm that we are going to use
     * @param SensebotConcepts the amount of concepts to be recognized if Sensebot is used
     * @param config_path the configuration path to get all the api keys
     * @return a list with the words recognized as important by the content semantic analysis algorithm we have chosen 
     */
    public List<String> perform(int iteration_counter,String directory_save, String domain, List<Boolean> enginechoice, String quer, int results_number, int top_visible,List<Double> SWebRankSettings,double alpha, List<Boolean> mozMetrics, int top_count_moz, boolean moz_threshold_option,double moz_threshold, List<Boolean> ContentSemantics, int SensebotConcepts, String config_path){ 
        //=======connect to mysql=========
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            ReadInput ri = new ReadInput();
            List<String> mysqlAdminSettings= ri.GetKeyFile(config_path, "mysqlAdmin");
            String port = mysqlAdminSettings.get(2);
            String dbname = mysqlAdminSettings.get(3);
            String url = "jdbc:mysql://localhost:"+port+"/"+dbname+"?zeroDateTimeBehavior=convertToNull";
            String user = mysqlAdminSettings.get(0);
            String password = mysqlAdminSettings.get(1);
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(url,user,password);
            LinksParseAnalysis ld=new LinksParseAnalysis();
            //we create the array that are going to store the results from each search engine
            String[] links_google=new String[results_number];
            String[] links_yahoo=new String[results_number];
            String[] links_bing=new String[results_number];
            //we create the array that is going to store all the results from all the search engines together
            String[] links_total=new String[(results_number*3)];
            //--------if we have selected to use a Moz metric, then we should set the links_total to be of size of top_count_seomoz*3 since it means that the results_number has been set to its max value (50)
            if(mozMetrics.get(0)){
                links_total=new String[(top_count_moz)*3];
            }
            int[] nlinks=new int[2];
            if(enginechoice.get(0)){
                //get bing results
                BingResults br = new BingResults();
                links_bing=br.Get(quer, results_number, directory_save,config_path);                     
            }
            if(enginechoice.get(1)){
                //get google results
                GoogleResults gr = new GoogleResults();
                links_google=gr.Get(quer,results_number,directory_save,config_path);
            }
            if(enginechoice.get(2)){
                //get yahoo results
                YahooResults yr = new YahooResults();
                links_yahoo=yr.Get(quer,results_number,directory_save,config_path);                    
            }
            HashMap<Integer,List<String>> EntitiesMapDBP = new HashMap<>();
            HashMap<Integer,List<String>> CategoriesMapDBP = new HashMap<>();
            HashMap<Integer,List<String>> EntitiesMapDand = new HashMap<>();
            HashMap<Integer,List<String>> CategoriesMapDand = new HashMap<>();
            HashMap<Integer,List<String>> EntitiesMapYahoo = new HashMap<>();
            HashMap<Integer,List<String>> CategoriesMapYahoo = new HashMap<>();
            HashMap<Integer,String> parseOutputList = new HashMap<>();
            for(int i=0;i<results_number*3;i++){
                parseOutputList.put(i,"");
            }
            //*************
            boolean false_flag=true;
            if(false_flag){
                if(mozMetrics.get(0)){
                    //we check if moz works
                    Moz moz=new Moz();
                    boolean checkmoz=moz.check(config_path);
                    if(checkmoz){
                         //perform 
                         if(links_yahoo.length>0){
                             links_yahoo=moz.perform(links_yahoo,top_count_moz,moz_threshold,moz_threshold_option,mozMetrics, config_path);
                         }
                         if(links_google.length>0){
                             links_google=moz.perform(links_google,top_count_moz,moz_threshold,moz_threshold_option,mozMetrics, config_path);
                         }
                         if(links_bing.length>0){
                             links_bing=moz.perform(links_bing,top_count_moz,moz_threshold,moz_threshold_option,mozMetrics, config_path);
                         }
                    }
                }
                //we are creating Sindice class in order to get the number of semantic triples of a webpage
                Sindice striple=new Sindice();
                //create htmlparser to get the number of links in a webpage
                if(mozMetrics.get(0)){
                    results_number=links_yahoo.length;
                }                                 
                WebParser htm=new WebParser();
                //create an array that contains all the links together
                for(int i=0;i<3;i++){
                    try{
                        if(i==0){System.arraycopy(links_yahoo, 0, links_total, 0, results_number);}
                        if(i==1){System.arraycopy(links_google, 0, links_total, links_yahoo.length, results_number);}
                        if(i==2){System.arraycopy(links_bing, 0, links_total,((links_yahoo.length)+(links_google.length)), results_number);}
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                        ArrayList<String> finalList = new ArrayList<String>();
                        return finalList;
                    } 
                }
                //merged true => visibility score
                if(enginechoice.get(3)){
                    VisibilityScore vb=new VisibilityScore();//we have a merged engine
                    //erase using vb.perform all the duplicate links
                    links_total=vb.perform(links_google, links_yahoo, links_bing);
                    //if we have Moz option set to true we have to get the results rearranged according to the moz metric selected
                    if(mozMetrics.get(0)){
                        Moz checkMoz=new Moz();
                        boolean check_seo=checkMoz.check(config_path);
                        if (check_seo){
                            Moz MOZ=new Moz();                      
                            links_total=MOZ.perform(links_total,top_count_moz,moz_threshold,moz_threshold_option,mozMetrics, config_path);
                        }
                    }
                    //here we calculate the visibility score
                    links_total=vb.visibility_score(links_total, links_yahoo, links_bing, links_google, top_visible);
                }
                String[][] total_catent= new String[links_total.length][2];
                for(int r=0;r<total_catent.length;r++){
                    total_catent[r][0]="";
                    total_catent[r][1]="";
                }
                for(int j=0;j<links_total.length;j++){
                    if(links_total[j]!=null){
                        String urlString=links_total[j];
                        if(urlString.length()>199){
                            urlString=links_total[j].substring(0, 198);
                        }
                        int rank=-1;
                        int engine=-1;//0 for yahoo,1 for google,2 for bing
                        if(j<results_number){
                            rank=j;
                            engine=0;
                        }
                        else if(j<results_number*2){
                            rank=j-results_number;
                            engine=1;
                        }
                        else if(j<results_number*3){
                            rank=j-results_number*2;
                            engine=2;
                        }
                        try{
                            //we initialize the row in settings table
                            conn = DriverManager.getConnection(url,user,password);
                            stmt = conn.prepareStatement("INSERT INTO SETTINGS (url,query,search_engine,search_engine_rank,domain) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine),domain=VALUES(domain)");
                            stmt.setString(1,urlString);
                            stmt.setString(2,quer);
                            stmt.setInt(3,engine);
                            stmt.setInt(4,rank);
                            stmt.setString(5,domain);
                            stmt.executeUpdate();
                        }
                        finally{
                            try {
                                if (stmt != null) stmt.close();
                                if (conn != null) conn.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        try{
                            //we initialize the row in semantic stats table 
                            conn = DriverManager.getConnection(url,user,password);
                            stmt = conn.prepareStatement("INSERT INTO SEMANTICSTATS (url,query,search_engine,search_engine_rank,domain) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine),domain=VALUES(domain)");
                            stmt.setString(1,urlString);
                            stmt.setString(2,quer);
                            stmt.setInt(3,engine);
                            stmt.setInt(4,rank);
                            stmt.setString(5,domain);
                            stmt.executeUpdate();
                        }
                        finally{
                            try {
                                if (stmt != null) stmt.close();
                                if (conn != null) conn.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        try{
                            //we initialize the row in namespaces stats table
                            conn = DriverManager.getConnection(url,user,password);
                            stmt = conn.prepareStatement("INSERT INTO NAMESPACESSTATS (url,query,search_engine,search_engine_rank,domain) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine),domain=VALUES(domain)");
                            stmt.setString(1,urlString);
                            stmt.setString(2,quer);
                            stmt.setInt(3,engine);
                            stmt.setInt(4,rank);
                            stmt.setString(5,domain);
                            stmt.executeUpdate();
                        }
                        finally{
                            try {
                                if (stmt != null) stmt.close();
                                if (conn != null) conn.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        try{
                            //we put the info inside the settings 
                            conn = DriverManager.getConnection(url,user,password);
                            StringBuilder settingsStmBuild = new StringBuilder();
                            settingsStmBuild.append("UPDATE SETTINGS SET ");
                            settingsStmBuild.append("`nTopics`=? , ");
                            settingsStmBuild.append("`alpha`=? , ");
                            settingsStmBuild.append("`beta`=? , ");
                            settingsStmBuild.append("`niters`=? , ");
                            settingsStmBuild.append("`prob_threshold`=? , ");
                            settingsStmBuild.append("`moz`=? , ");
                            settingsStmBuild.append("`top_count_moz`=? , ");
                            settingsStmBuild.append("`moz_threshold`=? , ");
                            settingsStmBuild.append("`moz_threshold_option`=? , ");
                            settingsStmBuild.append("`top_visible`=? , ");
                            settingsStmBuild.append("`Domain_Authority`=? , ");
                            settingsStmBuild.append("`External_MozRank`=?  , ");
                            settingsStmBuild.append("`MozRank`=?  , ");
                            settingsStmBuild.append("`MozTrust`=? , ");
                            settingsStmBuild.append("`Page_Authority`=? , ");
                            settingsStmBuild.append("`Subdomain_mozRank`=? , "); 
                            settingsStmBuild.append("`merged`=? , "); 
                            settingsStmBuild.append("`results_number`=? , "); 
                            settingsStmBuild.append("`Diffbotflag`=?  , "); 
                            settingsStmBuild.append("`LDAflag`=? , "); 
                            settingsStmBuild.append("`Sensebotflag`=? , "); 
                            settingsStmBuild.append("`TFIDFflag`=? , "); 
                            settingsStmBuild.append("`SensebotConcepts`=? , ");
                            settingsStmBuild.append("`nTopTopics`=? , ");
                            settingsStmBuild.append("`combinelimit`=? ,");
                            settingsStmBuild.append("`newtermstocombine`=? ,");
                            settingsStmBuild.append("`newqueriesmax`=? ,");
                            settingsStmBuild.append("`ngdthreshold`=? ,");
                            settingsStmBuild.append("`entitiesconfi`=? ,");
                            settingsStmBuild.append("`dbpediasup`=? ");
                            settingsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");

                            stmt = conn.prepareStatement(settingsStmBuild.toString());
                            stmt.setInt(1,SWebRankSettings.get(1).intValue());
                            stmt.setDouble(2,alpha);
                            stmt.setDouble(3,SWebRankSettings.get(0));
                            stmt.setInt(4,SWebRankSettings.get(2).intValue());
                            stmt.setDouble(5,SWebRankSettings.get(3));
                            stmt.setBoolean(6,mozMetrics.get(0));
                            stmt.setInt(7,top_count_moz);
                            stmt.setDouble(8,moz_threshold);
                            stmt.setBoolean(9,moz_threshold_option);
                            stmt.setInt(10,top_visible);
                            stmt.setBoolean(11,mozMetrics.get(1));
                            stmt.setBoolean(12,mozMetrics.get(2));
                            stmt.setBoolean(13,mozMetrics.get(3));
                            stmt.setBoolean(14,mozMetrics.get(4));
                            stmt.setBoolean(15,mozMetrics.get(5));
                            stmt.setBoolean(16,mozMetrics.get(6));
                            stmt.setBoolean(17,enginechoice.get(3));
                            stmt.setInt(18,results_number);
                            stmt.setBoolean(19,ContentSemantics.get(0));
                            stmt.setBoolean(20,ContentSemantics.get(1));
                            stmt.setBoolean(21,ContentSemantics.get(2));
                            stmt.setBoolean(22,ContentSemantics.get(3));
                            stmt.setInt(23,SensebotConcepts);
                            stmt.setInt(24,SWebRankSettings.get(11).intValue());
                            stmt.setInt(25,SWebRankSettings.get(7).intValue());
                            stmt.setInt(26,SWebRankSettings.get(9).intValue());
                            stmt.setInt(27,SWebRankSettings.get(10).intValue());
                            stmt.setDouble(28,SWebRankSettings.get(6));
                            stmt.setDouble(29,SWebRankSettings.get(12));
                            stmt.setDouble(30,SWebRankSettings.get(13));
                            stmt.setString(31,urlString);
                            stmt.setString(32,quer);
                            stmt.setInt(33,engine);
                            stmt.setString(34,domain);
                            stmt.executeUpdate();
                        }
                        finally{
                            try {
                                if (stmt != null) stmt.close();
                                if (conn != null) conn.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if(htm.checkconn(links_total[j])){//if we can connect to the url we continue to update semantics stats and namespaces stats tables
                            nlinks=htm.getnlinks(links_total[j]);
                            StringBuilder webstatsStmBuild = new StringBuilder();
                            try{
                                conn = DriverManager.getConnection(url,user,password);
                                webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                                webstatsStmBuild.append("`number_links`=? , ");
                                webstatsStmBuild.append("`redirect_links`=? , ");
                                webstatsStmBuild.append("`internal_links`=? ");
                                webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(webstatsStmBuild.toString());
                                stmt.setInt(1,nlinks[0]);//total numbers of links
                                stmt.setInt(2,nlinks[0]-nlinks[1]);
                                stmt.setInt(3,nlinks[1]);//internal links
                                stmt.setString(4,urlString);
                                stmt.setString(5,quer);
                                stmt.setInt(6,engine);
                                stmt.setString(7,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try{
                                conn = DriverManager.getConnection(url,user,password);
                                System.out.println("I am going to get the stats from Sindice\n");
                                int ntriples=striple.getsindicestats(links_total[j]);//get the amount of semantic triples using Sindice API
                                System.out.println("I am going insert the semantic triples number in the DB\n");
                                stmt = conn.prepareStatement("UPDATE SEMANTICSTATS SET `total_semantic_triples`=? WHERE `url` =? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setInt(1,ntriples);
                                stmt.setString(2,urlString);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                                System.out.println("I inserted the semantic triples number in the DB\n");
                                //---namespaces-----
                                System.out.println("I am going to insert the namespaces in the DB\n");
                                }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            if(striple.namespaces[0]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/vocab/bio/0.1/` = ?  WHERE `url` = ? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[1]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/dc/elements/1.1/` =? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[2]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/coo/n` = ? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[3]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://web.resource.org/cc/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[4]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://diligentarguont.ontoware.org/2005/10/arguonto`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[5]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://usefulinc.com/ns/doap`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[6]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://xmlns.com/foaf/0.1/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[7]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/goodrelations/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[8]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/muto/core`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[9]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://webns.net/mvcb/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[10]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/ontology/mo/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[11]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/innovation/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[12]){
                                try{    
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://openguid.net/rdf`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[13]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.slamka.cz/ontologies/diagnostika.owl`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }   
                            }
                            if(striple.namespaces[14]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/ontology/po/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }   
                            if(striple.namespaces[15]){
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/net/provenance/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }   
                            }
                            if(striple.namespaces[16]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/rss/1.0/modules/syndication`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[17]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://rdfs.org/sioc/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[18]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://madskills.com/public/xml/rss/module/trackback/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[19]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://rdfs.org/ns/void`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[20]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.fzi.de/2008/wise/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[21]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://xmlns.com/wot/0.1`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[22]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.w3.org/1999/02/22-rdf-syntax-ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[23]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `rdf-schema`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[24]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `XMLschema`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[25]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `OWL`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[26]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/dc/terms/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[27]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `VCARD`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if(striple.namespaces[28]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.geonames.org/ontology`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[29]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://search.yahoo.com/searchmonkey/commerce/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[30]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://search.yahoo.com/searchmonkey/media/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[31]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://cb.semsol.org/ns#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[32]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://blogs.yandex.ru/schema/foaf/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[33]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.w3.org/2003/01/geo/wgs84_pos#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[34]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://rdfs.org/sioc/ns#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[35]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://rdfs.org/sioc/types#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[36]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://smw.ontoware.org/2005/smw#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[37]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/rss/1.0/`= ? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,urlString);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            if(striple.namespaces[38]){
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.w3.org/2004/12/q/contentlabel#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                    stmt.setBoolean(1,true);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    stmt.setInt(4,engine);
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            System.out.println("I inserted the namespaces in the DB\n");
                            System.out.println("I will get the semantic entities and categories\n");
                            //get the semantic entities and categories from Yahoo Content Analysis Service
                            YahooEntityCategory yec=new YahooEntityCategory();
                            yec.connect(links_total[j],quer, false,SWebRankSettings.get(12));//without stemming
                            EntitiesMapYahoo.put(j, yec.GetEntitiesYahoo());
                            CategoriesMapYahoo.put(j, yec.GetCategoriesYahoo());
                            double ent_avg_yahoo_score = yec.GetEntitiesYahooScore();
                            double cat_avg_yahoo_score = yec.GetCategoriesYahooScore();
                            int cat_cnt=yec.GetCatQuerCnt();
                            int ent_cnt=yec.GetEntQuerCnt();
                            int cat_cnt_whole=yec.GetCatQuerCntWhole();
                            int ent_cnt_whole=yec.GetEntQuerCntWhole();
                            yec.connect(links_total[j],quer, true,SWebRankSettings.get(12));//with stemming
                            int cat_cnt_stem=yec.GetCatQuerCnt();
                            int ent_cnt_stem=yec.GetEntQuerCnt();
                            int cat_cnt_whole_stem=yec.GetCatQuerCntWhole();
                            int ent_cnt_whole_stem=yec.GetEntQuerCntWhole();
                            //get the semantic entities and categories from Dandelion Named entity extraction API
                            DandelionEntities dec = new DandelionEntities();
                            dec.connect(links_total[j], quer,false,config_path,SWebRankSettings.get(12));//without stemming
                            EntitiesMapDand.put(j, dec.GetEntitiesDand());
                            CategoriesMapDand.put(j, dec.GetCategoriesDand());
                            double ent_avg_d_score = dec.GetEntitiesScoreDand();
                            int cat_cnt_dand=dec.getCat();
                            int ent_cnt_dand=dec.getEnt();
                            int cat_cnt_dand_whole=dec.getCatWhole();
                            int ent_cnt_dand_whole=dec.getEntWhole();
                            dec.connect(links_total[j], quer,true,config_path,SWebRankSettings.get(12));//with stemming
                            int cat_cnt_dand_stem=dec.getCat();
                            int ent_cnt_dand_stem=dec.getEnt();
                            int cat_cnt_dand_whole_stem=dec.getCatWhole();
                            int ent_cnt_dand_whole_stem=dec.getEntWhole();
                            //get the semantic entities and categories from dbpedia spotlight
                            DBpediaSpotlightClient dbpspot = new DBpediaSpotlightClient(SWebRankSettings.get(12),SWebRankSettings.get(13).intValue());
                            dbpspot.countEntCat(links_total[j], quer,false);//false is not stemming
                            EntitiesMapDBP.put(j, dbpspot.getEntities());
                            CategoriesMapDBP.put(j, dbpspot.getCategories());
                            double ent_avg_dbpspot_score = dbpspot.getEntitiesAvgScore();
                            double ent_max_dbpspot_score = dbpspot.getEntitiesMaxScore();
                            double ent_min_dbpspot_score = dbpspot.getEntitiesMinScore();
                            double ent_median_dbpspot_score = dbpspot.getEntitiesMedianScore();
                            double ent_std_dbpspot_score = dbpspot.getEntitiesStdScore();
                            double ent_avg_dbpspot_support = dbpspot.getEntitiesAvgSupport();
                            double ent_max_dbpspot_support = dbpspot.getEntitiesMaxSupport();
                            double ent_min_dbpspot_support = dbpspot.getEntitiesMinSupport();
                            double ent_median_dbpspot_support = dbpspot.getEntitiesMedianSupport();
                            double ent_std_dbpspot_support = dbpspot.getEntitiesStdSupport();
                            double ent_avg_dbpspot_dif = dbpspot.getEntitiesAvgDif();
                            double ent_max_dbpspot_dif = dbpspot.getEntitiesMaxDif();
                            double ent_min_dbpspot_dif = dbpspot.getEntitiesMinDif();
                            double ent_median_dbpspot_dif = dbpspot.getEntitiesMedianDif();
                            double ent_std_dbpspot_dif = dbpspot.getEntitiesStdDif();
                            double unique_ent_cnt_dbpspot = dbpspot.getUniqueEntCnt();
                            double unique_ent_scoreSum_dbpspot = dbpspot.getUniqueEntScoreSum();
                            int cat_cnt_dbpspot = dbpspot.getcountCat();
                            int ent_cnt_dbpspot = dbpspot.getcountEnt();
                            int cat_cnt_dbpspot_whole = dbpspot.getcountCatWhole();
                            int ent_cnt_dbpspot_whole = dbpspot.getcountEntWhole();
                            double ent_sup_cnt_dbpspot = dbpspot.getcountSupEnt();
                            double ent_sim_cnt_dbpspot = dbpspot.getcountSimEnt();
                            double ent_dif_cnt_dbpspot = dbpspot.getcountDifEnt();
                            double high_precision_content_dbpspot = dbpspot.getHighPrecEntities();
                            dbpspot.countEntCat(links_total[j], quer,true);//true is for stemming
                            int cat_cnt_dbpspot_stem = dbpspot.getcountCat();
                            int ent_cnt_dbpspot_stem = dbpspot.getcountEnt();
                            int cat_cnt_dbpspot_whole_stem = dbpspot.getcountCatWhole();
                            int ent_cnt_dbpspot_whole_stem = dbpspot.getcountEntWhole();
                            double ent_sup_cnt_dbpspot_stem = dbpspot.getcountSupEnt();
                            double ent_sim_cnt_dbpspot_stem = dbpspot.getcountSimEnt();
                            double ent_dif_cnt_dbpspot_stem = dbpspot.getcountDifEnt();
                            System.out.println("I insert the semantic entities and categories stats in the DB\n");
                            StringBuilder entitiesStatementBuilder = new StringBuilder();
                            try{
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`ent_avg_y_score`=?,");
                                entitiesStatementBuilder.append("`cat_avg_y_score`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                conn = DriverManager.getConnection(url,user,password);
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setDouble(1,ent_avg_yahoo_score);
                                stmt.setDouble(2,cat_avg_yahoo_score);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try{
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`ent_avg_dand_score`=?,");
                                entitiesStatementBuilder.append("`ent_avg_dbpspot_score`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                conn = DriverManager.getConnection(url,user,password);
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setDouble(1,ent_avg_d_score);
                                stmt.setDouble(2,ent_avg_dbpspot_score);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try{
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`ent_max_dbpspot_score`=?,");
                                entitiesStatementBuilder.append("`ent_min_dbpspot_score`=?,");
                                entitiesStatementBuilder.append("`ent_median_dbpspot_score`=?,");
                                entitiesStatementBuilder.append("`ent_std_dbpspot_score`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                conn = DriverManager.getConnection(url,user,password);
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setDouble(1,ent_max_dbpspot_score);
                                stmt.setDouble(2,ent_min_dbpspot_score);
                                stmt.setDouble(3,ent_median_dbpspot_score);
                                stmt.setDouble(4,ent_std_dbpspot_score);
                                stmt.setString(5,links_total[j]);
                                stmt.setString(6,quer);
                                if(j<results_number){
                                    stmt.setInt(7,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(7,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(7,2);//2 for bing
                                }
                                stmt.setString(8,domain);
                                stmt.executeUpdate();
                            }
                            catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try{
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`ent_avg_dbpspot_support`=?,");
                                entitiesStatementBuilder.append("`ent_max_dbpspot_support`=?,");
                                entitiesStatementBuilder.append("`ent_min_dbpspot_support`=?,");
                                entitiesStatementBuilder.append("`ent_median_dbpspot_support`=?,");
                                entitiesStatementBuilder.append("`ent_std_dbpspot_support`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                conn = DriverManager.getConnection(url,user,password);
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setDouble(1,ent_avg_dbpspot_support);
                                stmt.setDouble(2,ent_max_dbpspot_support);
                                stmt.setDouble(3,ent_min_dbpspot_support);
                                stmt.setDouble(4,ent_median_dbpspot_support);
                                stmt.setDouble(5,ent_std_dbpspot_support);
                                stmt.setString(6,links_total[j]);
                                stmt.setString(7,quer);
                                if(j<results_number){
                                    stmt.setInt(8,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(8,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(8,2);//2 for bing
                                }
                                stmt.setString(9,domain);
                                System.out.println("avg db support"+ent_avg_dbpspot_support);
                                stmt.executeUpdate();
                            }
                            catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try{
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`ent_avg_dbpspot_dif`=?,");
                                entitiesStatementBuilder.append("`ent_max_dbpspot_dif`=?,");
                                entitiesStatementBuilder.append("`ent_min_dbpspot_dif`=?,");
                                entitiesStatementBuilder.append("`ent_median_dbpspot_dif`=?,");
                                entitiesStatementBuilder.append("`ent_std_dbpspot_dif`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                conn = DriverManager.getConnection(url,user,password);
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setDouble(1,ent_avg_dbpspot_dif);
                                stmt.setDouble(2,ent_max_dbpspot_dif);
                                stmt.setDouble(3,ent_min_dbpspot_dif);
                                stmt.setDouble(4,ent_median_dbpspot_dif);
                                stmt.setDouble(5,ent_std_dbpspot_dif);
                                stmt.setString(6,links_total[j]);
                                stmt.setString(7,quer);
                                if(j<results_number){
                                    stmt.setInt(8,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(8,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(8,2);//2 for bing
                                }
                                stmt.setString(9,domain);
                                stmt.executeUpdate();
                            }
                            catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`ent_sup_cnt_dbpspot`=?,");
                                entitiesStatementBuilder.append("`ent_dif_cnt_dbpspot`=?,");
                                entitiesStatementBuilder.append("`ent_sim_cnt_dbpspot`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setDouble(1,ent_sup_cnt_dbpspot);
                                stmt.setDouble(2,ent_dif_cnt_dbpspot);
                                stmt.setDouble(3,ent_sim_cnt_dbpspot);
                                stmt.setString(4,links_total[j]);
                                stmt.setString(5,quer);
                                if(j<results_number){
                                    stmt.setInt(6,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(6,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(6,2);//2 for bing
                                }
                                stmt.setString(7,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`ent_sup_cnt_dbpspot_stem`=?,");
                                entitiesStatementBuilder.append("`ent_dif_cnt_dbpspot_stem`=?,");
                                entitiesStatementBuilder.append("`ent_sim_cnt_dbpspot_stem`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setDouble(1,ent_sup_cnt_dbpspot_stem);
                                stmt.setDouble(2,ent_dif_cnt_dbpspot_stem);
                                stmt.setDouble(3,ent_sim_cnt_dbpspot_stem);
                                stmt.setString(4,links_total[j]);
                                stmt.setString(5,quer);
                                if(j<results_number){
                                    stmt.setInt(6,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(6,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(6,2);//2 for bing
                                }
                                stmt.setString(7,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`unique_ent_cnt_dbpspot`=?,");
                                entitiesStatementBuilder.append("`unique_ent_scoreSum_dbpspot`=?,");
                                entitiesStatementBuilder.append("`high_precision_content_dbpspot`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setDouble(1,unique_ent_cnt_dbpspot);
                                stmt.setDouble(2,unique_ent_scoreSum_dbpspot);
                                stmt.setDouble(3,high_precision_content_dbpspot);
                                stmt.setString(4,links_total[j]);
                                stmt.setString(5,quer);
                                if(j<results_number){
                                    stmt.setInt(6,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(6,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(6,2);//2 for bing
                                }
                                stmt.setString(7,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try{
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_Y`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_Y`=?,");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_Y_W`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                conn = DriverManager.getConnection(url,user,password);
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt);
                                stmt.setInt(2,ent_cnt);
                                stmt.setInt(3,cat_cnt_whole);
                                stmt.setString(4,urlString);
                                stmt.setString(5,quer);
                                if(j<results_number){
                                    stmt.setInt(6,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(6,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(6,2);//2 for bing
                                }
                                stmt.setString(7,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_Y_W`=?,");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_D`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_D`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,ent_cnt_whole);
                                stmt.setInt(2,cat_cnt_dand);
                                stmt.setInt(3,ent_cnt_dand);
                                stmt.setString(4,urlString);
                                stmt.setString(5,quer);
                                if(j<results_number){
                                    stmt.setInt(6,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(6,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(6,2);//2 for bing
                                }
                                stmt.setString(7,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_D_W`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_D_W`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_dand_whole);
                                stmt.setInt(2,ent_cnt_dand_whole);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_DBPspot`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_DBPspot`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_dbpspot);
                                stmt.setInt(2,ent_cnt_dbpspot);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_DBPspot_W`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_DBPspot_W`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_dbpspot_whole);
                                stmt.setInt(2,ent_cnt_dbpspot_whole);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_Y_Stem`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_Y_Stem`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_stem);
                                stmt.setInt(2,ent_cnt_stem);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_Y_W_Stem`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_Y_W_Stem`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_whole_stem);
                                stmt.setInt(2,ent_cnt_whole_stem);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_D_Stem`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_D_Stem`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_dand_stem);
                                stmt.setInt(2,ent_cnt_dand_stem);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_D_W_Stem`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_D_W_Stem`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_dand_whole_stem);
                                stmt.setInt(2,ent_cnt_dand_whole_stem);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");                            
                                entitiesStatementBuilder.append("`Categories_Contained_Query_DBPspot_Stem`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_DBPspot_Stem`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_dbpspot_stem);
                                stmt.setInt(2,ent_cnt_dbpspot_stem);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            try {
                                conn = DriverManager.getConnection(url,user,password);
                                entitiesStatementBuilder = new StringBuilder();
                                entitiesStatementBuilder.append("UPDATE SEMANTICSTATS SET ");
                                entitiesStatementBuilder.append("`Categories_Contained_Query_DBPspot_W_Stem`=?,");
                                entitiesStatementBuilder.append("`Entities_Contained_Query_DBPspot_W_Stem`=? ");
                                entitiesStatementBuilder.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(entitiesStatementBuilder.toString());
                                stmt.setInt(1,cat_cnt_dbpspot_whole_stem);
                                stmt.setInt(2,ent_cnt_dbpspot_whole_stem);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                            }
                            finally{
                                try {
                                    if (stmt != null) stmt.close();
                                    if (conn != null) conn.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            System.out.println("I inserted the semantic entities and categories stats in the DB\n");
                            System.out.println("I will get the html stats for the "+j+" link:"+links_total[j]+"\n"); 
                            boolean flag_htmlstats=htm.gethtmlstats(links_total[j]);//get the semantic stats from the html code
                            if(flag_htmlstats){
                                System.out.println("I got the html stats for the "+j+" link:"+links_total[j]+"\n"); 
                                int scripts_cnt = htm.scripts_number;
                                int nschem=htm.nschem;
                                int hreln=htm.hreln;
                                int total_micron=htm.total_micron;
                                int micron1=htm.micron1;
                                int micron2=htm.micron2;
                                int microd=htm.microd;
                                System.out.println("I will insert webstats in the DB\n");
                                webstatsStmBuild.setLength(0);
                                webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                                webstatsStmBuild.append("`scripts_cnt`=? ");
                                webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                try{
                                    conn = DriverManager.getConnection(url,user,password);
                                    stmt = conn.prepareStatement(webstatsStmBuild.toString());
                                    stmt.setInt(1,scripts_cnt);
                                    stmt.setString(2,urlString);
                                    stmt.setString(3,quer);
                                    if(j<results_number){
                                        stmt.setInt(4,0);//0 for yahoo
                                    }
                                    else if(j<results_number*2){
                                        stmt.setInt(4,1);//1 for google
                                    }
                                    else if(j<results_number*3){
                                        stmt.setInt(4,2);//2 for bing
                                    }
                                    stmt.setString(5,domain);
                                    stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                System.out.println("I inserted webstats in the DB\n");
                                
                                System.out.println("I will insert semantic stats in the DB\n");
                                StringBuilder semanticstatsStmBuild = new StringBuilder();
                                semanticstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                                semanticstatsStmBuild.append("`schema.org_entities`=? , ");
                                semanticstatsStmBuild.append("`hreltags`=? ");
                                semanticstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(semanticstatsStmBuild.toString());
                                stmt.setInt(1,nschem);
                                stmt.setInt(2,hreln);
                                stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                StringBuilder semanticstatsStmBuild = new StringBuilder();
                                semanticstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                                semanticstatsStmBuild.append("`total_microformats`=? , ");
                                semanticstatsStmBuild.append("`Microformats-1`=? ");
                                semanticstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(semanticstatsStmBuild.toString());
                                stmt.setInt(1,total_micron);
                                stmt.setInt(2,micron1);
                                 stmt.setString(3,urlString);
                                stmt.setString(4,quer);
                                if(j<results_number){
                                    stmt.setInt(5,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(5,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(5,2);//2 for bing
                                }
                                stmt.setString(6,domain);
                                stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                try {
                                    conn = DriverManager.getConnection(url,user,password);
                                StringBuilder semanticstatsStmBuild = new StringBuilder();
                                semanticstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                                semanticstatsStmBuild.append("`Microformats-2`=? , ");
                                semanticstatsStmBuild.append("`Microdata`=?  , ");
                                semanticstatsStmBuild.append("`FOAF_HTML`=? ");
                                semanticstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(semanticstatsStmBuild.toString());
                                stmt.setInt(1,micron2); 
                                stmt.setInt(2,microd);
                                stmt.setInt(3,htm.foaf);
                                stmt.setString(4,urlString);
                                stmt.setString(5,quer);
                                if(j<results_number){
                                    stmt.setInt(6,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(6,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(6,2);//2 for bing
                                }
                                stmt.setString(7,domain);
                                stmt.executeUpdate();
                                }
                                finally{
                                    try {
                                        if (stmt != null) stmt.close();
                                        if (conn != null) conn.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                System.out.println("I inserted semantic stats in the DB\n");
                            }
                            
                        }
                    } 
                }
                String[] parse_output;
                if(ContentSemantics.get(3)||ContentSemantics.get(1)){
                //we perform LDA or TFIDF analysis to the links obtained
                    if(!enginechoice.get(3)){
                        if(enginechoice.get(2)){//Yahoo
                            parse_output=ld.perform(links_yahoo, domain, "yahoo", directory_save, quer, SWebRankSettings.get(1).intValue(), alpha, SWebRankSettings.get(0).doubleValue(), SWebRankSettings.get(2).intValue(), SWebRankSettings.get(3).intValue(),ContentSemantics.get(1),ContentSemantics.get(3), config_path);
                            int j=0;
                            for(String s:parse_output){
                                parseOutputList.put(j,s);
                                j++;
                            }
                            System.gc();
                        }
                        if(enginechoice.get(1)){//Google
                            parse_output=ld.perform(links_google, domain, "google", directory_save, quer, SWebRankSettings.get(1).intValue(), alpha, SWebRankSettings.get(0).doubleValue(), SWebRankSettings.get(2).intValue(), SWebRankSettings.get(3).intValue(),ContentSemantics.get(1),ContentSemantics.get(3), config_path);
                            int j=results_number;
                            for(String s:parse_output){
                                parseOutputList.put(j, s);
                                j++;
                            }
                            System.gc();
                        }
                        if(enginechoice.get(0)){//Bing
                            parse_output=ld.perform(links_bing, domain, "bing", directory_save, quer, SWebRankSettings.get(1).intValue(), alpha, SWebRankSettings.get(0).doubleValue(), SWebRankSettings.get(2).intValue(), SWebRankSettings.get(3).intValue(),ContentSemantics.get(1),ContentSemantics.get(3), config_path);
                            int j=results_number*2;
                            for(String s:parse_output){
                                parseOutputList.put(j, s);
                                j++;
                            }
                            System.gc();
                        }
                    }
                    /*else{
                        System.gc();//links_total
                        parse_output=ld.perform(links_total, domain, "merged", directory_save, quer, SWebRankSettings.get(1).intValue(), alpha, SWebRankSettings.get(0).doubleValue(), SWebRankSettings.get(2).intValue(), SWebRankSettings.get(3).intValue(),"Merged",ContentSemantics.get(1),ContentSemantics.get(3), config_path);
                        Collections.addAll(parseOutputList, parse_output);
                        System.gc();
                    }*/
                }
            }
            System.gc();
            List<String> wordList=null;
            //hashmap for every engine, with topics, words and probability of each word
            HashMap<String,HashMap<Integer,HashMap<String,Double>>> enginetopicwordprobmap = new HashMap<>();
            List<String> lda_output = new ArrayList<>();
            if(ContentSemantics.get(3)){
                //get the top content from TFIDF
                System.out.println("i ll try to read the keys");
                wordList=ld.return_topWordsTFIDF();
                System.out.println("i returned the wordlist to search analysis");
            }
            else if (ContentSemantics.get(0)){//get the wordlist from Diffbot
                Diffbot db=new Diffbot();
                wordList=db.compute(links_total, directory_save, config_path);
            }
            else if (ContentSemantics.get(2)){//get the wordllist from Sensebot
                Sensebot sb=new Sensebot();
                wordList=sb.compute(links_total, directory_save,SensebotConcepts, config_path);
            }
            else {
                //get the top content from LDA
                System.out.println("i ll try to read the keys");
                LDAtopicsWords rk = new LDAtopicsWords();
                enginetopicwordprobmap= rk.readFile(directory_save, SWebRankSettings.get(4),SWebRankSettings.get(3).intValue(), SWebRankSettings.get(1).intValue(), SWebRankSettings.get(11).intValue());
                
                JSONArray ArrayEngineLevel = new JSONArray();
                List<String> ids=new ArrayList<>();
                //Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
                //Client client = node.client();
                Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name","lshrankldacluster").build();
                Client client = new TransportClient(settings)
                    .addTransportAddress(new
                            InetSocketTransportAddress("localhost", 9300)
                );
                //save in elastic search the produced by LDA distributions of words over topics for every engine
                for(String engine: enginetopicwordprobmap.keySet()){
                    HashMap<Integer,HashMap<String,Double>> topicwordprobmap = new HashMap<>();
                    topicwordprobmap=enginetopicwordprobmap.get(engine);
                    JSONObject objEngineLevel = new JSONObject();
                    JSONArray ArrayTopicLevel = new JSONArray();
                    //for every topic get the words and their probability
                    for(Integer topicindex:topicwordprobmap.keySet()){
                        JSONObject objTopicLevel = new JSONObject();
                        objTopicLevel.put("topic",topicindex);
                        JSONObject objmap = new JSONObject(topicwordprobmap.get(topicindex));
                        Set keySet = objmap.keySet();
                        Iterator iterator = keySet.iterator();
                        while(iterator.hasNext()){
                            String word = iterator.next().toString();
                            if(!lda_output.contains(word)){
                                lda_output.add(word);
                            }//get the words in a separate list
                        }
                        objTopicLevel.put("wordsmap",objmap);//write the words in elastic search
                        ArrayTopicLevel.add(objTopicLevel);
                    }
                    objEngineLevel.put("engine",engine);
                    objEngineLevel.put("query",quer);
                    objEngineLevel.put("domain",domain);
                    objEngineLevel.put("iteration",iteration_counter);
                    objEngineLevel.put("TopicsWordMap", ArrayTopicLevel);
                    ArrayEngineLevel.add(objEngineLevel);
                    String id = domain+"/"+quer+"/"+engine+"/"+iteration_counter;//create unique id for the elasticsearch document
                    ids.add(id);//add to the ids list which contains the ids of the current round
                    List<String> elasticIndexes=ri.GetKeyFile(config_path, "elasticSearchIndexes");
                    IndexRequest indexReq=new IndexRequest(elasticIndexes.get(3),"content",id);
                    indexReq.source(objEngineLevel);
                    IndexResponse indexRes = client.index(indexReq).actionGet();
                }
                //node.close();
                client.close();
                ElasticGetWordList elasticGetwordList=new ElasticGetWordList();//get the wordlist from elastic search for the ids from the current round
                wordList=elasticGetwordList.get(ids,config_path);
                DataManipulation datamanipulation = new  DataManipulation();
                wordList=datamanipulation.clearListString(wordList);
                System.out.println("i returned the wordlist to search analysis");
            }
            //get some stats regarding the entities, categories and parsed content from each link comparing it to the top words produced by lda 
            for(int j=0;j<links_total.length;j++){
                if(links_total[j]!=null){
                String urlString = links_total[j];
                if(urlString.length()>199){
                            urlString=links_total[j].substring(0, 198);
                }
                int rank=-1;
                int engine=-1;//0 for yahoo,1 for google,2 for bing
                if(j<results_number){
                    rank=j;
                    engine=0;
                }
                else if(j<results_number*2){
                    rank=j-results_number;
                    engine=1;
                }
                else if(j<results_number*3){
                    rank=j-results_number*2;
                    engine=2;
                }
                LDAsemStats ldaSemStats = new LDAsemStats();//get the stats by comparing the top words produced by LDA and the parsed content
                //check the LDAsemStats class for more
                StringBuilder webstatsStmBuild = new StringBuilder();
                if(!parseOutputList.isEmpty()){
                    if(!parseOutputList.get(j).equalsIgnoreCase("")&&!parseOutputList.get(j).equalsIgnoreCase("null")&&(parseOutputList.get(j).length()>0)){
                        ldaSemStats.getTopWordsStats(parseOutputList.get(j), lda_output, false);//without stemming
                        int top_words_lda = ldaSemStats.getTopStats();
                        double top_words_lda_per = ldaSemStats.getTopPercentageStats();
                        webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                        webstatsStmBuild.append("`top_words_lda`=? , ");
                        webstatsStmBuild.append("`top_words_lda_per`=? ");
                        webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                        try{
                            conn = DriverManager.getConnection(url,user,password);
                            stmt = conn.prepareStatement(webstatsStmBuild.toString());
                            stmt.setInt(1,top_words_lda);
                            stmt.setDouble(2,top_words_lda_per);
                            stmt.setString(3,urlString);
                            stmt.setString(4,quer);
                            stmt.setInt(5,engine);
                            stmt.setString(6,domain);
                            stmt.executeUpdate();
                        }
                        finally{
                            try {
                                if (stmt != null) stmt.close();
                                if (conn != null) conn.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        ldaSemStats.getTopWordsStats(parseOutputList.get(j), lda_output, true);//with stemming
                        int top_words_lda_stem = ldaSemStats.getTopStats();
                        double top_words_lda_per_stem = ldaSemStats.getTopPercentageStats();
                        webstatsStmBuild = new StringBuilder();
                        webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                        webstatsStmBuild.append("`top_words_lda_stem`=? , ");
                        webstatsStmBuild.append("`top_words_lda_per_stem`=? ");
                        webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                        try{
                            conn = DriverManager.getConnection(url,user,password);
                            stmt = conn.prepareStatement(webstatsStmBuild.toString());
                            stmt.setInt(1,top_words_lda_stem);
                            stmt.setDouble(2,top_words_lda_per_stem);
                            stmt.setString(3,urlString);
                            stmt.setString(4,quer);
                            stmt.setInt(5,engine);
                            stmt.setString(6,domain);
                            stmt.executeUpdate();
                        }
                        finally{
                            try {
                                if (stmt != null) stmt.close();
                                if (conn != null) conn.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                if(EntitiesMapDBP.get(j)!=null && CategoriesMapDBP.get(j) !=null){
                    //we are going to check if semantic entities and categories recognized exist in the lda words recognized as prominent
                    //we are going to use DBPEDIA spotligh and Dandelion named Entity Extraction API
                    //and stemming through Snowball Stemmer
                    ldaSemStats.getEntCatStats(EntitiesMapDBP.get(j), CategoriesMapDBP.get(j), lda_output, false);
                    int ent_cnt_dbpspot_lda = ldaSemStats.getEntStats();
                    int cat_cnt_dbpspot_lda = ldaSemStats.getCategoryStats();
                    webstatsStmBuild = new StringBuilder();
                    webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                    webstatsStmBuild.append("`ent_cnt_dbpspot_lda`=? , ");
                    webstatsStmBuild.append("`cat_cnt_dbpspot_lda`=? ");
                    webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                    try{
                        conn = DriverManager.getConnection(url,user,password);
                        stmt = conn.prepareStatement(webstatsStmBuild.toString());
                        stmt.setInt(1,ent_cnt_dbpspot_lda);
                        stmt.setInt(2,cat_cnt_dbpspot_lda);
                        stmt.setString(3,urlString);
                        stmt.setString(4,quer);
                        stmt.setInt(5,engine);
                        stmt.setString(6,domain);
                        stmt.executeUpdate();
                    }
                    finally{
                        try {
                            if (stmt != null) stmt.close();
                            if (conn != null) conn.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    ldaSemStats.getEntCatStats( EntitiesMapDBP.get(j), CategoriesMapDBP.get(j), lda_output, true);
                    int ent_cnt_dbpspot_lda_stem = ldaSemStats.getEntStats();
                    int cat_cnt_dbpspot_lda_stem = ldaSemStats.getCategoryStats();
                    webstatsStmBuild = new StringBuilder();
                    webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                    webstatsStmBuild.append("`ent_cnt_dbpspot_lda_stem`=? , ");
                    webstatsStmBuild.append("`cat_cnt_dbpspot_lda_stem`=? ");
                    webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                    try{
                        conn = DriverManager.getConnection(url,user,password);
                        stmt = conn.prepareStatement(webstatsStmBuild.toString());
                        stmt.setInt(1,ent_cnt_dbpspot_lda_stem);
                        stmt.setInt(2,cat_cnt_dbpspot_lda_stem);
                        stmt.setString(3,urlString);
                        stmt.setString(4,quer);
                        stmt.setInt(5,engine);
                        stmt.setString(6,domain);
                        stmt.executeUpdate();
                    }
                    finally{
                        try {
                            if (stmt != null) stmt.close();
                            if (conn != null) conn.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                if(EntitiesMapDand.get(j)!=null && CategoriesMapDand.get(j) !=null){
                    ldaSemStats.getEntCatStats(EntitiesMapDand.get(j), CategoriesMapDand.get(j), lda_output, false);
                    int ent_cnt_dand_lda = ldaSemStats.getEntStats();
                    int cat_cnt_dand_lda = ldaSemStats.getCategoryStats();
                    webstatsStmBuild = new StringBuilder();
                    webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                    webstatsStmBuild.append("`ent_cnt_dand_lda`=? , ");
                    webstatsStmBuild.append("`cat_cnt_dand_lda`=? ");
                    webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                    try{
                        conn = DriverManager.getConnection(url,user,password);
                        stmt = conn.prepareStatement(webstatsStmBuild.toString());
                        stmt.setInt(1,ent_cnt_dand_lda);
                        stmt.setInt(2,cat_cnt_dand_lda);
                        stmt.setString(3,urlString);
                        stmt.setString(4,quer);
                        stmt.setInt(5,engine);
                        stmt.setString(6,domain);
                        stmt.executeUpdate();
                    }
                    finally{
                        try {
                            if (stmt != null) stmt.close();
                            if (conn != null) conn.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    ldaSemStats.getEntCatStats(EntitiesMapDand.get(j), CategoriesMapDand.get(j), lda_output, true);
                    int ent_cnt_dand_lda_stem = ldaSemStats.getEntStats();
                    int cat_cnt_dand_lda_stem = ldaSemStats.getCategoryStats();
                    webstatsStmBuild = new StringBuilder();
                    webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                    webstatsStmBuild.append("`ent_cnt_dand_lda_stem`=? , ");
                    webstatsStmBuild.append("`cat_cnt_dand_lda_stem`=? ");
                    webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                    try{
                        conn = DriverManager.getConnection(url,user,password);
                        stmt = conn.prepareStatement(webstatsStmBuild.toString());
                        stmt.setInt(1,ent_cnt_dand_lda_stem);
                        stmt.setInt(2,cat_cnt_dand_lda_stem);
                        stmt.setString(3,urlString);
                        stmt.setString(4,quer);
                        stmt.setInt(5,engine);
                        stmt.setString(6,domain);
                        stmt.executeUpdate();
                    }
                    finally{
                        try {
                            if (stmt != null) stmt.close();
                            if (conn != null) conn.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                if(EntitiesMapYahoo.get(j)!=null && CategoriesMapYahoo.get(j) !=null){
                    //we are going to check if semantic entities and categories recognized exist in the lda words recognized as prominent
                    //we are going to use DBPEDIA spotligh and Dandelion named Entity Extraction API
                    //and stemming through Snowball Stemmer
                    ldaSemStats.getEntCatStats(EntitiesMapYahoo.get(j), CategoriesMapYahoo.get(j), lda_output, false);
                    int ent_cnt_y_lda = ldaSemStats.getEntStats();
                    int cat_cnt_y_lda = ldaSemStats.getCategoryStats();
                    webstatsStmBuild = new StringBuilder();
                    webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                    webstatsStmBuild.append("`ent_cnt_y_lda`=? , ");
                    webstatsStmBuild.append("`cat_cnt_y_lda`=? ");
                    webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                    try{
                        conn = DriverManager.getConnection(url,user,password);
                        stmt = conn.prepareStatement(webstatsStmBuild.toString());
                        stmt.setInt(1,ent_cnt_y_lda);
                        stmt.setInt(2,cat_cnt_y_lda);
                        stmt.setString(3,urlString);
                        stmt.setString(4,quer);
                        stmt.setInt(5,engine);
                        stmt.setString(6,domain);
                        stmt.executeUpdate();
                    }
                    finally{
                        try {
                            if (stmt != null) stmt.close();
                            if (conn != null) conn.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    ldaSemStats.getEntCatStats( EntitiesMapYahoo.get(j), CategoriesMapYahoo.get(j), lda_output, true);
                    int ent_cnt_y_lda_stem = ldaSemStats.getEntStats();
                    int cat_cnt_y_lda_stem = ldaSemStats.getCategoryStats();
                    webstatsStmBuild = new StringBuilder();
                    webstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                    webstatsStmBuild.append("`ent_cnt_y_lda_stem`=? , ");
                    webstatsStmBuild.append("`cat_cnt_y_lda_stem`=? ");
                    webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                    try{
                        conn = DriverManager.getConnection(url,user,password);
                        stmt = conn.prepareStatement(webstatsStmBuild.toString());
                        stmt.setInt(1,ent_cnt_y_lda_stem);
                        stmt.setInt(2,cat_cnt_y_lda_stem);
                        stmt.setString(3,urlString);
                        stmt.setString(4,quer);
                        stmt.setInt(5,engine);
                        stmt.setString(6,domain);
                        stmt.executeUpdate();
                    }
                    finally{
                        try {
                            if (stmt != null) stmt.close();
                            if (conn != null) conn.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                }
            }
            return wordList;
        } 
        catch (NullPointerException ex) {
            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
            ArrayList<String> finalList = new ArrayList<>();
            return finalList;
        } 
        catch (SQLException | ElasticsearchException ex) {
            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
            ArrayList<String> finalList = new ArrayList<>();
            return finalList;
        }
        finally{
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
