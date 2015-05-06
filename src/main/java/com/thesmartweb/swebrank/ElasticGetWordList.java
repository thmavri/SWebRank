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
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Class that contains method that retrieve words from an index in the cluster of ElasticSearch where the content is saved
 * @author Themistoklis Mavridis
 */
public class ElasticGetWordList {
    /**
     * Method gets all the words of all the documents regardless of topic for the ids passed as input
     * @param ids It contains all the ids for which the words are going to be captured
     * @param config_path configuration directory to get the names of the elastic search indexes
     * @return All the words in a List
     */
    public List<String> get(List<String> ids, String config_path) {
        try {
            //Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
            //Client client = node.client();
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name","lshrankldacluster").build();
            Client client = new TransportClient(settings)
                    .addTransportAddress(new
                            InetSocketTransportAddress("localhost", 9300)
                    );
            ReadInput ri = new ReadInput();
            List<String> elasticIndexes=ri.GetKeyFile(config_path, "elasticSearchIndexes");
            List<String> wordList=new ArrayList<>();
            for(String id:ids){
                SearchResponse responseSearch = client.prepareSearch(elasticIndexes.get(2))
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
            //node.close();
            client.close();
            return wordList;
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            List<String> wordList=new ArrayList<>();
            return wordList;
        }
        
    }
    /**
     * Method gets all the top N max words for each topic of all the documents with their IDs (of the documents) passed as input.
     * @param ids It contains all the ids for which the words are going to be captured
     * @param top It contains the number of max words to be returned
     * @return All the words in a List
     */
    public List<String> getMaxWords(List<String> ids, int top, String config_path) {
       try {
           ReadInput ri = new ReadInput();
            List<String> elasticIndexes=ri.GetKeyFile(config_path, "elasticSearchIndexes");
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name","lshrankldacluster").build();
            Client client = new TransportClient(settings)
                    .addTransportAddress(new
                            InetSocketTransportAddress("localhost", 9300)
                    );
            //Node node = nodeBuilder().client(true).clusterName("lshrankldacluster").node();
            //Client client = node.client();
            List<String> MaxwordList=new ArrayList<>();
            HashMap<String,Double> wordsMap=new HashMap<>();
            SortedSetMultimap<Double,String> wordsMultisorted=TreeMultimap.create();
            for(String id:ids){//for every id loop
                SearchResponse responseSearch = client.prepareSearch(elasticIndexes.get(2))
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setQuery(QueryBuilders.idsQuery().ids(id))
                        .execute()
                        .actionGet();//search for this id
                //----build an object with the response
                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject();
                responseSearch.toXContent(builder, ToXContent.EMPTY_PARAMS);
                builder.endObject();
                String JSONresponse=builder.string();
                //----parse the JSON response
                JsonParser parser = new JsonParser();
                JsonObject JSONobject = (JsonObject)parser.parse(JSONresponse);
                JsonObject hitsJsonObject = JSONobject.getAsJsonObject("hits");
                JsonArray hitsJsonArray = hitsJsonObject.getAsJsonArray("hits");
                //get all the JSON hits (check ElasticSearch typical response format for more)
                for(JsonElement hitJsonElement:hitsJsonArray){
                    JsonObject jsonElementObj= hitJsonElement.getAsJsonObject();
                    jsonElementObj=jsonElementObj.getAsJsonObject("_source");
                    JsonArray TopicsArray=jsonElementObj.getAsJsonArray("TopicsWordMap");//get the topics word map (every word has a probability
                    for(JsonElement Topic:TopicsArray){//for every topic I get the word with the max score
                        JsonObject TopicObj=Topic.getAsJsonObject();
                        JsonObject wordsmap = TopicObj.getAsJsonObject("wordsmap");//get the wordmap
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
                    }
                }
            }
            //we are going to sort all the max words
            Map<String,Double> wordsMapsorted = new HashMap<>();
            wordsMapsorted=sortByValue(wordsMap);//sorts the map in ascending fashion
            Iterator<Entry<String, Double>> iterator = wordsMapsorted.entrySet().iterator();
            //we are going to get the first top words from the list of Max words
            int beginindex=0;
            //===we find the beginning index
            if(wordsMapsorted.entrySet().size()>top){
                beginindex=wordsMapsorted.entrySet().size()-top;
            }
            int index=0;
            //if the beginning index is larger we try to find the element
            while(index<beginindex){
                iterator.next();
                index++;
            }
            //while the maxword list size is smaller than the top number and we have an extra value, add this word
            while(MaxwordList.size()<top && iterator.hasNext()){
                String word=iterator.next().getKey();
                MaxwordList.add(word);
                
            }
            client.close();
            //node.close();
            return MaxwordList;
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            List<String> MaxwordList=new ArrayList<>();
            return MaxwordList;
        }
        
    }
    
    
    /**
     * Method that sorts a Map
     * @param <K> any primitive
     * @param <V> any primitive
     * @param map the map to be sorted
     * @return The map sorted in ascending fashion
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValue( Map<K, V> map )
    {
      Map<K,V> result = new LinkedHashMap<>();
     Stream <Entry<K,V>> st = map.entrySet().stream();

     st.sorted(Comparator.comparing(e -> e.getValue())).forEach(e ->result.put(e.getKey(),e.getValue()));

     return result;
    }
}
