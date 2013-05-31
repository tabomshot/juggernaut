/*
 * Software Engineering Project Juggernaut
 * Jaehwan Lee
 * Sujeong Kim
 */
package cse.se.juggernaut;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jaehwan
 */
public class Parser {
    
    private static void parseFileHelper(Element parent, DefaultMutableTreeNode pnode){
        // get children
        NodeList children = parent.getChildNodes();
        
        // for every children
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child instanceof Element){
                Element node = (Element)child;
                if(node.getTagName().equals("group")){
                    // group: make treenode for group and add its children
                    DefaultMutableTreeNode tn = new DefaultMutableTreeNode(node.getAttribute("name"));
                    pnode.add(tn);
                    Parser.parseFileHelper(node, tn);
                } else if(node.getTagName().equals("item")){
                    // item: make treenode for leaf
                    DefaultMutableTreeNode tn = new DefaultMutableTreeNode(node.getAttribute("name"));
                    pnode.add(tn);
                }
            }
        }
        
    }
    
    public static DefaultMutableTreeNode parseFile( File file ) {
        
        DefaultMutableTreeNode res = new DefaultMutableTreeNode("$root");
        
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        try {
            builder = builderFactory.newDocumentBuilder();
            
            Document document = builder.parse( file );
            Element rootElement = document.getDocumentElement();
            NodeList children = rootElement.getChildNodes();
            
            for(int i=0; i<children.getLength(); i++){
                Node child = children.item(i);
                
                if(child instanceof Element){
                    Element node = (Element)child;
                    if(node.getAttribute("name").contains("ROOT") || node.getAttribute("name").contains("root")){
                        // call recursive function
                        Parser.parseFileHelper(node, res);
                    } else {
                        // error
                        System.out.println("error parse clsx");
                        return res;
                    }
                }
            }
            
        } catch (SAXException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return res;
    }
    
    private static void parseTreeHelper(FileWriter fw, DefaultMutableTreeNode node) throws IOException{
        String gs1 = "<group name=\"";
        String gs2 = "\">";
        String ge = "</group>";
        String is = "<item name=\"";
        String ie = "\" />";

        if(node.isLeaf()){       
            // item
            fw.write(is + node.toString() + ie + "\n");
        } else {
            // group start
            if(node.isRoot()){
                fw.write(gs1+"root"+gs2+"\n");
            } else {
                fw.write(gs1+node.toString()+gs2+"\n");
            }
            
            // write children
            for(int i=0; i<node.getChildCount(); i++){
                Parser.parseTreeHelper(fw, (DefaultMutableTreeNode) node.getChildAt(i));
            }
            
            // group end
            fw.append(ge+"\n");
        }
    }
    
    public static void parseTree(File file, DefaultMutableTreeNode root){
        String ss = "<cluster xmlns=\"http://rise.cs.drexel.edu/minos/clsx\">";
        String es = "</cluster>";
        
        try {
            FileWriter fw = new FileWriter(file);
            
            // write start
            fw.write(ss+'\n');
            
            // start write
            Parser.parseTreeHelper(fw, root);
            
            // write end
            fw.write(es+"\n");
            fw.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}