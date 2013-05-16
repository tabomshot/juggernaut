/*
 * Software Engineering Project Juggernaut
 * Jaehwan Lee
 * Sujeong Kim
 */
package cse.se.juggernaut;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author jaehwan
 */
public class Controller {
    private DSMatrix dsm;
    private ArrayList<String> entry;
    private int[][] table;
    
    public Controller(){
        dsm = new DSMatrix();
    }
    
    public boolean newDSM(int row)
    {
        if(row<=0) return false;
        
        dsm = new DSMatrix();
        for(int i=0; i<row; i++){
            entry = new ArrayList(row);
            entry.add("Entity_"+(i+1));
            dsm.addNode("Entity_"+(i+1), new ArrayList());
        }
        
        return true;
    }
    
    public boolean openDSM(File file){
        System.out.println("Selected file path : "+ file.getAbsolutePath());
        
        int row;
        try{
            
        } catch (){
            
        }
        return false;
    }
    
    
}
