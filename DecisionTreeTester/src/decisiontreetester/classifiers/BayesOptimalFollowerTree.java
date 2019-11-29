/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.classifiers;

import decisiontreetester.DecisionTreeTester;
import decisiontreetester.settings.GS;
import java.util.Arrays;
import moa.classifiers.core.AttributeSplitSuggestion;
import moa.classifiers.core.splitcriteria.SplitCriterion;
import java.util.HashSet;
import java.util.Set;
import moa.classifiers.trees.HoeffdingTree;
import weka.core.Instance;
import weka.core.Utils;

/**
 *
 * @author Dominic
 */
public class BayesOptimalFollowerTree extends TransparentHoeffdingTree {

   //public ArrayList<Double> errorProgression = new ArrayList<Double>();
    //public ArrayList<Double> queryProgression = new ArrayList<Double>();
    private DecisionTreeTester.SquarePlotWindow thisWindow;
    //public static double total = 0.0;
    // protected int seed;
    //protected int queryCount;
    public static int d = 0;
    //protected HoeffdingTree.Node lastFoundNode;
    //protected int lastChosenClass;

    public BayesOptimalFollowerTree(int seed) {
        super();
        this.seed = seed;
        this.queryCount = 0;
        this.lastFoundNode = null;
        this.lastChosenClass = -1;

    }

    public BayesOptimalFollowerTree() {
        super();
        this.seed = -1;
        this.queryCount = 0;
        this.lastFoundNode = null;
        this.lastChosenClass = -1;


    }

    @Override
    public void resetLearningImpl() {
        super.resetLearningImpl();
        this.queryCount = 0;
        this.lastFoundNode = null;
        this.lastChosenClass = -1;
    }

    protected boolean passesFilterCondition() {
        return true;
    }

    public HoeffdingTree.Node getTreeRoot() {
        return treeRoot;
    }

    public String printTree() {

        StringBuilder sb = new StringBuilder();
        this.treeRoot.describeSubtree(this, sb, 0);

        // System.out.println(sb);


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
        return ("HRoccoConsistence_"+GS.only_consistence);
    }

    @Override
    public int getDepth() {
        return this.treeRoot.subtreeDepth() + 1;
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        d = inst.numAttributes();
        if (this.treeRoot != null) {
            HoeffdingTree.FoundNode foundNode = this.treeRoot.filterInstanceToLeaf(inst,
                    null, -1);
            HoeffdingTree.Node leafNode = this.lastFoundNode = foundNode.node;
            if (leafNode == null) {
                leafNode = foundNode.parent;
            }

            double[] result = leafNode.getClassVotes(inst, this);
            this.lastChosenClass = Utils.maxIndex(result);
            return result;
        }
        return new double[0];
    }

    public static double computeHoeffdingBound(double range, double confidence,
            double n, int depth) {

        // double bound =  (Math.log(n) * 8 - Math.log(256)) * Math.sqrt((2 / (2*n)) * ((depth * Math.log(2 / confidence)) + Math.log( Math.pow(((d - 1) * n),2) * ((d - 1) * n - 1)  )));

        // double bound =  (Math.log(n) * 8 - Math.log(256)) * Math.sqrt((2 / (2*n)) * ((depth * Math.log(2 / confidence)) + Math.log( Math.pow(((d - 1) * 100),2) * ((d - 1) * 100 - 1)  )));


        // double bound =  Math.sqrt((1 / (2*n)) * ((depth * Math.log(2 / confidence)) + Math.log( Math.pow(((d - 1) * n),2) * ((d - 1) * n - 1)  )));

        //real number of tried split
        // double bound =  Math.sqrt((1 / (2*n)) * ((depth * Math.log(2 / confidence)) + Math.log( Math.pow(((d - 1) * 100),2) * ((d - 1) * (100 - 1))  )));

        //super easy
       // double bound = Math.sqrt(((depth * Math.log(2 / confidence)) + Math.log(d)) / (2 * n));


        //hoeff bound
         double bound = Math.sqrt(((range * range) * Math.log(1.0 / confidence)) / (2.0 * n));
        
        // System.out.println("la profondità: "+depth);
        /*System.out.println("numero delle etichette nella foglia: "+n);
         System.out.println("il numero degli attributi: "+K);
         
         System.out.println("il bound è: "+bound);*/
        return bound;
    }

