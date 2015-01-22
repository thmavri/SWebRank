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
import java.util.ArrayList;
import java.io.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import java.util.*;
import java.util.List;

/**
 *
 * @author themis
 */
public class ReadKeys {

    /**
     *
     * @param example_dir
     * @param prob_threshold
     * @param top_words
     * @param nTopics
     * @return
     */
    public HashMap<String,HashMap<Integer,HashMap<String,Double>>> readFile(String example_dir,Double prob_threshold,int top_words,int nTopics)  {
        DataManipulation getfiles=new DataManipulation();
        Collection<File> inputfiles = getfiles.getinputfiles(example_dir,"twords");
        String[] inarr=new String[inputfiles.size()];
        int j=0;
        for (File file : inputfiles){
            inarr[j]=file.getPath();
            j++;
        }
        int size = inarr.length * top_words * nTopics;
        
        String[] line = new String[size];
        File file_words = new File(example_dir + "words.txt");
        int k = 0;
        HashMap<String,HashMap<Integer,HashMap<String,Double>>> enginetopicwordprobmap=new HashMap<>();
        for (int i = 0; i < inarr.length; i++) {
            try {
                
                FileInputStream fstream = null;
                fstream = new FileInputStream(inarr[i]);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String test_line;
                //read the lines of the files and get the words that are not numbers
                int topicindex=-1;
                HashMap<Integer, HashMap<String,Double>> topicwordsmulti = new HashMap<>();
                HashMap<String,Double> wordprobmap=new HashMap<String,Double>();
                
                while ((test_line = br.readLine()) != null) {
                    if (test_line.startsWith("Topic")){
                        topicindex++;
                        wordprobmap=new HashMap<String,Double>();
                    }
                    if (test_line.startsWith("\t")) {
                        String li = test_line.trim();
                        String word = li.split(" ")[0].trim();
                        boolean flag_check_number = this.checkIfNumber(word);
                        if (flag_check_number == false) {
                            Double wordprobability = Double.parseDouble(li.split(" ")[1].trim());
                            if (wordprobability.compareTo(prob_threshold)>0) {
                                wordprobmap.put(word, wordprobability);
                                topicwordsmulti.put(topicindex, wordprobmap);
                                line[k] = word;
                                k = k + 1;
                            }
                        }
                    }
                }
                String engine="";
                if(inarr[i].toLowerCase().contains("bing")){
                    engine="bing";
                }
                if(inarr[i].toLowerCase().contains("google")){
                    engine="google";
                }
                if(inarr[i].toLowerCase().contains("yahoo")){
                    engine="yahoo";
                }
                enginetopicwordprobmap.put(engine, topicwordsmulti);
                
            } catch (IOException ex) {
                Logger.getLogger(ReadKeys.class.getName()).log(Level.SEVERE, null, ex);
                HashMap<String,HashMap<Integer,HashMap<String,Double>>> enginetopicswordsprobmapempty = new HashMap<>();
                return enginetopicswordsprobmapempty;
            }
        }
        return enginetopicwordprobmap;
    }

    /**
     *
     * @param in
     * @return
     */
    public boolean checkIfNumber(String in) {

        try {

           Double.parseDouble(in);

        } catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }
}
