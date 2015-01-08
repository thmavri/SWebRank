/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.lshrank;
import com.hp.hpl.jena.rdf.model.*;
/**
 *
 * @author Robot-XPS
 */
public class RDFparser {
        boolean[] namespaces=new boolean[40];
        int foaftotal=0;
        public int findNamespaces(String url){
            
            Model model = ModelFactory.createDefaultModel();
            model.read(url);
            model.write(System.out);
            String[] modelstring=model.toString().split("\\s+");
            
            NsIterator listNameSpaces = model.listNameSpaces();
            while (listNameSpaces.hasNext())
            {   
                String next = listNameSpaces.next();
                if(next.contains("http://purl.org/vocab/bio/0.1/")){
                    namespaces[0]=true;
                }
                
                if(next.contains("http://purl.org/dc/elements/1.1/")){
                    namespaces[1]=true;
                }
                if(next.contains("http://purl.org/coo/n")){
                    namespaces[2]=true;
                }
                if(next.contains("http://web.resource.org/cc/")){
                    namespaces[3]=true;
                }
                if(next.contains("http://usefulinc.com/ns/doap")){
                    namespaces[4]=true;
                }
                if(next.contains("http://xmlns.com/foaf/0.1/")){
                    namespaces[5]=true;
                }
                if(next.contains("http://purl.org/goodrelations/")){
                    namespaces[6]=true;
                }
                if(next.contains("http://purl.org/muto/core")){
                    namespaces[7]=true;
                }
                if(next.contains("http://webns.net/mvcb/")){
                    namespaces[8]=true;
                }
                if(next.contains("http://purl.org/ontology/mo/")){
                    namespaces[9]=true;
                }
                if(next.contains("http://purl.org/innovation/ns")){
                    namespaces[10]=true;
                }
                if(next.contains("http://openguid.net/rdf")){
                    namespaces[11]=true;
                }
                if(next.contains("http://www.slamka.cz/ontologies/diagnostika.owl")){
                    namespaces[12]=true;
                }
                if(next.contains("http://purl.org/ontology/po/")){
                    namespaces[13]=true;
                }
                if(next.contains("http://purl.org/net/provenance/ns")){
                    namespaces[14]=true;
                }
                if(next.contains("http://purl.org/rss/1.0/modules/syndication")){
                    namespaces[15]=true;
                }
                if(next.contains("http://rdfs.org/sioc/ns")){
                    namespaces[16]=true;
                }
                if(next.contains("http://madskills.com/public/xml/rss/module/trackback/")){
                    namespaces[17]=true;
                }
                if(next.contains("http://diligentarguont.ontoware.org/2005/10/arguonto")){
                    namespaces[18]=true;
                }
                if(next.contains("http://rdfs.org/ns/void")){
                    namespaces[19]=true;
                }
                if(next.contains("http://www.fzi.de/2008/wise/")){
                    namespaces[20]=true;
                }
                if(next.contains("http://xmlns.com/wot/0.1")){
                    namespaces[21]=true;
                }
                if(next.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns")){
                    namespaces[22]=true;
                }
                if(next.contains("http://www.w3.org/")&next.contains("rdf-schema")){
                    namespaces[23]=true;
                }
                if(next.contains("http://www.w3.org/")&next.contains("XMLSchema#")){
                    namespaces[24]=true;
                }
                if(next.contains("http://www.w3.org")&&next.contains("owl")){
                    namespaces[25]=true;
                }
                if(next.contains("http://purl.org/dc/terms/")){
                    namespaces[26]=true;
                }
                if(next.contains("http://www.w3.org/")&&next.contains("vcard")){
                    namespaces[27]=true;
                }
                if(next.contains("http://www.geonames.org/ontology")){
                    namespaces[28]=true;
                }
                
                if(next.contains("http://search.yahoo.com/searchmonkey/commerce/")){
                    namespaces[29]=true;
                }
                if(next.contains("http://search.yahoo.com/searchmonkey/media/")){
                    namespaces[30]=true;
                }
                if(next.contains("http://cb.semsol.org/ns#")){
                    namespaces[31]=true;
                }
                if(next.contains("http://blogs.yandex.ru/schema/foaf/")){
                    namespaces[32]=true;
                }
                if(next.contains("http://www.w3.org/2003/01/geo/wgs84_pos#")){
                    namespaces[33]=true;
                }
                if(next.contains("http://rdfs.org/sioc/ns#")){
                    namespaces[34]=true;
                }
                if(next.contains("http://rdfs.org/sioc/types#")){
                    namespaces[35]=true;
                }
                if(next.contains("http://smw.ontoware.org/2005/smw#")){
                    namespaces[36]=true;
                }
                if(next.contains("http://purl.org/rss/1.0/")){
                    namespaces[37]=true;
                }
                if(next.contains("http://www.w3.org/2004/12/q/contentlabel#")){
                    namespaces[38]=true;
                }
                
                
            }
            int foafcntprop=0;
            int foafcnttype=0;
            for(int i=0;i<modelstring.length;i++){
                if(modelstring[i].contains("foaf:")){
                    foafcntprop++;
                }
            }
            foaftotal=foafcnttype/2+foafcntprop;
            return foaftotal;
        
        }
    public boolean[] getnamespaces(){
            return namespaces;
    }
}
