 /*
 * Software Engineering Project Juggernaut
 * Jaehwan Lee
 * Sujeong Kim
 */
package cse.se.juggernaut;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.UIManager;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author jaehwan
 */
public class Controller {
    
    class RowHeaderRenderer extends JLabel implements ListCellRenderer {
        RowHeaderRenderer(JTable table){
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(LEFT);
            setForeground(header.getForeground());
            setBackground(Color.WHITE);
            setFont(header.getFont());
        }

        @Override
        public Component getListCellRendererComponent(JList jlist, Object e, int i, boolean bln, boolean bln1) {
            setText((e == null)?"":e.toString());
            return this;
        }
    }
    
    private DSMatrix dsm;
    private ArrayList<String> entry;
    private int[][] table;
    
    public Controller(){
        dsm = new DSMatrix();
    }
    
    public DSMatrix getModel(){return dsm;}
    
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
        System.out.println("[DEBUG] Selected file path : "+ file.getAbsolutePath());
        
        int row;
        
        // read file
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            
            row = Integer.parseInt(br.readLine());       
            
            table = new int[row][row];     
            for(int i=0; i<row; i++){
                String line = br.readLine();
                // debug line
                /*
                System.out.print("[DEBUG] read line : ");
                for(int j=0; j<row; j++){
                    System.out.print( line.charAt(j*2));
                }
                System.out.println("");
                */
                for(int j=0; j<row; j++){
                    table[i][j] = (line.charAt(j*2) == '1')? 1 : 0;
                }
            }
            
            entry = new ArrayList();
            for(int i=0; i<row; i++){
                entry.add(br.readLine());
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        // create dsm from data
        // dep list
        dsm = new DSMatrix();
        for(int i=0; i<row; i++){
            ArrayList<String> depl = new ArrayList();
            for(int j=0; j<row; j++){
                if(table[i][j] == 1){
                    depl.add(entry.get(j));
                }
            }
            dsm.addNode(entry.get(i), depl);
        }

        return true;
    }
    
    public String[][] getTable() {
        
        // get valid entries
        ArrayList<String> list = dsm.getEntries();
         
        for(int i=0; i<list.size(); i++){
            System.out.println("[DEBUG] entry ["+list.get(i)+"] is dependent to :");
            DefaultMutableTreeNode nn = dsm.findNode(dsm.getRoot(), list.get(i));
            ArrayList<String> dd = ((Module)nn.getUserObject()).depList;
            for(int j=0; j<dd.size(); j++){
                System.out.println("        " + dd.get(j));
            }
            System.out.println("");
        }
        
        list = dsm.getEntries();
        for(int i=0; i<list.size(); i++){
            System.out.println("Entry : " + list.get(i));
        }
        
        // column header [1, 2, 3, ...]
        String[] colheader = new String[list.size()];
        for(int i=0; i<list.size(); i++){
            colheader[i] = (new Integer(i)).toString();
        }
        
        // initialize table cells
        String[][] obj = new String[list.size()][list.size()];
        for(int i=0; i<list.size(); i++){
            for(int j=0; j<list.size(); j++){
                if(i==j) obj[i][j] = ".";
                else obj[i][j] = " ";
            }
        }
        
        // set up table cells from dependency lists
        for(int i=0; i<list.size(); i++){
            Module module = (Module) dsm.findNode(dsm.getRoot(), list.get(i)).getUserObject();
            for(int j=0; j<module.depList.size(); j++){
                String dep = module.depList.get(j);
                DefaultMutableTreeNode dnode = dsm.findNode(dsm.getRoot(), dep);
                TreeNode[] path = dnode.getPath();
                Object[] opath = dnode.getUserObjectPath();

                for(int k=0; k<path.length; k++){
                    if( !path[k].isLeaf() && !((Module)opath[k]).expand ){
                        int col = list.indexOf( path[k].toString() );
                        obj[i][col] = "x";
                        break;
                    } else if (path[k].isLeaf()) {
                        int col = list.indexOf(path[k].toString());
                        obj[i][col] = "x";
                        break;
                    }
                }
            }
        }
 
        /*
        System.out.println("====== [DEBUG] =====");
        for(int i=0; i<obj.length; i++){
            for(int j=0; j<obj.length; j++){
                System.out.print(obj[i][j] + " ");
            }
            System.out.println(" ");
        }
        */
        
        return obj;
    }
    
    public JList setCellRenderer(JList rowHeader, JTable table){
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));
        return rowHeader;
    }
}

    
