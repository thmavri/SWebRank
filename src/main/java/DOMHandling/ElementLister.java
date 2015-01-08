package DOMHandling;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Element;

public class ElementLister {

  public static void main(String[] args) throws IOException {
  
    
    SAXBuilder builder = new SAXBuilder();
    URL url = new URL("http://localhost/iFRM.html");
    try {
        org.jdom2.Document doc = builder.build(url);
        org.jdom2.Element root = doc.getRootElement();
        listChildren(root, 0);      
    }
    // indicates a well-formedness error
    catch (JDOMException e) { 
      System.out.println(url + " is not well-formed.");
      System.out.println(e.getMessage());
    }  
    catch (IOException e) { 
      System.out.println(e);
    }  
  
  }
  
  
  public static void listChildren(org.jdom2.Element current, int depth) {
   
    printSpaces(depth);
    System.out.println(current.getName());
    List children = current.getChildren();
    Iterator iterator = children.iterator();
    while (iterator.hasNext()) {
            org.jdom2.Element child = (org.jdom2.Element) iterator.next();
      listChildren(child, depth+1);
    }
    
  }
  
  private static void printSpaces(int n) {
    
    for (int i = 0; i < n; i++) {
      System.out.print(' '); 
    }
    
  }

}