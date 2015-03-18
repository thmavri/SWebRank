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

import java.util.*;
/**
 * Class regarding Visibility score http://www.advancedwebranking.com/user-guide/html/en/ch08s06.html
 * @author Themistoklis Mavridis
 */
public class VisibilityScore {

    /**
     * Method to get all the links in a single array from all the search engines (duplicates removed)
     * @param links1 the links of one engine
     * @param links2 the links of another engine
     * @param links3 the links of another engine
     * @return an array with all the links
     */
    public String[] perform(String[] links1,String[] links2,String[] links3){
        //gets as input the links from the engines and gets them to lists in order to combine them
        List<String> link_list1=Arrays.asList(links1);
        List<String> link_list2=Arrays.asList(links2);
        List<String> link_list3=Arrays.asList(links3);
        List<String> link_list_total=new ArrayList<>();
        link_list_total.addAll(link_list1);
        link_list_total.addAll(link_list2);
        link_list_total.addAll(link_list3);
        //********remove duplicate words************
        //Create a HashSet which allows no duplicates
        HashSet<String> hashSet = new HashSet<>(link_list_total);
        //Assign the HashSet to a new ArrayList
        ArrayList<String> arrayList = new ArrayList<>(hashSet);
        arrayList.removeAll(Collections.singleton(null));
        String[] links = arrayList.toArray(new String[arrayList.size()]);
        return links;
    }

    /**
     * Method to compute the visibility score and rank the links according to it
     * @param links All the links in an array
     * @param links1 the links of one engine
     * @param links2 the links of another engine
     * @param links3 the links of another engine
     * @param top_visible the amount of links to keep in the result
     * @return the links sorted according to their visibility score
     */
    public String[] visibility_score(String[] links,String[] links1,String[] links2,String[] links3,int top_visible){
        Integer[] scores=new Integer[links.length];
        for(int i=0;i<0;i++){scores[i]=0;}
        //we compare every link of the merged engine with the links of each engine and when we found it in an engine we give the link a score of the place we found it
        //as a result links with small score are better in the rank and links with high score worse
        for(int j=0;j<links.length;j++){
            for (int i=0;i<links1.length;i++){
                if(links[j]!=null&&links1[i]!=null){
                    if(links[j].equalsIgnoreCase(links1[i])){
                      scores[j]=i;
                    }
                }
            }
            for (int i=0;i<links2.length;i++){
                if(links[j]!=null&&links2[i]!=null){
                    if(links[j].equalsIgnoreCase(links2[i])){
                      scores[j]=i;
                    }
                }
            }
            for (int i=0;i<links1.length;i++){
                if(links[j]!=null&&links3[i]!=null){
                    if(links[j].equalsIgnoreCase(links3[i])){
                      scores[j]=i;
                    }
                }
            }
        }
        List<Integer> scores_list=Arrays.asList(scores);
        //create a hashmap in order to map the scores with the indexes
        IdentityHashMap<Integer, Integer> originalIndices = new IdentityHashMap<>();
        //copy the original scores list
        for(int i2=0; i2<scores_list.size(); i2++) {originalIndices.put(scores_list.get(i2), i2);}
        //sort the scores
         List<Integer> sorted_scores = new ArrayList<>();
         sorted_scores.addAll(scores_list);
         sorted_scores.removeAll(Collections.singleton(null));
         Collections.sort(sorted_scores);
        //to get the top
        int draw_counter=0;
        for(int i=top_visible;i<sorted_scores.size();i++){
            Integer score_previous=sorted_scores.get(i-1);
            Integer score_current=sorted_scores.get(i);
            if(score_current.compareTo(score_previous)==0){draw_counter++;}
        }
        int[] origIndex=new int[top_visible+draw_counter];
        for(int i=0; i<origIndex.length; i++) {
            Integer score = sorted_scores.get(i);
            // Lookup original index efficiently
            origIndex[i] = originalIndices.get(score);
        }
        int size_links_out=0;
        if(top_visible<origIndex.length){
            size_links_out=top_visible;
        }
        else{
            size_links_out=origIndex.length;
        }
        String[] links_out=new String[size_links_out];
        List<Integer> noDup = new ArrayList<>(); 
        noDup.add(origIndex[0]);
        for (int c = 1; c < origIndex.length-1; c++){
            if(!noDup.contains(origIndex[c])){
                noDup.add(origIndex[c]); 
            }
        }
        int[] noDupArray = new int[noDup.size()];
        for(int i = 0; i < noDup.size(); i++){noDupArray[i] = noDup.get(i);} 
        if(top_visible<noDupArray.length){size_links_out=top_visible;}
        else{size_links_out=noDupArray.length;}
        for(int j=0;j<size_links_out;j++){
            links_out[j]=links[noDupArray[j]];
        }

         return links_out;
    }
}
