/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;

/**
 *
 * @author Themis Mavridis
 */
import com.snowtide.PDF;
import com.snowtide.pdf.Document;
import com.snowtide.pdf.OutputTarget;
import java.io.*;
import java.util.*;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.json.simple.JSONObject;

/**
 *
 * @author themis
 */
public class LinksParseAnalysis {

    /**
     *
     */
    public String url_check;

    /**
     *
     */
    protected List<String> topWordsTFIDF;

    /**
     *
     * @param total_links
     * @param example_dir
     * @param quer
     * @param nTopics
     * @param alpha
     * @param beta
     * @param niters
     * @param top_words
     * @param search_engine
     * @param LDAflag
     * @param TFIDFflag
     * @param total_catent
     * @return
     */
    public String[] perform(String[] total_links, String domain, String engine, String example_dir,String quer,int nTopics,double alpha,double beta,int niters,int top_words,String search_engine,boolean LDAflag,boolean TFIDFflag, String config_path){
        String[] parse_output = new String[total_links.length];
        try {
            System.gc();
            WebParser web = new WebParser();
            APIconn apicon = new APIconn();
            int counter_LDA_documents = 0;
            Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
            Client client = node.client();
            for (int i = 0; i < (total_links.length); i++) {
                parse_output[i]="";
                if (total_links[i] != null) {
                    System.out.println("Link: "+total_links[i]+"\n");
                    if (total_links[i].contains("http://www.youtube.com/watch?") || total_links[i].contains("http://www.imdb.com") || total_links[i].contains("cm-gc.com")|| total_links[i].contains(".ppt") || total_links[i].contains("indymedia") || total_links[i].contains(".pdf") || !apicon.check_conn(total_links[i]).contains("ok-conn")) {
                        if (total_links[i].contains("http://www.youtube.com/watch?")) {
                            String ventry = total_links[i].substring(31);
                            JSONparsing ypr = new JSONparsing();
                            url_check=total_links[i];
                            File current_url = new File(example_dir+ search_engine +"/" + i + "/"+ "current_url.txt");
                            FileUtils.writeStringToFile(current_url ,url_check);
                            parse_output[i] = ypr.GetYoutubeDetails(ventry);
                            System.gc();
                            if (parse_output[i]!=null) {
                                counter_LDA_documents++;
                                String directory = example_dir+ search_engine + "/" + i + "/";
                                File file_content_lda = new File(directory + "youtube_content.txt");
                                FileUtils.writeStringToFile(file_content_lda, parse_output[i]);
                            }
                        }
                        if (total_links[i].contains(".pdf")) {
                            String ventry = total_links[i].substring(31);
                            JSONparsing ypr = new JSONparsing();
                            url_check=total_links[i];
                            File current_url = new File(example_dir+ search_engine +"/" + i + "/"+ "current_url.txt");
                            FileUtils.writeStringToFile(current_url ,url_check);
                            File current_pdf = new File(example_dir+ search_engine +"/" + i + "/"+ "current_pdf.txt");
                            URL URLlink = new  URL(url_check);
                            FileUtils.copyURLToFile(URLlink, current_pdf);
                            Document pdf = PDF.open(current_pdf);
                            StringWriter buffer = new StringWriter();
                            pdf.pipe(new OutputTarget(buffer));
                            pdf.close();
                            parse_output[i] = buffer.toString().replace("\n", "").replace("\r", "");
                            Stopwords stopwords = new Stopwords();
                            parse_output[i] = stopwords.stop(parse_output[i]);
                            System.gc();
                            boolean deleteQuietly = FileUtils.deleteQuietly(current_pdf);
                            if (parse_output[i]!=null) {
                                counter_LDA_documents++;
                                String directory = example_dir+ search_engine + "/" + i + "/";
                                File file_content_lda = new File(directory + "pdf_content.txt");
                                FileUtils.writeStringToFile(file_content_lda, parse_output[i]);
                            }
                        }
                    }
                    else {
                        int number = i;
                        String directory = example_dir + search_engine  + "/" + number + "/";
                        System.out.println("Link:"+total_links[i]+"\n");
                        url_check=total_links[i];
                        File current_url = new File(directory+"current_url.txt");                               
                        FileUtils.writeStringToFile(current_url ,url_check);
                        System.gc();
                        parse_output[i] = web.Parse(url_check);
                        System.gc();
                        if (parse_output[i]!=null) {
                            counter_LDA_documents++;
                            directory = example_dir+ search_engine + "/" + i + "/";
                            //write the output from the html parsing
                            File file_content_lda = new File(directory + "html_parse_content.txt");
                            //
                            FileUtils.writeStringToFile(file_content_lda, parse_output[i]);
                        }
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("ParsedContent", parse_output[i]);
                    String id=domain+"/"+quer+"/"+engine+"/"+total_links[i];
                    IndexRequest indexReq=new IndexRequest("lshrankurlcontent","content",id);
                    indexReq.source(obj);
                    IndexResponse indexRes = client.index(indexReq).actionGet();
                }
            }
            node.close();
            String output_string_content = Integer.toString(counter_LDA_documents);
            TwitterAnalysis tw=new TwitterAnalysis();
            String twitter_txt=tw.perform(quer,config_path);
            for (int i = 0; i < parse_output.length; i++) {
                if (parse_output[i]!=null) {
                    output_string_content = output_string_content + "\n" + parse_output[i];
                }
            }
            if(!(twitter_txt.equalsIgnoreCase("fail"))){
                output_string_content = output_string_content + "\n" + twitter_txt;
            }
            String directory = example_dir + search_engine + "/";
            //call LDA
            File file_content_lda = new File(directory + "content_for_analysis.txt");
            FileUtils.writeStringToFile(file_content_lda, output_string_content);
            if(LDAflag){
                LDAcall ld = new LDAcall();
                ld.call(nTopics, alpha, beta, niters, top_words, directory);
            }
            else if(TFIDFflag){
                TFIDF tf=new TFIDF();
                topWordsTFIDF=tf.compute(parse_output,top_words,example_dir);
            }
            return parse_output;
        } catch (IOException ex) {
            Logger.getLogger(LinksParseAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            return parse_output;
        }
}

    /**
     *
     * @return
     */
    public List<String> return_topWordsTFIDF(){return topWordsTFIDF;}
}
