/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;
/**
 *
 * @author Themis Mavridis
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import org.apache.poi.POIDocument;
import org.apache.poi.hssf.usermodel.*;
import java.io.FileOutputStream;
import java.io.File;
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

public class Search_analysis {
    public String ychk;
    public String gchk;
    public String bchk;
    public String tchk;
    public List<String> perform(String example_dir, List<Boolean> enginechoice, String quer, int results_number, int top_visible,List<Double> LSHrankSettings,double alpha, List<Boolean> mozMetrics, int top_count_moz, boolean moz_threshold_option,double moz_threshold, List<Boolean> ContentSemantics, int SensebotConcepts){ //set the directory

        try {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            String url = "jdbc:mysql://localhost:3306/LSHrankDB";
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
            int flag_links_check=0;
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
                //we call the function that we are going to use for the headless browsing part
                //HEADLESS-----------------------------
                //Headlessbrowse headless=new Headlessbrowse();
                //we create the .xls work book
                HSSFWorkbook workbook=new HSSFWorkbook();
                HSSFSheet firstSheet = workbook.createSheet("Headless sheet");
                HSSFRow rowA= firstSheet.createRow(0);
                HSSFCell cell   = rowA.createCell(1);
                cell.setCellValue("iframes_number");
                cell = rowA.createCell(2);
                cell.setCellValue("number_embeded_vid");
                cell = rowA.createCell(3);
                cell.setCellValue("scripts_cnt");
                cell = rowA.createCell(4);
                cell.setCellValue("total semantic triples");
                cell = rowA.createCell(5);
                cell.setCellValue("number_links");
                cell = rowA.createCell(6);
                cell.setCellValue("redirect_links_number");
                cell = rowA.createCell(7);
                cell.setCellValue("internal_links");
                cell = rowA.createCell(8);
                cell.setCellValue("schema.org entities");
                cell   = rowA.createCell(9);
                cell.setCellValue("hcards");
                cell   = rowA.createCell(10);
                cell.setCellValue("hcalendars");
                cell   = rowA.createCell(11);
                cell.setCellValue("hreviews");
                cell   = rowA.createCell(12);
                cell.setCellValue("hevents");
                cell   = rowA.createCell(13);
                cell.setCellValue("hadresses");
                cell   = rowA.createCell(14);
                cell.setCellValue("hgeo");
                cell   = rowA.createCell(15);
                cell.setCellValue("hreltags");
                cell   = rowA.createCell(16);
                cell.setCellValue("total microformats");
                cell = rowA.createCell(17);
                cell.setCellValue("search engine");
                cell = rowA.createCell(18);
                cell.setCellValue("query");
                cell   = rowA.createCell(19);
                cell.setCellValue("nTopics");
                cell   = rowA.createCell(20);
                cell.setCellValue("alpha");
                cell   = rowA.createCell(21);
                cell.setCellValue("beta");
                cell   = rowA.createCell(22);
                cell.setCellValue("niters");
                cell   = rowA.createCell(23);
                cell.setCellValue("prob_threshold");
                cell   = rowA.createCell(24);
                cell.setCellValue("moz");
                cell   = rowA.createCell(25);
                cell.setCellValue("top_count_moz");
                cell   = rowA.createCell(26);
                cell.setCellValue("moz_threshold");
                cell   = rowA.createCell(27);
                cell.setCellValue("moz_threshold_option");
                cell   = rowA.createCell(28);
                cell.setCellValue("top_visible");
                cell   = rowA.createCell(29);
                cell.setCellValue("Domain_Authority");
                cell   = rowA.createCell(30);
                cell.setCellValue("External_MozRank");
                cell   = rowA.createCell(31);
                cell.setCellValue("MozRank");
                cell   = rowA.createCell(32);
                cell.setCellValue("MozTrust");
                cell   = rowA.createCell(33);
                cell.setCellValue("Page_Authority");
                cell   = rowA.createCell(34);
                cell.setCellValue("Subdomain_mozRank");
                cell   = rowA.createCell(35);
                cell.setCellValue("merged");
                cell   = rowA.createCell(36);
                cell.setCellValue("results_number");
                cell   = rowA.createCell(37);
                cell.setCellValue("Diffbotflag");
                cell   = rowA.createCell(38);
                cell.setCellValue("LDAflag");
                cell   = rowA.createCell(39);
                cell.setCellValue("Sensebotflag");
                cell   = rowA.createCell(40);
                cell.setCellValue("TFIDFflag");
                cell   = rowA.createCell(41);
                cell.setCellValue("SensebotConcepts");
                cell   = rowA.createCell(42);
                cell.setCellValue("Entities Contained Query");
                cell   = rowA.createCell(43);
                cell.setCellValue("Categories Contained Query");
                cell   = rowA.createCell(44);
                cell.setCellValue("Microformats-1");
                cell   = rowA.createCell(45);
                cell.setCellValue("Microformats-2");
                cell = rowA.createCell(46);
                cell.setCellValue("Microdata");
                cell = rowA.createCell(47);
                cell.setCellValue("http://purl.org/vocab/bio/0.1/");
                cell = rowA.createCell(48);
                cell.setCellValue("http://purl.org/dc/elements/1.1/");
                cell = rowA.createCell(49);
                cell.setCellValue("http://purl.org/coo/n");
                cell = rowA.createCell(50);
                cell.setCellValue("http://web.resource.org/cc/");
                cell = rowA.createCell(51);
                cell.setCellValue("http://diligentarguont.ontoware.org/2005/10/arguonto");  
                cell = rowA.createCell(52);
                cell.setCellValue("http://usefulinc.com/ns/doap");        
                cell = rowA.createCell(53);
                cell.setCellValue("http://xmlns.com/foaf/0.1/");
                cell = rowA.createCell(54);
                cell.setCellValue("http://purl.org/goodrelations/");
                cell = rowA.createCell(55);
                cell.setCellValue("http://purl.org/muto/core");
                cell = rowA.createCell(56);
                cell.setCellValue("http://webns.net/mvcb/");
                cell = rowA.createCell(57);
                cell.setCellValue("http://purl.org/ontology/mo/");
                cell = rowA.createCell(58);
                cell.setCellValue("http://purl.org/innovation/ns");    
                cell = rowA.createCell(59);
                cell.setCellValue("http://openguid.net/rdf");         
                cell = rowA.createCell(60);
                cell.setCellValue("http://www.slamka.cz/ontologies/diagnostika.owl");        
                cell = rowA.createCell(61);
                cell.setCellValue("http://purl.org/ontology/po/");       
                cell = rowA.createCell(62);
                cell.setCellValue("http://purl.org/net/provenance/ns");           
                cell = rowA.createCell(63);
                cell.setCellValue("http://purl.org/rss/1.0/modules/syndication");         
                cell = rowA.createCell(64); 
                cell.setCellValue("http://rdfs.org/sioc/ns");
                cell = rowA.createCell(65); 
                cell.setCellValue("http://madskills.com/public/xml/rss/module/trackback/");
                cell = rowA.createCell(66); 
                cell.setCellValue("http://rdfs.org/ns/void");        
                cell = rowA.createCell(67); 
                cell.setCellValue("http://www.fzi.de/2008/wise/"); 
                cell = rowA.createCell(68); 
                cell.setCellValue("http://xmlns.com/wot/0.1");        
                cell = rowA.createCell(69); 
                cell.setCellValue("http://www.w3.org/1999/02/22-rdf-syntax-ns");         
                cell = rowA.createCell(70);
                cell.setCellValue("rdf-schema");        
                cell = rowA.createCell(71);
                cell.setCellValue("XMLschema");
                cell = rowA.createCell(72);
                cell.setCellValue("OWL");
                cell = rowA.createCell(73);
                cell.setCellValue("http://purl.org/dc/terms/");
                cell = rowA.createCell(74);
                cell.setCellValue("VCARD");        
                cell = rowA.createCell(75);
                cell.setCellValue("http://www.geonames.org/ontology");        
                cell = rowA.createCell(76);
                cell.setCellValue("http://search.yahoo.com/searchmonkey/commerce/");          
                cell = rowA.createCell(77);
                cell.setCellValue("http://search.yahoo.com/searchmonkey/media/");
                cell = rowA.createCell(78);
                cell.setCellValue("http://cb.semsol.org/ns#");
                cell = rowA.createCell(79);
                cell.setCellValue("http://blogs.yandex.ru/schema/foaf/");
                cell = rowA.createCell(80);
                cell.setCellValue("http://www.w3.org/2003/01/geo/wgs84_pos#");
                cell = rowA.createCell(81);
                cell.setCellValue("http://rdfs.org/sioc/ns#");
                cell = rowA.createCell(82);
                cell.setCellValue("http://rdfs.org/sioc/types#");
                cell = rowA.createCell(83);
                cell.setCellValue("http://smw.ontoware.org/2005/smw#");
                cell = rowA.createCell(84);
                cell.setCellValue("http://purl.org/rss/1.0/");
                cell = rowA.createCell(85);
                cell.setCellValue("http://www.w3.org/2004/12/q/contentlabel#");
                cell = rowA.createCell(86);
                cell.setCellValue("FOAF HTML");
                //we are going to use a mozilla browser
                //HEADLESS-----------------------------
                //Browser browser = BrowserFactory.createBrowser(BrowserType.Mozilla);
                //we are going to build a DOM
                //DOMBuilder builder = new DOMBuilder(); 
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
                YahooEntityCategory yec=new YahooEntityCategory();
                for(int j=0;j<links_total.length;j++){
                    //we continue only for not null links
                    rowA= firstSheet.createRow(j+1);
                    cell   = rowA.createCell(0);
                    cell.setCellValue(links_total[j]);
                    String[] catentities=new String[2];
                    catentities[0]="";
                    catentities[1]="";
                    String var="url";
                    String value="links_total[j]";
                    stmt = conn.prepareStatement("INSERT INTO STATS("+var+") VALUES(?)");
                    stmt.setString(1,value);
                    stmt.executeUpdate();
                    if(links_total[j]!=null){
                        flag_links_check=1;//a flag used in order to know that the link was not null
                        //HEADLESS-----------------------------
                        //flags = headless.execute(links_total[j],browser, builder);
                        //we print that we are back in search analysis
                        //System.out.println("I am back into search analysis");
                        if(htm.checkconn(links_total[j])){
                            nlinks=htm.getnlinks(links_total[j]);
                            stmt = conn.prepareStatement("INSERT INTO STATS(number_links) VALUES(?)");
                            stmt.setInt(1,nlinks[0]);
                            stmt.executeUpdate();
                            stmt = conn.prepareStatement("INSERT INTO STATS(external_links) VALUES(?)");
                            stmt.setInt(1,nlinks[0]-nlinks[1]);
                            stmt.executeUpdate();
                            stmt = conn.prepareStatement("INSERT INTO STATS(internal_links) VALUES(?)");
                            stmt.setInt(1,nlinks[1]);
                            stmt.executeUpdate();
                            stmt = conn.prepareStatement("INSERT INTO STATS(number_links) VALUES(?)");
                            stmt.setInt(1,nlinks[0]);
                            stmt.executeUpdate();
                            boolean flags_new=htm.gethtmlstats(links_total[j]);
                            if(flags_new){
                                System.out.println("I got the html stats for the "+j+" link:"+links_total[j]+"\n"); 
                                int iframes_number = htm.frames_number;
                                stmt = conn.prepareStatement("INSERT INTO STATS(iframes) VALUES(?)");
                                stmt.setInt(1,iframes_number);
                                stmt.executeUpdate();
                                //int links_number_int = headless.get_links_number_int();
                                int number_embeded_vid = htm.number_embeded_videos;
                                stmt = conn.prepareStatement("INSERT INTO STATS(number_embeded_vids) VALUES(?)");
                                stmt.setInt(1,number_embeded_vid);
                                stmt.executeUpdate();
                                //**************
                                int nauthority=0;
                                //*********************
                                //int redirect_links_number = headless.get_redirect_links_number();
                                int scripts_cnt = htm.scripts_number;
                                stmt = conn.prepareStatement("INSERT INTO STATS(scripts_cnt) VALUES(?)");
                                stmt.setInt(1,scripts_cnt);
                                stmt.executeUpdate();
                                int ntriples=striple.getsindicestats(links_total[j]);
                                stmt = conn.prepareStatement("INSERT INTO STATS(total_semantic_triples) VALUES(?)");
                                stmt.setInt(1,ntriples);
                                stmt.executeUpdate();
                                int nschem=htm.nschem;
                                stmt = conn.prepareStatement("INSERT INTO STATS(schema.org_entities) VALUES(?)");
                                stmt.setInt(1,nschem);
                                stmt.executeUpdate();
                                int hcardsn=htm.hcardsn;
                                stmt = conn.prepareStatement("INSERT INTO STATS(hcards) VALUES(?)");
                                stmt.setInt(1,hcardsn);
                                stmt.executeUpdate();
                                int hcalen=htm.hcalen;
                                stmt = conn.prepareStatement("INSERT INTO STATS(hcalendars) VALUES(?)");
                                stmt.setInt(1,hcalen);
                                stmt.executeUpdate();
                                int hrevn=htm.hrevn;
                                stmt = conn.prepareStatement("INSERT INTO STATS(hreviews) VALUES(?)");
                                stmt.setInt(1,hrevn);
                                stmt.executeUpdate();
                                int hevenn=htm.hevenn;
                                stmt = conn.prepareStatement("INSERT INTO STATS(hevents) VALUES(?)");
                                stmt.setInt(1,hevenn);
                                stmt.executeUpdate();
                                int haddrn=htm.haddrn;
                                stmt = conn.prepareStatement("INSERT INTO STATS(hadresses) VALUES(?)");
                                stmt.setInt(1,haddrn);
                                stmt.executeUpdate();
                                int hgeon=htm.hgeon;
                                stmt = conn.prepareStatement("INSERT INTO STATS(hgeo) VALUES(?)");
                                stmt.setInt(1,hgeon);
                                stmt.executeUpdate();
                                int hreln=htm.hreln;
                                stmt = conn.prepareStatement("INSERT INTO STATS(hreltags) VALUES(?)");
                                stmt.setInt(1,hreln);
                                stmt.executeUpdate();
                                int total_micron=htm.total_micron;
                                stmt = conn.prepareStatement("INSERT INTO STATS(total_microformats) VALUES(?)");
                                stmt.setInt(1,total_micron);
                                stmt.executeUpdate();
                                int micron1=htm.micron1;
                                stmt = conn.prepareStatement("INSERT INTO STATS(Microformats-1) VALUES(?)");
                                stmt.setInt(1,micron1);
                                stmt.executeUpdate();
                                int micron2=htm.micron2;
                                stmt = conn.prepareStatement("INSERT INTO STATS(Microformats-2) VALUES(?)");
                                stmt.setInt(1,micron2);
                                stmt.executeUpdate();
                                int microd=htm.microd;
                                stmt = conn.prepareStatement("INSERT INTO STATS(Microdata) VALUES(?)");
                                stmt.setInt(1,microd);
                                stmt.executeUpdate();
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
                                stmt = conn.prepareStatement("INSERT INTO STATS(Categories_Contained_Query) VALUES(?)");
                                stmt.setInt(1,cat_cnt);
                                stmt.executeUpdate();
                                stmt = conn.prepareStatement("INSERT INTO STATS(Entities_Contained_Query) VALUES(?)");
                                stmt.setInt(1,ent_cnt);
                                stmt.executeUpdate();
                                
                                //System.out.println("I am out of all the headless functions");
                                //we create all the rows, cells of the .xls file
                                cell   = rowA.createCell(1);
                                cell.setCellValue(iframes_number);
                                cell   = rowA.createCell(2);
                                cell.setCellValue(number_embeded_vid);
                                cell   = rowA.createCell(3);
                                cell.setCellValue(scripts_cnt);
                                cell   = rowA.createCell(4);
                                cell.setCellValue(ntriples);
                                cell   = rowA.createCell(5);
                                cell.setCellValue(nlinks[0]);
                                cell   = rowA.createCell(6);
                                cell.setCellValue(nlinks[0]-nlinks[1]);
                                cell   = rowA.createCell(7);
                                cell.setCellValue(nlinks[1]);
                                cell   = rowA.createCell(8);
                                cell.setCellValue(nschem);
                                cell   = rowA.createCell(9);
                                cell.setCellValue(hcardsn);
                                cell   = rowA.createCell(10);
                                cell.setCellValue(hcalen);
                                cell   = rowA.createCell(11);
                                cell.setCellValue(hrevn);
                                cell   = rowA.createCell(12);
                                cell.setCellValue(hevenn);
                                cell   = rowA.createCell(13);
                                cell.setCellValue(haddrn);
                                cell   = rowA.createCell(14);
                                cell.setCellValue(hgeon);
                                cell   = rowA.createCell(15);
                                cell.setCellValue(hreln);
                                cell   = rowA.createCell(16);
                                cell.setCellValue(total_micron);
                                cell   = rowA.createCell(17);
                                cell.setCellValue(j);
                                stmt = conn.prepareStatement("INSERT INTO STATS(search_engine) VALUES(?)");
                                stmt.setInt(1,j);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(18);
                                cell.setCellValue(quer);
                                stmt = conn.prepareStatement("INSERT INTO STATS(query) VALUES(?)");
                                stmt.setString(1,quer);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(19);
                                cell.setCellValue(LSHrankSettings.get(1));
                                stmt = conn.prepareStatement("INSERT INTO STATS(nTopics) VALUES(?)");
                                stmt.setInt(1,LSHrankSettings.get(1).intValue());
                                stmt.executeUpdate();
                                cell   = rowA.createCell(20);
                                cell.setCellValue(alpha);
                                stmt = conn.prepareStatement("INSERT INTO STATS(alpha) VALUES(?)");
                                stmt.setDouble(1,alpha);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(21);
                                cell.setCellValue(LSHrankSettings.get(0));
                                stmt = conn.prepareStatement("INSERT INTO STATS(beta) VALUES(?)");
                                stmt.setDouble(1,LSHrankSettings.get(0));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(22);
                                cell.setCellValue(LSHrankSettings.get(2));
                                stmt = conn.prepareStatement("INSERT INTO STATS(niters) VALUES(?)");
                                stmt.setInt(1,LSHrankSettings.get(2).intValue());
                                stmt.executeUpdate();
                                cell   = rowA.createCell(23);
                                cell.setCellValue(LSHrankSettings.get(3));
                                stmt = conn.prepareStatement("INSERT INTO STATS(prob_threshold) VALUES(?)");
                                stmt.setDouble(1,LSHrankSettings.get(3));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(24);
                                cell.setCellValue(mozMetrics.get(0));
                                stmt = conn.prepareStatement("INSERT INTO STATS(moz) VALUES(?)");
                                stmt.setBoolean(1,mozMetrics.get(0));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(25);
                                cell.setCellValue(top_count_moz);
                                stmt = conn.prepareStatement("INSERT INTO STATS(top_count_moz) VALUES(?)");
                                stmt.setInt(1,top_count_moz);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(26);
                                cell.setCellValue(moz_threshold);
                                stmt = conn.prepareStatement("INSERT INTO STATS(moz_threshold) VALUES(?)");
                                stmt.setDouble(1,moz_threshold);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(27);
                                cell.setCellValue(moz_threshold_option);
                                stmt = conn.prepareStatement("INSERT INTO STATS(moz_threshold_option) VALUES(?)");
                                stmt.setBoolean(1,moz_threshold_option);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(28);
                                cell.setCellValue(top_visible);
                                stmt = conn.prepareStatement("INSERT INTO STATS(top_visible) VALUES(?)");
                                stmt.setInt(1,top_visible);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(29);
                                cell.setCellValue(mozMetrics.get(1));
                                stmt = conn.prepareStatement("INSERT INTO STATS(Domain_Authority) VALUES(?)");
                                stmt.setBoolean(1,mozMetrics.get(1));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(30);
                                cell.setCellValue(mozMetrics.get(2));
                                stmt = conn.prepareStatement("INSERT INTO STATS(External_MozRank) VALUES(?)");
                                stmt.setBoolean(1,mozMetrics.get(2));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(31);
                                cell.setCellValue(mozMetrics.get(3));
                                 stmt = conn.prepareStatement("INSERT INTO STATS(MozRank) VALUES(?)");
                                stmt.setBoolean(1,mozMetrics.get(3));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(32);
                                cell.setCellValue(mozMetrics.get(4));
                                 stmt = conn.prepareStatement("INSERT INTO STATS(MozTrust) VALUES(?)");
                                stmt.setBoolean(1,mozMetrics.get(4));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(33);
                                cell.setCellValue(mozMetrics.get(5));
                                stmt = conn.prepareStatement("INSERT INTO STATS(Page_Authority) VALUES(?)");
                                stmt.setBoolean(1,mozMetrics.get(5));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(34);
                                cell.setCellValue(mozMetrics.get(6));
                                stmt = conn.prepareStatement("INSERT INTO STATS(Subdomain_mozRank) VALUES(?)");
                                stmt.setBoolean(1,mozMetrics.get(6));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(35);
                                cell.setCellValue(enginechoice.get(3));
                                stmt = conn.prepareStatement("INSERT INTO STATS(merged) VALUES(?)");
                                stmt.setBoolean(1,enginechoice.get(3));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(36);
                                cell.setCellValue(results_number);
                                stmt = conn.prepareStatement("INSERT INTO STATS(results_number) VALUES(?)");
                                stmt.setInt(1,results_number);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(37);
                                cell.setCellValue(ContentSemantics.get(0));
                                stmt = conn.prepareStatement("INSERT INTO STATS(Diffbotflag) VALUES(?)");
                                stmt.setBoolean(1,ContentSemantics.get(0));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(38);
                                cell.setCellValue(ContentSemantics.get(1));
                                stmt = conn.prepareStatement("INSERT INTO STATS(LDAflag) VALUES(?)");
                                stmt.setBoolean(1,ContentSemantics.get(1));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(39);
                                cell.setCellValue(ContentSemantics.get(2));
                                stmt = conn.prepareStatement("INSERT INTO STATS(Sensebotflag) VALUES(?)");
                                stmt.setBoolean(1,ContentSemantics.get(2));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(40);
                                cell.setCellValue(ContentSemantics.get(3));
                                stmt = conn.prepareStatement("INSERT INTO STATS(TFIDFflag) VALUES(?)");
                                stmt.setBoolean(1,ContentSemantics.get(3));
                                stmt.executeUpdate();
                                cell   = rowA.createCell(41);
                                cell.setCellValue(SensebotConcepts);
                                stmt = conn.prepareStatement("INSERT INTO STATS(SensebotConcepts) VALUES(?)");
                                stmt.setInt(1,SensebotConcepts);
                                stmt.executeUpdate();
                                cell   = rowA.createCell(42);
                                cell.setCellValue(cat_cnt);
                                cell   = rowA.createCell(43);
                                cell.setCellValue(ent_cnt);
                                cell   = rowA.createCell(44);
                                cell.setCellValue(micron1);
                                cell   = rowA.createCell(45);
                                cell.setCellValue(micron2);
                                cell = rowA.createCell(46);
                                cell.setCellValue(microd);
                                int cellno=46;
                                for(int nsi=0;nsi<striple.namespaces.length;nsi++){
                                    cellno=nsi+47;
                                    cell = rowA.createCell(cellno);
                                    cell.setCellValue(striple.namespaces[nsi]);
                                }
                                
                                if(striple.namespaces[0]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/vocab/bio/0.1/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[1]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/dc/elements/1.1/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[2]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/coo/n) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[3]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://web.resource.org/cc/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[4]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://diligentarguont.ontoware.org/2005/10/arguonto) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[5]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://usefulinc.com/ns/doap) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[6]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://xmlns.com/foaf/0.1/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[7]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/goodrelations/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[8]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/muto/core) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[9]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://webns.net/mvcb/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[10]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/ontology/mo/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[11]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/innovation/ns) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[12]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://openguid.net/rdf) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[13]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://www.slamka.cz/ontologies/diagnostika.owl) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[14]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/ontology/po/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[15]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/net/provenance/ns) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[16]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/rss/1.0/modules/syndication) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[17]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://rdfs.org/sioc/ns) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[18]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://madskills.com/public/xml/rss/module/trackback/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[19]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://rdfs.org/ns/void) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[20]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://www.fzi.de/2008/wise/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[21]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://xmlns.com/wot/0.1) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[22]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://www.w3.org/1999/02/22-rdf-syntax-ns) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[23]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(rdf-schema) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[24]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(XMLschema) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[25]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(OWL) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[26]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/dc/terms/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[27]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(VCARD) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                
                                if(striple.namespaces[28]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://www.geonames.org/ontology) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[29]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://search.yahoo.com/searchmonkey/commerce/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[30]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://search.yahoo.com/searchmonkey/media/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[31]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://cb.semsol.org/ns#) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[32]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://blogs.yandex.ru/schema/foaf/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[33]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://www.w3.org/2003/01/geo/wgs84_pos#) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[34]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://rdfs.org/sioc/ns#) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[35]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://rdfs.org/sioc/types#) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[36]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://smw.ontoware.org/2005/smw#) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[37]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://purl.org/rss/1.0/) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[38]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://www.w3.org/2004/12/q/contentlabel#) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }
                                if(striple.namespaces[39]=true){
                                    stmt = conn.prepareStatement("INSERT INTO STATS(http://www.w3.org/2004/12/q/contentlabel#) VALUES(?)");
                                    stmt.setBoolean(1,true);
                                    stmt.executeUpdate();
                                }                                
                                cell = rowA.createCell(cellno+1);
                                cell.setCellValue(htm.foaf);
                                stmt = conn.prepareStatement("INSERT INTO STATS(FOAF_HTML) VALUES(?)");
                                stmt.setInt(1,htm.foaf);
                                stmt.executeUpdate();
                                System.out.println("I created all cells");
                            }
                            else{
                                int ntriples=striple.getsindicestats(links_total[j]);
                                catentities=yec.connect(links_total[j],quer);
                                for(int okk=0;okk<catentities.length;okk++){
                                    if(!(catentities[okk].isEmpty())){
                                        DataManipulation txtpro = new DataManipulation();
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
                                cell   = rowA.createCell(1);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(2);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(3);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(4);
                                cell.setCellValue(ntriples);
                                cell   = rowA.createCell(5);
                                cell.setCellValue(nlinks[0]);
                                cell   = rowA.createCell(6);
                                cell.setCellValue(nlinks[0]-nlinks[1]);
                                cell   = rowA.createCell(7);
                                cell.setCellValue(nlinks[1]);
                                cell   = rowA.createCell(8);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(9);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(10);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(11);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(12);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(13);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(14);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(15);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(16);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(17);
                                cell.setCellValue(j);
                                cell   = rowA.createCell(18);
                                cell.setCellValue(quer);
                                cell   = rowA.createCell(19);
                                cell.setCellValue(LSHrankSettings.get(1));
                                cell   = rowA.createCell(20);
                                cell.setCellValue(alpha);
                                cell   = rowA.createCell(21);
                                cell.setCellValue(LSHrankSettings.get(0));
                                cell   = rowA.createCell(22);
                                cell.setCellValue(LSHrankSettings.get(2));
                                cell   = rowA.createCell(23);
                                cell.setCellValue(LSHrankSettings.get(3));
                                cell   = rowA.createCell(24);
                                cell.setCellValue(mozMetrics.get(0));
                                cell   = rowA.createCell(25);
                                cell.setCellValue(top_count_moz);
                                cell   = rowA.createCell(26);
                                cell.setCellValue(moz_threshold);
                                cell   = rowA.createCell(27);
                                cell.setCellValue(moz_threshold_option);
                                cell   = rowA.createCell(28);
                                cell.setCellValue(top_visible);
                                cell   = rowA.createCell(29);
                                cell.setCellValue(mozMetrics.get(1));
                                cell   = rowA.createCell(30);
                                cell.setCellValue(mozMetrics.get(2));
                                cell   = rowA.createCell(31);
                                cell.setCellValue(mozMetrics.get(3));
                                cell   = rowA.createCell(32);
                                cell.setCellValue(mozMetrics.get(4));
                                cell   = rowA.createCell(33);
                                cell.setCellValue(mozMetrics.get(5));
                                cell   = rowA.createCell(34);
                                cell.setCellValue(mozMetrics.get(6));
                                cell   = rowA.createCell(35);
                                cell.setCellValue(enginechoice.get(3));
                                cell   = rowA.createCell(36);
                                cell.setCellValue(results_number);
                                cell   = rowA.createCell(37);
                                cell.setCellValue(ContentSemantics.get(0));
                                cell   = rowA.createCell(38);
                                cell.setCellValue(ContentSemantics.get(1));
                                cell   = rowA.createCell(39);
                                cell.setCellValue(ContentSemantics.get(2));
                                cell   = rowA.createCell(40);
                                cell.setCellValue(ContentSemantics.get(3));
                                cell   = rowA.createCell(41);
                                cell.setCellValue(SensebotConcepts);
                                cell   = rowA.createCell(42);
                                cell.setCellValue(cat_cnt);
                                cell   = rowA.createCell(43);
                                cell.setCellValue(ent_cnt);
                                cell   = rowA.createCell(44);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(45);
                                cell.setCellValue(-10);
                                cell   = rowA.createCell(46);
                                cell.setCellValue(-10);
                                int cellno=46;
                                for(int nsi=0;nsi<striple.namespaces.length;nsi++){
                                    cellno=nsi+47;
                                    cell = rowA.createCell(cellno);
                                    cell.setCellValue(striple.namespaces[nsi]);
                                }
                                cell = rowA.createCell(cellno+1);
                                cell.setCellValue(-10);
                                System.out.println("I created unexecuted cells");
                            }
                        }
                        /*if(flag_links_check>0){
                            //we try to dispose the browser in order to remove useless data of him
                            System.out.println("I ll try to dispose");
                            //browser.dispose();

                            System.out.println("I have disposed");
                            //we create a new browser to be used
                            //browser = BrowserFactory.createBrowser(BrowserType.Mozilla);
                            //System.out.println("the new browser is ready");
                         }*//*
                            if((flag_links_check>0)&&(flags[1]==1)){
                                    //we try to dispose the browser in order to remove useless data of him
                                    System.out.println("I ll try to dispose");
                                    browser.dispose();
                                    System.out.println("I have disposed");
                                    //we create a new browser to be used
                                    browser = BrowserFactory.createBrowser(BrowserType.Mozilla);
                                    System.out.println("the new browser is ready");
                             }
                             else if(flags[1]==0){
                                 //we do not dispose the browser but maybe we should?
                                System.out.println("document stuck and dont dispose");
                             }*/
                    } 
                }
                //----save the above to excel file
                File file_xls = new File(example_dir + "workbook"+quer+".xls");
                try (FileOutputStream fileOut = new FileOutputStream(file_xls)) {
                    workbook.write(fileOut);
                }    
                System.out.println("I saved the xls");

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
