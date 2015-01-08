/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

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
 *
 * @author Administrator
 */
public class DataManipulation {
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
                        //-----------------------------------------        
                    
    }
     public boolean AppendString(String input, String file_string){
            //----------------append the wordlist to a file
                        File string_file = new File(file_string);
                        try {
                            FileUtils.writeStringToFile(string_file, input);
                            return true;
                        } catch (IOException ex) {
                            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
                            return false;
                        }
                        //-----------------------------------------        
                    
    }
    public List<String> AddAList(List<String> wordListtoAdd, List<String> wordListTotal){
            Iterator wordList_new_final_iterator=wordListtoAdd.iterator();
            while(wordList_new_final_iterator.hasNext()){
                    wordListTotal.add(wordList_new_final_iterator.next().toString());
            }
            return wordListTotal;
                    
    }
    public boolean FileTypeAnalyzed(String input){
        List<String> filetypesnotsupported=new ArrayList<String>();
        filetypesnotsupported.add(".pdf");
        filetypesnotsupported.add(".ppt");
        filetypesnotsupported.add(".doc");
        Iterator filesiterator=filetypesnotsupported.iterator();
        boolean flag_found=false;
        while(filesiterator.hasNext()&&flag_found){
            if(filesiterator.next().toString().contains(input)){
                flag_found=true;
                return flag_found;
            }
        }
        return flag_found;
    }
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
