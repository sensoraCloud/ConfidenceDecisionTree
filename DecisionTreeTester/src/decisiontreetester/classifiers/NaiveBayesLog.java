/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.classifiers;

import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.core.attributeclassobservers.AttributeClassObserver;
import moa.core.AutoExpandVector;
import moa.core.DoubleVector;
import weka.core.Instance;

/**
 *
 * @author Rocco De Rosa
 */
public class NaiveBayesLog extends NaiveBayes{
    
    
    protected double delta_confidence;
    
    protected int queryCount;
    
    
     public NaiveBayesLog(double confidence) {
         queryCount=0;
         delta_confidence=confidence;
     }
    
    @Override
    public double[] getVotesForInstance(Instance inst) {
        return doNaiveBayesPrediction(inst, this.observedClassDistribution,
                this.attributeObservers);
    }
    
    public static double[] doNaiveBayesPrediction(Instance inst,
            DoubleVector observedClassDistribution,
            AutoExpandVector<AttributeClassObserver> attributeObservers) {
        double[] votes = new double[observedClassDistribution.numValues()];
        double observedClassSum = observedClassDistribution.sumOfValues();
        for (int classIndex = 0; classIndex < votes.length; classIndex++) {
            votes[classIndex] = Math.log10(observedClassDistribution.getValue(classIndex)
                    / observedClassSum);
            for (int attIndex = 0; attIndex < inst.numAttributes() - 1; attIndex++) {
                int instAttIndex = modelAttIndexToInstanceAttIndex(attIndex,
                        inst);
                AttributeClassObserver obs = attributeObservers.get(attIndex);
                if ((obs != null) && !inst.isMissing(instAttIndex)) {
                    votes[classIndex] += Math.log10(obs.probabilityOfAttributeValueGivenClass(inst.value(instAttIndex), classIndex));
                }
            }
        }
        // TODO: need logic to prevent underflow?
        return votes;
    }
    
    
    
    public String getName() {
        return String.format("NB[C=%.1f]", this.delta_confidence);
    }
    
  protected boolean passesFilterCondition() {
        return true;
    }
    
    @Override
    public void trainOnInstanceImpl(Instance inst) {
        if (passesFilterCondition()) {
            super.trainOnInstanceImpl(inst);
            
                this.queryCount++;
            
        }
    }
    
     public int getQueryNumber() {
        return  queryCount ;
    }

    
}
