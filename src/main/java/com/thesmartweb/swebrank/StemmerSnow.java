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

import java.util.ArrayList;
import java.util.List;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/**
 * Class for using snowball stemmer
 * @author Themistoklis Mavridis
 */
public class StemmerSnow {
     /**
      * Method to perform stemming 
      * @param input the words we would like to stem in a list
      * @return the words stemmed
      */
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




    
   
