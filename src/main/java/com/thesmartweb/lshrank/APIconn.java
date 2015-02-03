/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thesmartweb.lshrank;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import javax.net.ssl.HttpsURLConnection;
/**
 *
 * @author Themis Mavridis
 */
public class APIconn {

    /**
     *
     */
    public HttpURLConnection httpCon;

    /**
     *
     */
    public HttpsURLConnection httpsCon;
    //public HttpsURLConnection[] httpsConn;

    /**
     *
     * @param link_ur
     * @return
     */
    public String sslconnect(URL link_ur) {
        try {
            HttpsURLConnection httpsConn=(HttpsURLConnection) link_ur.openConnection();
            String line=httpsConn.getResponseMessage();
            return line; 
        } catch (IOException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            String line="fail";
            return line;
        }

}

    /**
     *
     * @param link_ur
     * @return
     */
    public String connect(URL link_ur) {
        try {
            httpCon = (HttpURLConnection) link_ur.openConnection();
            if (httpCon.getResponseCode() != 200) {
                String line;
                line = "fail";
                return line;
                // throw new IOException(httpCon.getResponseMessage());
            } else {
                String line;
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpCon.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                }
                return line;
            }
        } catch (IOException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            String line="fail";
            return line;
        }

}

    /**
     *
     * @param link
     * @return
     */
    public String check_conn(String link){
        try {
            link=link.trim();
            String line="fail-conn";
            if(link.startsWith("http")){
                URL link_ur=new URL(link);
                line="DNS-error";
                if(link.startsWith("http:")){
                    httpCon = (HttpURLConnection) link_ur.openConnection();
                    httpCon.setDefaultUseCaches(false);
                    httpCon.setReadTimeout(20000);
                    httpCon.setDoInput(true);
                    httpCon.connect();
                    line="fail-conn";
                    try{
                        int responseCode=httpCon.getResponseCode();
                        if (responseCode==200){line="ok-conn";}
                    }
                    catch (Exception e){
                        System.out.println(link);
                        System.gc();
                        System.gc();
                        System.gc();
                        httpCon=null;
                        line="fail-conn";
                        return line;
                    }
                }
                else if (link.startsWith("https")){
                    httpsCon = (HttpsURLConnection) link_ur.openConnection();
                    httpsCon.setDefaultUseCaches(false);
                    httpsCon.setReadTimeout(20000);
                    httpsCon.setDoInput(true);
                    httpsCon.connect();
                    //httpCon.connect();
                    line="fail-conn";
                    try{
                        int responseCode=httpsCon.getResponseCode();
                        if (responseCode==200){line="ok-conn";}
                    }
                    catch (Exception e){
                        System.out.println(link);
                        System.gc();
                        System.gc();
                        System.gc();
                        httpsCon=null;
                        line="fail-conn";
                        return line;
                    }
                }
            }
            return line;
        } catch (MalformedURLException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            return "fail-conn";
        } catch (IOException ex) {
            Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            return "fail-conn";
        } 
        
}

    /**
     *
     * @param link_ur
     * @return
     */
    public String azureconnect(URL link_ur){
    String string_link_ur=link_ur.toString();
    String line="fail";
    if(string_link_ur.substring(23,28).equalsIgnoreCase("azure")){
        HttpsURLConnection[] httpsConn=new HttpsURLConnection[32];
        //String accountKey = "dvr6F3vxbj/LG4TOWzvrOHWOKP3/vGJAwm1bpMaBg+Y=";
        String[] accKeys = new String[32];
        accKeys[0]="/5a3s9S0ZrmVyZEob/xLWou8p0Sd5cIgBCzpPpR+Y7U=";//thmavrid84
        accKeys[1]="IRR/3p5Ag4unKUEnXAHqF0u0Bucg8d5adH1Op5TVCgs";//thmavri8436
        accKeys[2]="dvr6F3vxbj/LG4TOWzvrOHWOKP3/vGJAwm1bpMaBg+Y=";
        accKeys[3]="oiXaOibBuThBPdtsKOJpIM0LcLxtFX2L8l3lTsVeFTg";//thmavri843
        accKeys[4]="C1KD6JhkA0dyF7gjqluP0D/+d890wzFdqEp1ART2V8Q";//thmavri84364(outlook)
        accKeys[5]="3ZQEDS1AE3ukOKCEwbY/uL/GiMSF9pyKqGOJZxO1K3A";//thmavrid@843
        accKeys[6]="T/HTri4fu4FXIDfMK0VOUwzBIbIE4WUvWgp2ZKXSQvc";//thmavrid8436
        accKeys[7]="yVHjEHf2a9XN6BV1fI0Oqp7lINf8br5A6kjVmfQWUF0";
        accKeys[8]="QPl9Wjb5rxN0s0zxmNq3qFWG4IMvXcTWwB7nNa5w32E";//tmavrid84
        accKeys[9]="d7i2JqDvq8fBCYsQ1QAEkybeKpz6YY/5RzsvLm/N4PM";//tmavri843
        accKeys[10]="akKudvJ2/+aFcR/l2pMDqb0nZSmwEoIS+SNUdyMrfAA";//tmavrid8436@OUTLOOK
        accKeys[11]="0LWLD/Krc8onakw2mTD7017vScu7hcn1ocrc5Y4yHHU";//tmavrid84364
        accKeys[12]="rHppuc0aLG/FipWx3X9TJaRx1+purV84WjvT/2iwtus";//tmavrid8436
        accKeys[13]="L7yMjge0M8eR0C5YJycTWKVvOTm4vs0fX9QbHlr3Nz8";//tmavrid84 outlook
        accKeys[14]="v8u9ratqQrUq49/lOw4ZxgScnXMSatR0tSLM06ri1Tw";//tmavrid843 outlook
        accKeys[15]="RIRN9XMJfH9MoLIUR4jp/EgrvPpYyLieh16aJS4vnFw";//tmavrid8436 outlook
        accKeys[16]="WMbmT68sfvBdEH9No+B13ScmKaqGs98qLI5YlxQWnMM";//tmavrid84364 outlook
        accKeys[17]="IA1xbnFzI7GtVo8uZPlqxAMMZVFgQqBk783FmnliniY";//tmavrid843647 outlook
        accKeys[18]="mBynBAjzMMTZrBiG01j7GCIUw9wNE26R085JSW+ZSaY";//tmavrid84 live
        accKeys[19]="84KePB+IGuxG6Rs9UkhgpJ2rzSjy3gfe8hN/3ri0XkY";//tmavrid843 live
        accKeys[20]="mwrL9MvphfbGUWthEAoMVur8IosvhxzwGhOphMOQeol";//tmavrid8436 live
        accKeys[21]="AsTZ3Xn2/JzmyyJEcw7DztQc7+TM+aX1/ObdlZHpCMQ";//tmavrid84364 live
        accKeys[22]="s2c7LOjHWvca9BRYZPuBr6Nyw5nF+vIRJuRXcTg8Cr";//tmavrid843647 live*/
        accKeys[23]="cwPF/62/5GaZPZ6Id3Bmier6+VJLihUnfpOai9Ep71s";//ttmavrid outlook
        accKeys[24]="YD1nY7Z9hAT9H0P4OtO3iwIUDkcuEW+3xx9XAVVRpOI";//ttmavrid hotmail
        accKeys[25]="+eDZxz198zokCwchuevKO5HZ0XaPZoFD3hgD4fCkalg";//ttmavrid live
        accKeys[26]="FXb8Sm/1F+dYrKbHsEgCArNiY8NYDmGXlbUml4BmROA";//tthmavrid outlook
        accKeys[27]="yarxtng53tLALDKP1RlzMCvuaHy2wTE49N1QMHSO7Ls";//tthmavrid hotmail
        accKeys[28]="8Fkt4bwa9riv4g1SOYsBH+AwfF/A8w8bHbNF50zfLO4";//tthmavrid live
        accKeys[29]="GEWTCUtmDlxr+W8VzG+GTg7XmDV77TYUnVijyg+UFJY";//tthmavri outlook
        accKeys[30]="K4Jjq9oGz0fiU2uEU1/rIyc+wSGw9qtZkTvpyPvF9kU";//tthmavri hotmail
        accKeys[31]="J8HH3xFTZxX6AXK2rx8hSSPYQ6pXPZuVqa6SgE6Clhk";//tthmavri live
        int i=-1;
        int respp=0;
        do{
            try {
                i++;
                httpsConn[i]=(HttpsURLConnection) link_ur.openConnection();
                byte[] accountKeyBytes = Base64.encodeBase64((accKeys[i]+":"+accKeys[i]).getBytes());
                String accountKeyEnc = new String(accountKeyBytes);
                httpsConn[i].setRequestProperty("Authorization","Basic "+accountKeyEnc);
                respp=httpsConn[i].getResponseCode();
            } catch (IOException ex) {
                Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
                return line;
            }
        }
        while(respp!=200&&i<accKeys.length-1);
        int j=i;
        if(j>0){
            for(int k=0;k<j;k++){
                httpsConn[k].disconnect();
            }
        }
        if (respp != 200){
            
             if(respp!=503){
                 try {
                     String responseMessage = httpsConn[i].getResponseMessage();
                     if(responseMessage.startsWith("Insufficient")){
                         line="insufficient";
                     }
                     if(responseMessage.contains("provided")){
                         line="provided";
                     }
                 } catch (IOException ex) {
                     Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
                     return line;
                 }
             }
             return line;
             // throw new IOException(httpCon.getResponseMessage());
       } 
       else {
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(httpsConn[i].getInputStream()));
                StringBuilder sb = new StringBuilder();
                line="";
                while ((line = rd.readLine()) != null) {
                sb.append(line);
            }   line = sb.toString();
                rd.close();
                return line;
            } catch (IOException ex) {
                Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if(rd!=null){
                        rd.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(APIconn.class.getName()).log(Level.SEVERE, null, ex);
                    return line;
                }
            }
        }                
    }
    return line;

}

}
