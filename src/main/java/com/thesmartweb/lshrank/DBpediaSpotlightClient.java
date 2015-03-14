/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;

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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
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
 * @author pablomendes, Joachim Daiber
 */

public class DBpediaSpotlightClient extends AnnotationClient {

	//private final static String API_URL = "http://jodaiber.dyndns.org:2222/";
        private final static String API_URL = "http://spotlight.dbpedia.org/";
	private static final double CONFIDENCE = 0.10;
	private static final int SUPPORT = 5;
        private List<String> typesDBspot;
        private List<String> entitiesString;
        private int ent_cnt_dbpspot=0;
        private int cat_cnt_dbpspot=0;
        private int ent_cnt_dbpspot_whole=0;
        private int cat_cnt_dbpspot_whole=0;
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
        
        @Override
	public void extract(String url_check,boolean StemFlag) throws AnnotationException {
                LinkedList<DBpediaResource> resources = new LinkedList<DBpediaResource>();
                entitiesString = new ArrayList<>();
                typesDBspot = new ArrayList<>();
		try {
                    
                    LOG.info("Querying API.");
                    String spotlightResponse;
                    GetMethod getMethod = new GetMethod(API_URL + "rest/annotate/?" +
                            "confidence=" + CONFIDENCE
                            + "&support=" + SUPPORT
                            + "&url=" + URLEncoder.encode(url_check, "utf-8"));
                    getMethod.addRequestHeader(new Header("Accept", "application/json"));
                    spotlightResponse = request(getMethod);
                    
                    assert spotlightResponse != null;
                    
                    JSONObject resultJSON = null;
                    JSONArray entities = null;
                    resultJSON = new JSONObject(spotlightResponse);
                    entities = resultJSON.getJSONArray("Resources");
                    for(int i = 0; i < entities.length(); i++) {
                        try {
                            JSONObject entity = entities.getJSONObject(i);
                            String entityString = entity.getString("@URI").substring(28).toLowerCase().replaceAll("[\\_,\\%28,\\%29]", " ");
                            if(StemFlag){
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
                            if(!entitiesString.contains(entityString)){
                                entitiesString.add(entityString);
                            }
                            String typesString = entity.getString("@types");
                            String[] types = typesString.split("\\,");
                            String delimiter="";
                            for(String type :types){
                                if(type.contains("DBpedia")||type.contains("Schema")){
                                    delimiter = "\\:";
                                }
                                if(type.contains("Freebase")){
                                    delimiter = "\\/";
                                }
                                String[] typeStrings = type.split(delimiter);
                                String typeString = typeStrings[typeStrings.length-1].toLowerCase().replaceAll("[\\_,\\%28,\\%29]", " ");
                                if(StemFlag){
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
                            resources.add(new DBpediaResource(entity.getString("@URI"),Integer.parseInt(entity.getString("@support"))));
                            
                        } catch (JSONException e) {
                            LOG.error("JSON exception "+e);
                        }
                    }
                    int jk=0;
                } catch (UnsupportedEncodingException | JSONException ex) {
                    Logger.getLogger(DBpediaSpotlightClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
    public void run(String url_check,boolean StemFlag) throws Exception {


        DBpediaSpotlightClient c = new DBpediaSpotlightClient ();

//        File input = new File("/home/pablo/eval/manual/AnnotationText.txt");
//        File output = new File("/home/pablo/eval/manual/systems/Spotlight.list");
            //File input = new File("/home/pablo/eval/cucerzan/cucerzan.txt");
            //File output = new File("/home/pablo/eval/cucerzan/systems/cucerzan-Spotlight.set");
//        File input = new File("/home/pablo/eval/wikify/gold/WikifyAllInOne.txt");
//        File output = new File("/home/pablo/eval/wikify/systems/Spotlight.list");
            //File input = new File("/home/alexandre/Projects/test-files-spotlight/ExternalClients_TestFiles/Berlin.txt");
            //File output = new File("/home/alexandre/Projects/test-files-spotlight/ExternalClients_TestFiles/Spotlight.list");
            c.extract(url_check,StemFlag);
            //c.evaluate(input, output);
//        SpotlightClient c = new SpotlightClient(api_key);
//        List<DBpediaResource> response = c.extract(new Text(text));
//        PrintWriter out = new PrintWriter(manualEvalDir+"AnnotationText-Spotlight.txt.set");
//        System.out.println(response);

    }
    
    public void countEntCat(String url_check,String query,boolean StemFlag) {
        
            try {
                ent_cnt_dbpspot=0;
                cat_cnt_dbpspot=0;
                ent_cnt_dbpspot_whole=0;
                cat_cnt_dbpspot_whole=0;
                extract(url_check,StemFlag);
                query = query.toLowerCase();
                String[] splitQuery = query.split("\\+");
                if(StemFlag){
                    List<String> splitQuerylist = java.util.Arrays.asList(splitQuery);
                    StemmerSnow stemmer = new StemmerSnow();
                    splitQuerylist = stemmer.stem(splitQuerylist);
                    splitQuery = splitQuerylist.toArray(new String[splitQuerylist.size()]);
                }
                int ent_count=0;
                for(String s:entitiesString){
                    ent_count=0;
                    for(String splitStr:splitQuery){
                        if(s.contains(splitStr)){
                            ent_cnt_dbpspot++;
                            ent_count++;
                        }
                    }
                    if(ent_count==splitQuery.length){
                        ent_cnt_dbpspot_whole++;
                    }
                }
                int cat_count=0;
                for(String s:typesDBspot){
                    cat_count=0;
                    for(String splitStr:splitQuery){
                        if(s.contains(splitStr)){
                            cat_cnt_dbpspot++;
                            cat_count++;
                        }
                    }
                    if(cat_count==splitQuery.length){
                        cat_cnt_dbpspot_whole++;
                    }
                }
                int h=0;
            } catch (Exception ex) {
                Logger.getLogger(DBpediaSpotlightClient.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    public int getcountEnt(){return ent_cnt_dbpspot;}
    public int getcountCat(){return cat_cnt_dbpspot;}
    public int getcountEntWhole(){return ent_cnt_dbpspot_whole;}
    public int getcountCatWhole(){return cat_cnt_dbpspot_whole;}
    public List<String> getEntities(){return entitiesString;}
    public List<String> getCategories(){return typesDBspot;}
    

}