/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.gui;

import moa.classifiers.core.conditionaltests.NumericAttributeBinaryTest;
import moa.classifiers.trees.HoeffdingTree.Node;
import moa.classifiers.trees.HoeffdingTree.SplitNode;
import decisiontreetester.classifiers.TransparentHoeffdingTree;
import decisiontreetester.settings.GS;
import processing.core.PApplet;

/**
 * Questa classe costruisce un grafico in due dimensioni che rappresenta la
 * classificazione di un dataset a due attributi, rappresentando gli split come
 * separazioni verticali e orizzontali.
 *
 * @author Rocco De Rosa
 */
public class DrawableTree extends PApplet {

    /**
     * Radice dell'albero
     */
    public RectangleNode root = null;
    
    protected TransparentHoeffdingTree baseTree = null;
    
    protected String name;
    
    protected static float minX = 0.0f, maxX = 1.0f, minY = 0.0f, maxY = 1.0f;
    
    
    
    public void initDrawableTree(TransparentHoeffdingTree tree, String name) {
        this.baseTree = tree;
        this.name = name;
        this.root = makeNode(tree.getTreeRoot(), maxY, minY, minX, maxX);
        
    }
    
    
    public final RectangleNode makeNode(Node node, 
            double top, double bottom, double left, double right) {
        
        RectangleNode result;
        if (node instanceof SplitNode) {
            SplitNode n = (SplitNode) node;
            NumericAttributeBinaryTest nTest = (NumericAttributeBinaryTest) n.splitTest;
            
            if (nTest.attIndex == 0) {
                RectangleNode leftChild = makeNode(n.children.get(0), top, bottom, left, nTest.attValue);
                RectangleNode rightChild = makeNode(n.children.get(1), top, bottom, nTest.attValue, right);
                
                result = new SplitRectangle(leftChild, rightChild);
            } else {
                RectangleNode leftChild = makeNode(n.children.get(0), nTest.attValue, bottom, left, right);
                RectangleNode rightChild = makeNode(n.children.get(1), top, nTest.attValue, left, right);
                
                result = new SplitRectangle(leftChild, rightChild);
            }
            
        } else {
            double[] cDist = node.getObservedClassDistribution();
            double p = cDist.length == 2 ? cDist[1] / (cDist[0] + cDist[1]) : 0.0;
            
            result = new LeafRectangle(top, bottom, left, right, p);
        }
        return result;
    }

    @Override
    public void setup() {
        size(GS.RPLOT_WIDTH, GS.RPLOT_HEIGHT);
        colorMode(PApplet.HSB, 3.0f);
        rectMode(PApplet.CORNERS);
    }

    @Override
    public void draw() {    
        //stroke(0, 0);
        this.root.draw();
        
        drawText();
        save(GS.completeDir + "\\tree_plot\\" + name + ".png");
        
        noLoop();
    }
    
    public void drawText() {
        pushStyle();
        stroke(0);
        fill(0);
        textSize(16);
        text("Profondità: " + this.baseTree.getDepth() + 
                "; Nodi: " + this.baseTree.getNodeCount() + 
                String.format("; Errore: %.5f", this.baseTree.calculateError()) +
                "\nNumero Query: " + this.baseTree.getQueryNumber(), 
                GS.RPLOT_LEFT, GS.RPLOT_BOTTOM + 30);
        
         System.out.println("Tree Error:" + String.format("; Errore: %.5f", this.baseTree.calculateError()));

        popStyle();
    }
    
    
    
    protected abstract class RectangleNode {
        
        
        
        /**
         * Questo metodo permette di disegnare il nodo in processing.
         */
        public abstract void draw();
    }
    
    protected class SplitRectangle extends RectangleNode {
        
        private RectangleNode left = null;
        private RectangleNode right = null;
        
        public SplitRectangle(RectangleNode left, RectangleNode right) {
            this.left = left;
            this.right = right;
        }
        
        
        @Override
        public void draw() {
            this.left.draw();
            this.right.draw();
        }
        
    }
    
    protected class LeafRectangle extends RectangleNode {

        private double top;
        private double bottom;
        private double left;
        private double right;
        
        private double p;
        
        public LeafRectangle(double top, double bottom, double left, double right, double p) {
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
            
            this.p = p;
        }
        
        
        
        @Override
        public void draw() {
            int realTop = DrawableChart.convertRange(top, minY, maxY, GS.RPLOT_BOTTOM, GS.RPLOT_TOP);
            int realBottom = DrawableChart.convertRange(bottom, minY, maxY, GS.RPLOT_BOTTOM, GS.RPLOT_TOP);
            int realLeft = DrawableChart.convertRange(left, minX, maxX, GS.RPLOT_LEFT, GS.RPLOT_RIGHT);
            int realRight = DrawableChart.convertRange(right, minX, maxX, GS.RPLOT_LEFT, GS.RPLOT_RIGHT);
            
            fill((float)p, 3.0f, 3.0f);
            
            rect(realLeft, realTop, realRight, realBottom);
            
            
            if (realBottom - realTop > 15 && realRight - realLeft > 20) {
                pushStyle();
                stroke(0);
                fill(0);
                textSize(12);
                text(String.format("%.1f", p), realLeft + 3, realTop + 13 );
                popStyle();
            }
            
            
            
            
        }
        
    }
}
