/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;
/**
 *
 * @author Themis Mavridis
 */
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author themis
 */
public class CheckConvergence {

    /**
     *
     * @param wordList_new
     * @param wordList_previous
     * @return
     */
    public double performOld(List<String> wordList_new,List<String> wordList_previous/*,List<String> finalList*/){
        try {
            //System.out.print("\n" + "_______________________________________" + conversion_percentage+ "_____________________");

            String[] stringlist = wordList_new.toArray(new String[wordList_new.size()]);
            PorterStemmer sm = new PorterStemmer();
            stringlist = sm.process(stringlist);
            double conversion_percentage = 0;
            String[] stringlist_previous = wordList_previous.toArray(new String[wordList_previous.size()]);
            for (int i = 0; i < stringlist_previous.length; i++) {
                for (int j = 0; j < stringlist.length; j++) {
                    if (stringlist[j].equalsIgnoreCase(stringlist_previous[i])) {
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
    /*public double perform(List<String> wordList_new,List<String> wordList_previous){
        
            StemmerSnow sm = new StemmerSnow();
            wordList_new = sm.stem(wordList_new);
            NMIcalculator nmicalculator = new NMIcalculator();
            double nmi=nmicalculator.calculate(wordList_new, wordList_previous);
            return nmi;
        
    }*/
    public double F1Calc(List<String> wordList_new,List<String> wordList_previous){
        
            StemmerSnow sm = new StemmerSnow();
            wordList_new = sm.stem(wordList_new);
            double F1;
            double precision=0;
            for(String s:wordList_previous){
                if(wordList_new.contains(s)){
                    precision++;
                }
            }
            if(precision==0){return 0;}
            double recall=precision;
            precision=precision/wordList_new.size();
            recall=recall/wordList_previous.size();
            F1=2*(precision*recall)/(precision+recall);
            return F1;
        
    }
}
