/*
 *    RuleClassifierNBayes.java
 *    Copyright (C) 2012 University of Porto, Portugal
 *    @author P. Kosina, E. Almeida, J. Gama
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 *    
 */

package moa.classifiers.rules;

import java.util.ArrayList;
import java.util.Collections;
import weka.core.Instance;
import moa.classifiers.bayes.NaiveBayes;
import moa.options.IntOption;

/**
 * This classifier learn ordered and unordered rule set from data stream with naive Bayes learners.
 * 
 * <p>Learning Decision Rules from Data Streams, IJCAI 2011, J. Gama,  P. Kosina </p>
 *
 * 
 * <p>Parameters:</p>
 * <ul>
 * <li> -q: The number of instances a leaf should observe before permitting Naive Bayes.</li>
 * <li> -p: Minimum value of p </li>
 * <li> -t: Tie Threshold </li>
 * <li> -c: Split Confidence </li>
 * <li> -g: GracePeriod, the number of instances a leaf should observe between split attempts </li>
 * <li> -o: Prediction function to use. Ex:FirstHit </li>
 * <li> -r: Learn ordered or unordered rule </li>
 * </ul>
 * 
 * @author P. Kosina, E. Almeida, J. Gama
 * @version $Revision: 1 $
 */
public class RuleClassifierNBayes extends RuleClassifier {
	
	private static final long serialVersionUID = 1L;
	
	public IntOption nbThresholdOption = new IntOption(
			"nbThreshold",
            'q',
            "The number of instances a leaf should observe before permitting Naive Bayes.",
            0, 0, Integer.MAX_VALUE);
	
	@Override
	public double[] getVotesForInstance(Instance inst) {
		double[] votes = new double[observedClassDistribution.numValues()];
		switch (super.predictionFunctionOption.getChosenIndex()) {
        case 0:
        	votes = firstHitNB(inst);
        	break;
        case 1:
        	votes = weightedSumNB(inst);
        	break;
        case 2:
        	votes = weightedMaxNB(inst);
        	break;
        	}
		return votes; 
		}
	
	// The following three functions are used for the prediction 
	protected double[] firstHitNB(Instance inst) {
		int countFired = 0;
		boolean fired = false;
		double[] votes = new double[observedClassDistribution.numValues()];
		for (int j = 0; j < ruleSet.size(); j++) {
			if (ruleSet.get(j).ruleEvaluate(inst) == true) {
				countFired = countFired + 1;
				if (ruleSet.get(j).obserClassDistrib.sumOfValues() >= this.nbThresholdOption.getValue()) {
					votes = NaiveBayes.doNaiveBayesPredictionLog(inst, ruleSet.get(j).obserClassDistrib, ruleSet.get(j).observers, ruleSet.get(j).observersGauss);
		    	    votes = exponential(votes);
		    	    votes = normalize(votes);
		    	    } else {
		    	    	for (int z = 0; z < majority.get(j).size(); z++) {
		    	    		votes[z] = ruleSet.get(j).obserClassDistrib.getValue(z) 
		    	    				/ ruleSet.get(j).obserClassDistrib.sumOfValues();
		    	    		}
		    	    	}
				break;
				}
			}
		if (countFired > 0) {
			fired = true;
			} else {
				fired = false;
				}
		if (fired == false) {
			if (super.getWeightSeen() >= this.nbThresholdOption.getValue()) {
				votes = NaiveBayes.doNaiveBayesPredictionLog(inst, this.observedClassDistribution, this.attributeObservers, this.attributeObserversGauss);
				votes = exponential(votes);
				votes = normalize(votes);
				} else {
					votes = super.oberversDistribProb(inst, this.attributeObservers);
					}
			}
		return votes;
		}
	
