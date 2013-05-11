/*
 * Software Engineering Project Juggernaut
 * Jaehwan Lee
 * Sujeong Kim
 */
package cse.se.juggernaut;

import java.util.ArrayList;
import java.util.Enumeration; 
import java.util.ListIterator;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author jaehwan
 */
public class DSMatrix {
    
    private DefaultMutableTreeNode root;
    
    class Module {
        private String name;
        private ArrayList<String> depList;
        private boolean expand;
        
        @Override
        public String toString(){return name;}
    }
    
    public DSMatrix(){
        Module nroot = new Module();
        nroot.name = "$root";
        nroot.depList = null;
        nroot.expand = false;
        
        this.root = new DefaultMutableTreeNode(nroot);
    }
   
    public void addNode(String name, ArrayList<String> depList)
    {
        Module toadd = new Module();
        toadd.name = name;
        toadd.depList = depList;
        
        root.add(new DefaultMutableTreeNode(toadd));
    }
    
    public boolean renameNode(String oldName, String newName)
    {
        // rename module
        DefaultMutableTreeNode node = this.findNode(root, oldName);
        if(node!=null){
            Module module = (Module)node.getUserObject();
            module.name = newName;
            return true;
        }
        
        // rename module in dependency list
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            node = e.nextElement();
            Module module = (Module)node.getUserObject();
            module.depList.remove(oldName);
            module.depList.add(newName);
        }
        
        return false;
    }
    
    public void deleteNode(String name)
    {
        // remove module from tree
        DefaultMutableTreeNode todel = findNode(this.root, name);
        todel.removeFromParent();
        
        // remove module dependencies
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            ((Module)node.getUserObject()).depList.remove(name);
        }
    }
    
    public boolean moveNodeUp(String name)
    {
        // get node to move
        DefaultMutableTreeNode toMove = findNode(this.root, name);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) toMove.getParent();
        
        // get siblings
        ArrayList<DefaultMutableTreeNode> children = new ArrayList<>();
        
        DefaultMutableTreeNode tmp = (DefaultMutableTreeNode)parent.getFirstChild();
        children.add(tmp);
        while(tmp.getNextSibling() != null){
            children.add(tmp.getNextSibling());
            tmp = tmp.getNextSibling();
        }
        
        if(children.indexOf(toMove) > 0){
            // remove from parent
            ListIterator litr = children.listIterator();
            while(litr.hasNext()){
                tmp = (DefaultMutableTreeNode)litr.next();
                tmp.removeFromParent();
            }

            // move node up
            int index = children.indexOf(toMove);
            tmp = children.get(index);
            children.remove(tmp);
            children.add(index-1, tmp);
                    
            // add to parent again
            litr = children.listIterator();
            while(litr.hasNext()){
                tmp = (DefaultMutableTreeNode)litr.next();
                parent.add(tmp);
            }
            
            return true;
        } else {
            return false;
        }
    }
    
    public boolean moveNodeDown(String name)
    {
        // get node to move
        DefaultMutableTreeNode toMove = findNode(this.root, name);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) toMove.getParent();
        
        // get siblings
        ArrayList<DefaultMutableTreeNode> children = new ArrayList();
        DefaultMutableTreeNode tmp = (DefaultMutableTreeNode)parent.getFirstChild();
        children.add(tmp);
        while(tmp.getNextSibling() != null){
            children.add(tmp.getNextSibling());
            tmp = tmp.getNextSibling();
        }
        
        if(children.indexOf(toMove) < parent.getChildCount()-1){
            // remove from parent
            ListIterator litr = children.listIterator();
            while(litr.hasNext()){
                tmp = (DefaultMutableTreeNode)litr.next();
                tmp.removeFromParent();
            }

            // move node down
            int index = children.indexOf(toMove);
            tmp = children.get(index);
            children.remove(tmp);
            children.add(index+1, tmp);
                    
            // add to parent again
            litr = children.listIterator();
            while(litr.hasNext()){
                tmp = (DefaultMutableTreeNode)litr.next();
                parent.add(tmp);
            }
            
            return true;
        } else {
            return false;
        }
    }
    
    public DefaultMutableTreeNode findNode (DefaultMutableTreeNode root, String s) {
        
        // get node list
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().equalsIgnoreCase(s)) {
                return node;
            }
        }
        return null;
    }
    
    public void moveNodeTo(DefaultMutableTreeNode toHere, DefaultMutableTreeNode[] args){
        if(toHere!=null && args!=null && args.length>0){
            for(int i=0;i<args.length;i++){
                args[i].removeFromParent();
                toHere.add(args[i]);
            }
        }
    }
    
    public DefaultMutableTreeNode getRoot(){ return root; }
    
    public void group(String gname)
    {
        
    }
    
    public void ungroup(String gname)
    {
        
    }
    
    public void expandGroup(String name)
    {
        DefaultMutableTreeNode toexp = findNode(this.root, name);
        ((Module)toexp.getUserObject()).expand = true;
    }
    
    public void collapseGroup(String name)
    {
        DefaultMutableTreeNode toexp = findNode(this.root, name);
        ((Module)toexp.getUserObject()).expand = false;
    }
}