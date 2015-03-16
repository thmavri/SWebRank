
package com.thesmartweb.lshrank;

import java.util.*;
import java.util.List;

/**
 * A class that is used to create all the possible combinations
 * @author Themistoklis Mavridis
 */
public class Combinations_Engine {//function that calculates all the possible combinations and permutations and ranks them according to NGD threshold value

    /**
     * A method that calculates all the combinations and permutations
     * @param wordList A List<String> that contains all the words to be combined
     * @param combine_limit The number of words in every combination
     * @param queries A List<String> that contains the queries of the current round
     * @param nwd_threshold It contains the threshold for NWD
     * @param i It denotes the query against which NWD is going to be calculated
     * @param size_quer_new It contains the number of new queries to be calculated
     * @return A List<> that contains the top new queries for the i query
     */
    public List<String> perform(List<String> wordList,Double combine_limit,List<String> queries,Double nwd_threshold,int i, int size_quer_new, String config_path){
        int[] indices;
        System.gc();
        List<String> queries_new = new LinkedList<>();
        System.out.println("into comb engine");
        if(queries.size()>0){
            //we create an array of strings from the wordlist that is given as input
            String[] sl = (String[]) wordList.toArray(new String[wordList.size()]);
            //String example_dir = "/diplomatiki/examples-results/" + quer[i] + "-query/";
            List<String> wordList_new = new ArrayList<>();
            //we are going to create all the possible combinations considering the combine limit
            //Combination Generator is going to do the work for us
            for (int slcnt = 1; slcnt <= combine_limit.intValue(); slcnt++) {
                if(sl.length>slcnt&&sl.length>1){
                    System.out.println("into comb generator");
                    CombinationGenerator x = new CombinationGenerator(sl.length, slcnt);
                    System.out.println("out of comb generator");
                    System.out.println("total combinations:"+x.getTotal());
                    StringBuffer combination;
                    while (x.hasMore()) {
                        System.out.println("total combinations left:"+x.getNumLeft());
                        combination = new StringBuffer();
                        indices = x.getNext();
                        for (int i2 = 0; i2 < indices.length; i2++) {
                            combination.append(sl[indices[i2]]);
                            combination.append(" ");
                        }
                        String phrase = combination.toString().trim();
                        wordList_new.add(phrase);
                    }
                }
            }
            //get all the possible permutations of the words in wordList_new
            System.out.println("into permutation generator");
            List<String> combList = new ArrayList<>();
            if(!wordList_new.isEmpty()){
                combList = Calculate_Permutations(wordList_new);
            }
            //the comblist is going to include all the permutations
            System.out.println("out of permute generator");
            if (!combList.isEmpty()) {
                    //remove the duplicates from the list above
                    //Create a HashSet which allows no duplicates
                    HashSet<String> hashSet = new HashSet<>(combList);
                    //Assign the HashSet to a new ArrayList
                    ArrayList<String> ngd_in = new ArrayList<>(hashSet);
                    //get input(words) for ngd
                    String[] ngd_array = ngd_in.toArray(new String[ngd_in.size()]);
                    //NGD call, get the top words and run Search analysis and LDA on them
                    NGD_total ngt = new NGD_total();
                    if(queries.get(i)!=null){
                        int[] origIndex = ngt.call(ngd_array, queries, nwd_threshold, i, config_path);
                        //**** if we have max number of new queries*****
                        //for (int kk=0;kk<origIndex.length;kk++){
                        //    queries_new.add(kk,ngd_array[origIndex[kk]]);
                        //}

                        //int size_quer_new=1;
                        if(origIndex.length<size_quer_new){
                            size_quer_new=origIndex.length;
                        }
                        for (int kk=0;kk<size_quer_new;kk++){
                            queries_new.add(kk,ngd_array[origIndex[kk]]);
                        }
                    }
            }
        }
        System.out.println("I sorted the combinations");
        System.gc();
        return queries_new;
}

    /**
     * Method that calculates the Permutations of terms in a List<String> and adds a + between them for the search engine queries
     * @param wordList the List<String> that contains the terms to calculate the permutations
     * @return all the permutations in a List<String>
     */
    public List<String> Calculate_Permutations(List<String> wordList) {
            //this function calculates all the possible permitations of the phrases given in a wordlist
            ListIterator li = wordList.listIterator();
            List<String> finalList = new ArrayList<>();
            while (li.hasNext()) {
                Object obj = li.next();
                String rt = obj.toString().trim();
                //we count the number of the spaces in each phrases with regex in order to find the number of the words of the phrase
                int numberOfspaces = rt.replaceAll("[^\\s+]", "").length();
                String[] words = new String[numberOfspaces + 1];
                if (numberOfspaces > 0) {
                    for (int cnt = 0; cnt <= numberOfspaces; cnt++) {
                        words[cnt] = rt.split(" ")[cnt].trim().concat("+");
                    }
                    int[] indices;
                    PermutationGenerator x = new PermutationGenerator(words.length);
                    StringBuffer permutation;
                    while (x.hasMore()) {
                        permutation = new StringBuffer();
                        indices = x.getNext();
                        for (int i = 0; i < indices.length; i++) {
                            permutation.append(words[indices[i]]);
                        }
                        String phrase = permutation.toString().trim();
                        
                        finalList.add(phrase.substring(0, phrase.length() - 1));
                    }
                } else {
                    words[0] = rt;
                    finalList.add(words[0]);
                }
            }
            return finalList;
        }
}
