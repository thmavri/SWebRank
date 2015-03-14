/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author themis
 */
public class LDAsemStats {
        private int lda_top_words_parsed=0;
        private int ent_cnt=0;
        private int cat_cnt=0;
        private int lda_top_words_parsed_percentage=0;
     public void getEntCatStats(List<String> Entities,List<String> Categories,List<String> lda_output, boolean StemFlag){
        ent_cnt=0;
        cat_cnt=0;
        if(Entities!=null&&Categories!=null){
            List<String> splitEntitiesList = new ArrayList<>();
            String[] splitEntities;
            for(String entity:Entities){
                String[] splitEntity = entity.split(" ");
                for(String s:splitEntity){
                    splitEntitiesList.add(s);
                }
            }
            splitEntities = splitEntitiesList.toArray(new String[splitEntitiesList.size()]);


            List<String> splitCategoriesList = new ArrayList<>();
            String[] splitCategories;
            for(String category:Categories){
                String[] splitCategory = category.split(" ");
                for(String s:splitCategory){
                    splitCategoriesList.add(s);
                }
            }
            splitCategories = splitCategoriesList.toArray(new String[splitCategoriesList.size()]);
            if(StemFlag){
                StemmerSnow stemmer = new StemmerSnow();
                lda_output = stemmer.stem(lda_output);
                splitEntitiesList = stemmer.stem(splitEntitiesList);
                splitEntities= splitEntitiesList.toArray(new String[splitEntitiesList.size()]);
                splitCategoriesList = stemmer.stem(splitCategoriesList);
                splitCategories= splitCategoriesList.toArray(new String[splitCategoriesList.size()]);
            }
            for(String s:lda_output){
                for(String splitStr:splitEntities){
                    if(s.equalsIgnoreCase(splitStr)){
                        ent_cnt++;
                    }
                }
                for(String splitStr:splitCategories){
                    if(s.equalsIgnoreCase(splitStr)){
                        cat_cnt++;
                    }
                }
            }
        }
        
    }
    public void getTopWordsStats(String parsedContent,List<String> lda_output, boolean StemFlag){
        lda_top_words_parsed=0;
        lda_top_words_parsed_percentage=0;
        if(!parsedContent.isEmpty()){
            String[] parsedContentsplit = parsedContent.split(" ");
            
            if(StemFlag){
                List<String> parsedContentsplitList = Arrays.asList(parsedContentsplit);
                StemmerSnow stemmer = new StemmerSnow();
                parsedContentsplitList = stemmer.stem(parsedContentsplitList);
                parsedContentsplit= parsedContentsplitList.toArray(new String[parsedContentsplitList.size()]);
                lda_output = stemmer.stem(lda_output);                
            }
            for(String s:lda_output){
                for(String splitStr:parsedContentsplit){
                    if(s.equalsIgnoreCase(splitStr)){
                        lda_top_words_parsed++;
                    }
                }
            }
            lda_top_words_parsed_percentage = lda_top_words_parsed / parsedContentsplit.length;
        }
        
    }
    public int getTopStats(){return lda_top_words_parsed;}
    public int getTopPercentageStats(){return lda_top_words_parsed_percentage;}
    public int getEntStats(){return ent_cnt;}
    public int getCategoryStats(){return cat_cnt;}
}
