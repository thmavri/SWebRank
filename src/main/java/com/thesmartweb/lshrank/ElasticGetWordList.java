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
    public List<String> getMaxWords(List<String> ids) {
            
       try {
            Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
            Client client = node.client();
            List<String> MaxwordList=new ArrayList<>();
            ids.add("NBA/kobe+bryant/google/0");
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
                        MaxwordList.add(maxword);
                    }
                }
            }
            node.close();
            return MaxwordList;
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            List<String> MaxwordList=new ArrayList<>();
            return MaxwordList;
        }
        
    }
}
