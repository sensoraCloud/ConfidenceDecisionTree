/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.classifiers;

import java.util.Arrays;
import java.util.Random;
import weka.core.Utils;

/**
 *
 * @author Rocco De Rosa
 */
public class SelectiveSamplingHoeffdingTree extends HoeffCesaBoundTree {

    //protected int queryNumber;
    protected double selNumerator;
    protected Random randomTest = new Random();

    public SelectiveSamplingHoeffdingTree(int seed, double c1) {
        super(seed);
        this.selNumerator = c1;
    }

    public double getSelNumerator() {
        return selNumerator;
    }

    @Override
    public String getName() {
        return String.format("SSHT[c=%.1f,s=%d]", this.selNumerator, this.seed);
    }

    public boolean isNodeConsistent(Node node) {

        //random
        //return (randomTest.nextDouble() > this.selNumerator);
        
        
       double[] dis = node.getObservedClassDistribution();

        if (dis.length > 1) {

            Arrays.sort(dis);

            //only for binary class
            double p = dis[dis.length - 1] / Utils.sum(dis);
            
           // double p_best = dis[dis.length - 1] / Utils.sum(dis);

           // double p_second_best = dis[dis.length - 2] / Utils.sum(dis);
            double example_before=((ActiveLearningNode) node).weightSeenAtLastSplitEvaluation;
            //double example_before2=node.getNumEleme();
           
            //double node_count=this.getNodeCount();
            //double all_examples=this.elaboratedExamples;             
            double num_etichette=Utils.sum(dis);
            double num_example_seen=example_before+node.examplesSeen;
            //double depth=node.depth;          
            
             //if (num_etichette>num_example_seen)
               // example_before2=example_before2;
                        
            
            double leafBound = computeConsistentBound(1.0,
                    this.splitConfidenceOption.getValue(),
                    num_etichette,num_example_seen,this.selNumerator);

           // return ((p_best - p_second_best > leafBound) || (leafBound < this.tieThresholdOption.getValue()));

            //boolean cons=( Math.abs( p - .5 ) > leafBound);
            
            //if (cons==true)
              //  cons=true;
                      
            if (leafBound==0)
                leafBound=1;
            
             return ( Math.abs( p - .5 ) > leafBound);
            
        } else {
            
            return false;
            
        }



    }

    public double computeConsistentBound(double range, double confidence,
            double n, double t, double c) {
        
        //return Math.sqrt(((range * range) * Math.log(4 / confidence))/ (2.0 * n));
        
        return c*Math.sqrt(Math.log(t) / n);
        
         //return Math.sqrt(((range * range) * Math.log(n))
           //     / (2.0 * n));
    }

    public double calculateTrainingBound(Node node) {
                        
       return  (this.selNumerator  / Math.sqrt(Utils.sum(node.getObservedClassDistribution())));
                
      //return  (this.selNumerator  / (  Math.sqrt(  Math.log(this.getQueryNumber() - Utils.sum(node.getObservedClassDistribution()) + 1) * Utils.sum(node.getObservedClassDistribution()))));
        
      //  return  (this.selNumerator  / ( Math.log(this.getQueryNumber()-Utils.sum(node.getObservedClassDistribution())+1) +  Math.sqrt(Utils.sum(node.getObservedClassDistribution()))));
        
    }

    @Override
    protected boolean passesFilterCondition() {
        
        if (lastFoundNode != null) {
            lastFoundNode.examplesSeen++;
        }

        return (lastFoundNode == null || !isNodeConsistent(lastFoundNode));
        
        //return (lastFoundNode == null || !isNodeConsistent(lastFoundNode)
          //      || (randomTest.nextDouble() <= calculateTrainingBound(lastFoundNode)));
    }
}
