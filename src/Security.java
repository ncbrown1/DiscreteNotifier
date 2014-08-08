package com.eci.bcolor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;

/**
 *
 * @author ncbrown
 */
public class Security {
    
    public static String dehash(String in) {
        HashMap<Character, Character> d = generateDict();
        char[] dehashed = new char[in.length()];
        for(int i=0; i<in.length(); i++) {
            dehashed[i] = d.get(in.charAt(i));
        }
        String str = new String(dehashed);
        return str;
    }
    
    private static HashMap<Character, Character> generateDict() {
        HashMap<Character, Character> ht = new HashMap<Character, Character>();
        ht.put('a', 'b'); ht.put('b', 'L'); ht.put('c', 'U'); ht.put('d', 'B'); ht.put('e', '1'); ht.put('f', '8');
        ht.put('g', 'A'); ht.put('h', 'c'); ht.put('i', 'u'); ht.put('j', 'p'); ht.put('k', 'Z'); ht.put('l', 'q');
        ht.put('m', 'e'); ht.put('n', 'J'); ht.put('o', 'O'); ht.put('p', 't'); ht.put('q', '4'); ht.put('r', 'T');
        ht.put('s', '2'); ht.put('t', 'h'); ht.put('u', 'K'); ht.put('v', 'a'); ht.put('w', 'd'); ht.put('x', 'l');
        ht.put('y', 'Q'); ht.put('z', '7'); ht.put('A', 'M'); ht.put('B', 'f'); ht.put('C', 'j'); ht.put('D', 'C');
        ht.put('E', 'i'); ht.put('F', 'm'); ht.put('G', 'X'); ht.put('H', 'F'); ht.put('I', '3'); ht.put('J', 'z');
        ht.put('K', 'P'); ht.put('L', 'o'); ht.put('M', 'E'); ht.put('N', 'g'); ht.put('O', 'R'); ht.put('P', 'x');
        ht.put('Q', 'N'); ht.put('R', '0'); ht.put('S', 'k'); ht.put('T', '5'); ht.put('U', 'S'); ht.put('V', 'D');
        ht.put('W', 'n'); ht.put('X', '6'); ht.put('Y', 'y'); ht.put('Z', 's'); ht.put('0', 'G'); ht.put('1', 'I');
        ht.put('2', 'r'); ht.put('3', '9'); ht.put('4', 'v'); ht.put('5', 'W'); ht.put('6', 'Y'); ht.put('7', 'H');
        ht.put('8', 'w'); ht.put('9', 'V');
        return ht;
    }
}
