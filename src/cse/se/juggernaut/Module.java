/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cse.se.juggernaut;

import java.util.ArrayList;

/**
 *
 * @author jaehwan
 */
public class Module {
        public String name;
        public ArrayList<String> depList;
        public boolean expand;
        
        @Override
        public String toString(){return name;}
    }