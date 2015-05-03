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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Class that contains methods that manipulate different data
 * for SWebRank
 * @author Themis Mavridis
 */
public class DataManipulation {

    /**
     * Method that clears a List from duplicates and null elements
     * @param wordList It contains the List to be cleared
     * @return a List cleared from duplicates and null elements
     */
    public List<String> clearListString(List<String> wordList){
            //remove all null elements of the wordlist
            wordList.removeAll(Collections.singleton(null));
            //remove the duplicate elements since HashSet does not allow duplicates
            HashSet<String> hashSet_wordList = new HashSet<String>(wordList);
            //create an iterator to the hashset to add the elements back to the wordlist
            Iterator wordList_iterator=hashSet_wordList.iterator();
            //clear the wordlist
            wordList.clear();
            while(wordList_iterator.hasNext()){
                    wordList.add(wordList_iterator.next().toString());
            }
            return wordList;         
                    
    }

    /**
     * Method that writes a List to a file
     * @param wordList List to be saved
     * @param file_wordlist The file in a string format that the List is going to be saved
     * @return True/False 
     */
    public boolean AppendWordList(List<String> wordList, String file_wordlist){
        //----------------append the wordlist to a file
        File wordlist_file = new File(file_wordlist);
        try {
            FileUtils.writeLines(wordlist_file, wordList);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }    
    }

