/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.classifiers;

import java.util.ArrayList;
import java.util.Stack;
import moa.classifiers.trees.HoeffdingTree;
import decisiontreetester.DecisionTreeTester;
import decisiontreetester.settings.GS;
import java.util.Arrays;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Questa classe estende HoeffdingTree permettendo alcune operazioni di
 * riesamina più approfondite
 *
 * @author Rocco De Rosa
 */
public class TransparentHoeffdingTree extends HoeffdingTree {

    public ArrayList<Double> errorProgression;
    public ArrayList<Double> queryProgression;
    public ArrayList<Double> numNodesProgression;
    private DecisionTreeTester.SquarePlotWindow thisWindow = null;
    protected int seed;
    protected int queryCount;
    protected Node lastFoundNode;
    protected int lastChosenClass;
    protected int type_bound;
    protected int elaboratedExamples;

    
    public TransparentHoeffdingTree(int seed) {
        super();
        this.seed = seed;
        this.queryCount = 0;
        this.elaboratedExamples = 0;
        this.lastFoundNode = null;
        this.lastChosenClass = -1;
        binarySplitsOption.setValue(GS.binarySplit);
        leafpredictionOption.setValueViaCLIString(GS.leafPrediction);
        splitConfidenceOption.setValue(GS.delta);
        removePoorAttsOption.setValue(GS.poorAtt);
        gracePeriodOption.setValue(GS.grace);
        this.removePoorAttsOption.setValue(false);
        this.stopMemManagementOption.setValue(false);
        errorProgression = new ArrayList<Double>();
        queryProgression = new ArrayList<Double>();
        numNodesProgression = new ArrayList<Double>();
        this.tieThresholdOption.setValue(GS.tieThreshold);
        this.splitCriterionOption.setValueViaCLIString(GS.splitCriterionOption);

        this.type_bound=GS.TYPE_BOUND;
    }

    public TransparentHoeffdingTree() {
        super();
        this.seed = -1;
        this.queryCount = 0;
        elaboratedExamples=0;
        this.lastFoundNode = null;
        this.lastChosenClass = -1;
        binarySplitsOption.setValue(GS.binarySplit);
        leafpredictionOption.setValueViaCLIString(GS.leafPrediction);
        splitConfidenceOption.setValue(GS.delta);
        removePoorAttsOption.setValue(GS.poorAtt);
        gracePeriodOption.setValue(GS.grace);
        this.removePoorAttsOption.setValue(false);
        this.stopMemManagementOption.setValue(false);
        this.tieThresholdOption.setValue(GS.tieThreshold);
        errorProgression = new ArrayList<Double>();
        queryProgression = new ArrayList<Double>();
        numNodesProgression = new ArrayList<Double>();
        this.splitCriterionOption.setValueViaCLIString(GS.splitCriterionOption);
        this.type_bound=GS.TYPE_BOUND;
        
    }

    @Override
    public void resetLearningImpl() {
        super.resetLearningImpl();
        this.queryCount = 0;
        elaboratedExamples=0;
        this.lastFoundNode = null;
        this.lastChosenClass = -1;
    }

    protected boolean passesFilterCondition() {
        return true;
    }

    public Node getTreeRoot() {
        return treeRoot;
    }

    public String printTree() {
        /*
         StringBuilder sb = new StringBuilder();
         this.treeRoot.describeSubtree(this, sb, 0);
        
         System.out.println(sb);
         * 
         */
        String result = "PROFONDITA': " + this.treeRoot.subtreeDepth();
        result += "\nNODI tot: "
                + (this.decisionNodeCount
                + this.activeLeafNodeCount
                + this.inactiveLeafNodeCount);
        result += String.format("\nERRORE TOTALE FOGLIE: %.5f", this.calculateError());
        result += "\nNUMERO QUERY: " + this.getQueryNumber();
        return result;
    }

    public String getName() {
        return "HBase";
    }

    public int getQueryNumber() {
        return (seed >= 0) ? queryCount : (int) this.trainingWeightSeenByModel;
    }

    public double getNodeCount() {
        return this.decisionNodeCount;
    }
    
    public double getLeafCount() {
        return this.activeLeafNodeCount + this.inactiveLeafNodeCount;
    }

    public int getDepth() {
        return this.treeRoot.subtreeDepth() + 1;
    }

    public double calculateError() {
        Stack<Node> errorStack = new Stack<Node>();

        double m = this.trainingWeightSeenByModel;

        double total = 0.0;

        double error = 0.0;
        errorStack.push(treeRoot);

        while (!errorStack.isEmpty()) {
            Node n = errorStack.pop();

            if (n instanceof SplitNode) {
                SplitNode p = (SplitNode) n;
                for (int i = p.numChildren() - 1; i >= 0; i--) {
                    errorStack.push(p.getChild(i));
                }
            } else {

                double[] classDist = n.getObservedClassDistribution();

                Arrays.sort(classDist);
                /*
                 for (double d : classDist)
                 total += d;
                 */
                total += Utils.sum(classDist);
                double min = 0;


                try {

                    if (classDist.length == 2) {
                        min = classDist[classDist.length - 2];
                    } else {
                        min = Utils.sum(Arrays.copyOf(classDist, classDist.length - 1));
                    }

                } catch (Exception e) {
                    min = 0;
                }

                error += min;
            }
        }
        //System.out.println("TOT teorico :  " + m + " / TOT nell'albero: " + total);
        return error / total;
    }

    public double calcLeafWeight() {
        Stack<Node> errorStack = new Stack<Node>();

        double total = 0.0;
        errorStack.push(treeRoot);

        while (!errorStack.isEmpty()) {
            Node n = errorStack.pop();

            if (n instanceof SplitNode) {
                SplitNode p = (SplitNode) n;
                for (int i = p.numChildren() - 1; i >= 0; i--) {
                    errorStack.push(p.getChild(i));
                }
            } else {
                double[] classDist = n.getObservedClassDistribution();

                total += Utils.sum(classDist);
            }
        }
        return total;
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        if (this.treeRoot != null) {
            FoundNode foundNode = this.treeRoot.filterInstanceToLeaf(inst,
                    null, -1);
            Node leafNode = this.lastFoundNode = foundNode.node;
            if (leafNode == null) {
                leafNode = foundNode.parent;
            }
                        
            double[] result = leafNode.getClassVotes(inst, this);
            this.lastChosenClass = Utils.maxIndex(result);
            return result;
        }
        return new double[0];
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        
        this.elaboratedExamples++;
        
        if (seed < 0 || this.trainingWeightSeenByModel <= seed || passesFilterCondition()) {
            super.trainOnInstanceImpl(inst);
            if (this.trainingWeightSeenByModel > seed) {
                this.queryCount++;
            }
        }
    }
}
