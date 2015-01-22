/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

/**
 *
 * @author Robot-XPS
 */
import JavaMI.MutualInformation;
import JavaMI.Entropy;
import JavaMI.JointProbabilityState;
import JavaMI.ProbabilityState;
import JavaMI.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
public class NMIcalculator {

    
    public double calculate(List<String> vector1list,List<String> vector2list) {
       
            String[] vector1=vector1list.toArray(new String[vector1list.size()]);
            String[] vector2=vector2list.toArray(new String[vector2list.size()]);
            List<String> vectortemplist=new ArrayList<>();
            //---------
            if(vector1list.size()>vector2list.size()){
                vectortemplist=vector1list;
                vector1list=vector2list;
                vector2list=vectortemplist;
            }
            List<String> totalwords=new ArrayList<String>();
            totalwords.addAll(vector1list);
            totalwords.addAll(vector2list);
            
            DataManipulation dm = new DataManipulation();
            totalwords= dm.clearListString(totalwords);
            String[] totalwordsArray=totalwords.toArray(new String[totalwords.size()]);
            vector1=vector1list.toArray(new String[vector1list.size()]);
            vector2=vector2list.toArray(new String[vector2list.size()]);
            //----------------------------------
            //*/
            
            
            double[] c1=new double[vector1.length];
            double[] c2=new double[vector2.length];
            for(int i=0;i<c1.length;i++){
                for(int k=0;k<totalwordsArray.length;k++){
                    if(totalwordsArray[k].equalsIgnoreCase(vector1[i])){
                        c1[i]=k;
                    }
                }
            }
            for(int i=0;i<c2.length;i++){
                for(int k=0;k<totalwordsArray.length;k++){
                    if(totalwordsArray[k].equalsIgnoreCase(vector2[i])){
                        c2[i]=k;
                    }
                }
            }
            MutualInformation mf;
            mf = new MutualInformation();
            Entropy ep;
            ep =new Entropy();
            
            double[] ctemp=new double[c1.length];
            if(c1.length>c2.length){
                ctemp=c1;
                c1=c2;
                c2=ctemp;
            }
            double mi=mf.calculateMutualInformation(c1,c2);
            double ep1 = ep.calculateEntropy(c1);
            double ep2 = ep.calculateEntropy(c2);
            double nmi=mi/((ep1+ep2)/2);
            return nmi;
            
            
    }
}
