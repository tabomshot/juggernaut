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
        DSMatrix.Module nroot = new DSMatrix.Module();
        nroot.name = "$root";
        nroot.depList = new ArrayList<String>();
        nroot.expand = false;
        
        this.root = new DefaultMutableTreeNode(nroot);
    }
   
    public void addNode(String name, ArrayList<String> depList)
    {
        DSMatrix.Module toadd = new DSMatrix.Module();
        toadd.name = name;
        toadd.depList = depList;
        
        root.add(new DefaultMutableTreeNode(toadd));
    }
    
    public boolean renameNode(String oldName, String newName)
    {
        // rename module
        DefaultMutableTreeNode node = this.findNode(root, oldName);
        if(node!=null){
            DSMatrix.Module module = (DSMatrix.Module)node.getUserObject();
            module.name = newName;
            return true;
        }
        
        // rename module from dependency list
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            node = e.nextElement();
            DSMatrix.Module module = (DSMatrix.Module)node.getUserObject();
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
            ((DSMatrix.Module)node.getUserObject()).depList.remove(name);
        }
    }
    
    public boolean moveNodeUp(String name)
    {
        // get node to move
        DefaultMutableTreeNode toMove = findNode(this.root, name);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) toMove.getParent();
        
        // get siblings
        ArrayList<DefaultMutableTreeNode> children = new ArrayList<DefaultMutableTreeNode>();
        
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
    
    public void group(String gname, DefaultMutableTreeNode[] args)
    {
        if(args!=null && args.length>0 ){
           
            // set group module
            DSMatrix.Module nm = new DSMatrix.Module();
            nm.name = gname;
            nm.expand = false;
            nm.depList = new ArrayList<String>();

            // set group dependencies
            for(int i=0; i<args.length; i++){
                ArrayList<String> dl = ((DSMatrix.Module)args[i].getUserObject()).depList;
                for(int j=0; j<dl.size(); j++){
                    if( nm.depList.size()>0 ){
                        if( !nm.depList.contains(dl.get(j)) ){
                            nm.depList.add(dl.get(j));
                        }
                    } else {
                        nm.depList.add(dl.get(j));
                    }
                }
            }
            
            DefaultMutableTreeNode newGroup = new DefaultMutableTreeNode(nm);
            ((DefaultMutableTreeNode)args[0].getParent()).add(newGroup);

            // put nodes to group
            moveNodeTo(newGroup, args);
        }
    }
    
    public void ungroup(String gname)
    {
        DefaultMutableTreeNode groupToDel = findNode(this.root, gname);
        if(groupToDel!=null){
            
            // get parent
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)groupToDel.getParent();
            
            // make children list
            DefaultMutableTreeNode children[];
            children = new DefaultMutableTreeNode[groupToDel.getChildCount()];
            
            DefaultMutableTreeNode tmp = (DefaultMutableTreeNode)groupToDel.getFirstChild();
            children[0] = tmp;
            for(int i=1; i<groupToDel.getChildCount(); i++){
                children[i] = tmp.getNextSibling();
                tmp = tmp.getNextSibling();
            }
            
            // put children to parent
            moveNodeTo(parent, children);
            
            // set group dependencies
            DSMatrix.Module pm = (DSMatrix.Module)parent.getUserObject();
            for(int i=0; i<children.length; i++){
                ArrayList<String> dl = ((DSMatrix.Module)children[i].getUserObject()).depList;
                for(int j=0; j<dl.size(); j++){
                    if( pm.depList.size()>0 ){
                        if( !pm.depList.contains(dl.get(j)) ){
                            pm.depList.add(dl.get(j));
                        }
                    } else {
                        pm.depList.add(dl.get(j));
                    }
                }
            }
            
            // remove group node
            deleteNode(gname);
        }
    }
    
    public void expandGroup(String name)
    {
        DefaultMutableTreeNode toexp = findNode(this.root, name);
        ((DSMatrix.Module)toexp.getUserObject()).expand = true;
    }
    
    public void collapseGroup(String name)
    {
        DefaultMutableTreeNode toexp = findNode(this.root, name);
        ((DSMatrix.Module)toexp.getUserObject()).expand = false;
    }
}