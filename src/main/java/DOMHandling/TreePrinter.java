package DOMHandling;

import java.net.MalformedURLException;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TreePrinter {
    private ArrayList linksList = new ArrayList();
    private ArrayList linksListiframes = new ArrayList();
    private ArrayList linksListscripts = new ArrayList();
    private ArrayList linksListvideo = new ArrayList();
    private ArrayList semanticLinks= new ArrayList();
    String pageWords="";
    private String nestedIframesURL="";
    int cnt_frames=0;
    int cnt_frames_in=0;
    int cnt_src=0;
    int cnt_src_in=0;
    int cnt_emb=0;
    int cnt_emb_in=0;
    int cnt_auth=0;
    int links=0;  
    int schema_cnt=0;
    int hcardsn=0;
    int hreviewsn=0;
    int hevents=0;
    int hcalendars=0;
    int adrn=0;
    int geo=0;
    int reltags=0;
    int total_microformats=0;
    public TreePrinter(Document document,String parentURL){
      try{//System.out.println("i am in the tree");
        
      nestedIframesURL = parentURL;
      if(nestedIframesURL.contains("localhost")){
         nestedIframesURL = "http://"+nestedIframesURL; 
      }
      
      
      // Process the root element
      try{
          process(document.getRootElement(),parentURL);
                //System.out.println("out of process");
      }catch(Exception e){
          System.out.println("url is not well formed");
      }
      
      }
      catch (Exception ex){
       System.out.println("exception treeprinter main");                 
       Logger.getLogger(TreePrinter.class.getName()).log(Level.SEVERE, null, ex);}
    }
     
    
    private void printLinks(ArrayList l){
       try{ for(int i=0;i<linksList.size();i++){
          System.out.println((i+1)+")"+linksList.get(i));
      }}
       catch (Exception ex){
       System.out.println("exception treeprinter printlinks");                 
       Logger.getLogger(TreePrinter.class.getName()).log(Level.SEVERE, null, ex);}
    }
    public ArrayList getLinks(){
        
      
        return linksList;
    }
      public ArrayList getscriptLinks(){
        
      
        return linksListscripts;
    }
        public ArrayList getframeLinks(){
        
      
        return linksListiframes;
    }
    public ArrayList getvideoLinks(){
        
      
        return linksListvideo;
    }
        
  
   public String getPageWords(){
       return pageWords;
   }
   public int getsrc(){
       return cnt_src;
   }
   public int getsrc_in(){
       return cnt_src_in;
   }
   public int getframes(){
       
       return cnt_frames;
   }
   public int getframes_in(){
       
       return cnt_frames_in;
   }
   public int getemb(){
       
       return cnt_emb;
   }
   public int getemb_in(){
       
       return cnt_emb_in;
   }
    public int getauth(){
       
       return cnt_auth;
   }
     public int getschema(){
       
       return schema_cnt;
   }
     public int gethcards(){
       
       return hcardsn;
   }
      public int gethreviews(){
       
       return hreviewsn;
   }
      public int gethevents(){
       
       return hevents;
   }
      public int gethcalendars(){
       
       return hcalendars;
   }
       public int getadrn(){
       
       return adrn;
   }
     public int getgeo(){
       
       return geo;
   }
     public int reltags(){
       
       return reltags;
   }
    public int total_micro(){
       
       return total_microformats;
   }
     
     
    

  // Recursively descend the tree
  public  void process(Element element,String parentURL) {
   try{ System.gc();
          System.gc();
          System.gc();
          System.gc();
    inspect(element,parentURL);
          //System.out.println("out of finished inspect");
    List content = element.getContent();
    Iterator iterator = content.iterator();
    
    while (iterator.hasNext()) {
      Object o = iterator.next();
      if (o instanceof Element) {
        Element child = (Element) o;
        process(child,parentURL);
      }
    }
          //System.out.println("finished process");
    int j=0;
   }
   catch (Exception ex){
       System.out.println("exception treeprinter process");                 
       Logger.getLogger(TreePrinter.class.getName()).log(Level.SEVERE, null, ex);}
  }

  // Print the properties of each element
  public  void inspect(Element element,String parentURL) {
     try{
      System.gc();
          System.gc();
          System.gc();
          System.gc();
    boolean foundIframe = false;
    boolean foundscript = false;
    boolean foundparam = false;
    boolean foundembed = false;
    boolean foundlink = false;
    boolean founddiv = false;
    boolean foundmeta = false;
    boolean foundsth=false;
    boolean foundaref=false;
    Namespace namespace = element.getNamespace();
    String localName="";
    
    if (namespace != Namespace.NO_NAMESPACE) {
      localName = element.getName();
     
      if(localName.toUpperCase().equals("IFRAME")){
          foundIframe = true;
          cnt_frames++;
          //foundsth = true;
      }
      
      if(localName.toUpperCase().equals("SCRIPT")){
          foundscript = true; 
          cnt_src++;
           
          //foundsth = true;
      }
       if(localName.toUpperCase().equals("EMBED")){
          foundembed = true;  
          cnt_emb++;
         // foundsth = true;
      }
       if(localName.toUpperCase().equals("LINK")){
          foundlink= true; 
          foundsth = true;
      }
       if(localName.toUpperCase().equals("DIV")){
          founddiv= true; 
          //foundsth = true;
      }
        if(localName.toUpperCase().equals("META")){
          foundmeta= true; 
          //foundsth = true;
      }
        if(localName.toUpperCase().equals("A")){
          foundaref= true; 
          //foundsth = true;
      }
      
  
    }
    List attributes = element.getAttributes();
    foundsth=true;
    String domain="";
                        if(parentURL.substring(0,5).equalsIgnoreCase("https"))
                        { 
                            domain=parentURL.substring(8);
                        }
                        else if(parentURL.substring(0,4).equalsIgnoreCase("http"))
                        { 
                            domain=parentURL.substring(7);
                        }
                        if(domain.length()>4){
                            if(domain.startsWith("www.")){
                                domain=domain.substring(4);
                            }

                        }
                        domain=domain.split("\\.")[0];
    if (!attributes.isEmpty()&&foundsth) {
      Iterator iterator = attributes.iterator();
      while (iterator.hasNext()) {
          System.gc();
          System.gc();
          System.gc();
          System.gc();
        Attribute attribute = (Attribute) iterator.next();
        String name = attribute.getName();
        String value = attribute.getValue();
        String url_temp_frame="";
        String value_temp_frame="";
        String url_temp_src="";
        String value_temp_src="";
        //System.out.println("("+parentURL+":"+localName+":"+name+","+value+")");//to name exei timh href kai to value einai to onoma tou link
              if(foundIframe&&name.equals("src")){
                  //cnt_frames++;
                  if(value.contains(domain)){
                  cnt_frames_in++;
                  
                  }
                  /*  
                  if(!value.contains("http")){
                        if(nestedIframesURL.endsWith("/")){
                        url_temp_frame=nestedIframesURL;
                        }
                        else {
                        url_temp_frame=nestedIframesURL+"/";
                        }
                        if(value.startsWith("/"))
                        {value_temp_frame=value.substring(1);}
                        if(value.startsWith("//"))
                        {value_temp_frame=value.substring(2);}
                        linksListiframes.add(url_temp_frame+value_temp_frame);

                    }else{
                        linksListiframes.add(value);
                    }*/
                }
              /*if(foundlink){
                    
                    if(name.equals("rel")&&(value.contains("me")||value.contains("author")||value.toString().contains("publisher"))){
                       cnt_auth++;
                    }
              }*/
           if(foundscript&&name.equals("src")){
                //cnt_src++;
                /*if(!value.contains("http")){
                    
                    if(value.startsWith("//")){linksListscripts.add("http:"+value);}
                    else if(value.startsWith("/")){linksListscripts.add(nestedIframesURL+"/"+value);}
                    else {linksListscripts.add("http://"+value);}
                               
                }else{
                    linksListscripts.add(value);
                }*/
                if(value.contains(domain)){
                  cnt_src_in++;
                  
                  }
                
            }
          if(foundembed&&name.equals("src")){
                    /*if(!value.contains("http")){
                    
                        if(value.startsWith("//")){linksListvideo.add("http:"+value);}
                         else if(value.startsWith("/")){linksListvideo.add(nestedIframesURL+"/"+value);}   
                         else {linksListvideo.add("http://"+value);}          
                    }else{
                    linksListvideo.add(value);
                    }*/
               if(value.contains(domain)){
                  cnt_emb_in++;
                  
                  }
                
          }
        //System.out.print("name:"+name+"\nvalue:"+value+"\n");
        //get <a href links>
        if(name.equals("href")){
                   /*if(!value.contains("http")){
                 
                    if(value.startsWith("//")){linksList.add("http:"+value);}
                    else if(value.startsWith("/")){linksList.add(nestedIframesURL+"/"+value);}
                    else {linksList.add("http://"+value);}
                               
                }else{
                    linksList.add(value);
                }*/
                if(value.contains("plus.google.com")&&(value.contains("rel=me")||value.contains("rel=author")||value.contains("rel=publisher"))){
                cnt_auth++;
                
                }
        }
         if(name.equals("itemtype")){
         
                if(value.contains("schema.org")){
                    schema_cnt++;
                
                }
         }
         if(name.equals("rel")){
         
                if(value.contains("me")||value.contains("author")||value.toString().contains("publisher")){
                    cnt_auth++;
                
                }
                if(foundaref){
                    reltags++;
                    total_microformats++;
                }
         }
          

         if(name.equals("class")){
         
                if(value.contains("vcard")){
                    hcardsn++;
                    total_microformats++;
                }
                if(value.contains("hreview")){
                    hreviewsn++;
                    total_microformats++;
                
                }
                if(value.contains("vevent")){
                    hevents++;
                    total_microformats++;
                
                }
                if(value.contains("vcalendar")){
                    hcalendars++;
                    total_microformats++;
                
                }
                if(value.contains("geo")){
                    geo++;
                    total_microformats++;
                
                }
                 if(value.contains("adrn")){
                    adrn++;
                    total_microformats++;
                
                }
         }
        
        //System.out.println("finished inspect");
        
        
      }
    }
    
    /*List namespaces = element.getAdditionalNamespaces();
    if (!namespaces.isEmpty()) {
      Iterator iterator = namespaces.iterator();
      while (iterator.hasNext()) {
        Namespace additional = (Namespace) iterator.next();
        String uri = additional.getURI();
        String prefix = additional.getPrefix();
    //      System.out.println(
    //       "  xmlns:" + prefix + "=\"" + uri + "\""); //ok
      }
    }*/
  }
  catch (Exception ex){
       System.out.println("exception treeprinter inspect");                 
       Logger.getLogger(TreePrinter.class.getName()).log(Level.SEVERE, null, ex);}
  }
  


}