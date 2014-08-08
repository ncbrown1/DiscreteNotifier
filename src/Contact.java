package com.eci.bcolor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ncbrown
 */
public class Contact {
    private String email;
    private String sms;
    private String name;
    private String room_num;
    
    public Contact(String n, String e, String s, String rn) {
        this.name = n;
        this.email = e;
        this.sms = s;
        this.room_num = rn;
    }
    
    public Contact() {
        this("","","","");
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom_num() {
        return room_num;
    }

    public void setRoom_num(String room_num) {
        this.room_num = room_num;
    }
    
    public String stringForm() {
        return name + ";" + email + ";" + sms + ";" + room_num;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
