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

import java.io.File;
import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class related to the parsing procedures of HTML files by our mechanism
 * @author Themis Mavridis
 */
public class WebParser {

    /**
     * The number of embeded videos
     */
    public int number_embeded_videos;

    /**
     * The number of embeded videos that are internal to the domain links
     */
    public int number_embeded_videos_internal;

    /**
     * The number of scripts
     */
    public int scripts_number;

    /**
     * The number of scripts that are internal
     */
    public int scripts_internal;

    /**
     * The number of frames
     */
    public int frames_number;

    /**
     * The number of internal frames
     */
    public int frames_internal;

    /**
     * The number of links
     */
    public int links_number;

    /**
     * The number of internal links
     */
    public int links_internal;

    /**
     * The number of schema.org usages
     */
    public int nschem;

    /**
     * The number of hcards
     */
    public int hcardsn;

    /**
     * hcalendars
     */
    public int hcalen;

    /**
     * hreviews
     */
    public int hrevn;

    /**
     * hevents
     */
    public int hevenn;

    /**
     *hadresses
     */
    public int haddrn;

    /**
     * hgeo
     */
    public int hgeon;

    /**
     *rel tags
     */
    public int hreln;

    /**
     *total microformats
     */
    public int total_micron;

    /**
     *microformats-1
     */
    public int micron1;

    /**
     *microformats-2
     */
    public int micron2;

    /**
     *microdata
     */
    public int microd;

    /**
     *number of foaf
     */
    public int foaf;

    /**
     * Get the text content of a url cleaned from stopwords and symbols and lemmatized
     * @param html_string the url to parse
     * @return the content in a string 
     */
    public String Parse(String html_string){
        String content;
        content=cleanhtml(html_string);
        if(content!=null){
             DataManipulation txtpro = new DataManipulation();
             Stopwords st = new Stopwords();
             content=txtpro.removeChars(content);
             content=st.stop(content);
             content=txtpro.removeChars(content);
             //List<String> contentList = Arrays.asList(content.split(" "));
             //StemmerSnow snowballstemmer = new StemmerSnow();
             //contentList=snowballstemmer.stem(contentList);
             //for(String contentListItem : contentList){
             //    content=content+" "+contentListItem;
             //}
             Lemmatizer lemmatizer = new Lemmatizer();
             List<String> contentList=lemmatizer.lemmatize(content);
             content="";
             for(String contentListItem : contentList){
                 content=content+" "+contentListItem;
             }
         }
        return content;
      
    }

    /**
     * Parse the url and get all the content
     * @param link_html the url to parse
     * @return The content parsed
     */
    public String cleanhtml(String link_html)
    { 
        try {
            Document doc = Jsoup.connect(link_html).timeout(10*1000).get();
            String title = doc.title();
            String mainbody = doc.body().text();
            Elements links = doc.select("a[href]");
            Elements media = doc.select("[src]");
            //fix link html to remove https:// or http:// and simple /
            if(link_html.substring(link_html.length()-1,link_html.length()).equalsIgnoreCase("/")){link_html=link_html.substring(0,link_html.length()-1);}
            if(link_html.substring(0,5).equalsIgnoreCase("https")){ 
                link_html=link_html.substring(8);
            }else if(link_html.substring(0,4).equalsIgnoreCase("http")){ 
                link_html=link_html.substring(7);
            }
            String anchortext = "";
            String alttext="";
            //-----get the anchor text of internal links
            for (Element link : links) {
                String str_check=link.attr("abs:href").toString();
                if (link.attr("abs:href").contains(link_html) && link.text().length() > 1) {
                    anchortext = anchortext + link.text() + " ";
                }   
            }
            //-------get alt text to internal images links
            for (Element medi : media) {
                if(medi.getElementsByTag("img").attr("src").toString().contains(link_html)){
                    alttext=alttext+" "+medi.getElementsByTag("img").attr("alt").toString();
                }
                if(medi.getElementsByTag("img").attr("src").toString().startsWith("/")){
                    alttext=alttext+" "+medi.getElementsByTag("img").attr("alt").toString();
                }
            }
            String content = mainbody + title + anchortext+alttext;
            
            return content;
        } catch (IOException ex) {
            Logger.getLogger(com.thesmartweb.swebrank.WebParser.class.getName()).log(Level.SEVERE, null, ex);
            String check=null;
            return check;
        }
        catch (NullPointerException ex) {
            Logger.getLogger(com.thesmartweb.swebrank.WebParser.class.getName()).log(Level.SEVERE, null, ex);
            String check=null;
            return check;
        }
         catch (Exception ex) {
            Logger.getLogger(com.thesmartweb.swebrank.WebParser.class.getName()).log(Level.SEVERE, null, ex);
            String check=null;
            return check;
        }
    
    }
    
