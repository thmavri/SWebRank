package DOMHandling;

import java.net.MalformedURLException;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import java.io.IOException;
import java.net.URL;


public class JDOMValidator {

  public static void main(String[] args) throws MalformedURLException {
  
    URL url = new URL("http://www.auth.gr");
      
    SAXBuilder builder = new SAXBuilder();
    
  
                                    //  ^^^^
                                    // Turn on validation
     
    // command line should offer URIs or file names
    try {
      builder.build(url);
      // If there are no well-formedness or validity errors, 
      // then no exception is thrown.
      System.out.println(url + " is valid.");
    }
    // indicates a well-formedness or validity error
    catch (JDOMException e) { 
      System.out.println(url + " is not valid.");
      System.out.println(e.getMessage());
    }  
    catch (IOException e) { 
      System.out.println("Could not check " + url);
      System.out.println(" because " + e.getMessage());
    }  
  
  }

}