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
package com.thesmartweb.lshrank;

import jgibblda.Estimator;
import jgibblda.Inferencer;
import jgibblda.LDACmdOption;
import jgibblda.Model;
import jgibblda.*;
/**
 * Class for the use of Latent Dirichlet Allocation
 * @author Themistoklis Mavridis
 */
public class LDAcall {

    /**
     * Method to setup LDA
     * @param nTopics number of Topics top recognize
     * @param alpha alpha value of LDA
     * @param beta beta value of LDA
     * @param niters number of iterations the algorithm to run
     * @param top_words the amount of top words per topic to get
     * @param directory the directory to save the output
     */
    public void call(int nTopics,double alpha,double beta,int niters,int top_words,String directory){
        //run the LDA
        String directory_LDA=directory;
        System.gc();
        LDAestimate( nTopics, directory_LDA , alpha,beta,niters, top_words);
        System.gc();
    }

    /**
     * Method to run LDA
     * @param nTopics number of Topics top recognize
     * @param alpha alpha value of LDA
     * @param beta beta value of LDA
     * @param niters number of iterations the algorithm to run
     * @param top_words the amount of top words per topic to get
     * @param directory the directory to save the output
     */
    public void LDAestimate(int nTopics, String directory, double alpha, double beta, int niters,int top_words){
        System.gc();
        System.out.println("Starting LDA for discovering " + nTopics + " topics in " + directory + "content_for_analysis.txt");
        LDACmdOption option = new LDACmdOption();
        option.est = true;
        option.alpha = alpha;
        option.beta = beta;
        option.K = nTopics;
        option.niters = niters;
        option.savestep = 2000;
        option.dir = directory;
        option.twords = top_words;
        option.dfile = "content_for_analysis.txt";
        System.out.println("Gibbs LDA Parameters:");
        System.out.println("alpha:\t" + option.alpha);
        System.out.println("beta:\t" + option.beta);
        System.out.println("Topics:\t" + option.K);
        System.out.println("Iterations:\t" + option.niters);
        System.out.println("savestep:\t" + option.savestep);
        System.out.println("Topic Words:\t" + option.twords);
        System.out.println("dfile:\t" + option.dfile);
        try {
            if (option.est || option.estc) {
                if (option.est)
                {
                    System.out.println("Estimate the LDA model from scratch");
                }
                else
                {
                    System.out.println("Continue to estimate the model from a previously estimated model");
                }
                Estimator estimator = new Estimator();
                estimator.init(option);
                estimator.estimate();
            }
            else if (option.inf)
            {
                System.out.println("Do inference for previously unseen (new) data using a previously estimated LDA model");
                Inferencer inferencer = new Inferencer();
                inferencer.init(option);

                Model newModel = inferencer.inference();

                for (int i = 0; i < newModel.phi.length; ++i){
                        //phi: K * V
                        //System.out.println("-----------------------\ntopic" + i  + " : ");
                        for (int j = 0; j < 10; ++j){
                                //System.out.println(inferencer.globalDict.id2word.get(j) + "\t" + newModel.phi[i][j]);
                        }
                }
            }
        }
        catch (Exception e){
                System.out.println("Error in main: " + e.getMessage());
                return;
        }
    }

}
