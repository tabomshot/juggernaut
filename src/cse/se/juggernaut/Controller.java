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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import cse.se.juggernaut.Module;

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
    
    // table model listener
    public class TableChanger implements TableModelListener{
        Controller ctr;
        private JTable table;
        private DSMatrix dsm;
        
        public TableChanger(Controller ctr, JTable table, DSMatrix dsm){
            this.ctr = ctr;
            this.table = table;
            this.dsm  = dsm;
        }
        
        @Override
        public void tableChanged(TableModelEvent tme) {
            System.out.println("Table changed ");
            ArrayList<String> ten = dsm.getTableEntry();
            for(int i=0; i<ten.size(); i++){
                ArrayList dep = new ArrayList();
                DefaultMutableTreeNode node = dsm.findNode(dsm.getRoot(), ten.get(i));
                if(node.isLeaf()){
                    for(int j=0; j<ten.size() ; j++){
                        String val = (String) this.table.getModel().getValueAt(i, j);
                        if(  val.contains("x")){
                            System.out.println("dep : "+ dsm.findNode(dsm.getRoot(), ten.get(j)));
                            dep.add( dsm.findNode(dsm.getRoot(), ten.get(j)) );
                        }
                    }
                    ((Module)node.getUserObject()).depList = dep;
                }
            }
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
        boolean flag = false;
        do{
            for(int i=0; i<this.dsm.getRoot().getChildCount(); i++){
                if( !this.dsm.getRoot().getChildAt(i).isLeaf() ){
                    flag=true;
                    break;
                } else {
                    flag=false;
                }
            }
            
            Enumeration<DefaultMutableTreeNode> e = this.dsm.getRoot().depthFirstEnumeration();
            while(e.hasMoreElements()){
                DefaultMutableTreeNode node = e.nextElement();
                if( !node.isRoot() && !node.isLeaf() ){
                    this.getModel().ungroup(node.toString());
                }
            }
        } while(flag);
    }
    
    public boolean openClustering(File file){
        try {
            // parse xml, get tree
            DefaultMutableTreeNode res = Parser.parseFile(file);
            
            // apply group entry from bottom
            Enumeration<DefaultMutableTreeNode> e = res.postorderEnumeration();
            while(e.hasMoreElements()){
                DefaultMutableTreeNode node = e.nextElement();
                if(!node.isRoot() && !node.isLeaf()){
                    // update group name
                    String gname = node.toString();
                    if(this.dsm.findNode(this.dsm.getRoot(), gname) != null){
                        int i=0;
                        while(this.dsm.findNode(this.dsm.getRoot(), gname+".g"+i) != null) i++;
                        gname +=".g"+i;
                    }
                    node.setUserObject(gname);
                    
                    DefaultMutableTreeNode args[] = new DefaultMutableTreeNode[node.getChildCount()];
                    for(int i=0; i<node.getChildCount(); i++){
                        args[i] = this.getModel().findNode(this.getModel().getRoot(), node.getChildAt(i).toString());
                    }
                    this.getModel().group(gname, args);
                }
            }
            return true;
            
        } catch (Exception ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void saveClustering(File file){
        Parser.parseTree(file, this.getModel().getRoot());
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
                
                DefaultMutableTreeNode node;
                node = (DefaultMutableTreeNode) tree.getPathForRow(i).getLastPathComponent();
                Module module = (Module) node.getUserObject();
                //System.out.println("[DEBUG] M:"+tree.getPathForRow(i).toString() + "::expand["+module.expand+"]");
                
                // apply group expand property
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
        
        list = dsm.getTableEntry();

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
            Module module = (Module) (dsm.findNode(dsm.getRoot(), list.get(i)).getUserObject());
            for(int j=0; j<module.depList.size(); j++){
                String dep = module.depList.get(j);
                DefaultMutableTreeNode dnode = dsm.findNode(dsm.getRoot(), dep);
                TreeNode[] path = dnode.getPath();
                Object[] opath = dnode.getUserObjectPath();

                for(int k=0; k<path.length; k++){
                    if( !path[k].isLeaf() && !((Module)opath[k]).expand ){
                        int col = list.indexOf( path[k].toString() );
                        if(i==col){
                            obj[i][col] = ".";
                        } else{
                            obj[i][col] = "x";
                        }
                        break;
                    } else if (path[k].isLeaf()) {
                        int col = list.indexOf(path[k].toString());
                        if(i==col){
                            obj[i][col] = ".";
                        } else{
                            obj[i][col] = "x";
                        }
                        break;
                    }
                }
            }
        }

        return obj;
    }
    
    public JTable getColoredTable(){
        
        // make table
        Object[][] obj = this.getTable();
        ArrayList<String> en = this.dsm.getTableEntry();
        
        String[] colheader = new String[en.size()];
        for(int i=0; i<colheader.length; i++){
            colheader[i] = (new Integer(i)).toString();
        }
        
        JTable table = new JTable(obj, colheader);

        // generate group list
        ArrayList<String> gl = new ArrayList();
        Enumeration<DefaultMutableTreeNode> eg = this.getModel().getRoot().breadthFirstEnumeration();
        while(eg.hasMoreElements()){
            DefaultMutableTreeNode node = eg.nextElement();
            if( !node.isRoot() && !node.isLeaf() ){
                gl.add(node.toString());
            }  
        }
        
        // start set color from root
        Color[] cList = { new Color(128,255,128), Color.YELLOW, new Color(128,255,255), Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK, Color.RED };
        
        Color[][] objColor = new Color[obj.length][obj.length];
        for(int i=0; i<obj.length; i++){
            for(int j=0; j<obj.length; j++){
                objColor[i][j] = Color.WHITE;
            }
        }
        
        // set color every group
        for(int i=0; i<gl.size(); i++){
            DefaultMutableTreeNode group = this.getModel().findNode(this.getModel().getRoot(), gl.get(i));
            Enumeration<DefaultMutableTreeNode> children = group.breadthFirstEnumeration();
            int low=100000000, high=0; // this looks sick
            while(children.hasMoreElements()){
                DefaultMutableTreeNode child = children.nextElement();
                int index = en.indexOf(child.toString());
                if(index > -1){
                    if(index<low) low = index;
                    if(index>high) high = index;
                }
            }
            //System.out.println("low:"+low+"::"+"high"+high);
            for(int j=low; j<=high; j++){
                for(int k=low; k<=high; k++){
                    //if(objColor[j][k].equals(Color.WHITE)){
                        objColor[j][k] = cList[ (group.getPath().length - 2) % cList.length];
                    //}
                }
            }
        }
        
        // set cell renderer
        table.setDefaultRenderer(Object.class, new CellColorRenderer(objColor) );
        
        // set cell edit listener
        table.getModel().addTableModelListener(new TableChanger(this, table, this.dsm));
        
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
        Enumeration<DefaultMutableTreeNode> eg = this.getModel().getRoot().preorderEnumeration();
        while(eg.hasMoreElements()){
            DefaultMutableTreeNode node = eg.nextElement();
            if( !node.isLeaf() ){
                // bubble sort children for each group
                for(int i=node.getChildCount()-1; i>=0; i--){
                    for(int j=0; j<i; j++){
                        // (b compare to a) || (length)
                        //if( node.getChildAt(j).toString().length() > node.getChildAt(j+1).toString().length()){
                            //this.getModel().moveNodeDown(node.getChildAt(j).toString());
                        //} else if (node.getChildAt(j).toString().length() == node.getChildAt(j+1).toString().length()){
                        if( node.getChildAt(j).toString().compareToIgnoreCase(node.getChildAt(j+1).toString()) > 0 ) {
                            this.getModel().moveNodeDown(node.getChildAt(j).toString());
                        }
                    }
                }
            }  
        }
        
    }
    
    
    private boolean partitionEntryHelper(ArrayList<String> history, ArrayList<String> circuit, DefaultMutableTreeNode node ){
        
        if( history.contains(node.toString())){
            System.out.println("[DEBUG]found circuit for: " + node.toString());
            for(int i=0; i<history.size(); i++){
                System.out.println("[DEBUG]history: " + history.get(i));
            }
            // if contains, make a circuit and return true
            int start = history.indexOf(node.toString());
            int end = history.size();
            
            for(int i=start; i<end; i++){
                circuit.add(history.get(i));
            }
            return true;
            
        } else {
            // if not contains, add to history and continue
            history.add(node.toString());
        }
        
        // get dependency list and search
        ArrayList<String> dep = ((Module) node.getUserObject()).depList;
        
        for(int i=0; i<dep.size(); i++){
            
            DefaultMutableTreeNode dnode = this.dsm.findNode(this.dsm.getRoot(), dep.get(i));
            TreeNode[] dpath = dnode.getPath();
            DefaultMutableTreeNode res = null;
            
            // get actual entry node of dnode (leaf or group entry)
            for(int j=0; j<dpath.length; j++){
                res = (DefaultMutableTreeNode) dpath[j];
                if( !res.isLeaf()  && !((Module)res.getUserObject()).expand ){
                    break;
                } else if (res.isLeaf()) {
                    break;
                }
            }
            
            // exclude dnode with self dependency group
            if( !res.isLeaf() && !res.toString().equals(node.toString())){
                // do search down for group node
                if( this.partitionEntryHelper((ArrayList<String>) history.clone(), circuit, res) ){
                    return true;
                }
            } else if( res.isLeaf()) {
                // do search down for leaf node
                if( this.partitionEntryHelper((ArrayList<String>) history.clone(), circuit, res) ){
                    return true;
                }
            }
        }
        return false;
    }
    
    public void partitionEntry(){
        
        for(int g=0; true; g++){
            // get list
            ArrayList<String> list = this.dsm.getTableEntry();
            ArrayList<String> step1list = (ArrayList<String>) list.clone();
            ArrayList<String> step2list = (ArrayList<String>) list.clone();
                    
            // DSM Partitioning [Step 1]
            for(int i=0; i<step1list.size(); i++){
                DefaultMutableTreeNode node = this.dsm.findNode(this.dsm.getRoot(), step1list.get(i));
                Module module = (Module) node.getUserObject();
                
                // with no input -> no deplist
                if(module.depList.isEmpty()){
                    // move node to the top
                    for(int j=0; j<step1list.size(); j++){
                        this.dsm.moveNodeUp(node.toString());
                    }
                    list.remove(step1list.get(i));
                }
            }
            
            // DSM Partitioning [Step 2]
            // fin i(col) that gives output to j(row)
            for(int i=0; i<step2list.size(); i++){
                boolean isout = false;
                
                // check deplist of each j
                for(int j=0; j<step2list.size(); j++){
                    Module module = (Module) this.dsm.findNode(this.dsm.getRoot(), step2list.get(j)).getUserObject();
                    
                    // check deplist that dependent to i
                    for(int k=0; k<module.depList.size(); k++){
                        DefaultMutableTreeNode dnode;
                        TreeNode[] dpath;
                        
                        DefaultMutableTreeNode res = null;
                        dnode = this.dsm.findNode(this.dsm.getRoot(), module.depList.get(k) );
                        dpath = dnode.getPath();
                        
                        for(int p=0; p<dpath.length; p++){
                            res = (DefaultMutableTreeNode) dpath[p];
                            if( !res.isLeaf()  && !((Module)res.getUserObject()).expand ){
                                break;
                            } else if (res.isLeaf()) {
                                break;
                            }
                        }
                        
                        if(step2list.get(i).equals(res.toString())){
                            isout = true;
                            break;
                        }
                    }
                }
                
                // node with no output moves to the top
                if( !isout ){
                    //System.out.println("[DEBUG] go bottom : " + step2list.get(i));
                    for(int j=0; j<step2list.size(); j++){
                        this.dsm.moveNodeDown(step2list.get(i).toString());
                    }
                    list.remove(step2list.get(i));
                }
            }
            
            // partitioning is over if the list is empty [Step3]
            //if(list.isEmpty()) break;
            if(g>10) break;

            // DSM Partitioning [Step4]
            for(int i=0; i<list.size(); i++){
                // search a circuit
                DefaultMutableTreeNode node = this.dsm.findNode(this.dsm.getRoot(), list.get(i));
                ArrayList<String> circuit = new ArrayList();
                if(this.partitionEntryHelper(new ArrayList(), circuit, node)){
                    // found a circuit, make a group 
                    DefaultMutableTreeNode args[] = new DefaultMutableTreeNode[circuit.size()];
                    for(int j=0; j<args.length; j++){
                        args[j] = this.dsm.findNode(this.dsm.getRoot(), circuit.get(j));
                    }
                    this.dsm.group("partitioning.group."+g, args);
                    
                    // remove from the list
                    for(int j=0; j<args.length; j++){
                        list.remove(circuit.get(j));
                    }
                    
                    // start over
                    break;
                }
            }
        }
        
    }
    
    public JList setCellRenderer(JList rowHeader, JTable table, boolean showRowLabels){
        rowHeader.setCellRenderer(new RowHeaderRenderer(table, showRowLabels));
        return rowHeader;
    }
}

    
