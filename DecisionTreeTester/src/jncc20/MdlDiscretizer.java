/**
 * MdlDiscretizer.java
 * @author Giorgio Corani (giorgio@idsia.ch)
 * 
 * Copyright:
 * Giorgio Corani, Marco Zaffalon
 *
 * IDSIA
 * Istituto Dalle Molle di Studi sull'Intelligenza Artificiale
 * Manno, Switzerland
 * www.idsia.ch
 *
 * The JNCC distribution is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation (either version 2 of the License or, at your option, any later
 * version), provided that this notice and the name of the author appear in all 
 * copies. JNCC is distributed "as is", in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 * You should have received a copy of the GNU General Public License
 * along with the JNCC distribution. If not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package jncc20;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/**
 * Implements recursive MDL-based supervised discretization (Fayyad and Irani, 1993).<p>
 * The constructor receives three arguments:
 * a vector containing the numerical values, in the different classes, of the feature to be discretized;<br>
 * a vector containing the corresponding classes;<br>
 * an integer which is the number of classes of the problem.<p>
 * The constructor directly discretizes the variables, and the cut points can be accessed via the function 
 * getCutPoints.
 */
class MdlDiscretizer {

	/**
	 *Computes the discretization intervals using the supplied class and feature vector; stores them into cutPoints
	 */
	MdlDiscretizer(ArrayList<Double> SuppliedFeatureValues, ArrayList<Integer> SuppliedClassValues, int suppliedNumClasses)
	{
		pairVector=new Pair[SuppliedFeatureValues.size()];
		for (int i=0;i<SuppliedFeatureValues.size();i++){
			pairVector[i]=new Pair(SuppliedFeatureValues.get(i),SuppliedClassValues.get(i));
		}
		Arrays.sort(pairVector);
		numClasses=suppliedNumClasses;
		cutPoints=new ArrayList<Double>();
		possibleCutPointsIdxInPairVector=new ArrayList<Integer>();
		possibleCutPoints=new ArrayList<Double>();
		computePossibleCutPoints();

		//discretize feature, if possible
		if (!possibleCutPoints.isEmpty()){
			recursiveMDLDiscretization(0,pairVector.length-1);

			//IF CUT POINTS FOUND
			if (!cutPoints.isEmpty())
				Collections.sort(cutPoints);	
		}
	}

	/**
	 * Identifies the feature values that can constitute possible cutPoints (i.e., possible discretization intervals)
	 */
	private void computePossibleCutPoints(){
		double CurrentValue;
		double PreviousValue = pairVector[0].getFeatureValue();
		ArrayList<Integer> PreviousClassSet = new ArrayList<Integer>();
		PreviousClassSet = getClassList(0);
		ArrayList<Integer> CurrentClassSet = new ArrayList<Integer>();

		for (int i = 1; i < pairVector.length; i++) {
			CurrentValue =pairVector[i].getFeatureValue(); 
			if (CurrentValue==PreviousValue)
				continue;

			CurrentClassSet = getClassList(i);

			// found possible cut point if the previous or the current value
			// has associated more than one class
			//or if we have two distinc single values
			if (((PreviousClassSet.size()) > 1 | (CurrentClassSet.size()) > 1)  | 
					(PreviousClassSet.get(0) != CurrentClassSet.get(0)) )
			{
				possibleCutPoints.add((CurrentValue + PreviousValue) / 2);
				possibleCutPointsIdxInPairVector.add(i-1);
			}

			// in the remaining case, we have two distinct identical values,
			// and no cutpoint is worthy being added.
			PreviousClassSet = CurrentClassSet;
			PreviousValue = CurrentValue;
		}
		// ==POSSIBLE CUT POINTS HAVE BEEN FOUND
	}


	/**
	 * Returns the list of classes
	 * corresponding to a certain numerical value of a feature, within the PairVector.
	 */
	private ArrayList<Integer> getClassList(int index) {

		ArrayList<Integer> FoundClasses = new ArrayList<Integer>();
		FoundClasses.add(pairVector[index].getClassValue()); 
		Double featureValue=pairVector[index].getFeatureValue();
		int length=pairVector.length;
		index ++;

		while ((index<length) && (featureValue.compareTo( pairVector[index].getFeatureValue())==0)) 
		{
			int candidateClass=pairVector[index].getClassValue();
			if (FoundClasses.indexOf(candidateClass)==-1){
				FoundClasses.add(candidateClass); 
			}
			index++;
		}
		return FoundClasses;
	}