    /**
     * Method that writes a String to a file
     * @param input String to be saved
     * @param file_string The file in a string format that the String input is going to be saved
     * @return True/False 
     */
    public boolean AppendString(String input, String file_string){
        File string_file = new File(file_string);
        try {
            FileUtils.writeStringToFile(string_file, input);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Method that adds a List to another List
     * @param wordListtoAdd List to be added
     * @param wordListTotal List in which the elements of wordListtoAdd are going to be added
     * @return wordListTotal which contains the elements already existent in it along with the elements of wordListtoAdd
     */
    public List<String> AddAList(List<String> wordListtoAdd, List<String> wordListTotal){
        Iterator wordList_new_final_iterator=wordListtoAdd.iterator();
        while(wordList_new_final_iterator.hasNext()){
                wordListTotal.add(wordList_new_final_iterator.next().toString());
        }
        return wordListTotal;
    }

    /**
     * Method that recognizes if a string contains a an extension that is not supported
     * @param input String of the file
     * @return True/False
     */
    public boolean StructuredFileCheck(String input){
        List<String> structuredFileTypes=new ArrayList<>();
        structuredFileTypes.add(".pdf");
        structuredFileTypes.add(".ppt");
        structuredFileTypes.add(".doc");
        Iterator filesiterator=structuredFileTypes.iterator();
        boolean flag_found=false;
        while(filesiterator.hasNext()&&!flag_found){
            if(filesiterator.next().toString().contains(input)){
                flag_found=true;
                return flag_found;
            }
        }
        return flag_found;
    }

    /**
     * Removes the characters from a string
     * @param str String to be cleaned from the characters
     * @return the String cleaned from characters
     */
    public String removeChars(String str){
        if (str != null) {
            try {
                //str = str.replaceAll("(\r\n|\r|\n|\n\r)", " "); //Clear Paragraph escape sequences
                str = str.replaceAll("\\.", " "); //Clear dots
                str = str.replaceAll("\\-", " "); //
                str = str.replaceAll("\\_", " "); //
                str = str.replaceAll(":", " ");
                str = str.replaceAll("\\+", " ");
                str = str.replaceAll("\\/", " ");
                str = str.replaceAll("\\|", " ");
                str = str.replaceAll("\\[", " ");
                str = str.replaceAll("\\?", " ");
                str = str.replaceAll("\\#", " ");
                str = str.replaceAll("\\!", " ");
                str = str.replaceAll("'", " "); //Clear apostrophes
                str = str.replaceAll(",", " "); //Clear commas
                str = str.replaceAll("@", " "); //Clear @'s (optional)
                str = str.replaceAll("$", " "); //Clear $'s (optional)
                str = str.replaceAll("\\\\", "**&**"); //Clear special character backslash 4 \'s due to regexp format
                str = str.replaceAll("&amp;", "&"); //change &amp to &
                str = str.replaceAll("&lt;", "<"); //change &lt; to <
                str = str.replaceAll("&gt;", ">"); //change &gt; to >
                //		str = str.replaceAll("<[^<>]*>"," ");		//drop anything in <>
                str = str.replaceAll("&#\\d+;", " "); //change &#[digits]; to space
                str = str.replaceAll("&quot;", " "); //change &quot; to space
                //		str = str.replaceAll("http://[^ ]+ "," ");	//drop urls
                str = str.replaceAll("-", " "); //drop non-alphanumeric characters
                str = str.replaceAll("[^0-9a-zA-Z ]", " "); //drop non-alphanumeric characters
                str = str.replaceAll("&middot;", " ");
                str = str.replaceAll("\\>", " ");
                str = str.replaceAll("\\<", " ");
                str = str.replaceAll("<[^>]*>", "");
                str = str.replaceAll("\\d"," ");
                //str=str.replaceAll("\\<.*?\\>", "");
                str = str.replace('β', ' ');
                str = str.replace('€', ' ');
                str = str.replace('™', ' ');
                str = str.replace(')', ' ');
                str = str.replace('(', ' ');
                str = str.replace('[', ' ');
                str = str.replace(']', ' ');
                str = str.replace('`', ' ');
                str = str.replace('~', ' ');
                str = str.replace('!', ' ');
                str = str.replace('#', ' ');
                str = str.replace('%', ' ');
                str = str.replace('^', ' ');
                str = str.replace('*', ' ');
                str = str.replace('&', ' ');
                str = str.replace('_', ' ');
                str = str.replace('=', ' ');
                str = str.replace('+', ' ');
                str = str.replace('|', ' ');
                str = str.replace('\\', ' ');
                str = str.replace('{', ' ');
                str = str.replace('}', ' ');
                str = str.replace(',', ' ');
                str = str.replace('.', ' ');
                str = str.replace('/', ' ');
                str = str.replace('?', ' ');
                str = str.replace('"', ' ');
                str = str.replace(':', ' ');
                str = str.replace('>', ' ');
                str = str.replace(';', ' ');
                str = str.replace('<', ' ');
                str = str.replace('$', ' ');
                str = str.replace('-', ' ');
                str = str.replace('@', ' ');
                str = str.replace('©', ' ');
                //remove space
                InputStreamReader in = new InputStreamReader(IOUtils.toInputStream(str));
                BufferedReader br = new BufferedReader(in);
                Pattern p;
                Matcher m;
                String afterReplace = "";
                String strLine;
                String inputText = "";
                while ((strLine = br.readLine()) != null) {
                    inputText = strLine;
                    p = Pattern.compile("\\s+");
                    m = p.matcher(inputText);
                    afterReplace = afterReplace + m.replaceAll(" ");
                }
                br.close();
                str = afterReplace;
                return str;
            } catch (IOException ex) {
                Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
                str=null;
                return str;
            }
        } else {
            return str;
        }
    }
   
    /**
     * Method that sorts a HashMap according to their values
     * @param map the HashMap to be sorted
     * @return a List that contains the keys in sorted (descending) fashion
     */
    public List<String> sortHashmap (final HashMap<String,Double> map){
        Set<String> set = map.keySet();
        List<String> keys=new ArrayList<String>(set);
        Collections.sort(keys,new Comparator<String>(){
            @Override
            public int compare(String s1, String s2){
               return Double.compare(map.get(s2), map.get(s1));
            }
        });
        return keys;
    }
    
    public HashMap sortHashMapByValuesD(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues,Collections.reverseOrder());
        Collections.sort(mapKeys,Collections.reverseOrder());

        HashMap sortedMap = new HashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put(key, (Double)val);
                    break;
                }

            }

        }
        return sortedMap;
    }
    /**
     * Method that returns all the files of a certain extension from a directory
     * @param directory_path A String with the directory 
     * @param filetype A string with the filetype (without dot symbol)
     * @return a Collection that contains all the files found
     */
    public Collection<File> getinputfiles(String directory_path,String filetype){
        String[] extensions = {filetype};//set the file extensions you would like to parse, e.g. you could have {txt,jpeg,pdf}
        File directory = new File(directory_path);
        //----FileUtils listfiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter)

        //---- file filter is set to the extensions
        //---- the dirFilter is set to true and it performs recursive search to all the subdirectories
        String collection = FileUtils.listFiles(directory, extensions, true).toString();
        Collection<File> Files = FileUtils.listFiles(directory, extensions, true);
        String[] paths = new String[Files.size()];//----the String array will contain all the paths of the files
        int j=0;
        for (File file : Files) {
            paths[j]=file.getPath();
            j++;
        }
        return Files;
    }
    
}
