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

/**
 *
 * @author themis
 */


import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import scala.actors.threadpool.Arrays;

/**
 * Simple web service-based annotation client for DBpedia Spotlight.
 *
 * @author pablomendes, Joachim Daiber, Themistoklis Mavridis
 */

public class DBpediaSpotlightClient extends AnnotationClient {
        /**
        * @param API_URL the url of the api
        * @param CONFIDENCE the confidence value for the DBpedia spotlight API
        * @param SUPPORT the support value for the DBpedia spotlight API
        * @param typesDBspot the list to contain all the semantic types (categories) 
        * @param entitiesString the list to contain all the semantic entities
        * @param ent_cnt_dbpspot the number of entities that contained a term of the query 
        * @param cat_cnt_dbpspot the number of categories that contained a term of the query
        * @param ent_cnt_dbpspot_whole the number of entities that contained the query as a whole
        * @param cat_cnt_dbpspot_whole the number of categories that contained  the query as a whole
        * @param ent_avg_score the average score of the entities recognized
        * @param ent_max_score the maximum score of the entities recognized
        * @param ent_min_score the minimum score of the entities recognized
        * @param ent_median_score the median of scores of the entities recognized
        * @param ent_std_score the standard deviation of scores of the entities recognized
        * @param ent_avg_support the average support of the entities recognized
        * @param ent_max_support the maximum support of the entities recognized
        * @param ent_min_support the minimum support of the entities recognized
        * @param ent_median_support the median support of the entities recognized
        * @param ent_std_support the standard deviation of supports of the entities recognized
        * @param ent_avg_dif the average difference in similarity scores between first and second ranked entities
        * @param ent_max_dif the maximum difference in similarity scores between first and second ranked entities
        * @param ent_min_dif the minimum difference in similarity scores between first and second ranked entities
        * @param ent_median_dif the median difference in similarity scores between first and second ranked entities
        * @param ent_std_dif the standard deviation of difference in similarity scores between first and second ranked entities
        * @param ent_sim_cnt_dbpspot the average similarity score of entities that contained a term of the query
        * @param ent_sup_cnt_dbpspot the average support of entities that contained a term of the query
        * @param ent_dif_cnt_dbpspot the average difference in similarity scores between first and second ranked of entities that contained a term of the query
        * @param unique_ent_cnt_dbpspot the number of entities that don't have second candidate
        * @param unique_ent_scoreSum_dbpspot the sum of similarities scores of the entities that don't have second candidate
        * @param high_precision_content the percentage of total entities which are annotated using high precision settings
        */
        //support = resource prominence
        //similarity score = topical relevance
        //percentageOfSecondRank = contextual ambiguity
	//private final static String API_URL = "http://jodaiber.dyndns.org:2222/";
        private final static String API_URL = "http://spotlight.dbpedia.org/";
	private static final double CONFIDENCE = 0.20;
	private static final int SUPPORT = 5;
        private List<String> typesDBspot; 
        private List<String> entitiesString;
        private List<Double> similarityScores;
        private List<Double> similarityDifference;
        private List<Double> supports;
        private List<String> allEntities;
        private int ent_cnt_dbpspot=0;
        private int cat_cnt_dbpspot=0;
        private int ent_cnt_dbpspot_whole=0;
        private int cat_cnt_dbpspot_whole=0;
        private double ent_avg_score=-1.0;
        private double ent_max_score=-1.0;
        private double ent_min_score=-1.0;
        private double ent_median_score=-1.0;
        private double ent_std_score=-1.0;
        private double ent_avg_support=-1.0;
        private double ent_max_support=-1.0;
        private double ent_min_support=-1.0;
        private double ent_median_support=-1.0;
        private double ent_std_support=-1.0;
        private double ent_avg_dif=-1.0;
        private double ent_max_dif=-1.0;
        private double ent_min_dif=-1.0;
        private double ent_median_dif=-1.0;
        private double ent_std_dif=-1.0;
        private double ent_dif_cnt_dbpspot=-1.0;
        private double ent_sim_cnt_dbpspot=-1.0;
        private double ent_sup_cnt_dbpspot=-1.0;
        private double unique_ent_cnt_dbpspot=0.0;
        private double unique_ent_scoreSum_dbpspot=-1.0;
        private double high_precision_content=0.0;
       
