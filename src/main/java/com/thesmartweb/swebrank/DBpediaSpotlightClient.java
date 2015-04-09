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
        */
	//private final static String API_URL = "http://jodaiber.dyndns.org:2222/";
        private final static String API_URL = "http://spotlight.dbpedia.org/";
	private static final double CONFIDENCE = 0.20;
	private static final int SUPPORT = 5;
        private List<String> typesDBspot; 
        private List<String> entitiesString;
        private int ent_cnt_dbpspot=0;
        private int cat_cnt_dbpspot=0;
        private int ent_cnt_dbpspot_whole=0;
        private int cat_cnt_dbpspot_whole=0;
        private double ent_avg_score=0.0;
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
                ent_avg_score=0.0;
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
                                if(flag_new_entity){
                                    ent_avg_score = ent_avg_score+Double.valueOf(entity.getString("@similarityScore"));
                                }
                                //resources.add(new DBpediaResource(entity.getString("@URI"),Integer.parseInt(entity.getString("@support"))));
                            } catch (JSONException e) {
                                LOG.error("JSON exception "+e);
                            }
                        }
                        ent_avg_score = ent_avg_score / (double) entities.length();
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
                ent_avg_score=0.0;
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
    public double getEntitiesScore(){return ent_avg_score;}

}