/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

import java.util.ArrayList;
import java.util.List;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/**
 *
 * @author themis
 */
public class StemmerSnow {
    
     public List<String> stem(List<String> input) {
        List<String> output=new ArrayList<>();
        SnowballStemmer snowballStemmer = new englishStemmer();
        for(String word:input){
            snowballStemmer.setCurrent(word);
            snowballStemmer.stem();
            output.add(snowballStemmer.getCurrent());
        }
        return output;
    }
    
}




    
   
