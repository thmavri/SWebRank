/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;
/**
 *
 * @author Themis Mavridis
 */
public class NGD_Analysis {

    /**
     *
     * @param term1
     * @param term2
     * @return
     */
    public double NGD_score(String term1, String term2) {
            System.out.println("into ngd score");
            Long M = 10000000000L; //802080446201L (2007)
            double freqx = logResults(term1);
            double freqy = logResults(term2);
            System.out.println("into taking results");
            String xy = term1.concat("+").concat(term2);
            double freqxy = logResults(xy);
            if (freqx == Double.NEGATIVE_INFINITY || freqy == Double.NEGATIVE_INFINITY) {
                //deal with zero results = infinite logarithms
                return 1; //return 1 by definition
            } else {
                double num = Math.max(freqx, freqy) - freqxy;
                double den = Math.log10(M) - Math.min(freqx, freqy);
                double formula = num / den;
                return formula;
            }

    }

    /**
     *
     * @param term
     * @return
     */
    public double logResults(String term) {
        
            
            //GoogleResults gr=new GoogleResults();
            //YahooResults yr = new YahooResults();
            //long sc = yr.Get_Results_Number(term);
            BingResults bg=new BingResults();
            long sc=bg.Get_Results_Number(term);
            System.out.println(term+":"+sc);
            return Math.log10(sc);
       

    }
}
