/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;
/**
 *
 * @author Themis Mavridis
 */

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import java.util.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import static org.elasticsearch.client.Requests.createIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.*;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilders.*;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import org.jdom2.input.DOMBuilder;
//import com.teamdev.jxbrowser.Browser;
//import com.teamdev.jxbrowser.BrowserFactory;
//import com.teamdev.jxbrowser.BrowserType;
//import com.teamdev.jxbrowser.dom.DOMDocument;

/**
 *
 * @author themis
 */
public class Search_analysis {

    /**
     *
     */
    public String ychk;

    /**
     *
     */
    public String gchk;

    /**
     *
     */
    public String bchk;

    /**
     *
     */
    public String tchk;

    /**
     *
     * @param example_dir
     * @param enginechoice
     * @param quer
     * @param results_number
     * @param top_visible
     * @param LSHrankSettings
     * @param alpha
     * @param mozMetrics
     * @param top_count_moz
     * @param moz_threshold_option
     * @param moz_threshold
     * @param ContentSemantics
     * @param SensebotConcepts
     * @return
     */
    public List<String> perform(int iteration_counter,String example_dir, String domain, List<Boolean> enginechoice, String quer, int results_number, int top_visible,List<Double> LSHrankSettings,double alpha, List<Boolean> mozMetrics, int top_count_moz, boolean moz_threshold_option,double moz_threshold, List<Boolean> ContentSemantics, int SensebotConcepts){ 
        try {
            Connection conn = null;
            PreparedStatement stmt = null;
            String url = "jdbc:mysql://localhost:3306/LSHrankDB?zeroDateTimeBehavior=convertToNull";
            String user = "root";
            String password = "843647";
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
                links_bing=br.Get(quer, results_number, example_dir);                     
            }
            if(enginechoice.get(1)){
                //get google results
                GoogleResults gr = new GoogleResults();
                links_google=gr.Get(quer,results_number,example_dir);
            }
            if(enginechoice.get(2)){
                //get yahoo results
                YahooResults yr = new YahooResults();
                links_yahoo=yr.Get(quer,results_number,example_dir);                    
            }
            
            //*************
            boolean false_flag=true;
            if(false_flag){
                if(mozMetrics.get(0)){
                    //we check if moz works
                    Moz moz=new Moz();
                    boolean checkmoz=moz.check();
                    if(checkmoz){
                         //perform 
                         if(links_yahoo.length>0){
                             links_yahoo=moz.perform(links_yahoo,top_count_moz,moz_threshold,moz_threshold_option,mozMetrics);
                         }
                         if(links_google.length>0){
                             links_google=moz.perform(links_google,top_count_moz,moz_threshold,moz_threshold_option,mozMetrics);
                         }
                         if(links_bing.length>0){
                             links_bing=moz.perform(links_bing,top_count_moz,moz_threshold,moz_threshold_option,mozMetrics);
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
                    links_total=vb.perform(links_google, links_yahoo, links_bing, top_visible);
                    //if we have Moz option set to true we have to get the results rearranged according to the moz metric selected
                    if(mozMetrics.get(0)){
                        CheckMoz checkSEOmoz=new CheckMoz();
                        boolean check_seo=checkSEOmoz.check();
                        if (check_seo){
                            Moz MOZ=new Moz();                      
                            links_total=MOZ.perform(links_total,top_count_moz,moz_threshold,moz_threshold_option,mozMetrics);
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
                        
                        stmt = conn.prepareStatement("INSERT INTO SETTINGS (url,query,search_engine,search_engine_rank,domain) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine),domain=VALUES(domain)");
                        stmt.setString(1,links_total[j]);
                        stmt.setString(2,quer);
                        stmt.setInt(3,engine);
                        stmt.setInt(4,rank);
                        stmt.setString(5,domain);
                        stmt.executeUpdate();
                        
                        stmt = conn.prepareStatement("INSERT INTO WEBSTATS (url,query,search_engine,search_engine_rank,domain) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine),domain=VALUES(domain)");
                        stmt.setString(1,links_total[j]);
                        stmt.setString(2,quer);
                        stmt.setInt(3,engine);
                        stmt.setInt(4,rank);
                        stmt.setString(5,domain);
                        stmt.executeUpdate();
                        
                        stmt = conn.prepareStatement("INSERT INTO SEMANTICSTATS (url,query,search_engine,search_engine_rank,domain) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine),domain=VALUES(domain)");
                        stmt.setString(1,links_total[j]);
                        stmt.setString(2,quer);
                        stmt.setInt(3,engine);
                        stmt.setInt(4,rank);
                        stmt.setString(5,domain);
                        stmt.executeUpdate();
                        
                        
                        stmt = conn.prepareStatement("INSERT INTO NAMESPACESSTATS (url,query,search_engine,search_engine_rank,domain) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine),domain=VALUES(domain)");
                        stmt.setString(1,links_total[j]);
                        stmt.setString(2,quer);
                        stmt.setInt(3,engine);
                        stmt.setInt(4,rank);
                        stmt.setString(5,domain);
                        stmt.executeUpdate();
                        
                        stmt = conn.prepareStatement("INSERT INTO ENTCATSTATS (url,query,search_engine,search_engine_rank,domain) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine),domain=VALUES(domain)");
                        stmt.setString(1,links_total[j]);
                        stmt.setString(2,quer);
                        stmt.setInt(3,engine);
                        stmt.setInt(4,rank);
                        stmt.setString(5,domain);
                        stmt.executeUpdate();
                        
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
                        settingsStmBuild.append("`SensebotConcepts`=? "); 
                        settingsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                        
                        stmt = conn.prepareStatement(settingsStmBuild.toString());
                        stmt.setInt(1,LSHrankSettings.get(1).intValue());
                        stmt.setDouble(2,alpha);
                        stmt.setDouble(3,LSHrankSettings.get(0));
                        stmt.setInt(4,LSHrankSettings.get(2).intValue());
                        stmt.setDouble(5,LSHrankSettings.get(3));
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
                        stmt.setString(24,links_total[j]);
                        stmt.setString(25,quer);
                        stmt.setInt(26,engine);
                        stmt.setString(27,domain);
                        stmt.executeUpdate();
                        
                        if(htm.checkconn(links_total[j])){
                            nlinks=htm.getnlinks(links_total[j]);
                            StringBuilder webstatsStmBuild = new StringBuilder();
                            webstatsStmBuild.append("UPDATE WEBSTATS SET ");
                            webstatsStmBuild.append("`number_links`=? , ");
                            webstatsStmBuild.append("`redirect_links`=? , ");
                            webstatsStmBuild.append("`internal_links`=? ");
                            webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                            stmt = conn.prepareStatement(webstatsStmBuild.toString());
                            stmt.setInt(1,nlinks[0]);
                            stmt.setInt(2,nlinks[0]-nlinks[1]);
                            stmt.setInt(3,nlinks[1]);
                            stmt.setString(4,links_total[j]);
                            stmt.setString(5,quer);
                            stmt.setInt(6,engine);
                            stmt.setString(7,domain);
                            stmt.executeUpdate();
                            System.out.println("I am going to get the stats from Sindice\n");
                            int ntriples=striple.getsindicestats(links_total[j]);
                            System.out.println("I am going insert the semantic triples number in the DB\n");
                            stmt = conn.prepareStatement("UPDATE SEMANTICSTATS SET `total_semantic_triples`=? WHERE `url` =? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                            stmt.setInt(1,ntriples);
                            stmt.setString(2,links_total[j]);
                            stmt.setString(3,quer);
                            stmt.setInt(4,engine);
                            stmt.setString(5,domain);
                            stmt.executeUpdate();
                            System.out.println("I inserted the semantic triples number in the DB\n");
                            //---namespaces-----
                            
                            System.out.println("I am going to insert the namespaces in the DB\n");
                            if(striple.namespaces[0]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/vocab/bio/0.1/` = ?  WHERE `url` = ? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[1]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/dc/elements/1.1/` =? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[2]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/coo/n` = ? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[3]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://web.resource.org/cc/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[4]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://diligentarguont.ontoware.org/2005/10/arguonto`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[5]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://usefulinc.com/ns/doap`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[6]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://xmlns.com/foaf/0.1/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[7]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/goodrelations/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[8]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/muto/core`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[9]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://webns.net/mvcb/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[10]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/ontology/mo/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[11]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/innovation/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[12]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://openguid.net/rdf`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[13]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.slamka.cz/ontologies/diagnostika.owl`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[14]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/ontology/po/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[15]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/net/provenance/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[16]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/rss/1.0/modules/syndication`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[17]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://rdfs.org/sioc/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[18]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://madskills.com/public/xml/rss/module/trackback/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[19]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://rdfs.org/ns/void`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[20]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.fzi.de/2008/wise/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[21]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://xmlns.com/wot/0.1`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[22]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.w3.org/1999/02/22-rdf-syntax-ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[23]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `rdf-schema`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[24]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `XMLschema`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[25]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `OWL`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[26]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/dc/terms/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[27]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `VCARD`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[28]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.geonames.org/ontology`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[29]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://search.yahoo.com/searchmonkey/commerce/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[30]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://search.yahoo.com/searchmonkey/media/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[31]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://cb.semsol.org/ns#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[32]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://blogs.yandex.ru/schema/foaf/`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[33]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.w3.org/2003/01/geo/wgs84_pos#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[34]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://rdfs.org/sioc/ns#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[35]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://rdfs.org/sioc/types#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[36]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://smw.ontoware.org/2005/smw#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[37]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://purl.org/rss/1.0/`= ? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[38]){
                                stmt = conn.prepareStatement("UPDATE NAMESPACESSTATS SET `http://www.w3.org/2004/12/q/contentlabel#`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                                stmt.setBoolean(1,true);
                                stmt.setString(2,links_total[j]);
                                stmt.setString(3,quer);
                                stmt.setInt(4,engine);
                                stmt.setString(5,domain);
                                stmt.executeUpdate();
                            }
                            System.out.println("I inserted the namespaces in the DB\n");
                            //we continue only for not null links
                            System.out.println("I will get the semantic entities and categories\n");
                            String[] catentities=new String[2];
                            catentities[0]="";
                            catentities[1]="";
                            YahooEntityCategory yec=new YahooEntityCategory();
                            catentities=yec.connect(links_total[j],quer);
                            for(int okk=0;okk<catentities.length;okk++){
                                if(!(catentities[okk].isEmpty())){
                                    DataManipulation txtpro = new  DataManipulation();
                                    Stopwords st = new Stopwords();
                                    catentities[okk]=txtpro.removeChars(catentities[okk]);
                                    catentities[okk]=st.stop(catentities[okk]);
                                    catentities[okk]=txtpro.removeChars(catentities[okk]);
                                }
                               total_catent[j][1]=total_catent[j][1]+catentities[okk];
                            }
                            total_catent[j][0]=links_total[j];
                            int cat_cnt=yec.GetCatQuerCnt();
                            int ent_cnt=yec.GetEntQuerCnt();
                            System.out.println("I insert the semantic entities and categories stats in the DB\n");
                            stmt = conn.prepareStatement("UPDATE ENTCATSTATS SET `Categories_Contained_Query`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                            stmt.setInt(1,cat_cnt);
                            stmt.setString(2,links_total[j]);
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
                            
                            stmt = conn.prepareStatement("UPDATE ENTCATSTATS SET `Categories_TF`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                            if(cat_cnt>0){
                                stmt.setBoolean(1,true);
                            }
                            else {
                                stmt.setBoolean(1,false);
                            }
                            stmt.setString(2,links_total[j]);
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
                            
                            stmt = conn.prepareStatement("UPDATE ENTCATSTATS SET `Entities_Contained_Query`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                            stmt.setInt(1,ent_cnt);
                            stmt.setString(2,links_total[j]);
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
                            
                            stmt = conn.prepareStatement("UPDATE ENTCATSTATS SET `Entities_TF`=? WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?" );
                            if(ent_cnt>0){
                                stmt.setBoolean(1,true);
                            }else {
                                stmt.setBoolean(1,false);
                            }
                            stmt.setString(2,links_total[j]);
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
                            String check=stmt.toString();
                            stmt.executeUpdate();
                            
                            System.out.println("I inserted the semantic entities and categories stats in the DB\n");
                            System.out.println("I will get the html stats for the "+j+" link:"+links_total[j]+"\n"); 
                            boolean flag_htmlstats=htm.gethtmlstats(links_total[j]);
                            if(flag_htmlstats){
                                System.out.println("I got the html stats for the "+j+" link:"+links_total[j]+"\n"); 
                                int iframes_number = htm.frames_number;
                                int number_embeded_vid = htm.number_embeded_videos;
                                int nauthority=0;
                                int scripts_cnt = htm.scripts_number;
                                int nschem=htm.nschem;
                                int hcardsn=htm.hcardsn;
                                int hcalen=htm.hcalen;
                                int hrevn=htm.hrevn;
                                int hevenn=htm.hevenn;
                                int haddrn=htm.haddrn;
                                int hgeon=htm.hgeon;
                                int hreln=htm.hreln;
                                int total_micron=htm.total_micron;
                                int micron1=htm.micron1;
                                int micron2=htm.micron2;
                                int microd=htm.microd;
                                
                                System.out.println("I will insert webstats in the DB\n");
                                webstatsStmBuild.setLength(0);
                                webstatsStmBuild.append("UPDATE WEBSTATS SET ");
                                webstatsStmBuild.append("`iframes`=? , ");
                                webstatsStmBuild.append("`number_embeded_vids`=? , ");
                                webstatsStmBuild.append("`scripts_cnt`=? ");
                                webstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(webstatsStmBuild.toString());
                                stmt.setInt(1,iframes_number);
                                stmt.setInt(2,number_embeded_vid);
                                stmt.setInt(3,scripts_cnt);
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
                                System.out.println("I inserted webstats in the DB\n");
                                
                                System.out.println("I will insert semantic stats in the DB\n");
                                StringBuilder semanticstatsStmBuild = new StringBuilder();
                                semanticstatsStmBuild.append("UPDATE SEMANTICSTATS SET ");
                                semanticstatsStmBuild.append("`schema.org_entities`=? , ");
                                semanticstatsStmBuild.append("`hcards`=? , ");
                                semanticstatsStmBuild.append("`hcalendars`=? , ");
                                semanticstatsStmBuild.append("`hreviews`=? , ");
                                semanticstatsStmBuild.append("`hevents`=? , ");
                                semanticstatsStmBuild.append("`hadresses`=? , ");
                                semanticstatsStmBuild.append("`hgeo`=? , ");
                                semanticstatsStmBuild.append("`hreltags`=? , ");
                                semanticstatsStmBuild.append("`total_microformats`=? , ");
                                semanticstatsStmBuild.append("`Microformats-1`=? , ");
                                semanticstatsStmBuild.append("`Microformats-2`=? , ");
                                semanticstatsStmBuild.append("`Microdata`=?  , ");
                                semanticstatsStmBuild.append("`FOAF_HTML`=? ");
                                semanticstatsStmBuild.append("WHERE `url`=? AND `query`=? AND `search_engine`=? AND `domain`=?");
                                stmt = conn.prepareStatement(semanticstatsStmBuild.toString());
                                stmt.setInt(1,nschem);
                                stmt.setInt(2,hcardsn);
                                stmt.setInt(3,hcalen);
                                stmt.setInt(4,hrevn);
                                stmt.setInt(5,hevenn);
                                stmt.setInt(6,haddrn);
                                stmt.setInt(7,hgeon);
                                stmt.setInt(8,hreln);
                                stmt.setInt(9,total_micron);
                                stmt.setInt(10,micron1);
                                stmt.setInt(11,micron2); 
                                stmt.setInt(12,microd);
                                stmt.setInt(13,htm.foaf);
                                stmt.setString(14,links_total[j]);
                                stmt.setString(15,quer);
                                if(j<results_number){
                                    stmt.setInt(16,0);//0 for yahoo
                                }
                                else if(j<results_number*2){
                                    stmt.setInt(16,1);//1 for google
                                }
                                else if(j<results_number*3){
                                    stmt.setInt(16,2);//2 for bing
                                }
                                stmt.setString(17,domain);
                                stmt.executeUpdate();
                                System.out.println("I inserted semantic stats in the DB\n");
                            }
                            
                        }
                    } 
                }

                if(ContentSemantics.get(3)||ContentSemantics.get(1)){
                //we perform LDA or TFIDF analysis to the links obtained
                    if(!enginechoice.get(3)){
                        if(enginechoice.get(2)){
                            ychk=ld.perform(links_yahoo, domain, "yahoo", example_dir, quer, LSHrankSettings.get(1).intValue(), alpha, LSHrankSettings.get(0).doubleValue(), LSHrankSettings.get(2).intValue(), LSHrankSettings.get(3).intValue(),"yahoo",ContentSemantics.get(1),ContentSemantics.get(3),total_catent);
                            System.gc();
                        }
                        if(enginechoice.get(1)){
                            gchk=ld.perform(links_google, domain, "google", example_dir, quer, LSHrankSettings.get(1).intValue(), alpha, LSHrankSettings.get(0).doubleValue(), LSHrankSettings.get(2).intValue(), LSHrankSettings.get(3).intValue(),"google",ContentSemantics.get(1),ContentSemantics.get(3),total_catent);
                            System.gc();
                        }
                        if(enginechoice.get(0)){
                            bchk=ld.perform(links_bing, domain, "bing", example_dir, quer, LSHrankSettings.get(1).intValue(), alpha, LSHrankSettings.get(0).doubleValue(), LSHrankSettings.get(2).intValue(), LSHrankSettings.get(3).intValue(),"bing",ContentSemantics.get(1),ContentSemantics.get(3),total_catent);
                            System.gc();
                        }
                    }
                    else{
                        System.gc();//links_total
                        tchk=ld.perform(links_total, domain, "merged", example_dir, quer, LSHrankSettings.get(1).intValue(), alpha, LSHrankSettings.get(0).doubleValue(), LSHrankSettings.get(2).intValue(), LSHrankSettings.get(3).intValue(),"Merged",ContentSemantics.get(1),ContentSemantics.get(3),total_catent);
                        System.gc();
                    }
                }
            }
            System.gc();
            List<String> wordList=null;
            HashMap<String,HashMap<Integer,HashMap<String,Double>>> enginetopicwordprobmap = new HashMap<>();
            if(ContentSemantics.get(3)){
                //get the top content from TFIDF
                System.out.println("i ll try to read the keys");
                wordList=ld.return_topWordsTFIDF();
                System.out.println("i returned the wordlist to search analysis");
            }
            else if (ContentSemantics.get(0)){
                Diffbot db=new Diffbot();
                wordList=db.compute(links_total, example_dir);
            }
            else if (ContentSemantics.get(2)){
                Sensebot sb=new Sensebot();
                wordList=sb.compute(links_total, example_dir,SensebotConcepts);
            }
            else {
                //get the top content from LDA
                System.out.println("i ll try to read the keys");
                ReadKeys rk = new ReadKeys();
                enginetopicwordprobmap= rk.readFile(example_dir, LSHrankSettings.get(4),LSHrankSettings.get(3).intValue(), LSHrankSettings.get(1).intValue());
                // on startup of elastic search
              
                //obj.put("query",quer);
                //obj.put("Domain",domain);
                JSONArray ArrayEngineLevel = new JSONArray();
                List<String> ids=new ArrayList<>();
                Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
                Client client = node.client();
                for(String engine: enginetopicwordprobmap.keySet()){
                    HashMap<Integer,HashMap<String,Double>> topicwordprobmap = new HashMap<>();
                    topicwordprobmap=enginetopicwordprobmap.get(engine);
                    JSONObject objEngineLevel = new JSONObject();
                    JSONArray ArrayTopicLevel = new JSONArray();
                    for(Integer topicindex:topicwordprobmap.keySet()){
                        JSONObject objTopicLevel = new JSONObject();
                        objTopicLevel.put("topic",topicindex);
                        JSONObject objmap = new JSONObject(topicwordprobmap.get(topicindex));
                        objTopicLevel.put("wordsmap",objmap);
                        ArrayTopicLevel.add(objTopicLevel);
                    }
                    objEngineLevel.put("engine",engine);
                    objEngineLevel.put("query",quer);
                    objEngineLevel.put("domain",domain);
                    objEngineLevel.put("iteration",iteration_counter);
                    objEngineLevel.put("TopicsWordMap", ArrayTopicLevel);
                    ArrayEngineLevel.add(objEngineLevel);
                    String id = domain+"/"+quer+"/"+engine+"/"+iteration_counter;
                    ids.add(id);
                    IndexRequest indexReq=new IndexRequest("lshranklda","content",id);
                    indexReq.source(objEngineLevel);
                    IndexResponse indexRes = client.index(indexReq).actionGet();
                    
                    
                }
                node.close();
                ElasticGetWordList elasticGetwordList=new ElasticGetWordList();
                wordList=elasticGetwordList.get(ids);
                DataManipulation datamanipulation = new  DataManipulation();
                wordList=datamanipulation.clearListString(wordList);
                System.out.println("i returned the wordlist to search analysis");
            }
            return wordList;
        }  
        catch (NullPointerException ex) {
            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
            ArrayList<String> finalList = new ArrayList<String>();
            return finalList;
        } 
        catch (SQLException | ElasticsearchException ex) {
            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
            ArrayList<String> finalList = new ArrayList<String>();
            return finalList;
        }
    }
}
