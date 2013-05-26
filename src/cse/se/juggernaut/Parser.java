/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cse.se.juggernaut;


import java.io.File;
import java.io.IOException;
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
    
    private static void addChildren(Element parent, DefaultMutableTreeNode pnode){
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
                    Parser.addChildren(node, tn);
                } else if(node.getTagName().equals("item")){
                    // item: make treenode for leaf
                    DefaultMutableTreeNode tn = new DefaultMutableTreeNode(node.getAttribute("name"));
                    pnode.add(tn);
                }
            }
        }
        
    }
    
    public static DefaultMutableTreeNode parse( File file ) {
        
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
                    if(node.getAttribute("name").equalsIgnoreCase("ROOT")){
                        // call recursive function
                        Parser.addChildren(node, res);
                    } else {
                        // error
                        return res;
                    }
                }
            }
            
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  
        }
        
        return res;
    }

}