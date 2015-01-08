/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author Administrator
 */
public class TFIDF {
    protected List<String> topWordsList;
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
   public List<String> compute(String[] allDocs,int topWords, String directory){
         try {
             
             List<List<String>> allwordsList = new ArrayList<List<String>>();
             
             //String[][] allwordsArray=new String[allDocs.length][];
             int counterwords=0;
             int negtfidf=0;
             for(int i=0;i<allDocs.length;i++){
                 List<String> allwordsList_single = new ArrayList<String>();
                 if(!(allDocs[i]==null)){
                    String stringtosplit = allDocs[i].toString();
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
            
             HashMap<String, Double> wordTFIDFscores = new HashMap<String, Double>();
             List<String> topwordsTFIDF;
             topwordsTFIDF = new ArrayList<String>();
             List<String> wordsTFIDF=new ArrayList<String>();
             List<Double> TFIDFscoreslist;
             List<Double> TFIDFscoreslistcopy=new ArrayList<Double>();
             TFIDFscoreslist = new ArrayList<Double>();
             for(int i=0;i<allDocs.length;i++){
                 if(!(allDocs[i]==null)){
                    String stringtosplit = allDocs[i].toString();
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
             //Collections.sort(TFIDFscoreslist);
             DataManipulation shmap=new DataManipulation();
             //use the code below if you would like to check the max entry of the hashmap
             /*
             Map.Entry<String,Double> maxEntry=null;
             for (Map.Entry<String, Double> entry : wordTFIDFscores.entrySet())
             {
                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                {
                     maxEntry = entry;
                }
             }*/
             topwordsTFIDF=shmap.sortHashmap(wordTFIDFscores).subList(0, topWords);
             
             
             /*String[] topWordsTFIDF=new String[topWords];
             List<String> topWordsTFIDFlist=new ArrayList<String>();
             int i=0;
             while(i<TFIDFscoreslist.size()&&topWordsTFIDFlist.size()<topWords){
                 Double score = TFIDFscoreslist.get(TFIDFscoreslist.size()-1-i);
                 int k=0;
                 int flag_found=0;
                 searchwhile:
                 while(k<TFIDFscoreslistcopy.size()&&flag_found==0){
                     if(TFIDFscoreslistcopy.get(k).doubleValue()==score){
                         if(!topWordsTFIDFlist.contains(wordsTFIDF.get(k).toString())){
                            topWordsTFIDFlist.add(wordsTFIDF.get(k).toString());
                            flag_found++;
                            System.out.println("Flag:"+flag_found);
                            break searchwhile;
                         }
                     }
                     //System.out.println("K:"+k);
                     k++;
                 }
                 System.out.println("I:"+i);
                 i++;
             }
             /*for(int i3=0;i3<topWords;i3++){
                 Double score = TFIDFscoreslist.get(TFIDFscoreslist.size()-1-i3);
                 int k=0;
                 int flag_found=0;
                 while(k<TFIDFscoreslistcopy.size()&&flag_found==0){
                     flag_found=0;
                     System.out.println(TFIDFscoreslistcopy.get(k));
                     if(TFIDFscoreslistcopy.get(k).doubleValue()==score){
                         topWordsTFIDF[i3]=wordsTFIDF.get(k); 
                         flag_found=1;
                     }
                     k++;
                 }
             }*/
             
             /*
             for(int i3=0; i3<topWords; i3++) {
                 Double score = TFIDFscoreslist.get(TFIDFscoreslist.size()-1-i3);
                 //Lookup original index efficiently
                 Set<Map.Entry<String, Double>> wordTFIDFentrySet = wordTFIDFscores.entrySet();
                 Iterator<Map.Entry<String, Double>> wordTFIDFiterator = wordTFIDFentrySet.iterator();
                 while(wordTFIDFiterator.hasNext()){
                     if(wordTFIDFiterator.next().getValue()==score){
                         topWordsTFIDF[i3]=wordTFIDFiterator.next().getKey();
                     }
                 }
             }*/
             //topWordsList=Arrays.asList(topWordsTFIDF);
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