    /**
     * Method to get the number of links (total, internal)
     * @param link_html the url to parse
     * @return the number of links
     */
    public int[] getnlinks(String link_html){
        int[] nlinks= new int[2];
        nlinks[0]=0;//total number of links
        nlinks[1]=0;//number of internal links 
        try {
            Document doc = Jsoup.connect(link_html).timeout(10*1000).get();
            Elements links = doc.select("a[href]");
            nlinks[0]=links.size();
            //----we check if a link is internal or not (abs is used to get the whole link (abs stands for abs)
            for (Element link : links) {
                if (link.attr("abs:href").contains(link_html)) {nlinks[1]++;}
            }
            return nlinks;
        } catch (Exception ex) {
            Logger.getLogger(com.thesmartweb.swebrank.WebParser.class.getName()).log(Level.SEVERE, null, ex);
            return nlinks;
        }
    
    }
       
    /**
     * Method to get the various html stats
     * @param link_html the url to analyze
     * @return flag if we got all the stats
     */
    public boolean gethtmlstats(String link_html){
        try {
            Document doc = Jsoup.connect(link_html).timeout(10*1000).get();
            Elements schemas=doc.getElementsByAttributeValueContaining("itemtype", "schema.org");
            Elements microdata=doc.getElementsByAttribute("itemtype");
            Elements microformats_vcard=doc.getElementsByAttributeValueContaining("class", "vcard");
            Elements microformats_hreview=doc.getElementsByAttributeValueContaining("class", "hreview");
            Elements microformats_vevent=doc.getElementsByAttributeValueContaining("class", "vevent");
            Elements microformats_vcalendar=doc.getElementsByAttributeValueContaining("class", "vcalendar");
            Elements microformats_vgeo=doc.getElementsByAttributeValueContaining("class", "geo");
            Elements microformats_vadrn=doc.getElementsByAttributeValueContaining("class", "ardn");
            Elements microformats_acquaintance=doc.getElementsByAttributeValueContaining("rel", "link_html");
            Elements microformats_alternate=doc.getElementsByAttributeValueContaining("rel", "alternate");
            Elements microformats_appendix=doc.getElementsByAttributeValueContaining("rel", "appendix");
            Elements microformats_bookmark=doc.getElementsByAttributeValueContaining("rel", "bookmark");
            Elements microformats_chapter=doc.getElementsByAttributeValueContaining("rel", "chapter");
            Elements microformats_child=doc.getElementsByAttributeValueContaining("rel", "child");
            Elements microformats_coll=doc.getElementsByAttributeValueContaining("rel", "colleague");
            Elements microformats_contact=doc.getElementsByAttributeValueContaining("rel", "contact");
            Elements microformats_contents=doc.getElementsByAttributeValueContaining("rel", "contents");
            Elements microformats_copyright=doc.getElementsByAttributeValueContaining("rel", "copyright");
            Elements microformats_coresident=doc.getElementsByAttributeValueContaining("rel", "co-resident");
            Elements microformats_coworker=doc.getElementsByAttributeValueContaining("rel", "co-worker");
            Elements microformats_crush=doc.getElementsByAttributeValueContaining("rel", "crush");
            Elements microformats_date=doc.getElementsByAttributeValueContaining("rel", "date");
            Elements microformats_friend=doc.getElementsByAttributeValueContaining("rel", "friend");
            Elements microformats_glossary=doc.getElementsByAttributeValueContaining("rel", "glossary");
            Elements microformats_help=doc.getElementsByAttributeValueContaining("rel", "help");
            Elements microformats_itsrules=doc.getElementsByAttributeValueContaining("rel", "its-rules");
            Elements microformats_kin=doc.getElementsByAttributeValueContaining("rel", "kin");
            Elements microformats_license=doc.getElementsByAttributeValueContaining("rel", "license");
            Elements microformats_me=doc.getElementsByAttributeValueContaining("rel", "me");
            Elements microformats_met=doc.getElementsByAttributeValueContaining("rel", "met");
            Elements microformats_muse=doc.getElementsByAttributeValueContaining("rel", "muse");
            Elements microformats_neighbor=doc.getElementsByAttributeValueContaining("rel", "neighbor");
            Elements microformats_next=doc.getElementsByAttributeValueContaining("rel", "next");
            Elements microformats_nofollow=doc.getElementsByAttributeValueContaining("rel", "nofollow");
            Elements microformats_parent=doc.getElementsByAttributeValueContaining("rel", "parent");
            Elements microformats_prev=doc.getElementsByAttributeValueContaining("rel", "prev");
            Elements microformats_previous=doc.getElementsByAttributeValueContaining("rel", "previous");
            Elements microformats_section=doc.getElementsByAttributeValueContaining("rel", "section");
            Elements microformats_sibling=doc.getElementsByAttributeValueContaining("rel", "sibling");
            Elements microformats_spouse=doc.getElementsByAttributeValueContaining("rel", "spouse");
            Elements microformats_start=doc.getElementsByAttributeValueContaining("rel", "start");
            Elements microformats_stylesheet=doc.getElementsByAttributeValueContaining("rel", "stylesheet");
            Elements microformats_subsection=doc.getElementsByAttributeValueContaining("rel", "subsection");
            Elements microformats_sweetheart=doc.getElementsByAttributeValueContaining("rel", "sweetheart");
            Elements microformats_tag=doc.getElementsByAttributeValueContaining("rel", "tag");
            Elements microformats_toc=doc.getElementsByAttributeValueContaining("rel", "toc");
            Elements microformats_transformation=doc.getElementsByAttributeValueContaining("rel", "transformation");
            Elements microformats_appleti=doc.getElementsByAttributeValueContaining("rel", "apple-touch-icon");
            Elements microformats_appletip=doc.getElementsByAttributeValueContaining("rel", "apple-touch-icon-precomposed");
            Elements microformats_appletsi=doc.getElementsByAttributeValueContaining("rel", "apple-touch-startup-image");
            Elements microformats_attachment=doc.getElementsByAttributeValueContaining("rel", "attachment");
            Elements microformats_can=doc.getElementsByAttributeValueContaining("rel", "canonical");
            Elements microformats_categ=doc.getElementsByAttributeValueContaining("rel", "category");
            Elements microformats_compon=doc.getElementsByAttributeValueContaining("rel", "component");
            Elements microformats_chromewebi=doc.getElementsByAttributeValueContaining("rel", "chrome-webstore-item");
            Elements microformats_disclosure=doc.getElementsByAttributeValueContaining("rel", "disclosure");
            Elements microformats_discussion=doc.getElementsByAttributeValueContaining("rel", "discussion");
            Elements microformats_dns=doc.getElementsByAttributeValueContaining("rel", "dns-prefetch");
            Elements microformats_edit=doc.getElementsByAttributeValueContaining("rel", "edit");
            Elements microformats_edituri=doc.getElementsByAttributeValueContaining("rel", "EditURI");
            Elements microformats_entrycon=doc.getElementsByAttributeValueContaining("rel", "entry-content");
            Elements microformats_external=doc.getElementsByAttributeValueContaining("rel", "external");
            Elements microformats_home=doc.getElementsByAttributeValueContaining("rel", "home");
            Elements microformats_hub=doc.getElementsByAttributeValueContaining("rel", "hub");
            Elements microformats_inreplyto=doc.getElementsByAttributeValueContaining("rel", "in-reply-to");
            Elements microformats_index=doc.getElementsByAttributeValueContaining("rel", "index");
            Elements microformats_indieauth=doc.getElementsByAttributeValueContaining("rel", "indieauth");
            Elements microformats_issues=doc.getElementsByAttributeValueContaining("rel", "issues");
            Elements microformats_lightbox=doc.getElementsByAttributeValueContaining("rel", "lightbox");
            Elements microformats_meta=doc.getElementsByAttributeValueContaining("rel", "meta");
            Elements microformats_openid=doc.getElementsByAttributeValueContaining("rel", "opendid");
            Elements microformats_p3pv1=doc.getElementsByAttributeValueContaining("rel", "p3pv1");
            Elements microformats_pgpkey=doc.getElementsByAttributeValueContaining("rel", "pgpkey");
            Elements microformats_pingback=doc.getElementsByAttributeValueContaining("rel", "pingback");
            Elements microformats_prerender=doc.getElementsByAttributeValueContaining("rel", "prerender");
            Elements microformats_profile=doc.getElementsByAttributeValueContaining("rel", "profile");
            Elements microformats_rendition=doc.getElementsByAttributeValueContaining("rel", "rendition");
            Elements microformats_service=doc.getElementsByAttributeValueContaining("rel", "service");
            Elements microformats_shortlink=doc.getElementsByAttributeValueContaining("rel", "shortlink");
            Elements microformats_sidebar=doc.getElementsByAttributeValueContaining("rel", "sidebar");
            Elements microformats_sitemap=doc.getElementsByAttributeValueContaining("rel", "sitemap");
            Elements microformats_subresource=doc.getElementsByAttributeValueContaining("rel", "subresource");
            Elements microformats_syndication=doc.getElementsByAttributeValueContaining("rel", "syndication");
            Elements microformats_timesheet=doc.getElementsByAttributeValueContaining("rel", "timesheet");
            Elements microformats_webmention=doc.getElementsByAttributeValueContaining("rel", "webmention");
            Elements microformats_widget=doc.getElementsByAttributeValueContaining("rel", "widget");
            Elements microformats_wlwmanifest=doc.getElementsByAttributeValueContaining("rel", "wlwmanifest");
            Elements microformats_imgsrc=doc.getElementsByAttributeValueContaining("rel", "image_src");
            Elements microformats_cmisacl=doc.getElementsByAttributeValueContaining("rel", "http://docs.oasis-open.org/ns/cmis/link/200908/acl");
            Elements microformats_stylesheetless=doc.getElementsByAttributeValueContaining("rel", "stylesheet/less");
            Elements microformats_accessibility=doc.getElementsByAttributeValueContaining("rel", "accessibility");
            Elements microformats_biblio=doc.getElementsByAttributeValueContaining("rel", "bibliography");
            Elements microformats_cite=doc.getElementsByAttributeValueContaining("rel", "cite");
            Elements microformats_group=doc.getElementsByAttributeValueContaining("rel", "group");
            Elements microformats_jslicence=doc.getElementsByAttributeValueContaining("rel", "jslicense");
            Elements microformats_longdesc=doc.getElementsByAttributeValueContaining("rel", "longdesc");
            Elements microformats_map=doc.getElementsByAttributeValueContaining("rel", "map");
            Elements microformats_member=doc.getElementsByAttributeValueContaining("rel", "member");
            Elements microformats_source=doc.getElementsByAttributeValueContaining("rel", "source");
            Elements microformats_status=doc.getElementsByAttributeValueContaining("rel", "status");
            Elements microformats_archive=doc.getElementsByAttributeValueContaining("rel", "archive");
            Elements microformats_archives=doc.getElementsByAttributeValueContaining("rel", "archives");
            Elements microformats_comment=doc.getElementsByAttributeValueContaining("rel", "comment");
            Elements microformats_contribution=doc.getElementsByAttributeValueContaining("rel", "contribution");
            Elements microformats_endorsed=doc.getElementsByAttributeValueContaining("rel", "endorsed");
            Elements microformats_fan=doc.getElementsByAttributeValueContaining("rel", "fan");
            Elements microformats_feed=doc.getElementsByAttributeValueContaining("rel", "feed");
            Elements microformats_footnote=doc.getElementsByAttributeValueContaining("rel", "footnote");
            Elements microformats_icon=doc.getElementsByAttributeValueContaining("rel", "icon");
            Elements microformats_kinstyle=doc.getElementsByAttributeValueContaining("rel", "kinetic-stylesheet");
            Elements microformats_prettyphoto=doc.getElementsByAttributeValueContaining("rel", "prettyPhoto");
            Elements microformats_clearbox=doc.getElementsByAttributeValueContaining("rel", "clearbox");
            Elements microformats_made=doc.getElementsByAttributeValueContaining("rel", "made");
            Elements microformats_microsummary=doc.getElementsByAttributeValueContaining("rel", "microsummary");
            Elements microformats_noreferrer=doc.getElementsByAttributeValueContaining("rel", "noreferrer");
            Elements microformats_permalink=doc.getElementsByAttributeValueContaining("rel", "permalink");
            Elements microformats_popover=doc.getElementsByAttributeValueContaining("rel", "popover");
            Elements microformats_prefetch=doc.getElementsByAttributeValueContaining("rel", "prefetch");
            Elements microformats_publickey=doc.getElementsByAttributeValueContaining("rel", "publickey");
            Elements microformats_publisher=doc.getElementsByAttributeValueContaining("rel", "publisher");
            Elements microformats_referral=doc.getElementsByAttributeValueContaining("rel", "referral");
            Elements microformats_related=doc.getElementsByAttributeValueContaining("rel", "related");
            Elements microformats_replies=doc.getElementsByAttributeValueContaining("rel", "replies");
            Elements microformats_resource=doc.getElementsByAttributeValueContaining("rel", "resource");
            Elements microformats_search=doc.getElementsByAttributeValueContaining("rel", "search");
            Elements microformats_sponsor=doc.getElementsByAttributeValueContaining("rel", "sponsor");
            Elements microformats_tooltip=doc.getElementsByAttributeValueContaining("rel", "tooltip");
            Elements microformats_trackback=doc.getElementsByAttributeValueContaining("rel", "trackback");
            Elements microformats_unendorsed=doc.getElementsByAttributeValueContaining("rel", "unendorsed");
            Elements microformats_user=doc.getElementsByAttributeValueContaining("rel", "user");
            Elements microformats_wlw=doc.getElementsByAttributeValueContaining("rel", "wlwmanifest");
            //-----microformats2
            Elements microformats2_hadr=doc.getElementsByAttributeValueContaining("class", "h-adr");
            Elements microformats2_hcard=doc.getElementsByAttributeValueContaining("class", "h-card");
            Elements microformats2_hentry=doc.getElementsByAttributeValueContaining("class", "h-entry");
            Elements microformats2_hevent=doc.getElementsByAttributeValueContaining("class", "h-event");
            Elements microformats2_hgeo=doc.getElementsByAttributeValueContaining("class", "h-geo");
            Elements microformats2_hitem=doc.getElementsByAttributeValueContaining("class", "h-item");
            Elements microformats2_hproduct=doc.getElementsByAttributeValueContaining("class", "h-product");
            Elements microformats2_hrecipe=doc.getElementsByAttributeValueContaining("class", "h-recipe");
            Elements microformats2_hresume=doc.getElementsByAttributeValueContaining("class", "h-resume");
            Elements microformats2_hreview=doc.getElementsByAttributeValueContaining("class", "h-review");
            Elements microformats2_hreviewagg=doc.getElementsByAttributeValueContaining("class", "h-review-aggregate");
            Elements foaf_autodiscoveries=doc.getElementsByAttributeValueContaining("href", "foaf");
            Elements foaf_types=doc.getElementsByAttributeValueContaining("type", "foaf");
            Elements media = doc.select("embed");
            Elements iframes = doc.select("iframe");
            Elements script_el=doc.select("script");
            Elements reltags=doc.select("link[rel]");
            Elements reltags_a=doc.select("a[rel]");
            number_embeded_videos=media.size();
            scripts_number=script_el.size();
            frames_number=iframes.size();
            nschem=schemas.size();
            hreln=reltags.size()+reltags_a.size();
            foaf=foaf_autodiscoveries.size()+foaf_types.size();
            micron1=microformats_cmisacl.size()+microformats_vcard.size()+microformats_vevent.size()+microformats_hreview.size()+microformats_vgeo.size()+microformats_vcalendar.size()+microformats_vadrn.size()+microformats_acquaintance.size()+microformats_alternate.size()+microformats_appendix.size()+ microformats_biblio.size()+microformats_bookmark.size()+microformats_chapter.size()+ microformats_child.size()+microformats_coll.size()+microformats_contact.size()+microformats_contents.size()+microformats_copyright.size()+microformats_coresident.size()+microformats_coworker.size()+microformats_crush.size()+microformats_date.size()+microformats_friend.size()+microformats_glossary.size()+microformats_help.size()+microformats_itsrules.size()+microformats_kin.size()+microformats_license.size()+microformats_me.size()+microformats_met.size()+microformats_muse.size()+microformats_neighbor.size()+microformats_next.size()+microformats_nofollow.size()+microformats_parent.size()+microformats_prev.size()+microformats_previous.size()+microformats_section.size()+microformats_sibling.size()+microformats_spouse.size()+microformats_start.size()+microformats_stylesheet.size()+microformats_subsection.size()+microformats_sweetheart.size()+microformats_tag.size()+microformats_toc.size()+microformats_transformation.size()+microformats_appleti.size()+microformats_appletip.size()+microformats_appletsi.size()+microformats_attachment.size()+microformats_can.size()+microformats_categ.size()+microformats_compon.size()+microformats_chromewebi.size()+microformats_disclosure.size()+microformats_discussion.size()+microformats_dns.size()+microformats_edit.size()+microformats_edituri.size()+microformats_entrycon.size()+microformats_external.size()+microformats_home.size()+microformats_hub.size()+microformats_inreplyto.size()+microformats_index.size()+microformats_indieauth.size()+microformats_issues.size()+microformats_lightbox.size()+microformats_meta.size()+microformats_openid.size()+microformats_p3pv1.size()+microformats_pgpkey.size()+microformats_pingback.size()+microformats_prerender.size()+microformats_profile.size()+microformats_rendition.size()+microformats_service.size()+microformats_shortlink.size()+microformats_sidebar.size()+microformats_sitemap.size()+microformats_subresource.size()+microformats_syndication.size()+microformats_timesheet.size()+ microformats_webmention.size()+microformats_widget.size()+microformats_wlwmanifest.size()+microformats_imgsrc.size()+microformats_imgsrc.size()+microformats_stylesheetless.size()+microformats_accessibility.size()+microformats_accessibility.size()+microformats_cite.size()+microformats_group.size()+ microformats_jslicence.size()+microformats_longdesc.size()+microformats_map.size()+microformats_member.size()+microformats_source.size()+ microformats_status.size()+microformats_archive.size()+microformats_archives.size()+microformats_comment.size()+microformats_contribution.size()+microformats_endorsed.size()+microformats_fan.size()+microformats_feed.size()+microformats_footnote.size()+microformats_icon.size()+microformats_kinstyle.size()+microformats_prettyphoto.size()+microformats_clearbox.size()+microformats_made.size()+microformats_microsummary.size()+microformats_noreferrer.size()+microformats_permalink.size()+microformats_popover.size()+microformats_prefetch.size()+microformats_publickey.size()+microformats_publisher.size()+microformats_referral.size()+microformats_related.size()+microformats_replies.size()+microformats_resource.size()+microformats_search.size()+microformats_sponsor.size()+microformats_tooltip.size()+microformats_trackback.size()+microformats_unendorsed.size()+microformats_user.size()+microformats_wlw.size()+foaf;
            micron2=microformats2_hadr.size()+microformats2_hcard.size()+microformats2_hentry.size()+microformats2_hevent.size()+microformats2_hgeo.size()+microformats2_hitem.size()+microformats2_hproduct.size()+microformats2_hrecipe.size()+microformats2_hresume.size()+microformats2_hreview.size()+microformats2_hreviewagg.size();
            total_micron=micron1+micron2;
            microd=microdata.size();
            return true;
        } catch (IOException | IllegalCharsetNameException ex) {
            Logger.getLogger(com.thesmartweb.swebrank.WebParser.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    
    }  

    /**
     * Method to get all the elements with a specific html feature (not used in SWebRank's current version)
     * @param link_html the url to check
     * @param dir the directory to save the file
     * @return a list with the text of all the elements
     */
    public List<String> getbold(String link_html,String dir)
    {  List<String> SEwords=new ArrayList<String>();
        try {
            //link_html="http://www.themismavridis.com/";
            Document doc = Jsoup.connect(link_html).get();
            //---------to select the rest of the terms
            Elements bold= doc.select("em");
           //bold=bold.select("b");
            for (Element btext : bold) {
                String stringtosplit = btext.text().toString().toString();
                    if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){       
                        stringtosplit=stringtosplit.replaceAll("[\\W&&[^\\s]]", "");
                        if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){
                            String[] tokenizedTerms=stringtosplit.split("\\W+");
                            for(int j=0;j<tokenizedTerms.length;j++){
                                if(!(tokenizedTerms[j]==null)&&(!(tokenizedTerms[j].equalsIgnoreCase("")))){
                                    SEwords.add(tokenizedTerms[j]);
                                }    
                            }
                        }
                    }
            }
            File file_thelist = new File(dir+"Javawords.txt");
            FileUtils.writeLines(file_thelist, SEwords);
            return SEwords;
        }  catch (IOException ex) {
            Logger.getLogger(WebParser.class.getName()).log(Level.SEVERE, null, ex);
            System.out.print("can not create the content file for SEwords");
            return SEwords;
        }
    
    }

    /**
     * Method to check if we can connect with JSOUP to a specific url
     * @param link_html the url to connect
     * @return true/false
     */
    public boolean checkconn(String link_html){  
        try {
            Connection.Response response = Jsoup.connect(link_html).timeout(10*1000).execute();
            return response.statusCode() == 200;
        }  catch (Exception ex) {
            Logger.getLogger(WebParser.class.getName()).log(Level.SEVERE, null, ex);
            System.out.print("can not connect to:"+link_html);
            return false;
        }
    }
 
}