	/**Discretizes the variable and instantiates cutPoints; called from the constructor.
	 * 
	 */
	private boolean recursiveMDLDiscretization(int lowerIdx,int upperIdx) {


		int limit;


		//find out relevant cutPoints in the current partition
		int lowerCutPointIdx=0, upperCutPointIdx=0;
		boolean foundLower=false;
		boolean found;

		for (int i=0; i<possibleCutPointsIdxInPairVector.size();i++)
		{
			if ((possibleCutPointsIdxInPairVector.get(i)+1>lowerIdx) && (possibleCutPointsIdxInPairVector.get(i)+1<upperIdx)){
				if (foundLower)
				{
					upperCutPointIdx=i;
				}
				else{

					lowerCutPointIdx=i;

					//this necessary in case we are on the very last cutpoin
					upperCutPointIdx=i;
					foundLower=true;
				}
			}
		}

		double currentBestCutPoint=Double.MIN_VALUE;
		double currentBestInfo=Double.MIN_VALUE;
		double[] PartitionInformation;
		double[] LeftInformation;
		double[] RightInformation;

		for (int j=lowerCutPointIdx;j<=upperCutPointIdx;j++) {

			limit=possibleCutPointsIdxInPairVector.get(j);

			// ==Sub-Partitions have been built
			PartitionInformation = new double [2];
			LeftInformation=new double [2];
			RightInformation =new double [2];
			double TrialPartitionEntropy;
			double InformationGain;
			double InformationThreshold;
			

			int length=upperIdx-lowerIdx+1;
			PartitionInformation = computeEntropy(lowerIdx,upperIdx);
			LeftInformation = computeEntropy(lowerIdx,limit);
			RightInformation = computeEntropy(limit+1,upperIdx);
			TrialPartitionEntropy = ((LeftInformation[0] * (limit+1-lowerIdx)) + (RightInformation[0] * (upperIdx-limit)));
			TrialPartitionEntropy /= length; 
			InformationGain=PartitionInformation[0]-TrialPartitionEntropy;

			InformationThreshold=Math.log(length-1)/Math.log(2)+
			(Math.log(Math.pow(3,PartitionInformation[1])-2))/Math.log(2);
			InformationThreshold -= PartitionInformation[0]*PartitionInformation[1];
			InformationThreshold += LeftInformation[0]*LeftInformation[1]+RightInformation[0]*RightInformation[1];
			InformationThreshold /=length;

			if ((InformationGain>InformationThreshold) && (InformationGain>currentBestInfo))
			{	
				currentBestCutPoint=possibleCutPoints.get(j);
				currentBestInfo=InformationGain;
			}
		}//==ALL CUT POINTS SCANNED

		if (currentBestInfo==Double.MIN_VALUE){
			found=false;
		}

		else 
		{
			cutPoints.add(currentBestCutPoint);
			//==CUT POINT DECIDED

			int cutPointIdxInPairVector = possibleCutPointsIdxInPairVector.get(possibleCutPoints.indexOf(currentBestCutPoint));
			//the pairvector might contain several identical values of the features, which should go in the same partition
			while (pairVector[cutPointIdxInPairVector].getFeatureValue()==pairVector[cutPointIdxInPairVector+1].getFeatureValue())
				{
				cutPointIdxInPairVector++;
				}

			//now the counter indexes the last element smaller than the cutPoint
			if (possibleCutPoints.indexOf(currentBestCutPoint) > 0)
				recursiveMDLDiscretization(lowerIdx,cutPointIdxInPairVector);

			if (possibleCutPoints.indexOf(currentBestCutPoint) <possibleCutPoints.size()-1){
				recursiveMDLDiscretization(cutPointIdxInPairVector+1,upperIdx);
			}
			found=true;
		}//==END if(AcceptableCutPoints.size()!=0)	
		return found;
	}

	/**
	 * Computes the entropy of the partion of pairVector comprised between the indexes lowerBound and upperBound
	 */
	private double[] computeEntropy(int lowerBound, int upperBound)
	{
		int[] ClassFrequencies=new int[numClasses];
		Arrays.fill(ClassFrequencies,0);
		int i;
		double[] ReturnedArray=new double[2];

		// find list of classes in the partition;
		for (i = lowerBound; i < upperBound+1; i++) {
			ClassFrequencies[pairVector[i].getClassValue()]++;
		}
		// ==Contigency Map filled
		double N = upperBound-lowerBound+1;
		double info = N * Math.log(N);
		int counter=0;
		for (int j=0;j<ClassFrequencies.length;j++)
		{
			if  (!(ClassFrequencies[j]==0)){
				info -= ClassFrequencies[j] * Math.log(ClassFrequencies[j]);
				counter++;
			}
		}
		info /= (N * Math.log(2));


		ReturnedArray[0]=info;
		ReturnedArray[1]=counter;	
		return ReturnedArray;
	}


	//==GETTER AND SETTER METHODS
	public ArrayList<Double> getCutPoints() {
		return cutPoints;
	}

	// =====DATA MEMBERS=====
	/**
	 * Vector of feature/class pairs
	 */
	private  Pair[] pairVector;

	/**
	 * Discretization intervals (if any) identified by the algorihtm 
	 */
	private  ArrayList<Double> cutPoints;

	/**
	 *Numerical values of the feature, which constitues possible discretization intervals (i.e, possible cutPoints) 
	 */
	private  ArrayList<Double> possibleCutPoints;

	/**
	 * Total number of classes (immutable, hence final)
	 */
	private final int numClasses;

	/**
	 * Indexes of the possible cutPoints, with reference to PairVector
	 */
	private  ArrayList<Integer> possibleCutPointsIdxInPairVector;



	//==NESTED CLASS: PAIR
	/**
	 * Helper class for MdlDiscretizer, which effectively stores feature-class pairs
	 */
	private static class Pair implements Comparable {


		Pair(double SuppliedFeatureValue, int SuppliedClassValue)
		{
			FeatureValue=SuppliedFeatureValue;
			ClassValue=SuppliedClassValue;
		}

		/**Compares two Pair, ordering the on the basis of the feature value. Returns:
		 *  a negative integer, zero, or a positive integer if the this Pair object object 
		 *  is less than, equal to, or greater than the supplied Pair.
		 */
		public int compareTo(Object secondPair) throws ClassCastException {
			if (!(secondPair instanceof Pair))
				throw new ClassCastException("Pair object expected.");
			return FeatureValue.compareTo( ((Pair) secondPair).getFeatureValue());
		}

		//==GETTERS
		public Integer getClassValue() {
			return ClassValue;
		}

		public Double getFeatureValue() {
			return FeatureValue;
		}
		// =====DATA MEMBERS=====
		private Double FeatureValue;
		private Integer ClassValue;

	}


}












