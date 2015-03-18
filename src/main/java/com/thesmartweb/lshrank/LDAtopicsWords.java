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
package com.thesmartweb.lshrank;

import java.io.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

/**
 * Class for LDA's results manipulation
 * @author themis
 */
public class LDAtopicsWords {

    /**
     * Method that gets the LDA results from the produced file by jgibblda
     * @param example_dir the directory to get the file from
     * @param prob_threshold the probability threshold to use in the selection of top words
     * @param top_words the amount of top words per topic to choose
     * @param nTopics the number of topics of LDA
     * @return a hashmap with every engine's topics and words per topic
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
                Logger.getLogger(LDAtopicsWords.class.getName()).log(Level.SEVERE, null, ex);
                HashMap<String,HashMap<Integer,HashMap<String,Double>>> enginetopicswordsprobmapempty = new HashMap<>();
                return enginetopicswordsprobmapempty;
            }
        }
        return enginetopicwordprobmap;
    }

    /**
     * Method to check if a string is a number
     * @param in stirng to check
     * @return true or false
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
