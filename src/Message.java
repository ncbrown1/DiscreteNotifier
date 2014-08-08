/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eci.bcolor;

import com.sun.mail.smtp.SMTPTransport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author ncbrown
 */
public class Message {
    
    public static void sendEmail(String from, String to_csv, String subject, String body, com.eci.bcolor.Preferences pref) {
        try {
            Properties props = System.getProperties();
            props.put("mail.smtps.host",pref.getEmailHost());
            props.put("mail.smtps.auth","false");
            Session session = Session.getInstance(props, null);
            javax.mail.Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(to_csv, false));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setSentDate(new Date());
            SMTPTransport t = (SMTPTransport)session.getTransport("smtps");
            t.connect(pref.getEmailHost(), pref.getEmailUser(), pref.getEmailPassword());
            t.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Response: " + t.getLastServerResponse());
            t.close();
        } catch (MessagingException ex) {
            System.out.println("Error with Message -> " + ex.getMessage());
        }
    }
    
    public static void sendSMS(String rcpt, String message, com.eci.bcolor.Preferences pref) {
        Message.sms_send(pref.getSmsHost(),pref.getSmsPort(),pref.getSmsUser(),pref.getSmsPassword(),rcpt,message);
    }
    
    private static String sms_send(String server, int port, String user, String password, String phonenumber, String text) {
        String returnstring;
        returnstring = null;
        if (server == null) {
   	    System.out.println("sendsms.server value not set");
            return returnstring;
        }
        text = text.replace(" ", "%20");
        String url_str = "/sendmsg?user=" + user + "&passwd=" + password + "&cat=1&to=\"" + phonenumber + "\"&text=" + text;
        try {
            URL url2 = new URL("http", server, port, url_str);
            HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
            connection.setRequestMethod("GET");
            String res=connection.getResponseMessage();
            System.out.println("Response Code  -> "+res);
            int code = connection.getResponseCode () ;
            if ( code == HttpURLConnection.HTTP_OK ) {
                //Get response data.
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String str;
                while( null != ((str = in.readLine()))) {
                    if (str.startsWith("MessageID=")) {
                        returnstring = returnstring + str + "\r\n";
                        System.out.println(str);
                    }
                }
                connection.disconnect() ;
            }
        } catch(IOException e) {
            System.out.println("unable to create new url"+e.getMessage());
        }
        return returnstring;
    }
}