	protected double[] weightedMaxNB(Instance inst) {
		int countFired = 0;
		int count = 0;
		boolean fired = false;
		double highest = 0.0;
		double[] votes = new double[observedClassDistribution.numValues()];
		ArrayList<Double> ruleSetVotes = new ArrayList<Double>();
		ArrayList<ArrayList<Double>> majorityProb = new ArrayList<ArrayList<Double>>();
		for (int j = 0; j < ruleSet.size(); j++) {
			ArrayList<Double> ruleClassDistribProb=new ArrayList<Double>();
			if(ruleSet.get(j).ruleEvaluate(inst) == true) {
				countFired = countFired + 1;
				if (ruleSet.get(j).obserClassDistrib.sumOfValues() >= this.nbThresholdOption.getValue()) {
					votes = NaiveBayes.doNaiveBayesPredictionLog(inst, ruleSet.get(j).obserClassDistrib, ruleSet.get(j).observers, ruleSet.get(j).observersGauss);
					votes = exponential(votes);
		    	    votes = normalize(votes);
		    	    } else {
		    	    	count = count + 1;
		    	    	for (int z = 0; z < majority.get(j).size(); z++){
		    	    		ruleSetVotes.add(ruleSet.get(j).obserClassDistrib.getValue(z) / ruleSet.get(j).obserClassDistrib.sumOfValues());
		    	    		ruleClassDistribProb.add(ruleSet.get(j).obserClassDistrib.getValue(z) / ruleSet.get(j).obserClassDistrib.sumOfValues());
		    	    		}
		    	    	majorityProb.add(ruleClassDistribProb);
		    	    	}
				}
			}
		if (count > 0) {
			Collections.sort(ruleSetVotes); 
		    highest = ruleSetVotes.get(ruleSetVotes.size() - 1);
		    for (int t = 0; t < majorityProb.size(); t++) {
		    	for (int m = 0; m < majorityProb.get(t).size(); m++) {
		    		if(majorityProb.get(t).get(m) == highest){
		    			for(int h = 0; h < majorityProb.get(t).size(); h++){
		    				votes[h]=majorityProb.get(t).get(h);
		    				}
 		    			break;
		    			}
		    		}
		    	}
		    }
		if (countFired > 0) {
			fired=true;
			}  else {
				fired=false;
				}
		if (fired == false) {
			if(super.getWeightSeen() >= this.nbThresholdOption.getValue()) {
				votes = NaiveBayes.doNaiveBayesPredictionLog(inst, this.observedClassDistribution, this.attributeObservers, this.attributeObserversGauss);
				votes = exponential(votes);
				votes = normalize(votes);
				} else {
					votes = super.oberversDistribProb(inst, this.attributeObservers);
					}
			}
		return votes;
		}
	
	protected double[] weightedSumNB(Instance inst) {
		int countFired = 0;
		int count = 0;
		boolean fired = false;
		double[] votes = new double[observedClassDistribution.numValues()];
		ArrayList<Double> weightSum = new ArrayList<Double>();
		ArrayList<ArrayList<Double>> majorityProb = new ArrayList<ArrayList<Double>>();
		for ( int j = 0; j < ruleSet.size(); j++) {
			ArrayList<Double> ruleClassDistribProb=new ArrayList<Double>();
			if (ruleSet.get(j).ruleEvaluate(inst) == true) {
				countFired = countFired + 1;
				if (ruleSet.get(j).obserClassDistrib.sumOfValues() >= this.nbThresholdOption.getValue()) {
					votes = NaiveBayes.doNaiveBayesPredictionLog(inst, ruleSet.get(j).obserClassDistrib, ruleSet.get(j).observers, ruleSet.get(j).observersGauss);
	    	        votes = exponential(votes);
	    	        votes = normalize(votes);
	    	        } else {
	    	        	count=count+1;
	    	        	for (int z = 0; z < majority.get(j).size(); z++) {
	    	        		ruleClassDistribProb.add(ruleSet.get(j).obserClassDistrib.getValue(z) / ruleSet.get(j).obserClassDistrib.sumOfValues());
	    	        		}
	    	        	majorityProb.add(ruleClassDistribProb);
	    	        	}
				}
			}
		 if(count > 0) {
			 for (int m = 0; m < majorityProb.get(0).size(); m++) {
				 double sum = 0.0;
				 for (int t = 0; t < majorityProb.size(); t++){
					 sum = sum + majorityProb.get(t).get(m);
					 }
				 weightSum.add(sum);
				 }
			 for (int h = 0; h < weightSum.size(); h++) {
				 votes[h] = weightSum.get(h) / majorityProb.size();
				 }
			 }
		 if(countFired>0){
			 fired = true;
			 } else {
				 fired=false;
				 }
		 if (fired == false) {
			 if (super.getWeightSeen() >= this.nbThresholdOption.getValue()) {
				 votes = NaiveBayes.doNaiveBayesPredictionLog(inst, this.observedClassDistribution, this.attributeObservers, this.attributeObserversGauss);
			  	 votes = exponential(votes);
	  		 	 votes = normalize(votes);
	  		 	 } else {
	  		 		 votes = super.oberversDistribProb(inst, this.attributeObservers);
	  		 		 }
			 }
		 return votes;
		 }
	
	protected double[] normalize(double[] votes) {
		double sum=0;
		for (int i = 0; i < votes.length; i++) {
			sum = sum + votes[i];
			}
		for (int j = 0; j < votes.length; j++) {
			votes[j] = votes[j] / sum;
			}
		return votes;
		}
	
	protected double[] exponential(double[] votes) {
		for (int i = 0; i < votes.length; i++) {
			votes[i] = Math.exp(votes[i]);
			}
		return votes;
		}


  
}