    @Override
    protected void attemptToSplit(HoeffdingTree.ActiveLearningNode node, HoeffdingTree.SplitNode parent,
            int parentIndex) {

        //     System.out.println("profondita figlio"+node.subtreeDepth());
        //     System.out.println("profondita padre"+parent.subtreeDepth());
        if (!node.observedClassDistributionIsPure()) {
            
            SplitCriterion splitCriterion = (SplitCriterion) getPreparedClassOption(this.splitCriterionOption);
            AttributeSplitSuggestion[] bestSplitSuggestions = node.getBestSplitSuggestions(splitCriterion, this);
            Arrays.sort(bestSplitSuggestions);

            boolean shouldSplit = false;
            
            
            if (bestSplitSuggestions.length < 2) {
                shouldSplit = bestSplitSuggestions.length > 0;
            } else {
                
                
                double hoeffdingBound;
                if (parent != null) {
                    hoeffdingBound = computeHoeffdingBound(splitCriterion.getRangeOfMerit(node.getObservedClassDistribution()),
                            this.splitConfidenceOption.getValue(), node.getWeightSeen(), node.getDepth());
                    //System.out.println("profondità nodo"+());
                    //    System.out.println(this.printTree());
                } else {
                    hoeffdingBound = computeHoeffdingBound(splitCriterion.getRangeOfMerit(node.getObservedClassDistribution()),
                            this.splitConfidenceOption.getValue(), node.getWeightSeen(), 1);
                }
                
                AttributeSplitSuggestion bestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 1];
                AttributeSplitSuggestion secondBestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 2];
                
                if (GS.only_consistence == 0) {

                    if ((bestSuggestion.merit - secondBestSuggestion.merit > hoeffdingBound)
                            || (hoeffdingBound < this.tieThresholdOption.getValue())) {
                        shouldSplit = true;
                    }

                }else{
                
                    shouldSplit = true;
                }


                //if shouldSplit control if the best split function generate consistence childs
                if (shouldSplit) {

                    AttributeSplitSuggestion splitDecision = bestSplitSuggestions[bestSplitSuggestions.length - 1];

                    double[][] leaves = splitDecision.resultingClassDistributions;

                    if (leaves.length == 0) {
                        shouldSplit = false;
                    }

                    for (int i = 0; i < leaves.length && shouldSplit; i++) {

                        if (!isNodeConsistent(leaves[i],node.getDepth())) {
                            shouldSplit = false;
                        }
                    }

                    /*      if (Math.abs(p + leafBound - 0.5) < GS.BOF_EPSILON_SPLIT
                     && Math.abs(p - leafBound - 0.5) < GS.BOF_EPSILON_SPLIT) {
                     shouldSplit = true;
                     }*/
                }



                // }
                if ((this.removePoorAttsOption != null)
                        && this.removePoorAttsOption.isSet()) {
                    Set<Integer> poorAtts = new HashSet<Integer>();
                    // scan 1 - add any poor to set
                    for (int i = 0; i < bestSplitSuggestions.length; i++) {
                        if (bestSplitSuggestions[i].splitTest != null) {
                            int[] splitAtts = bestSplitSuggestions[i].splitTest.getAttsTestDependsOn();
                            if (splitAtts.length == 1) {
                                if (bestSuggestion.merit
                                        - bestSplitSuggestions[i].merit > hoeffdingBound) {
                                    poorAtts.add(new Integer(splitAtts[0]));
                                }
                            }
                        }
                    }
                    // scan 2 - remove good ones from set
                    for (int i = 0; i < bestSplitSuggestions.length; i++) {
                        if (bestSplitSuggestions[i].splitTest != null) {
                            int[] splitAtts = bestSplitSuggestions[i].splitTest.getAttsTestDependsOn();
                            if (splitAtts.length == 1) {
                                if (bestSuggestion.merit
                                        - bestSplitSuggestions[i].merit < hoeffdingBound) {
                                    poorAtts.remove(new Integer(splitAtts[0]));
                                }
                            }
                        }
                    }
                    for (int poorAtt : poorAtts) {
                        node.disableAttribute(poorAtt);
                    }
                }
            }


