package decisiontreetester.classifiers;

import moa.classifiers.trees.HoeffdingTree;
import decisiontreetester.DecisionTreeTester;
import decisiontreetester.settings.GS;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import moa.classifiers.core.AttributeSplitSuggestion;
import moa.classifiers.core.splitcriteria.SplitCriterion;
import weka.core.Instance;
import weka.core.Utils;

/**
 *
 * @author Marco
 */
public class HoeffCesaBoundTree extends TransparentHoeffdingTree {

    //public ArrayList<Double> errorProgression = new ArrayList<Double>();
    //public ArrayList<Double> queryProgression = new ArrayList<Double>();
    private DecisionTreeTester.SquarePlotWindow thisWindow ;
    //public static double total = 0.0;
    // protected int seed;
    //protected int queryCount;
    public static int d = 0;
    //protected HoeffdingTree.Node lastFoundNode;
    //protected int lastChosenClass;

    public HoeffCesaBoundTree(int seed) {
        super();
        this.seed = seed;
        this.queryCount = 0;
        this.lastFoundNode = null;
        this.lastChosenClass = -1;
         
    }

    public HoeffCesaBoundTree() {
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
        return "HRocco";
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
            double n, int depth, double t,int type) {
        
        double bound = 0;
        
            
        switch (type) {
            
            //Entropy
            case 1:  bound = GS.c_bound * Math.log(n) * Math.sqrt( Math.log( Math.pow(n, 2)*Math.pow(depth, 2)*t*d  ) / n   );       
                     break;
                
            case 2:  bound = GS.c_bound * Math.sqrt( Math.log( Math.pow(n, 2)*Math.pow(depth, 2)*t*d  ) / n   );       
                     break;
                
            case 3:  bound = GS.c_bound * Math.sqrt( Math.log( Math.pow(n, 2)*Math.pow(depth, 2)*t*d  ) / n   );       
                     break;
                     
        }
        
        
        
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
                            this.splitConfidenceOption.getValue(), Utils.sum(node.getObservedClassDistribution()), node.getDepth(), this.elaboratedExamples, this.type_bound);
                    //System.out.println("profondità nodo"+());
                    //    System.out.println(this.printTree());
                } else {
                    hoeffdingBound = computeHoeffdingBound(splitCriterion.getRangeOfMerit(node.getObservedClassDistribution()),
                            this.splitConfidenceOption.getValue(), Utils.sum(node.getObservedClassDistribution()) , 1, this.elaboratedExamples, this.type_bound);
                }
                AttributeSplitSuggestion bestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 1];
                AttributeSplitSuggestion secondBestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 2];
                if ((bestSuggestion.merit - secondBestSuggestion.merit > hoeffdingBound)
                        || (hoeffdingBound < this.tieThresholdOption.getValue())) {
                    shouldSplit = true;
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
