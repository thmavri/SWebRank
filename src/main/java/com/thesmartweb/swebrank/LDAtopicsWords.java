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

import java.io.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import java.util.stream.Stream;

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
    public HashMap<String,HashMap<Integer,HashMap<String,Double>>> readFile(String example_dir,Double prob_threshold,int top_words,int nTopics,int nTopTopics)  {
        DataManipulation getfiles=new DataManipulation();
        Collection<File> inputfiles = getfiles.getinputfiles(example_dir,"twords");
        String[] twordsarray=new String[inputfiles.size()];
        int j=0;
        for (File file : inputfiles){
            twordsarray[j]=file.getPath();
            j++;
        }
        int size = twordsarray.length * top_words * nTopics;
        String[] line = new String[size];
        File file_words = new File(example_dir + "words.txt");
        int k = 0;
        HashMap<String,HashMap<Integer,HashMap<String,Double>>> enginetopicwordprobmap=new HashMap<>();
        for (int i = 0; i < twordsarray.length; i++) {
            try {
                String engine="";
                if(twordsarray[i].toLowerCase().contains("bing")){
                    engine="bing";
                }
                if(twordsarray[i].toLowerCase().contains("google")){
                    engine="google";
                }
                if(twordsarray[i].toLowerCase().contains("yahoo")){
                    engine="yahoo";
                }
                Map<Integer,Double> TopicsAvgProb = getTopicsTotalProb(example_dir,nTopics,50,engine);
                Set<Map.Entry<Integer, Double>> TopicsAvgProbEntrySet = TopicsAvgProb.entrySet();
                Iterator it = TopicsAvgProbEntrySet.iterator();
                int topTopicsCounter=0;
                while(it.hasNext()){
                    topTopicsCounter++;
                    it.next();
                    if(topTopicsCounter>nTopTopics){
                        it.remove();
                    }
                }
                FileInputStream fstream = null;
                fstream = new FileInputStream(twordsarray[i]);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String test_line;
                //read the lines of the files and get the words that are not numbers
                int topicindex=-1;
                int topicindexprev=-1;
                HashMap<Integer, HashMap<String,Double>> topicwordsmulti = new HashMap<>();
                HashMap<String,Double> wordprobmap=new HashMap<>();
                boolean flagfirstline=true;
                while ((test_line = br.readLine()) != null) {
                    if (test_line.startsWith("Topic")){
                        String li = test_line.trim();
                        String index = li.split(" ")[1].trim();
                        topicindex = Integer.parseInt(index.split("th")[0].trim());
                        if(!flagfirstline&&TopicsAvgProb.containsKey(topicindexprev)){
                            topicwordsmulti.put(topicindexprev, wordprobmap);
                        }
                        topicindexprev = topicindex;
                        wordprobmap=new HashMap<>();
                        flagfirstline=false;
                    }
                    if (test_line.startsWith("\t")) {
                        String li = test_line.trim();
                        String word = li.split(" ")[0].trim();
                        boolean flag_check_number = this.checkIfNumber(word);
                        if (flag_check_number == false) {
                            Double wordprobability = Double.parseDouble(li.split(" ")[1].trim());
                            if (wordprobability.compareTo(prob_threshold)>0&&TopicsAvgProb.containsKey(topicindex)) {
                                wordprobmap.put(word, wordprobability);
                            }
                        }
                    }
                }
                if(TopicsAvgProb.containsKey(topicindex)){
                    topicwordsmulti.put(topicindex, wordprobmap);
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
    public Map<Integer,Double> getTopicsTotalProb(String example_dir, int nTopics,int nDocs,String engine){
        Map<Integer,Double> TopicsTotalProb = new HashMap<>();
        DataManipulation datamanipulation=new DataManipulation();
        Collection<File> inputfiles = datamanipulation.getinputfiles(example_dir,"theta");
        String[] thetaArray=new String[inputfiles.size()];
        int j=0;
        for (File file : inputfiles){
            thetaArray[j]=file.getPath();
            j++;
        }
        for (int i = 0; i < thetaArray.length; i++) {
            if(thetaArray[i].contains(engine)){
                try {
                    FileInputStream fstream = null;
                    fstream = new FileInputStream(thetaArray[i]);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String test_line;
                    boolean flagfirstline=true;
                    while ((test_line = br.readLine()) != null) {
                        String[] topicProbs=test_line.split(" ");
                        for(int k=0;k<topicProbs.length;k++){
                            if(flagfirstline){
                                double topicprob = Double.parseDouble(topicProbs[k]);
                                TopicsTotalProb.put(k, topicprob);
                            }
                            else{
                                Double currentTopicprob = TopicsTotalProb.get(k);
                                double topicprob = currentTopicprob + Double.parseDouble(topicProbs[k]);
                                TopicsTotalProb.put(k, topicprob);
                            }                                             
                        }
                        flagfirstline=false;
                    }
                    for(int k=0;k<nTopics;k++){
                        double currentTopicprob = TopicsTotalProb.get(k);
                        currentTopicprob = (double) currentTopicprob / (double) nDocs;
                        TopicsTotalProb.put(k, currentTopicprob);
                    }
                    //TopicsTotalProb = datamanipulation.sortHashMapByValuesD(TopicsTotalProb);
                    TopicsTotalProb = sortByValue(TopicsTotalProb);
                    int jasda=0;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(LDAtopicsWords.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LDAtopicsWords.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return TopicsTotalProb;
    
    }
    public static Map sortByValue(Map unsortedMap) {
        Map sortedMap = new TreeMap(new ValueComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }
}
class ValueComparator implements Comparator {
 
	Map map;
 
	public ValueComparator(Map map) {
		this.map = map;
	}
 
	public int compare(Object keyA, Object keyB) {
		Comparable valueA = (Comparable) map.get(keyA);
		Comparable valueB = (Comparable) map.get(keyB);
		return valueB.compareTo(valueA);
	}
}