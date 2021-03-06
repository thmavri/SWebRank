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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to check the convergence percentage between the words of two rounds
 * @author Themistoklis
 */
public class CheckConvergence {

    /**
     * Method to check the convergence by simply comparing the amount of words of the new round that existed in the previous round
     * employing Porter Stemmer
     * @param wordList_new the list of words of the current round
     * @param wordList_previous the list of words of the previous round
     * @return a number with value in a range 0 - 1 with 1 to that all the words in the new wordlist existed in the old
     */
    public double performOld(List<String> wordList_new,List<String> wordList_previous/*,List<String> finalList*/){
        try {
            String[] stringlist = wordList_new.toArray(new String[wordList_new.size()]);
            PorterStemmer sm = new PorterStemmer();
            stringlist = sm.process(stringlist);
            double conversion_percentage = 0;
            String[] stringlist_previous = wordList_previous.toArray(new String[wordList_previous.size()]);
            for (String string_previous : stringlist_previous) {
                for (String string : stringlist) {
                    if (string.equalsIgnoreCase(string_previous)) {
                        conversion_percentage++;
                    }
                }
            }
            conversion_percentage = conversion_percentage/ stringlist.length;
            return conversion_percentage;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CheckConvergence.class.getName()).log(Level.SEVERE, null, ex);
            double conversion_percentage=0;
            return conversion_percentage;
        }
    
    }
    /**
     * Method to check the convergence 
     * employing Snowball stemmer
     * @param wordList_new the list of words of the current round
     * @param wordList_previous the list of words of the previous round
     * @return return the convergence score 0 - 1 with 1 to be perfect convergence
     */
    public double ConvergenceCalc(List<String> wordList_new,List<String> wordList_previous){
        
            StemmerSnow sm = new StemmerSnow();//employ stemming
            wordList_new = sm.stem(wordList_new);
            wordList_previous = sm.stem(wordList_previous);
            double convergence=0;
            for(String s:wordList_previous){
                if(wordList_new.contains(s)){
                    convergence++;
                }
            }
            if(convergence==0){return 0;}
            convergence=convergence/wordList_new.size();
            return convergence;
        
    }
}
