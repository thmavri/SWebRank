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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import org.apache.commons.lang.StringUtils;

/**
 * Class for the various Sindice functionalities
 * @author Themistoklis Mavridis
 */
public class Sindice {
     int triplecount=0;//the amount of semantic triples 
     boolean[] namespaces;//the semantic namespaces recognized

    /**
     * Method that gets the amount of semantic triples and recognizes the semantic namespaces used
     * @param url the url that we are going to get the stats for
     * @return the amount of semantic triples
     */
    public int getsindicestats(String url){
        try {
            String chk;
            String line="";
            APIconn sind=new APIconn();
            chk=sind.check_conn(url);
            if(chk.equalsIgnoreCase("ok-conn")){
                URL link_ur =new URL("http://api.sindice.com/v2/live?url="+url+"&format=json");
                line=sind.connect(link_ur);
                line="fail";
            }
            if (!line.equalsIgnoreCase("fail")) {
                JSONparsing gg = new JSONparsing();
                String chck="\"o\":";
                triplecount=StringUtils.countMatches(line, chck);
                namespaces=gg.TripleParse(line);
                //triplecount=0;
            }
            return triplecount;
        } catch (IOException ex) {
            Logger.getLogger(Sindice.class.getName()).log(Level.SEVERE, null, ex);
            return triplecount;
        }
     }
}