            if (shouldSplit) {

                AttributeSplitSuggestion splitDecision = bestSplitSuggestions[bestSplitSuggestions.length - 1];

                if (splitDecision.splitTest == null) {
                    // preprune - null wins
                    deactivateLearningNode(node, parent, parentIndex);
                } else {
                    HoeffdingTree.SplitNode newSplit = newSplitNode(splitDecision.splitTest,
                            node.getObservedClassDistribution());

                    double somma = 0;
                    /*   for (int i=0; i<splitDecision.resultingClassDistributions.length;  i++){
               
                     somma+=splitDecision.resultingClassDistributions[0][i];
                     somma+=splitDecision.resultingClassDistributions[1][i];
                     }
                     /* for (int i=1; i<bestSplitSuggestions.length;  i++){
                     for(int j=0; j<bestSplitSuggestions[i].resultingClassDistributions[0].length; j++){
                     somma +=bestSplitSuggestions[i].resultingClassDistributions[1][j];
                     somma +=bestSplitSuggestions[i].resultingClassDistributions[0][j];
                     }
                     }*/
                    //         double now= node.getWeightSeen();
                    //      double prec= node.getWeightSeenAtLastSplitEvaluation();
                    //   System.out.println(pesiFoglie(treeRoot));


                    for (int i = 0; i < splitDecision.numSplits(); i++) {

                        double[] decision = splitDecision.resultingClassDistributionFromSplit(i);
                        HoeffdingTree.Node newChild = newLearningNode(decision);

                        if (parentIndex != -1) {
                            newChild.setDepth(node.getDepth() + 1);
                        } else {
                            newChild.setDepth(2);
                        }

                        newSplit.setChild(i, newChild);
                        newChild.setNumElem((int) Utils.sum(splitDecision.resultingClassDistributionFromSplit(i)));

                    }

                    this.activeLeafNodeCount--;
                    this.decisionNodeCount++;
                    this.activeLeafNodeCount += splitDecision.numSplits();

                    if (parent == null) {
                        this.treeRoot = newSplit;
                    } else {
                        parent.setChild(parentIndex, newSplit);
                    }
                }
                // manage memory
                enforceTrackerLimit();
            }
        }
    }

    public double computeConsistentBound(double range, double confidence,
            double n, int depth) {

        return Math.sqrt( ((range * range) * depth * Math.log(8/confidence))
                / (2.0 * n));

    }

    public boolean isNodeConsistent(double[] childObservedClassDistribution, int depth) {


        if (childObservedClassDistribution.length > 1) {

            Arrays.sort(childObservedClassDistribution);

            double p_best = childObservedClassDistribution[childObservedClassDistribution.length - 1] / Utils.sum(childObservedClassDistribution);

            double p_second_best = childObservedClassDistribution[childObservedClassDistribution.length - 2] / Utils.sum(childObservedClassDistribution);

            double leafBound = computeConsistentBound(1.0,
                    this.splitConfidenceOption.getValue(),
                    Utils.sum(childObservedClassDistribution),depth);

            return ((p_best - p_second_best > leafBound) || (leafBound < this.tieThresholdOption.getValue()));

        } else {
            return false;
        }

    }

    public double pesiFoglie(Node nodo) {
        double pesi = 0;
        try {
            int figli = ((SplitNode) nodo).children.size();
            for (int i = 0; i < figli; i++) {
                Node figlio = ((SplitNode) nodo).children.get(i);
                if (figlio.isLeaf()) {
                    pesi += Utils.sum(figlio.getObservedClassDistribution());
                } else {
                    pesi += pesiFoglie(figlio);
                }
            }
        } catch (Exception e) {
            pesi += Utils.sum(treeRoot.getObservedClassDistribution());
        }
        return pesi;
    }
}
