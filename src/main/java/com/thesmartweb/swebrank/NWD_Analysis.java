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
/**
 * Class for Normalized Web Distance or Normalized Google distance calculation
 * http://arxiv.org/abs/0905.4039
 * http://en.wikipedia.org/wiki/Normalized_Google_distance
 * @author Themis Mavridis
 */
public class NWD_Analysis {

    /**
     * Method that calculates the Normalized Web Distance (or former Normalized Google Distance) score
     * @param term1 the first term 
     * @param term2 the second term
     * @param config_path the path were to find the api keys of the Search Engine we would like to use
     * @return the NWD or NGD score (closest to zero means closer resemblance)
     */
    public double NWD_score(String term1, String term2, String config_path) {
        System.out.println("into ngd score");
        Long M = 10000000000L; //802080446201L (2007)
        double freqx = logResults(term1, config_path);
        double freqy = logResults(term2, config_path);
        System.out.println("into taking results");
        String xy = term1.concat("+").concat(term2);
        double freqxy = logResults(xy, config_path);
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
     * Method that calculates the Normalized Web Distance (or former Normalized Google Distance) score given the first terms frequency
     * @param term1 the first term 
     * @param term2 the second term
     * @param config_path the path were to find the api keys of the Search Engine we would like to use
     * @param freqx the log10 of the amount of results of the first term
     * @return the NWD or NGD score (closest to zero means closer resemblance)
     */
    public double NWD_score(String term1, String term2, String config_path, double freqx) {
        System.out.println("into ngd score");
        Long M = 10000000000L; //802080446201L (2007)
        //double freqx = logResults(term1, config_path);
        double freqy = logResults(term2, config_path);
        System.out.println("into taking results");
        String xy = term1.concat("+").concat(term2);
        double freqxy = logResults(xy, config_path);
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
     * Method that get the log of the number of results that correspond to a specific query in a search engine
     * @param term the term to get results number for
     * @param config_path the path were to find the api keys of the Search Engine we would like to use
     * @return the log of the of the number of results that correspond to a specific query in a search engine
     */
    public double logResults(String term, String config_path) {
        //GoogleResults gr=new GoogleResults();
        //YahooResults yr = new YahooResults();
        //long sc = yr.Get_Results_Number(term);
        BingResults bg=new BingResults();
        long sc=bg.Get_Results_Number(term, config_path);
        System.out.println(term+":"+sc);
        return Math.log10(sc);
    }
}
