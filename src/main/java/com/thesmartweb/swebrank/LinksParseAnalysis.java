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
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.json.simple.JSONObject;

/**
 * Class to analyze the urls of the results of the Search Engine APIs
 * @author Themistoklis Mavridis
 */
public class LinksParseAnalysis {

    /**
     * the url to analyze
     */
    public String url_check;

    /**
     * the top words if we would like to use TF-IDF
     */
    protected List<String> topWordsTFIDF;

    /**
     * Method that exports the content from the urls provided and stores it in the ElasticSearch cluster of ours in a specific index
     * and calls the Semantic Analysis algorithm selected. Until now the method exports content from: 
     * -html
     * -youtube videos
     * -pdf files
     * @param total_links It contains all the links that we are going to analyze
     * @param domain The domain that we analyze
     * @param engine The search engine that we analyze the results from
     * @param example_dir It contains the directory where to save the results of the analysis
     * @param quer It contains the query for which the urls were the results for (it is used for the creation of the id in elasticsearch)
     * @param nTopics The number of topics for Latent Dirichlet Allocation
     * @param alpha The alpha value of LDA
     * @param beta The beta value of LDA
     * @param niters The number of iterations of LDA
     * @param top_words The amount of top words per topic to keep for LDA
     * @param LDAflag Flag if LDA is used
     * @param TFIDFflag Flag if TFIDF is used
     * @param config_path the path that contains the configuration files
     * @return the parsed output for each url provided
     */
    public String[] perform(String[] total_links, String domain, String engine, String example_dir,String quer,int nTopics,double alpha,double beta,int niters,int top_words,boolean LDAflag,boolean TFIDFflag, String config_path){
        String[] parse_output = new String[total_links.length];
        try {
            System.gc();
            WebParser web = new WebParser();//our web parser
            APIconn apicon = new APIconn();//our instance to check the connection to a url
            int counter_LDA_documents = 0;
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name","lshrankldacluster").build();
            Client client = new TransportClient(settings)
                    .addTransportAddress(new
                            InetSocketTransportAddress("localhost", 9300)
                    );
            
            //Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();//our elasticsearch node builder
           //Client client = node.client();//the client for elasticsearch node
            for (int i = 0; i < (total_links.length); i++) {
                parse_output[i]="";
                if (total_links[i] != null) {
                    System.out.println("Link: "+total_links[i]+"\n");
                    DataManipulation dm = new DataManipulation();
                    boolean structuredFiled = dm.StructuredFileCheck(total_links[i]);//we check if the url contains a structured document file type
                    
                    if (!apicon.check_conn(total_links[i]).contains("ok-conn")||structuredFiled||total_links[i].contains("http://www.youtube.com/watch?")) {
                        if (total_links[i].contains("http://www.youtube.com/watch?")) {//if the link is a youtube link we have to treat its JSON differently
                            String ventry = total_links[i].substring(31);
                            JSONparsing ypr = new JSONparsing();
                            url_check=total_links[i];
                            File current_url = new File(example_dir+ engine +"/" + i + "/"+ "current_url.txt");
                            FileUtils.writeStringToFile(current_url ,url_check);
                            parse_output[i] = ypr.GetYoutubeDetails(ventry);
                            System.gc();
                            if (parse_output[i]!=null) {
                                counter_LDA_documents++;
                                String directory = example_dir+ engine + "/" + i + "/";
                                File file_content_lda = new File(directory + "youtube_content.txt");
                                FileUtils.writeStringToFile(file_content_lda, parse_output[i]);
                            }
                        }
                        if (total_links[i].contains(".pdf")) {//if the link has a pdf we use Snowtide Pdf reader
                            url_check=total_links[i];
                            File current_url = new File(example_dir+ engine +"/" + i + "/"+ "current_url.txt");
                            FileUtils.writeStringToFile(current_url ,url_check);
                            File current_pdf = new File(example_dir+ engine +"/" + i + "/"+ "current_pdf.txt");
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
                            boolean deleteQuietly = FileUtils.deleteQuietly(current_pdf);//we delete the file after we read it
                            if (parse_output[i]!=null) {
                                counter_LDA_documents++;
                                String directory = example_dir+ engine + "/" + i + "/";
                                File file_content_lda = new File(directory + "pdf_content.txt");
                                FileUtils.writeStringToFile(file_content_lda, parse_output[i]);
                            }
                        }
                    }
                    else {//if the link does not follow to the cases above, we parse it using WebParser
                        int number = i;
                        String directory = example_dir + engine  + "/" + number + "/";
                        System.out.println("Link:"+total_links[i]+"\n");
                        url_check=total_links[i];
                        File current_url = new File(directory+"current_url.txt");                               
                        FileUtils.writeStringToFile(current_url ,url_check);
                        System.gc();
                        parse_output[i] = web.Parse(url_check);//we call the parser
                        System.gc();
                        if (parse_output[i]!=null) {
                            counter_LDA_documents++;//we count the amount of documents, as it is needed for JGibbLDA as seen in http://jgibblda.sourceforge.net/#2.3._Input_Data_Format
                            directory = example_dir+ engine + "/" + i + "/";
                            //write the output from the html parsing
                            File file_content_lda = new File(directory + "html_parse_content.txt");
                            FileUtils.writeStringToFile(file_content_lda, parse_output[i]);
                        }
                    }
                    JSONObject obj = new JSONObject();//an object to save the parsed content in elasticsearch
                    obj.put("ParsedContent", parse_output[i]);
                    String id=domain+"/"+quer+"/"+engine+"/"+total_links[i];
                    ReadInput ri = new ReadInput();
                    List<String> elasticIndexes=ri.GetKeyFile(config_path, "elasticSearchIndexes");
                    IndexRequest indexReq=new IndexRequest(elasticIndexes.get(4),"content",id);
                    indexReq.source(obj);
                    IndexResponse indexRes = client.index(indexReq).actionGet();
                }
            }
            //node.close();
            client.close();
            String output_string_content = Integer.toString(counter_LDA_documents);
            TwitterAnalysis tw=new TwitterAnalysis();//we are going gather info from Twitter using Twitter4j
            String twitter_txt=tw.perform(quer,config_path);
            for (int i = 0; i < parse_output.length; i++) {//we combine all the parsed content into one document
                if (parse_output[i]!=null) {
                    output_string_content = output_string_content + "\n" + parse_output[i];
                }
            }
            if(!(twitter_txt.equalsIgnoreCase("fail"))){
                output_string_content = output_string_content + "\n" + twitter_txt;//we add also the twitter content
            }
            String directory = example_dir + engine + "/";
            //call LDA
            File file_content_lda = new File(directory + "content_for_analysis.txt");//we are going to save the content also in txt format for backup and usage for LDA
            FileUtils.writeStringToFile(file_content_lda, output_string_content);
            if(LDAflag){
                LDAcall ld = new LDAcall();//we call lda
                ld.call(nTopics, alpha, beta, niters, top_words, directory);
            }
            else if(TFIDFflag){
                TFIDF tf=new TFIDF();//we call TFIDF
                topWordsTFIDF=tf.compute(parse_output,top_words,example_dir);
            }
            return parse_output;
        } catch (IOException | ElasticsearchException | ArrayIndexOutOfBoundsException ex) {
            Logger.getLogger(LinksParseAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            return parse_output;
        }
}

    /**
     * Method to return the topWords from TFIDF
     * @return topWords from TFIDF in a List
     */
    public List<String> return_topWordsTFIDF(){return topWordsTFIDF;}
}
