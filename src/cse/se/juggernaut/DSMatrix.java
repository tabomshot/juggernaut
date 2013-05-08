/*
 * Software Engineering Project Juggernaut
 * Jaehwan Lee
 * Sujeong Kim
 */
package cse.se.juggernaut;

import java.util.ArrayList;
import java.util.ListIterator;


public class DSMatrix {
    private int size;
    private ArrayList<ArrayList<Integer>> matrix;
    private ArrayList<String> entry;
    
    public void init(int size)
    {
        entry = new ArrayList<>(size);
        for(int i=0; i<size; i++){
            entry.add("Entry_"+i);
        }
        
        matrix = new ArrayList<>();
        for(int i=0; i<size; i++){
            ArrayList<Integer> toadd = new ArrayList<>();
            for(int j=0; j<size; j++){
                toadd.add(0);
            }
            matrix.add(toadd);
        }
        
        this.size = size;
    }
    
    public void addRow(String mname)
    {
        entry.add(mname);
        
        ListIterator<ArrayList<Integer>> litr = matrix.listIterator();
        while(litr.hasNext()){
            litr.next().add(0);
        }
        
        ArrayList<Integer> toadd = new ArrayList<>();
        for(int i=0; i<size+1; i++){
            toadd.add(0);
        }
        matrix.add(toadd);
        this.size++;
    }
        
    public void deleteRow(int index)
    {
        entry.remove(index);
        
        ListIterator<ArrayList<Integer>> litr = matrix.listIterator();
        while(litr.hasNext()){
            ArrayList<Integer> rmcol = litr.next();
            rmcol.remove(index);
        }
        matrix.remove(index);
        size--;
    }
    
    public void renameRow(int index, String newName)
    {
        entry.set(index, newName);
    }
    
    // index : node to go up
    public void upRow(int index)
    {
        ArrayList<Integer> temp = matrix.get(index);
        matrix.set(index, matrix.get(index-1));
        matrix.set(index-1, temp);
        
        String stemp = entry.get(index);
        entry.set(index, entry.get(index-1));
        entry.set(index-1, stemp);
    }
    
    public void upRow(int index, int count)
    {
        // to do : implement 
    }
    
    public void downRow(int index)
    {
        ArrayList<Integer> temp = matrix.get(index);
        matrix.set(index, matrix.get(index+1));
        matrix.set(index+1, temp);
        
        String stemp = entry.get(index);
        entry.set(index, entry.get(index+1));
        entry.set(index+1, stemp);
    }
    
    public void downRow(int index, int count)
    {
        // to do : implement 
    }
    
    public int getIndex(String mname)
    {
        return entry.indexOf(mname);
    }
}