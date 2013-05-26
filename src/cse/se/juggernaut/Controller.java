 /*
 * Software Engineering Project Juggernaut
 * Jaehwan Lee
 * Sujeong Kim
 */
package cse.se.juggernaut;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.UIManager;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author jaehwan
 */
public class Controller {
    
    // row header renderer class
    class RowHeaderRenderer extends JLabel implements ListCellRenderer {
        private boolean showRowLabels;
        
        RowHeaderRenderer(JTable table, boolean showRowLabels){
            this.showRowLabels = showRowLabels;
            
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
            if(showRowLabels){
                setText((e == null)?"":(new Integer(i)).toString() + " " + e.toString());
            } else {
                setText((e == null)?"":(new Integer(i)).toString() );
            }
            
            return this;
        }
    }
    
    // table cell color renderer
    public class CellColorRenderer extends DefaultTableCellRenderer {
        private Color[][] cTable;
        
        public CellColorRenderer(Color[][] cTable){
            this.cTable = cTable;
            
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
            Component comp = super.getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, column);            
            
            // render cell with use comp.setBackground(Color.XXX)
            comp.setBackground(cTable[row][column]);
            comp.setForeground(Color.BLACK);
            
            return comp;
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
    
    public void saveDSM(File file)
    {
        System.out.println("file : "+ file.toString());
        ArrayList<String> el = new ArrayList();
        Enumeration<DefaultMutableTreeNode> entry = this.getModel().getRoot().preorderEnumeration();
        while(entry.hasMoreElements()){
            DefaultMutableTreeNode node = entry.nextElement();
            if(node.isLeaf()){
                el.add(node.toString());
            }
        }
        
        FileWriter fw;
        try {
            fw = new FileWriter(file);
            fw.write(el.size()+"\n");
            
            // for every entry module, write dependency table
            for(int i=0; i<el.size(); i++){
                
                // create dependency table
                int deplist[] = new int[el.size()];
                for(int j=0; j<el.size(); j++){
                    deplist[j] = 0;
                }
                DefaultMutableTreeNode node = this.getModel().findNode(this.getModel().getRoot(), el.get(i));
                Module module = (Module) node.getUserObject();
                for(int j=0; j<module.depList.size(); j++){
                    deplist[ el.indexOf(module.depList.get(j)) ] = 1;
                }
                
                // write line
                for(int j=0; j<el.size(); j++){
                    fw.write(deplist[j] + " ");
                }
                fw.write("\n");
            }
            
            // write entry list
            for(int i=0; i<el.size(); i++){
                fw.write(el.get(i) + "\n");
            }
            
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void newClustering(){
        
        // reset cluster information
        Enumeration<DefaultMutableTreeNode> e = this.getModel().getRoot().preorderEnumeration();
        while(e.hasMoreElements()){
            DefaultMutableTreeNode node = e.nextElement();
            if( !node.isRoot() && !node.isLeaf() ){
                this.getModel().ungroup(node.toString());
            }
        }
    }
    
    public boolean openClustering(File file){
        
        return false;
    }
    
    public void saveClustering(File file){
        
    }
    
    public JTree getTreeViewUpdate(){
            
        JTree tree = new JTree(this.getModel().getRoot());
        
        // tree expansion listener
        tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent tee) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tee.getPath().getLastPathComponent();
                Module module = (Module) node.getUserObject();
                module.expand = true;
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent tee) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tee.getPath().getLastPathComponent();
                Module module = (Module) node.getUserObject();
                module.expand = false;
            }
        });
        
        DefaultMutableTreeNode root = this.getModel().getRoot();
        
        if(((Module)root.getUserObject()).expand == false){
            tree.collapseRow(0);
        } else {
            for(int i=1; i < tree.getRowCount(); i++){
                //TODO: apply group expand property
                DefaultMutableTreeNode node;
                node = (DefaultMutableTreeNode) tree.getPathForRow(i).getLastPathComponent();
                Module module = (Module) node.getUserObject();
                //System.out.println("[DEBUG] M:"+tree.getPathForRow(i).toString() + "::expand["+module.expand+"]");
                if(module.expand == true){
                    tree.expandRow(i);
                } else {
                    tree.collapseRow(i);
                }
            }
        }
        
        tree.setVisible(true);  
        return tree;
    }
    
    public String[][] getTable() {
        
        // get valid entries
        ArrayList<String> list = dsm.getTableEntry();
         
        for(int i=0; i<list.size(); i++){
            System.out.println("[DEBUG] entry ["+list.get(i)+"] is dependent to :");
            DefaultMutableTreeNode nn = dsm.findNode(dsm.getRoot(), list.get(i));
            ArrayList<String> dd = ((Module)nn.getUserObject()).depList;
            for(int j=0; j<dd.size(); j++){
                System.out.println("        " + dd.get(j));
            }
            System.out.println("");
        }
        
        list = dsm.getTableEntry();
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
    
    public JTable getColoredTable(){
        
        // make table
        Object[][] obj = this.getTable();
        ArrayList<String> en = this.getModel().getTableEntry();
        
        String[] colheader = new String[en.size()];
        for(int i=0; i<colheader.length; i++){
            colheader[i] = (new Integer(i)).toString();
        }
        
        JTable table = new JTable(obj, colheader);
        
        // Coloring here!
        // generate group list
        ArrayList<String> gl = new ArrayList();
        Enumeration<DefaultMutableTreeNode> eg = this.getModel().getRoot().depthFirstEnumeration();
        while(eg.hasMoreElements()){
            DefaultMutableTreeNode node = eg.nextElement();
            if(!node.isRoot() && !node.isLeaf()){
                gl.add(node.toString());
            }  
        }
        
        // start set color from root
        Color[] cList = { Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW, Color.BLUE};
        
        Color[][] objColor = new Color[obj.length][obj.length];
        for(int i=0; i<obj.length; i++){
            for(int j=0; j<obj.length; j++){
                objColor[i][j] = Color.WHITE;
            }
        }
        
        // set color every group
        for(int i=0; i<gl.size(); i++){
            DefaultMutableTreeNode group = this.getModel().findNode(this.getModel().getRoot(), gl.get(i));
            Enumeration<DefaultMutableTreeNode> children = group.depthFirstEnumeration();
            int low=100000000, high=0; // this looks seek
            while(children.hasMoreElements()){
                DefaultMutableTreeNode child = children.nextElement();
                int index = en.indexOf(child.toString());
                if(index > -1){
                    if(index<low) low = index;
                    if(index>high) high = index;
                }
            }
            System.out.println("low:"+low+"::"+"high"+high);
            for(int j=low; j<=high; j++){
                for(int k=low; k<=high; k++){
                    if(objColor[j][k].equals(Color.WHITE)){
                        objColor[j][k] = cList[group.getPath().length % cList.length];
                    }
                }
            }
        }
        
        // set cell renderer
        table.setDefaultRenderer(Object.class, new CellColorRenderer(objColor) );
        
        return table;
    }
    
    public void setExpandAll(){
        // expand root, expand groups
        
        ((Module)this.getModel().getRoot().getUserObject()).expand = true;
        
        Enumeration<DefaultMutableTreeNode> e = this.getModel().getRoot().depthFirstEnumeration();
        while(e.hasMoreElements()){
            DefaultMutableTreeNode node = e.nextElement();
            if( !node.isLeaf() ){
                ((Module)node.getUserObject()).expand = true;
            }
        }
    }
    
    public void setCollapseAll(){
        // collapse rot, collapse groups
        ((Module)this.getModel().getRoot().getUserObject()).expand = false;
        
        Enumeration<DefaultMutableTreeNode> e = this.getModel().getRoot().depthFirstEnumeration();
        while(e.hasMoreElements()){
            DefaultMutableTreeNode node = e.nextElement();
            if( !node.isLeaf() ){
                ((Module)node.getUserObject()).expand = false;
            }
        }
    }
    
    public void sortEntry(){
        
        // get group list with root group
        Enumeration<DefaultMutableTreeNode> eg = this.getModel().getRoot().depthFirstEnumeration();
        while(eg.hasMoreElements()){
            DefaultMutableTreeNode node = eg.nextElement();
            if( !node.isLeaf() ){
                // bubble sort children for each group
                for(int i=node.getChildCount()-1; i>=0; i--){
                    for(int j=0; j<i; j++){
                        // (b compare to a) || (length)
                        if( node.getChildAt(j).toString().length() > node.getChildAt(j+1).toString().length()){
                            this.getModel().moveNodeDown(node.getChildAt(j).toString());
                        } else if (node.getChildAt(j).toString().length() == node.getChildAt(j+1).toString().length()){
                            if( node.getChildAt(j).toString().compareTo(node.getChildAt(j+1).toString()) > 0 ) {
                                this.getModel().moveNodeDown(node.getChildAt(j).toString());
                            }
                        }
                    }
                }
            }  
        }
    }
    
    public JList setCellRenderer(JList rowHeader, JTable table, boolean showRowLabels){
        rowHeader.setCellRenderer(new RowHeaderRenderer(table, showRowLabels));
        return rowHeader;
    }
}

    
