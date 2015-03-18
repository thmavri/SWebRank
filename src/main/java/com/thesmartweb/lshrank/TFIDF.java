/* 
 * Copyright 2015 themis.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
/**
 * Class for TFIDF analysis
 * @author Themistoklis Mavridis
 */
public class TFIDF {

    /**
     * a list with the top words recognized by TFIDF
     */
    protected List<String> topWordsList;

    /**
     * Method to calculate TF score
     * @param Doc the document to analyze
     * @param termToCheck the term to calculate tf for
     * @return th TF score
     */
    public double tfCalculator(String Doc, String termToCheck) {
        double count = 0;  //to count the overall occurrence of the term termToCheck
        String[] tokenizedTerms = Doc.toString().replaceAll("[\\W&&[^\\s]]", "").split("\\W+");   //to get individual terms
        for (String s : tokenizedTerms) {
            if (s.equalsIgnoreCase(termToCheck)) {
                count++;
            }
        }
        double tfvalue= Math.pow((count / tokenizedTerms.length),0.5);
        
        return tfvalue;
    }

    /**
     * Method to calculate idf score
     * @param allwordsList all the words
     * @param termToCheck the term to check for
     * @param NumberOfDocs the number of documents we analyze
     * @return the idf score
     */
    public double idfCalculator(List<List<String>> allwordsList, String termToCheck, int NumberOfDocs) {
        double count = 0;
        for (List<String> wordList : allwordsList){
            for (String s : wordList){
                if(s.equalsIgnoreCase(termToCheck)){
                    count++;
                    break;
                }
            }
        
        }
      
        double output=1+Math.log(NumberOfDocs/ (1+count));
        return output;
    }

    /**
     * Method to compute the TFIDF score
     * @param allDocs all the documents to analyze
     * @param topWords the amount of top words to get
     * @param directory the directory to save the output
     * @return a list with the top words
     */
    public List<String> compute(String[] allDocs,int topWords, String directory){
        try{
             List<List<String>> allwordsList = new ArrayList<>();
             int counterwords=0;
             int negtfidf=0;
             for(int i=0;i<allDocs.length;i++){
                 List<String> allwordsList_single = new ArrayList<>();
                 if(!(allDocs[i]==null)){
                    String stringtosplit = allDocs[i];
                    if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){       
                        stringtosplit=stringtosplit.replaceAll("[\\W&&[^\\s]]", "");
                        if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){
                            String[] tokenizedTerms=stringtosplit.split("\\W+");
                            for(int j=0;j<tokenizedTerms.length;j++){
                                if(!(tokenizedTerms[j]==null)&&(!(tokenizedTerms[j].equalsIgnoreCase("")))){
                                    allwordsList_single.add(tokenizedTerms[j]);
                                    counterwords++;
                                }    
                            }
                        }
                    }
                 }
                allwordsList.add(i,allwordsList_single);
             }
            
             HashMap<String, Double> wordTFIDFscores = new HashMap<>();
             List<String> topwordsTFIDF;
             topwordsTFIDF = new ArrayList<>();
             List<String> wordsTFIDF=new ArrayList<>();
             List<Double> TFIDFscoreslist;
             List<Double> TFIDFscoreslistcopy=new ArrayList<>();
             TFIDFscoreslist = new ArrayList<>();
             for(int i=0;i<allDocs.length;i++){
                 if(!(allDocs[i]==null)){
                    String stringtosplit = allDocs[i];
                    if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){       
                        stringtosplit=stringtosplit.replaceAll("[\\W&&[^\\s]]", "");
                        if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){
                            String[] tokenizedTerms=stringtosplit.split("\\W+"); 
                                for(int j=0;j<tokenizedTerms.length;j++){
                                    if(!(tokenizedTerms[j]==null)&&(!(tokenizedTerms[j].equalsIgnoreCase("")))){
                                        Double tfvalue=tfCalculator(allDocs[i],tokenizedTerms[j]);
                                        Double idfvalue=idfCalculator(allwordsList,tokenizedTerms[j],allDocs.length);
                                        Double tfidfvalue=tfvalue*idfvalue;
                                        if(tfidfvalue<0){negtfidf++;}
                                        TFIDFscoreslist.add(tfvalue.doubleValue());
                                        TFIDFscoreslistcopy.add(tfvalue.doubleValue());
                                        wordsTFIDF.add(tokenizedTerms[j]);
                                        if(wordTFIDFscores.get(tokenizedTerms[j])==null||wordTFIDFscores.get(tokenizedTerms[j]).doubleValue()>tfidfvalue){
                                            wordTFIDFscores.put(tokenizedTerms[j], tfidfvalue);
                                        }
                                    }
                                }
                        }
                    }
                }
             }
             DataManipulation shmap=new DataManipulation();
             topwordsTFIDF=shmap.sortHashmap(wordTFIDFscores).subList(0, topWords);
             topWordsList=topwordsTFIDF;        
             File file_words = new File(directory + "words.txt");
             FileUtils.writeLines(file_words,topWordsList);
             return topWordsList;
         } catch (IOException ex) {
             Logger.getLogger(TFIDF.class.getName()).log(Level.SEVERE, null, ex);
            
             return topWordsList;
         }
   
   
   }
}
