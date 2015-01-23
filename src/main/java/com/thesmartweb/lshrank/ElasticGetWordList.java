/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 *
 * @author themis
 */
public class ElasticGetWordList {
    
    public List<String> get(List<String> ids) {
            
        try {
            Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
            Client client = node.client();
            
            List<String> wordList=new ArrayList<>();
            for(String id:ids){
                SearchResponse responseSearch = client.prepareSearch("lshranklda")
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setQuery(QueryBuilders.idsQuery().ids(id))
                        .execute()
                        .actionGet();
                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject();
                responseSearch.toXContent(builder, ToXContent.EMPTY_PARAMS);
                builder.endObject();
                String JSONresponse=builder.string();
                JsonParser parser = new JsonParser();
                JsonObject JSONobject = (JsonObject)parser.parse(JSONresponse);
                JsonObject hitsJsonObject = JSONobject.getAsJsonObject("hits");
                JsonArray hitsJsonArray = hitsJsonObject.getAsJsonArray("hits");
                for(JsonElement hitJsonElement:hitsJsonArray){
                    JsonObject jsonElementObj= hitJsonElement.getAsJsonObject();
                    jsonElementObj=jsonElementObj.getAsJsonObject("_source");
                    JsonArray TopicsArray=jsonElementObj.getAsJsonArray("TopicsWordMap");
                    for(JsonElement Topic:TopicsArray){
                        JsonObject TopicObj=Topic.getAsJsonObject();
                        JsonObject wordsmap = TopicObj.getAsJsonObject("wordsmap");
                        Set<Map.Entry<String,JsonElement>> entrySet=wordsmap.entrySet();
                        Iterator<Map.Entry<String, JsonElement>> iterator = entrySet.iterator();
                        while(iterator.hasNext()){
                            Map.Entry<String, JsonElement> next = iterator.next();
                            String word=next.getKey();
                            wordList.add(word);
                        }
                    }
                }
            }
            node.close();
            return wordList;
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            List<String> wordList=new ArrayList<>();
            return wordList;
        }
        
    }
    public List<String> getMaxWords(List<String> ids, int top) {
            
       try {
            Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
            Client client = node.client();
            List<String> MaxwordList=new ArrayList<>();
            HashMap<String,Double> wordsMap=new HashMap<>();
            SortedSetMultimap<Double,String> wordsMultisorted=TreeMultimap.create();
            for(String id:ids){
                SearchResponse responseSearch = client.prepareSearch("lshranklda")
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setQuery(QueryBuilders.idsQuery().ids(id))
                        .execute()
                        .actionGet();
                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject();
                responseSearch.toXContent(builder, ToXContent.EMPTY_PARAMS);
                builder.endObject();
                String JSONresponse=builder.string();
                JsonParser parser = new JsonParser();
                JsonObject JSONobject = (JsonObject)parser.parse(JSONresponse);
                JsonObject hitsJsonObject = JSONobject.getAsJsonObject("hits");
                JsonArray hitsJsonArray = hitsJsonObject.getAsJsonArray("hits");
                for(JsonElement hitJsonElement:hitsJsonArray){
                    JsonObject jsonElementObj= hitJsonElement.getAsJsonObject();
                    jsonElementObj=jsonElementObj.getAsJsonObject("_source");
                    JsonArray TopicsArray=jsonElementObj.getAsJsonArray("TopicsWordMap");
                    for(JsonElement Topic:TopicsArray){
                        JsonObject TopicObj=Topic.getAsJsonObject();
                        JsonObject wordsmap = TopicObj.getAsJsonObject("wordsmap");
                        Set<Map.Entry<String,JsonElement>> entrySet=wordsmap.entrySet();
                        Iterator<Map.Entry<String, JsonElement>> iterator = entrySet.iterator();
                        double max=0.0;
                        String maxword="";
                        while(iterator.hasNext()){
                            Map.Entry<String, JsonElement> next = iterator.next();
                            if(next.getValue().getAsDouble()>max){
                                maxword=next.getKey();
                                max=next.getValue().getAsDouble();
                            }
                        }
                        if(wordsMap.containsKey(maxword)){
                            if(wordsMap.get(maxword)<max){
                                wordsMap.put(maxword, max);
                            }
                        }
                        else{
                            wordsMap.put(maxword, max);
                        }
                        //wordsMultisorted.put(max,maxword);
                        //MaxwordList.add(maxword);
                    }
                }
            }
            Map<String,Double> wordsMapsorted = new HashMap<>();
            wordsMapsorted=sortByValue(wordsMap);
            Iterator<Entry<String, Double>> iterator = wordsMapsorted.entrySet().iterator();
            
            int beginindex=wordsMapsorted.entrySet().size()-top;
            int index=0;
            while(index<beginindex){
                iterator.next();
                index++;
            }
            while(MaxwordList.size()<top){
                String word=iterator.next().getKey();
                MaxwordList.add(word);
                
            }
            //Set<Double> keySet = wordsMultisorted.keySet();
            //Multiset<Double> keys = wordsMultisorted.keys();
            node.close();
            return MaxwordList;
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            List<String> MaxwordList=new ArrayList<>();
            return MaxwordList;
        }
        
    }
    public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValue( Map<K, V> map )
    {
      Map<K,V> result = new LinkedHashMap<>();
     Stream <Entry<K,V>> st = map.entrySet().stream();

     st.sorted(Comparator.comparing(e -> e.getValue())).forEach(e ->result.put(e.getKey(),e.getValue()));

     return result;
    }
}
