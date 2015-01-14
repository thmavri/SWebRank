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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author themis
 */
public class Checkconversion {

    /**
     *
     * @param wordList_new
     * @param wordList_previous
     * @return
     */
    public double perform(List<String> wordList_new,List<String> wordList_previous/*,List<String> finalList*/){
    try {
        //System.out.print("\n" + "_______________________________________" + conversion_percentage+ "_____________________");
        
        String[] stringlist = wordList_new.toArray(new String[wordList_new.size()]);
        Stemmer sm = new Stemmer();
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
        Logger.getLogger(Checkconversion.class.getName()).log(Level.SEVERE, null, ex);
        double conversion_percentage=0;
        return conversion_percentage;
    }
    
}
}
