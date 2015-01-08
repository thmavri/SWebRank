/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;

/**
 *
 * @author Themis Mavridis
 */
import java.io.*;
import java.util.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

public class LinksParseAnalysis {
    public String url_check;
    protected List<String> topWordsTFIDF;
public String perform(String[] total_links,String example_dir,String quer,int nTopics,double alpha,double beta,int niters,int top_words,String search_engine,boolean LDAflag,boolean TFIDFflag, String[][] total_catent){
        try {
            System.gc();
            WebParser web = new WebParser();
            String chk = null;
            String[] parse_output = new String[total_links.length];
            APIconn apicon = new APIconn();
            int counter_LDA_documents = 0;
            for (int i = 0; i < (total_links.length); i++) {
                try {
                    parse_output[i]="";
                    if (total_links[i] != null) {
                        for(int p=0;p<total_catent.length;p++){
                            if(total_catent[p][0].equalsIgnoreCase(total_links[i])){
                                parse_output[i]=parse_output[i]+total_catent[p][1];
                            }
                        }
                        if (total_links[i].contains("http://www.youtube.com/watch?") || total_links[i].contains("http://www.imdb.com") || total_links[i].contains("cm-gc.com")|| total_links[i].contains(".ppt") || total_links[i].contains(".pdf") || !apicon.check_conn(total_links[i]).contains("ok-conn")) {
                            if (total_links[i].contains("http://www.youtube.com/watch?")) {
                                String ventry = total_links[i].substring(31);
                                JSONparsing ypr = new JSONparsing();
                                System.gc();
                                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&"+total_links[i]+"&&&&&&&&&&&&&");
                                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                                url_check=total_links[i];
                                File current_url = new File(example_dir+ search_engine +"/" + i + "/"+ "current_url.txt");
                                FileUtils.writeStringToFile(current_url ,url_check);
                                parse_output[i] = ypr.GetYoutubeDetails(ventry);
                                System.gc();
                                chk = "ok";
                                if (parse_output[i]!=null) {
                                    counter_LDA_documents++;
                                    String directory = example_dir+ search_engine + "/" + i + "/";
                                    File file_content_lda = new File(directory + "youtube_content.txt");
                                    FileUtils.writeStringToFile(file_content_lda, parse_output[i]);
                                }
                            }
                        }
                        else {
                                
                                int number = i;
                                String directory = example_dir + search_engine  + "/" + number + "/";
                                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&"+total_links[i]+"&&&&&&&&&&&&&");
                                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
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
                                } else {
                                    chk = parse_output[i];
                                }
                            }
                        }
                    }
                 catch (IOException ex) {
                    Logger.getLogger(GoogleResults.class.getName()).log(Level.SEVERE, null, ex);
                    chk = null;
                    return chk;
                }
            }
            String output_string_content = Integer.toString(counter_LDA_documents);
            TwitterAnalysis tw=new TwitterAnalysis();
            String twitter_txt=tw.perform(quer);
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
            return chk="ok";
        } catch (IOException ex) {
            Logger.getLogger(LinksParseAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            String chk=null;
            return chk;
        }
}
public List<String> return_topWordsTFIDF(){return topWordsTFIDF;}
}
