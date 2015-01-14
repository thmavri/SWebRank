/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;
/**
 *
 * @author Themis Mavridis
 */

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
    public List<String> perform(String example_dir, List<Boolean> enginechoice, String quer, int results_number, int top_visible,List<Double> LSHrankSettings,double alpha, List<Boolean> mozMetrics, int top_count_moz, boolean moz_threshold_option,double moz_threshold, List<Boolean> ContentSemantics, int SensebotConcepts){ 
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
                        //INSERT INTO STATS (url) VALUES (?) ON DUPLICATE KEY UPDATE url = VALUES(url) 
                        //stmt = conn.prepareStatement("UPDATE STATS SET (url,query,search_engine) VALUES (?,?,?) where `query`='query',`search_engine`='query'");
                        stmt = conn.prepareStatement("INSERT INTO STATS (url,query,search_engine) VALUES (?,?,?) ON DUPLICATE KEY UPDATE url=VALUES(url),query=VALUES(query),search_engine=VALUES(search_engine)");
                        stmt.setString(1,links_total[j]);
                        stmt.setString(2,quer);
                        if(j<results_number){
                            stmt.setInt(3,0);//0 for yahoo
                        }
                        else if(j<results_number*2){
                            stmt.setInt(3,1);//1 for google
                        }
                        else if(j<results_number*3){
                            stmt.setInt(3,2);//2 for bing
                        }
                        stmt.executeUpdate();
                        
                        stmt = conn.prepareStatement("UPDATE STATS SET `search_engine_rank`=? WHERE `url`=? AND `query`=? AND `search_engine`=?");
                        if(j<results_number){
                            stmt.setInt(1,j);
                        }
                        else if(j<results_number*2){
                            stmt.setInt(1,j-results_number);
                        }
                        else if(j<results_number*3){
                            stmt.setInt(1,j-results_number*2);
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
                        stmt.executeUpdate();


                        stmt = conn.prepareStatement("UPDATE STATS SET `nTopics`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setInt(1,LSHrankSettings.get(1).intValue());
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `alpha`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setDouble(1,alpha);
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `beta`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setDouble(1,LSHrankSettings.get(0));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `niters`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setInt(1,LSHrankSettings.get(2).intValue());
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `prob_threshold`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setDouble(1,LSHrankSettings.get(3));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `moz`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,mozMetrics.get(0));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `top_count_moz`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setInt(1,top_count_moz);
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `moz_threshold`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setDouble(1,moz_threshold);
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `moz_threshold_option`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,moz_threshold_option);
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `top_visible`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setInt(1,top_visible);
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `Domain_Authority`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,mozMetrics.get(1));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `External_MozRank`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,mozMetrics.get(2));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `MozRank`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,mozMetrics.get(3));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `MozTrust`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,mozMetrics.get(4));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `Page_Authority`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,mozMetrics.get(5));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `Subdomain_mozRank`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,mozMetrics.get(6));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `merged`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,enginechoice.get(3));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `results_number`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setInt(1,results_number);
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `Diffbotflag`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,ContentSemantics.get(0));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `LDAflag`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,ContentSemantics.get(1));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `Sensebotflag`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,ContentSemantics.get(2));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `TFIDFflag`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setBoolean(1,ContentSemantics.get(3));
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
                        stmt.executeUpdate();

                        stmt = conn.prepareStatement("UPDATE STATS SET `SensebotConcepts`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                        stmt.setInt(1,SensebotConcepts);
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
                        stmt.executeUpdate();
                        if(htm.checkconn(links_total[j])){
                            nlinks=htm.getnlinks(links_total[j]);
                            
                            stmt = conn.prepareStatement("UPDATE STATS SET `number_links`=? WHERE `url`=? AND `query`=? AND `search_engine`=?");
                            stmt.setInt(1,nlinks[0]);
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
                            stmt.executeUpdate();
                            
                            stmt = conn.prepareStatement("UPDATE STATS SET `redirect_links`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                            stmt.setInt(1,nlinks[0]-nlinks[1]);
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
                            stmt.executeUpdate();
                            
                            stmt = conn.prepareStatement("UPDATE STATS SET `internal_links`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                            stmt.setInt(1,nlinks[1]);
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
                            stmt.executeUpdate();
                            
                            int ntriples=striple.getsindicestats(links_total[j]);
                            
                            stmt = conn.prepareStatement("UPDATE STATS SET `total_semantic_triples`=? WHERE `url` =? AND `query`=? AND `search_engine`=?" );
                            stmt.setInt(1,ntriples);
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
                            stmt.executeUpdate();
                            //---namespaces-----
                            if(striple.namespaces[0]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/vocab/bio/0.1/` = ?  WHERE `url` = ? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[1]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/dc/elements/1.1/` =? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[2]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/coo/n` = ? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[3]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://web.resource.org/cc/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[4]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://diligentarguont.ontoware.org/2005/10/arguonto`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[5]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://usefulinc.com/ns/doap`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[6]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://xmlns.com/foaf/0.1/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[7]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/goodrelations/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[8]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/muto/core`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[9]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://webns.net/mvcb/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[10]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/ontology/mo/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[11]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/innovation/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[12]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://openguid.net/rdf`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[13]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://www.slamka.cz/ontologies/diagnostika.owl`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[14]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/ontology/po/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[15]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/net/provenance/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[16]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/rss/1.0/modules/syndication`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[17]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://rdfs.org/sioc/ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[18]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://madskills.com/public/xml/rss/module/trackback/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[19]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://rdfs.org/ns/void`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[20]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://www.fzi.de/2008/wise/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[21]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://xmlns.com/wot/0.1`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[22]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://www.w3.org/1999/02/22-rdf-syntax-ns`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[23]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `rdf-schema`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[24]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `XMLschema`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[25]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `OWL`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[26]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/dc/terms/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[27]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `VCARD`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }

                            if(striple.namespaces[28]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://www.geonames.org/ontology`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[29]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://search.yahoo.com/searchmonkey/commerce/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[30]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://search.yahoo.com/searchmonkey/media/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[31]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://cb.semsol.org/ns#`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[32]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://blogs.yandex.ru/schema/foaf/`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[33]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://www.w3.org/2003/01/geo/wgs84_pos#`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[34]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://rdfs.org/sioc/ns#`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[35]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://rdfs.org/sioc/types#`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[36]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://smw.ontoware.org/2005/smw#`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[37]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://purl.org/rss/1.0/`= ? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            if(striple.namespaces[38]){
                                stmt = conn.prepareStatement("UPDATE STATS SET `http://www.w3.org/2004/12/q/contentlabel#`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setBoolean(1,true);
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
                                stmt.executeUpdate();
                            }
                            
                            //we continue only for not null links
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
                            
                            stmt = conn.prepareStatement("UPDATE STATS SET `Categories_Contained_Query`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
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
                            stmt.executeUpdate();
                            
                            stmt = conn.prepareStatement("UPDATE STATS SET `Categories_TF`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
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
                            stmt.executeUpdate();
                            
                            stmt = conn.prepareStatement("UPDATE STATS SET `Entities_Contained_Query`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
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
                            stmt.executeUpdate();
                            
                            stmt = conn.prepareStatement("UPDATE STATS SET `Entities_TF`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
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
                            String check=stmt.toString();
                            stmt.executeUpdate();
                            
                            boolean flags_new=htm.gethtmlstats(links_total[j]);
                            if(flags_new){
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
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `iframes`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,iframes_number);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `number_embeded_vids`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,number_embeded_vid);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `scripts_cnt`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,scripts_cnt);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `schema.org_entities`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,nschem);
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
                                stmt.executeUpdate();
                               
                                stmt = conn.prepareStatement("UPDATE STATS SET `hcards`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,hcardsn);
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
                                stmt.executeUpdate();
                               
                                stmt = conn.prepareStatement("UPDATE STATS SET `hcalendars`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,hcalen);
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
                                stmt.executeUpdate();
                               
                                stmt = conn.prepareStatement("UPDATE STATS SET `hreviews`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,hrevn);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `hevents`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,hevenn);
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
                                stmt.executeUpdate();
                               
                                stmt = conn.prepareStatement("UPDATE STATS SET `hadresses`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,haddrn);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `hgeo`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,hgeon);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `hreltags`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,hreln);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `total_microformats`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,total_micron);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `Microformats-1`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,micron1);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `Microformats-2`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,micron2);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `Microdata`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,microd);
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
                                stmt.executeUpdate();
                                
                                stmt = conn.prepareStatement("UPDATE STATS SET `FOAF_HTML`=? WHERE `url`=? AND `query`=? AND `search_engine`=?" );
                                stmt.setInt(1,htm.foaf);
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
                                stmt.executeUpdate();
                                
                                
                                System.out.println("I created all cells");
                            }
                            
                        }
                    } 
                }

                if(ContentSemantics.get(3)||ContentSemantics.get(1)){
                //we perform LDA or TFIDF analysis to the links obtained
                    if(!enginechoice.get(3)){
                        if(enginechoice.get(2)){
                            ychk=ld.perform(links_yahoo, example_dir, quer, LSHrankSettings.get(1).intValue(), alpha, LSHrankSettings.get(0).doubleValue(), LSHrankSettings.get(2).intValue(), LSHrankSettings.get(3).intValue(),"yahoo",ContentSemantics.get(1),ContentSemantics.get(3),total_catent);
                            System.gc();
                        }
                        if(enginechoice.get(1)){
                            gchk=ld.perform(links_google, example_dir, quer, LSHrankSettings.get(1).intValue(), alpha, LSHrankSettings.get(0).doubleValue(), LSHrankSettings.get(2).intValue(), LSHrankSettings.get(3).intValue(),"google",ContentSemantics.get(1),ContentSemantics.get(3),total_catent);
                            System.gc();
                        }
                        if(enginechoice.get(0)){
                            bchk=ld.perform(links_bing, example_dir, quer, LSHrankSettings.get(1).intValue(), alpha, LSHrankSettings.get(0).doubleValue(), LSHrankSettings.get(2).intValue(), LSHrankSettings.get(3).intValue(),"bing",ContentSemantics.get(1),ContentSemantics.get(3),total_catent);
                            System.gc();
                        }
                    }
                    else{
                        System.gc();//links_total
                        tchk=ld.perform(links_total, example_dir, quer, LSHrankSettings.get(1).intValue(), alpha, LSHrankSettings.get(0).doubleValue(), LSHrankSettings.get(2).intValue(), LSHrankSettings.get(3).intValue(),"Merged",ContentSemantics.get(1),ContentSemantics.get(3),total_catent);
                        System.gc();
                    }
                }
            }
            System.gc();
            List<String> wordList=null;
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
                wordList = rk.readFile(example_dir, LSHrankSettings.get(4),LSHrankSettings.get(3).intValue(), LSHrankSettings.get(1).intValue());
                System.out.println("i returned the wordlist to search analysis");
            }
            return wordList;
        }  
        catch (NullPointerException ex) {
            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
            ArrayList<String> finalList = new ArrayList<String>();
            return finalList;
        } 
        catch (Exception ex) {
            Logger.getLogger(Search_analysis.class.getName()).log(Level.SEVERE, null, ex);
            ArrayList<String> finalList = new ArrayList<String>();
            return finalList;
        }
    }
}
