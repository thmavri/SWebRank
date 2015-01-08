/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;

/**
 *
 * @author Themis Mavridis
 */
import com.seomoz.api.authentication.Authenticator;
import com.seomoz.api.service.URLMetricsService;
public class CheckMoz {
public boolean check(){
                boolean moz=false;
                String accessID = "member-87c6a749b0";
                //Add your secretKey here
                String secretKey = "46fed510cc0c03a934b65ddc5ca54cfa";
                Authenticator authenticator = new Authenticator();
		authenticator.setAccessID(accessID);
		authenticator.setSecretKey(secretKey);
                String objectURL ="www.sei.org";
                URLMetricsService urlMetricsService = new URLMetricsService(authenticator);
                String response = urlMetricsService.getUrlMetrics(objectURL);
                //if moz fails we set the moz option "false" and if we do not have merged option as true we set the results number=top_count_seomoz
                if(response.length()!=0){
                    moz=true;
                }
                return moz;
}
}