	@Override
	public List<DBpediaResource> extract(Text text) throws AnnotationException {

            LOG.info("Querying API.");
            String spotlightResponse;
            try {
                    GetMethod getMethod = new GetMethod(API_URL + "rest/annotate/?" +
                                    "confidence=" + CONFIDENCE
                                    + "&support=" + SUPPORT
                                    + "&text=" + URLEncoder.encode(text.text(), "utf-8"));
                    getMethod.addRequestHeader(new Header("Accept", "application/json"));

                    spotlightResponse = request(getMethod);
            } catch (UnsupportedEncodingException e) {
                    throw new AnnotationException("Could not encode text.", e);
            }

            assert spotlightResponse != null;

            JSONObject resultJSON = null;
            JSONArray entities = null;

            try {
                    resultJSON = new JSONObject(spotlightResponse);
                    entities = resultJSON.getJSONArray("Resources");
            } catch (JSONException e) {
                    throw new AnnotationException("Received invalid response from DBpedia Spotlight API.");
            }

            LinkedList<DBpediaResource> resources = new LinkedList<DBpediaResource>();
            for(int i = 0; i < entities.length(); i++) {
                    try {
                            JSONObject entity = entities.getJSONObject(i);
                            resources.add(new DBpediaResource(entity.getString("@URI"),Integer.parseInt(entity.getString("@support"))));

                    } catch (JSONException e) {
                    LOG.error("JSON exception "+e);
                }

            }
            return resources;
	}
        /**
        * Method that recognizes the entities through DBpedia spotlight the content of a given URL
        * @param url_check the url to be annotated
        * @param StemFlag a flag to determine if we want to use stemming
        */
        @Override
	public void extract(String url_check,boolean StemFlag) throws AnnotationException {
                LinkedList<DBpediaResource> resources = new LinkedList<DBpediaResource>();
                entitiesString = new ArrayList<>();
                typesDBspot = new ArrayList<>();
                similarityScores = new ArrayList<>();
                similarityDifference = new ArrayList<>();
                supports = new ArrayList<>();
                allEntities = new ArrayList<>();
                double simScore=0.0;
                double percOfSec=0.0;
		try {
                    
                    LOG.info("Querying API.");
                    String spotlightResponse;
                    String request = API_URL + "rest/annotate?" +
                            "confidence=" + CONFIDENCE
                            + "&support=" + SUPPORT
                            + "&url=" + URLEncoder.encode(url_check, "utf-8");
                    GetMethod getMethod = new GetMethod(request);
                    getMethod.addRequestHeader(new Header("Accept", "application/json"));
                    spotlightResponse = request(getMethod);
                    
                    assert spotlightResponse != null;
                    
                    JSONObject resultJSON = null;
                    JSONArray entities = null;
                    if(spotlightResponse.startsWith("{")){
                        resultJSON = new JSONObject(spotlightResponse);
                    
                        entities = resultJSON.getJSONArray("Resources");
                        
                        for(int i = 0; i < entities.length(); i++) {
                            try {
                                JSONObject entity = entities.getJSONObject(i);
                                //get the entity string by getting the last part of the URI
                                String entityString = entity.getString("@URI").substring(28).toLowerCase().replaceAll("[\\_,\\%28,\\%29]", " ");
                                if(StemFlag){//if we use stemming, we use Snowball stemmr of both entities and queries
                                    String[] splitEntity = entityString.split(" ");
                                    entityString="";
                                    StemmerSnow stemmer = new StemmerSnow();
                                    List<String> splitEntityList=stemmer.stem(Arrays.asList(splitEntity));
                                    StringBuilder sb = new StringBuilder();
                                    for(String s:splitEntityList){
                                        sb.append(s.trim());
                                        sb.append(" ");
                                    }
                                    entityString = sb.toString().trim();
                                }
                                boolean flag_new_entity=false;
                                if(!entitiesString.contains(entityString)){
                                    flag_new_entity=true;
                                    entitiesString.add(entityString);//if we have found a unique entity we include it in the list
                                }
                                String typesString = entity.getString("@types");//we get the semantic types/categories
                                String[] types = typesString.split("\\,");
                                String delimiter="";//the delimiter is different according to the type
                                for(String type :types){
                                    if(type.contains("DBpedia")||type.contains("Schema")){ //if it is DBpedia or Schema
                                        delimiter = "\\:";
                                    }
                                    if(type.contains("Freebase")){//if it is Freebase
                                        delimiter = "\\/";
                                    }
                                    String[] typeStrings = type.split(delimiter);
                                    String typeString = typeStrings[typeStrings.length-1].toLowerCase().replaceAll("[\\_,\\%28,\\%29]", " ");
                                    if(StemFlag){//if we choose to use stemming
                                        String[] splitType = typeString.split(" ");
                                        typeString="";
                                        StemmerSnow stemmer = new StemmerSnow();
                                        List<String> splitTypeList=stemmer.stem(Arrays.asList(splitType));
                                        StringBuilder sb = new StringBuilder();
                                        for(String s:splitTypeList){
                                            sb.append(s.trim());
                                            sb.append(" ");
                                        }
                                        typeString = sb.toString().trim();
                                    }
                                    if(!typesDBspot.contains(typeString)){
                                        typesDBspot.add(typeString);
                                    }
                                }
                                simScore = Double.valueOf(entity.getString("@similarityScore"));
                                percOfSec = Double.valueOf(entity.getString("@percentageOfSecondRank"));
                                allEntities.add(entityString);
                                similarityScores.add(simScore);
                                supports.add(Double.valueOf(entity.getString("@support")));
                                if (percOfSec==-1.0) similarityDifference.add(-1.0);
                                else similarityDifference.add(simScore*(1-percOfSec));
                                
                                //resources.add(new DBpediaResource(entity.getString("@URI"),Integer.parseInt(entity.getString("@support"))));
                            } catch (JSONException e) {
                                LOG.error("JSON exception "+e);
                            }
                        }
                       
                        //calculate statistics - similarity score
                        ent_avg_score=getMean(similarityScores);
                        ent_max_score=getMax(similarityScores);
                        ent_min_score=getMin(similarityScores);
                        ent_median_score=getMedian(similarityScores);
                        ent_std_score=getStd(similarityScores);
                        
                        //calculate statistics - support
                        ent_avg_support=getMean(supports);
                        ent_max_support=getMax(supports);
                        ent_min_support=getMin(supports);
                        ent_median_support=getMedian(supports);
                        ent_std_support=getStd(supports);
                        
                        //calculate statistics - difference in similarity scores between first and second ranked entities
                        unique_ent_cnt_dbpspot=0.0;
                        unique_ent_scoreSum_dbpspot=0;
                        List<Double> tempList=new ArrayList<>();
                        for (int i=0; i<similarityDifference.size(); i++){
                            if(similarityDifference.get(i)==-1){
                                unique_ent_cnt_dbpspot+=1;
                                unique_ent_scoreSum_dbpspot+=similarityScores.get(i);
                            }
                            else{
                                tempList.add(similarityDifference.get(i));
                            }
                        }
                        
                        unique_ent_cnt_dbpspot=unique_ent_cnt_dbpspot/allEntities.size();
                        
                        if(unique_ent_scoreSum_dbpspot==0) unique_ent_scoreSum_dbpspot=-1;
                        
                        ent_avg_dif=getMean(tempList);
                        ent_max_dif=getMax(tempList);
                        ent_min_dif=getMin(tempList);
                        ent_median_dif=getMedian(tempList);
                        ent_std_dif=getStd(tempList); 
                        
                        //calculate high precision content
                        high_precision_content = (double)getHighPrecContent(url_check)/allEntities.size();
                    }
                } catch (UnsupportedEncodingException | JSONException ex) {
                    Logger.getLogger(DBpediaSpotlightClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
    /**
     * Method to count the statistics for the entities and categories
     * @param url_check the url to count the statistics for
     * @param query the query term that which the url was a result of
     * @param StemFlag flag to use stemming or not
     */
    public void countEntCat(String url_check,String query,boolean StemFlag) {
        
            try {
                ent_cnt_dbpspot=0;
                cat_cnt_dbpspot=0;
                ent_cnt_dbpspot_whole=0;
                cat_cnt_dbpspot_whole=0;
                ent_dif_cnt_dbpspot=0.0;
                ent_sim_cnt_dbpspot=0.0;
                ent_sup_cnt_dbpspot=0.0;
                extract(url_check,StemFlag);//we get the entities and categoriss
                query = query.toLowerCase();
                String[] splitQuery = query.split("\\+");//we split the query with + because the queries to the Search APIs have + between the terms
                if(StemFlag){//we stem the query
                    List<String> splitQuerylist = java.util.Arrays.asList(splitQuery);
                    StemmerSnow stemmer = new StemmerSnow();
                    splitQuerylist = stemmer.stem(splitQuerylist);
                    splitQuery = splitQuerylist.toArray(new String[splitQuerylist.size()]);
                }
                int ent_count=0;//counter to count if we matched the whole query to an entity
                int index;
                for(String s:entitiesString){
                    ent_count=0;
                    for(String splitStr:splitQuery){
                        if(s.contains(splitStr)){
                            ent_cnt_dbpspot++;
                            ent_count++;
                        }
                    }
                    if(ent_count==splitQuery.length){//if the counter is equal to the splitQuery length, it means that all the query terms are included in the entity
                        ent_cnt_dbpspot_whole++;
                    }
                }
                int ent_count_all=0;
                int ent_count_dif=0;
                for (int i=0; i<allEntities.size(); i++){
                    for(String splitStr:splitQuery){
                        if(allEntities.get(i).contains(splitStr)){
                            ent_count_all++;
                            ent_sup_cnt_dbpspot+=supports.get(i);
                            ent_sim_cnt_dbpspot+=similarityScores.get(i);
                            if(similarityDifference.get(i)!=-1){
                                ent_dif_cnt_dbpspot+=similarityDifference.get(i);
                                ent_count_dif++;
                            }
                        }
                    }
                }
                if (ent_count_all!=0){
                    ent_sup_cnt_dbpspot /= (double)ent_count_all;
                    ent_sim_cnt_dbpspot /= (double)ent_count_all;
                }
                else{
                    ent_sup_cnt_dbpspot=-1.0;
                    ent_sim_cnt_dbpspot=-1.0;
                }
                if (ent_count_dif!=0)
                    ent_dif_cnt_dbpspot /= (double)ent_count_dif;
                else
                    ent_dif_cnt_dbpspot=-1.0;
                
                int cat_count=0;//counter to count if we matched the whole query to a category
                for(String s:typesDBspot){
                    cat_count=0;
                    for(String splitStr:splitQuery){//if the counter is equal to the splitQuery length, it means that all the query terms are included in the category
                        if(s.contains(splitStr)){
                            cat_cnt_dbpspot++;
                            cat_count++;
                        }
                    }
                    if(cat_count==splitQuery.length){
                        cat_cnt_dbpspot_whole++;
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(DBpediaSpotlightClient.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    
    /**
     * Method to get the entities counter (partial query match)
     * @return entities counter
     */
    public int getcountEnt(){return ent_cnt_dbpspot;}
    /**
     * Method to get the categories counter (partial query match)
     * @return categories counter that have a partial query match
     */
    public int getcountCat(){return cat_cnt_dbpspot;}
    /**
     * Method to get the entities counter (whole query match)
     * @return entities counter that have whole query match
     */
    public int getcountEntWhole(){return ent_cnt_dbpspot_whole;}
    /**
     * Method to get the categories counter (whole query match)
     * @return categories counter that have whole query match
     */
    public int getcountCatWhole(){return cat_cnt_dbpspot_whole;}
    /**
     * Method to get the entities List
     * @return entities List
     */
    public List<String> getEntities(){return entitiesString;}
    /**
     * Method to get the categories List
     * @return categories List
     */
    public List<String> getCategories(){return typesDBspot;}
    
    /**
     * Method to get the entities average score
     * @return entities average score
     */
    public double getEntitiesAvgScore(){return ent_avg_score;}
    
    /**
     * Method to get the entities max score
     * @return entities max score
     */
    public double getEntitiesMaxScore(){return ent_max_score;}
    
    /**
     * Method to get the entities min score
     * @return entities min score
     */
    public double getEntitiesMinScore(){return ent_min_score;}
    
    /**
     * Method to get the entities median score
     * @return entities median score
     */
    public double getEntitiesMedianScore(){return ent_median_score;}
    
    /**
     * Method to get the standard deviation of entities' score
     * @return standard deviation of entities' score
     */
    public double getEntitiesStdScore(){return ent_std_score;}
    
    /**
     * Method to get the entities average support
     * @return entities average support
     */
    public double getEntitiesAvgSupport(){return ent_avg_support;}
    
    /**
     * Method to get the entities max support
     * @return entities max support
     */
    public double getEntitiesMaxSupport(){return ent_max_support;}
    
    /**
     * Method to get the entities min support
     * @return entities min support
     */
    public double getEntitiesMinSupport(){return ent_min_support;}
    
    /**
     * Method to get the entities median support
     * @return entities median support
     */
    public double getEntitiesMedianSupport(){return ent_median_support;}
    
    /**
     * Method to get the standard deviation of entities' support
     * @return standard deviation of entities' support
     */
    public double getEntitiesStdSupport(){return ent_std_support;}

    /**
     * Method to get the entities support-weighted average
     * @return entities support-weighted average
     */
    public double getcountSupEnt(){return ent_sup_cnt_dbpspot;}
    
    /**
     * Method to get the entities similarity-weighted average
     * @return entities similarity-weighted average
     */
    public double getcountSimEnt(){return ent_sim_cnt_dbpspot;}
    
    /**
     * Method to get the entities similarity difference-weighted average
     * @return entities similarity difference-weighted average
     */
    public double getcountDifEnt() {return ent_dif_cnt_dbpspot;}
    
    /**
     * Method to get the entities average difference from second resource
     * @return entities average difference from second resource
     */
    public double getEntitiesAvgDif(){return ent_avg_dif;}
    
    /**
     * Method to get the entities max difference from second resource
     * @return entities max difference from second resource
     */
    public double getEntitiesMaxDif(){return ent_max_dif;}
    
    /**
     * Method to get the entities min difference from second resource
     * @return entities min difference from second resource
     */
    public double getEntitiesMinDif(){return ent_min_dif;}
    
    /**
     * Method to get the entities median difference from second resource
     * @return entities median difference from second resource
     */
    public double getEntitiesMedianDif(){return ent_median_dif;}
    
    /**
     * Method to get the entities standard deviation of difference from second resource
     * @return entities standard deviation of difference from second resource
     */
    public double getEntitiesStdDif(){return ent_std_dif;}
    
    /**
     * Method to get the number of entities which are the only candidates
     * @return number of entities which are the only candidates
     */
    public double getUniqueEntCnt() {return unique_ent_cnt_dbpspot;}
    
    /**
     * Method to get the total similarity score of entities which are the only candidates
     * @return total similarity score of entities which are the only candidates
     */
    public double getUniqueEntScoreSum() {return unique_ent_scoreSum_dbpspot;}
    
    /**
     * Method to get high precision content
     * @return percentage of total content which is annotated using high precision settings
     */
    public double getHighPrecEntities() {return high_precision_content;}
    
    /**
     * The following methods are used to get various statistics
     * @param list of type double
     * @return mean,maximum,minimum,median,standard deviation
     */
    
    private double getMean(List<Double> data)
    {
        if (data.isEmpty()) return -1.0;
        
        double sum = 0.0;
        for(Double d : data)
            sum += d;
        return sum/data.size();
    }
    
    private double getMax(List<Double> data)
    {
        if (data.isEmpty()) return -1.0;
        
        double max=data.get(0);
        
        for(Double d : data){
            if(d>max)
                max=d;
        }
        return max;       
    }
    
    private double getMin(List<Double> data){
        if (data.isEmpty()) return -1.0;
        
        double min=data.get(0);
        
        for(Double d : data){
            if(d<min)
                min=d;
        }
        return min;       
    }
    
    private double getStd(List<Double> data){
        if (data.isEmpty()) return -1.0;
        
        double mean = getMean(data);
        double temp = 0;
        for(Double d :data)
            temp += (mean-d)*(mean-d);
        temp=temp/data.size();
        return Math.sqrt(temp);
    }
    
    private double getMedian(List<Double> data){
       if (data.isEmpty()) return -1.0;
       
       List<Double> sorted=new ArrayList<>();
       sorted=data;
       Collections.sort(sorted);

       if (sorted.size() % 2 == 0) 
       {
          return (sorted.get((sorted.size() / 2) - 1) + sorted.get(sorted.size() / 2)) / 2.0;
       } 
       else 
       {
          return sorted.get(sorted.size() / 2);
       }
    }
    
    /**
     * Method to annotate a url using high precision settings
     * @param url_check the url to be annotated
     * @return total number of entities 
     * @throws AnnotationException 
     */
    private int getHighPrecContent(String url_check){
        try {
                LOG.info("Querying API.");
                String spotlightResponse;
                String request = API_URL + "rest/annotate?" +
                        "confidence=" + 0.6
                        + "&support=" + 2000
                        + "&url=" + URLEncoder.encode(url_check, "utf-8");
                GetMethod getMethod = new GetMethod(request);
                getMethod.addRequestHeader(new Header("Accept", "application/json"));
                spotlightResponse = request(getMethod);
                assert spotlightResponse != null;
                JSONObject resultJSON = null;
                JSONArray entities = null;
                if(spotlightResponse.startsWith("{")){
                    resultJSON = new JSONObject(spotlightResponse);
                    entities = resultJSON.getJSONArray("Resources");
                    return entities.length();
                }
                return 0;

            } catch (UnsupportedEncodingException | JSONException | AnnotationException ex) {
                Logger.getLogger(DBpediaSpotlightClient.class.getName()).log(Level.SEVERE, null, ex);
                return 0;
            }
    }
        
}
   
    