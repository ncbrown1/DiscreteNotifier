package com.eci.bcolor;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ncbrown
 */
public class Preferences {
    
    private List<com.eci.bcolor.Contact> clist;
    private com.eci.bcolor.Contact self;
    private String mildMsg;
    private String urgentMsg;
    private String smsHost;
    private int smsPort;
    private String smsUser;
    private String smsPassword;
    private String emailHost;
    private final String home;
    
    public static void main(String[] args) {
        Preferences pref = new Preferences();
        pref.importFromDB();
    }
    
    public Preferences() {
        clist = new ArrayList<Contact>();
        self = new Contact();
        mildMsg = "";
        urgentMsg = "";
        home = System.getProperty("user.home"); 
    }
    
    public void exportToDB() {
        Connection con = null;
        Statement st = null;
        String url = "jdbc:derby:cdbconfig;create=true";
        try {
            System.setProperty("derby.system.home", home + "/.bcolor");
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
            
            con = DriverManager.getConnection(url);
            st = con.createStatement();
            st.executeUpdate("CREATE TABLE contacts(id INT PRIMARY KEY, name VARCHAR(30), email VARCHAR(64), sms VARCHAR(11), roomnum VARCHAR(64))");
            addContactsToDB(st);
            st.executeUpdate("CREATE TABLE messages(id INT PRIMARY KEY, message VARCHAR(255))");
            st.executeUpdate("INSERT INTO messages VALUES (1,'" + mildMsg + "')");
            System.out.println("Export Mild Message: " + mildMsg);
            st.executeUpdate("INSERT INTO messages VALUES (2,'" + urgentMsg + "')");
            System.out.println("Export Urgent Message: " + urgentMsg);
            st.executeUpdate("CREATE TABLE service (id INT PRIMARY KEY, host VARCHAR(255), port INT, uname VARCHAR(30), passwd VARCHAR(64))");
            st.executeUpdate("INSERT INTO service VALUES (1,'" + smsHost + "'," + smsPort + ",'" + smsUser + "','" + smsPassword + "')");
            System.out.println("Export SMS service definitions.");
            st.executeUpdate("INSERT INTO service VALUES (2,'" + emailHost + "'," + 587 + ",'" + "nobody" + "','" + "secret" + "')");
            System.out.println("Export email service definitions.");
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Preferences.class.getName());
            if (((ex.getErrorCode() == 50000) && ("XJ015".equals(ex.getSQLState())))) {
                System.out.println("Database export successful");
                //lgr.log(Level.INFO, "Derby shut down normally", ex);
            } else {
                if(ex.getMessage().contains("already exists")) {
                    overwriteDB();
                } else {
                    lgr.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(Preferences.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    private void overwriteDB() {
        Connection con = null;
        Statement st = null;
        String url = "jdbc:derby:cdbconfig;";
        try {
            System.setProperty("derby.system.home", home + "/.bcolor");
            
            con = DriverManager.getConnection(url);
            st = con.createStatement();
            st.execute("DELETE FROM contacts WHERE id >= 0");
            addContactsToDB(st);
            st.execute("DELETE FROM messages WHERE id >= 0");
            st.executeUpdate("INSERT INTO messages VALUES (1,'" + mildMsg + "')");
            System.out.println("Export Mild Message: " + mildMsg);
            st.executeUpdate("INSERT INTO messages VALUES (2,'" + urgentMsg + "')");
            System.out.println("Export Urgent Message: " + urgentMsg);
            st.execute("DELETE FROM service WHERE id >= 0");
            st.executeUpdate("INSERT INTO service VALUES (1,'" + smsHost + "'," + smsPort + ",'" + smsUser + "','" + smsPassword + "')");
            System.out.println("Export SMS service definitions.");
            st.executeUpdate("INSERT INTO service VALUES (2,'" + emailHost + "'," + 587 + ",'" + "nobody" + "','" + "secret" + "')");
            System.out.println("Export email service definitions.");
            DriverManager.getConnection("jdbc:derby:;shutdown=true");

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Preferences.class.getName());
            if (((ex.getErrorCode() == 50000) && ("XJ015".equals(ex.getSQLState())))) {
                System.out.println("Database export successful");
                //lgr.log(Level.INFO, "Derby shut down normally", ex);
            } else {
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(Preferences.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        
    }
    
    private void addContactsToDB(Statement st) throws SQLException {
        addDBContact(st, self, 0);
        System.out.println("Export Self Identity: " + self.stringForm());
        int i = 1;
        for(com.eci.bcolor.Contact c : clist) {
            System.out.println("Export Contact: " + c.stringForm());
            addDBContact(st,c,i);
            i++;
        }       
    }
    
    private void addDBContact(Statement st, com.eci.bcolor.Contact c, int i) throws SQLException {
        st.executeUpdate("INSERT INTO contacts VALUES(" + i + ",'" + c.getName() + "','" + c.getEmail() + "','" + c.getSms() + "','" + c.getRoom_num() + "')");
    }
    
    public void importFromDB() {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        String url = "jdbc:derby:cdbconfig;";
        try {
            System.setProperty("derby.system.home",  home + "/.bcolor");
            con = DriverManager.getConnection(url);
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM contacts");
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String email = rs.getString(3);
                String sms = rs.getString(4);
                String roomnum = rs.getString(5);
                com.eci.bcolor.Contact c = new com.eci.bcolor.Contact(name, email, sms, roomnum);
                if(id == 0) {
                    self = c;
                    System.out.println("Import Self Identity: " + c.stringForm());
                } else {
                    clist.add(c);
                    System.out.println("Import Contact: " + c.stringForm());
                }
            }
            rs = st.executeQuery("SELECT * FROM messages");
            while (rs.next()) {
                int id = rs.getInt(1);
                String message = rs.getString(2);
                if (id == 1) {
                    mildMsg = message;
                    System.out.println("Import Mild Message: " + message);
                } else if (id == 2) {
                    urgentMsg = message;
                    System.out.println("Import Urgent Message: " + message);
                }                
            }
            rs = st.executeQuery("SELECT * FROM service");
            while (rs.next()) {
                int id = rs.getInt(1);
                String host = rs.getString(2);
                int port = rs.getInt(3);
                String user = rs.getString(4);
                String pass = rs.getString(5);
                if (id == 1) {
                    smsHost = host;
                    smsPort = port;
                    smsUser = user;
                    smsPassword = pass;
                    System.out.println("Import SMS Definitions.");
                } else if (id == 2) {
                    emailHost = host;
                    System.out.println("Import Email Definitions.");
                }                
            }
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Preferences.class.getName());
            if (((ex.getErrorCode() == 50000) && ("XJ015".equals(ex.getSQLState())))) {
                System.out.println("Database import successful");
                //lgr.log(Level.INFO, "Derby shut down normally", ex);
            } else {
                if (ex.getMessage().contains("not found")) {
                    System.out.println("Database not found, creating database now.");
                    String defaultname = System.getProperty("user.name");
                    System.out.println("Setting user name property to: " + defaultname);
                    self.setName(defaultname);
                    exportToDB();
                }
                //lgr.log(Level.SEVERE, ex.getMessage(), ex);
            } 
        } finally { 
            try {
                if (rs != null) { rs.close(); }
                if (st != null) { st.close(); }
                if (con != null) { con.close(); }
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(Preferences.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    public String importPrefs(String path) {
        try {
            File fXmlFile = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
 
            doc.getDocumentElement().normalize();
            if(doc.getDocumentElement().getNodeName().equals("Preferences")) {
                NodeList contacts = doc.getElementsByTagName("contact");
                for(int i=0; i<contacts.getLength(); i++) {
                    Node cNode = contacts.item(i);
                    if(cNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element cElement = (Element) cNode;
                        String name = cElement.getElementsByTagName("name").item(0).getTextContent();
                        String email = cElement.getElementsByTagName("email").item(0).getTextContent();
                        String sms = cElement.getElementsByTagName("sms").item(0).getTextContent();
                        String roomnum = cElement.getElementsByTagName("roomnum").item(0).getTextContent();
                        com.eci.bcolor.Contact con = new com.eci.bcolor.Contact(name, email, sms, roomnum);
                        clist.add(con);
                    }
                }
                NodeList sList = doc.getElementsByTagName("self");
                try {
                    Node sNode = sList.item(0);
                    if(sNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element sElement = (Element) sNode;
                        String name = sElement.getElementsByTagName("name").item(0).getTextContent();
                        String email = sElement.getElementsByTagName("email").item(0).getTextContent();
                        String sms = sElement.getElementsByTagName("sms").item(0).getTextContent();
                        String roomnum = sElement.getElementsByTagName("roomnum").item(0).getTextContent();
                        self = new com.eci.bcolor.Contact(name, email, sms, roomnum);
                    }
                } catch(NullPointerException e) {
                    System.out.println("Invalid self identity in configuration file.");
                }
                NodeList mList = doc.getElementsByTagName("mild");
                try {
                    Node mNode = mList.item(0);
                    if(mNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element mElement = (Element) mNode;
                        String msg = mElement.getElementsByTagName("message").item(0).getTextContent();
                        mildMsg = msg;
                    }
                } catch(NullPointerException e) {
                    System.out.println("Invalid mild message in configuration file.");
                }
                NodeList uList = doc.getElementsByTagName("urgent");
                try {
                    Node uNode = uList.item(0);
                    if(uNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element uElement = (Element) uNode;
                        String msg = uElement.getElementsByTagName("message").item(0).getTextContent();
                        urgentMsg = msg;
                    }
                } catch(NullPointerException e) {
                    System.out.println("Invalid urgent message in configuration file.");
                }
                NodeList sdList = doc.getElementsByTagName("smsdef");
                try {
                    Node sdNode = sdList.item(0);
                    if(sdNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element sElement = (Element) sdNode;
                        String host = sElement.getElementsByTagName("host").item(0).getTextContent();
                        int port = new Integer(sElement.getElementsByTagName("port").item(0).getTextContent());
                        String user = sElement.getElementsByTagName("user").item(0).getTextContent();
                        String pass = sElement.getElementsByTagName("pass").item(0).getTextContent();
                        smsHost = host;
                        smsPort = port;
                        smsUser = user;
                        smsPassword = pass;
                    }
                } catch(NullPointerException e) {
                    System.out.println("Invalid sms host definition in configuration file.");
                }
                NodeList edList = doc.getElementsByTagName("smsdef");
                try {
                    Node edNode = edList.item(0);
                    if(edNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element sElement = (Element) edNode;
                        String host = sElement.getElementsByTagName("host").item(0).getTextContent();
                        emailHost = host;
                    }
                } catch(NullPointerException e) {
                    System.out.println("Invalid email host definition in configuration file.");
                }
                System.out.println("Configuration File Imported.");
            }
        } catch (IOException ioe) {
            return "Error -> " + ioe.getMessage();
        } catch (ParserConfigurationException pce) {
            return "Error -> " + pce.getMessage();
        } catch (DOMException de) {
            return "Error -> " + de.getMessage();            
        } catch (SAXException se) {
            return "Error -> " + se.getMessage();
        }
        return "Preferences Successfully Imported.";
    }
    
    public void exportPrefs(String path) {
        exportToDB();
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
            // root elements
            Document doc = docBuilder.newDocument();
            doc.setXmlStandalone(true);
            Element rootElement = doc.createElement("Preferences");
            doc.appendChild(rootElement);
            // contact elements
            for(com.eci.bcolor.Contact c : clist) {
                Element con = doc.createElement("contact");
                rootElement.appendChild(con);
                Element name = doc.createElement("name");
                name.appendChild(doc.createTextNode(c.getName()));
                con.appendChild(name);
                Element email = doc.createElement("email");
                email.appendChild(doc.createTextNode(c.getEmail()));
                con.appendChild(email);
                Element sms = doc.createElement("sms");
                sms.appendChild(doc.createTextNode(c.getSms()));
                con.appendChild(sms);
                Element roomnum = doc.createElement("roomnum");
                roomnum.appendChild(doc.createTextNode(c.getRoom_num()));
                con.appendChild(roomnum);
            }
            Element slf = doc.createElement("self");
            rootElement.appendChild(slf);
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(self.getName()));
            slf.appendChild(name);
            Element email = doc.createElement("email");
            email.appendChild(doc.createTextNode(self.getEmail()));
            slf.appendChild(email);
            Element sms = doc.createElement("sms");
            sms.appendChild(doc.createTextNode(self.getSms()));
            slf.appendChild(sms);
            Element roomnum = doc.createElement("roomnum");
            roomnum.appendChild(doc.createTextNode(self.getRoom_num()));
            slf.appendChild(roomnum);  
            
            Element mild = doc.createElement("mild");
            rootElement.appendChild(mild);
            Element msg = doc.createElement("message");
            msg.appendChild(doc.createTextNode(mildMsg));
            mild.appendChild(msg);
            
            Element urgent = doc.createElement("urgent");
            rootElement.appendChild(urgent);
            Element msg2 = doc.createElement("message");
            msg2.appendChild(doc.createTextNode(urgentMsg));
            urgent.appendChild(msg2);
            
            Element smsdef = doc.createElement("smsdef");
            rootElement.appendChild(smsdef);
            Element shost = doc.createElement("host");
            shost.appendChild(doc.createTextNode(smsHost));
            smsdef.appendChild(shost);
            Element sport = doc.createElement("port");
            sport.appendChild(doc.createTextNode("" + smsPort));
            smsdef.appendChild(sport);
            Element suser = doc.createElement("user");
            suser.appendChild(doc.createTextNode(smsUser));
            smsdef.appendChild(suser);
            Element spass = doc.createElement("pass");
            spass.appendChild(doc.createTextNode(""));
            smsdef.appendChild(spass); 
            
            Element emaildef = doc.createElement("emaildef");
            rootElement.appendChild(emaildef);
            Element ehost = doc.createElement("host");
            ehost.appendChild(doc.createTextNode(emailHost));
            emaildef.appendChild(ehost);
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");    
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
            System.out.println("Configuration File Saved.");
        } catch (ParserConfigurationException pce) {
            System.out.println("Error -> " + pce.getMessage());
        } catch (TransformerException te) {
            System.out.println("Error -> " + te.getMessage());
	}
    }

    public List<com.eci.bcolor.Contact> getContactList() {
        return clist;
    }

    public void setContactList(List<com.eci.bcolor.Contact> clist) {
        this.clist = clist;
    }
    
    public void addContact(com.eci.bcolor.Contact c) {
        clist.add(c);
    }
    
    public void deleteContact(int i) {
        clist.remove(i);
    }

    public com.eci.bcolor.Contact getSelf() {
        return self;
    }

    public void setSelf(com.eci.bcolor.Contact self) {
        this.self = self;
    }

    public String getMildMsg() {
        return mildMsg;
    }

    public void setMildMsg(String mildMsg) {
        this.mildMsg = mildMsg;
    }

    public String getUrgentMsg() {
        return urgentMsg;
    }

    public void setUrgentMsg(String urgentMsg) {
        this.urgentMsg = urgentMsg;
    }

    public List<com.eci.bcolor.Contact> getClist() {
        return clist;
    }

    public void setClist(List<com.eci.bcolor.Contact> clist) {
        this.clist = clist;
    }

    public String getSmsHost() {
        return smsHost == null ? "" : smsHost;
    }

    public void setSmsHost(String smsHost) {
        this.smsHost = smsHost;
    }

    public int getSmsPort() {
        return smsPort;
    }

    public void setSmsPort(int smsPort) {
        this.smsPort = smsPort;
    }

    public String getSmsUser() {
        return smsUser == null ? "" : smsUser;
    }

    public void setSmsUser(String smsUser) {
        this.smsUser = smsUser;
    }

    public String getSmsPassword() {
        return smsPassword == null ? "" : smsPassword;
    }

    public void setSmsPassword(String smsPassword) {
        this.smsPassword = smsPassword;
    }

    public String getEmailHost() {
        return emailHost == null ? "" : emailHost;
    }

    public void setEmailHost(String emailHost) {
        this.emailHost = emailHost;
    }
}
