/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.classifiers;

import java.util.ArrayList;
import moa.classifiers.trees.HoeffdingAdaptiveTree;
import moa.classifiers.trees.HoeffdingTree.Node;
import moa.classifiers.trees.HoeffdingTree.SplitNode;

/**
 *
 * @author Rocco De Rosa
 */
public class MyHoeffdingTree extends HoeffdingAdaptiveTree{
    
    public int getActiveLeafNodeCount() {
        return this.activeLeafNodeCount;
    }
    
    public int getInactiveLeafNodeCount() {
        return this.inactiveLeafNodeCount;
    }
    
    public int getDecisionNodeCount() {
        return this.decisionNodeCount;
    }
    
    public int[] getTotalNodeLeafCount() {
        int[] c = new int[]{0, 0, 0};
        ArrayList<Node> queue = new ArrayList<Node>(); 
        ArrayList<Integer> queueDepth = new ArrayList<Integer>();
        queue.add(treeRoot);
        queueDepth.add(1);
        int maxDepth = 0;
        while (queue.size() > 0) {
            Node n = queue.remove(0);
            int d = queueDepth.remove(0);
            maxDepth = d > maxDepth ? d : maxDepth;
            if (n instanceof SplitNode) {
                c[0]++;
                SplitNode sn = (SplitNode) n;
                for (int i = 0; i < sn.numChildren(); i++) {
                    queue.add(sn.getChild(i));
                    queueDepth.add(d + 1);
                }
            } else 
                c[1]++;
        }
        c[2] = maxDepth;
        return c;
    }
}
