/**
 * NaiveClassifier.java
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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**Abstract super-class for Naive Classifiers 
 * 
 */
abstract public class NaiveClassifier {	

	/**
	 * Initializes all features and output classes; computes all the relevant conditionalFreq on the training set, setting the specified prior
	 * (0:0; 1:laplace; 2:uniform) 
	 */
	NaiveClassifier(ArrayList<int[]> TrainingSet, ArrayList<String> FeatureNames, ArrayList<String> classNames, ArrayList<Integer> numClassForEachFeature, int priorType)
	{	
		//init of various data members
		numClasses=classNames.size();
		numFeatures=FeatureNames.size();
		trainInstances=TrainingSet.size();
		numValues=numClassForEachFeature.toArray(new Integer[] {});

		//prior counts for classes, conditional and unconditional frequencies
		pcCond=new double [numFeatures];
		pcUncond=new double [numFeatures];

		if (priorType==0 | priorType==1){
			pcClass=priorType;
			for (int i=0; i<numFeatures; i++){
				pcCond[i]=priorType; 
				pcUncond[i]=priorType; 
			}
		}

		else	if (priorType==2){
			pcClass=(double)1/numClasses; 
			for (int i=0; i<numFeatures; i++){
				pcCond[i]=(double)1/(numClasses*numValues[i]); 
				pcUncond[i]=(double)1/(numValues[i]); 
			}
		}

		else {
			System.out.println("Wrong prior type specified");
			System.exit(0);
		}
		//all prior counts initialized

		buildOutputClasses(TrainingSet, classNames);
		buildFeatureSet (TrainingSet, FeatureNames, numClassForEachFeature);

	}

	/**Abstract function
	 */
	abstract void classifyInstances(ArrayList<int[]> TestingSet);






	/**
	 * Instantiates the FeatureSet, by 
	 * computing all the relevant conditionalFreq of all features on the training set; note that a Laplace prior can be introduced, by setting
	 * parameter priorCounts different from zero (i.e., the quantity priorCounts will be then added to each computed count).  
	 * In particular, it computes:<p>
	 * the bivariates count n(a_i,c_j), that correspond to the occurences ignoring missing data for NBC,
	 * and to the lower counts for NCC;<p>
	 * for each output class, the number of missing data of the current feature, needed to then compute the upper counts
	 * for NCC. 
	 * The priorType defines the prior to be used (0:0; 1:laplace; 2:uniform) .
	 * 
	 * 
	 * <p>
	 */
	protected void buildFeatureSet (ArrayList<int[]> TrainingSet, ArrayList<String> FeatureNames, 
			ArrayList<Integer> NumClassForEachFeature) {

		//last column in TrainingSet contains output classes
		int i,j;
		int currentFeatValue, currentClass;
		double[][] Frequencies;
		int[] Missing;
		featureSet=new Feature[FeatureNames.size()];

		//loop over all features
		for (i=0; i<numFeatures; i++)
		{		
			Frequencies = new double[numClasses][NumClassForEachFeature.get(i)];
			Missing = new int[numClasses];

			for (double[] ArrayRow :Frequencies){
				Arrays.fill(ArrayRow,pcCond[i]);
			}

			Arrays.fill(Missing,0);

			//scan all the rows of the training set
			for (j=0; j<trainInstances; j++)
			{
				currentFeatValue=TrainingSet.get(j)[i];
				currentClass=TrainingSet.get(j)[numFeatures];

				//non-missing datum
				if (currentFeatValue != -9999){
					Frequencies[currentClass][currentFeatValue]++;
				}
				else{
					Missing[currentClass]++;
				}
			}

			featureSet[i]= new Feature (FeatureNames.get(i), Frequencies, Missing); 
		}



	}//==END buildFeatureSet



	/**
	 *Instantiates class names and conditionalFreq of the OutputClass;  prior is defined by parameter priorType (0:0; 1:laplace; 2:uniform)
	 */
	protected  void buildOutputClasses(ArrayList<int[]> TrainingSet, ArrayList<String >ClassNames)
	{
		int i;
		int currentClass;
		double[] ClassFrequencies= new double [numClasses];

		Arrays.fill(ClassFrequencies,pcClass);

		//class is in the last position within the traning set
		int LastIdx=(TrainingSet.get(1).length)-1;

		for (i=0; i<trainInstances; i++)
		{
			currentClass=TrainingSet.get(i)[LastIdx];
			ClassFrequencies[currentClass]++;
		}


		//Now, compute the a priori probability of each class
		double LogProbability[]=new double[numClasses];
		double CountSum=ArrayUtils.arraySum(ClassFrequencies)[0];


		for (i=0; i<ClassFrequencies.length; i++)
		{
			LogProbability[i]=Math.log((double)ClassFrequencies[i]/CountSum);
		}

		outputClasses=new OutputClass[numClasses];
		for (i=0; i<numClasses;i++)
		{
			outputClasses[i]= new OutputClass (ClassNames.get(i), ClassFrequencies[i], LogProbability[i]);
		}

	}

	/**The gamma function is necessary in order to compute the marginal likelihood. 
	 * Code taken from  StatsconLib.java
	see: www.symbolicnet.org/conferences/iamc02/IAMCNosal.pdf
    Function gammaln: returns the value of ln(gamma(xx)) for xx > 0*
	 */
	double gammaln(double xx) {
		double x, tmp, ser;
		double cof[] = {76.18009173, -86.50532033, 24.01409822, -
				1.231739516, 0.120858003e-2, -0.536382e-5};
		int j;
		x = xx - 1.0;
		tmp = x + 5.5;
		tmp-= (x+0.5)*Math.log(tmp);
		ser = 1.0;
		for(j = 0; j <= 5; j++) {
			x += 1.0;
			ser+= cof[j]/x;
		}
		return -tmp+Math.log(2.50662827465*ser);
	}

	void saveProbabilities(String fileAddress)
	{
		DecimalFormat formatter= new DecimalFormat("#0.000");
		try{
			int i,j;
			BufferedWriter out=new BufferedWriter(new FileWriter(fileAddress,false));
			for (i=0; i<probabilities.length;i++)
			{
				for (j=0; j<probabilities[0].length-1;j++)
					out.write(formatter.format(probabilities[i][j])+"\t");
				out.write(formatter.format(probabilities[i][j])+"\n");
			}
			out.close();
		}

		catch (IOException e)
		{
			System.out.println("Problems saving probabilities to file");
		}		
	}

	//==GETTERS
	public OutputClass[] getOutputClasses() {
		return outputClasses;
	}



	//==DATA MEMBERS
	/**
	 * Array of Feature objects, that represents the feature set of the classifier
	 */
	protected Feature[] featureSet;


	/**
	 * Array of OutputClass objects, that represents the possible output classes of the problem
	 */
	protected OutputClass[] outputClasses;	

//	==Data members

	/**number of classes*/
	protected int numClasses;

	/**number of categories for categorical features and number of bins for numerical, then discretized, features . Each position refers to
	 * a different feature*/
	protected Integer[] numValues;
	
	/**number of features*/
	protected int numFeatures;

//	the following variables store prior counts to be added to the empirical frequencies.
//	the counts are designed to implement a uniform prior.
//	note that all prior counts variables are named pcType.
	protected int trainInstances;

	/**
	 * Probabilities estimated for each class, for each instance
	 */
	protected double[][] probabilities;


//	the following variables store prior counts to be added to the empirical frequencies.
//	the counts are designed to implement a uniform prior.
//	note that all prior counts variables are named pcType.
//	this is 1/nc (nc=number of classes)
	
	/**prior counts for classes*/
	protected double pcClass; 

	/**prior counts for conditional frequencies*/
	protected double[] pcCond;

	/**prior counts for unconditional frequencies*/
	protected double[] pcUncond;




	//==NESTED CLASS Feature
	/**
	 * Helper class for Naive Classifiers, that implements Mar and NonMar features.
	 * Features are characterized by the bivariate counts of their effective occurrences 
	 * (Frequencies), by the number of missing data for each output class (Missing) and
	 * by  the logarithm of conditioned probabilities (LogProbability)
	 */
	protected  class Feature {

		/**Constructor that copies the name and the conditionalFreq table, and computes the log-probabilities table
		 * 		 */
		Feature (String SuppliedName, double[][] SuppliedFrequencies, int[] SuppliedMissing) 
		{
			name=SuppliedName;
			conditionalFreq=SuppliedFrequencies.clone();
			missing=SuppliedMissing.clone();
			uncondFrequencies=new double[conditionalFreq[0].length];

			int i,j;
			for (i=0; i<conditionalFreq[0].length; i++){
				uncondFrequencies[i]=0;
				for (j=0; j<conditionalFreq.length;j++)
					uncondFrequencies[i]+=conditionalFreq[j][i];
			}

			//calculate log of probabilities
			logProbability=new double[conditionalFreq.length][conditionalFreq[1].length];
			double  currentCountSum;


			for (i=0; i<conditionalFreq.length; i++)
			{
				currentCountSum=ArrayUtils.arraySum(conditionalFreq[i])[0];
				for (j=0; j<conditionalFreq[1].length; j++)
				{
					logProbability[i][j]=Math.log((double)conditionalFreq[i][j]/currentCountSum);
				}
			}
		}



		/**Counts that correspond to counts-after-dropping-missing for MarFeatures,
		 * bivariate count: frequency are computed for each output class and for each class of the
		 * feature. They are double to manage possible partial units due to the prior. The rows refer to the different output classes, and the columns to the 
		 * different feature classes.
		 */
		private final double[][] conditionalFreq;

		/**Simple uncondFrequencies, not conditioned. Useful to computed Bma*/
		private final double[] uncondFrequencies;

		/**Logarithm of conditioned probabilities: Log(P(ai|c)) 
		 */
		private final double[][] logProbability;

		/**How many times the feature is missing, for every output class.
		 */
		private final int[] missing;

		/**Name
		 * 
		 */
		private final String name;

		/**
		 * @return Returns the conditionalFreq.
		 */
		public double[][] getConditionalFreq() {
			return conditionalFreq;
		}

		/**
		 * @return Returns the conditionalFreq for a specified class
		 */
		public double[] getCondFrequencies(int ClassIdx) {
			return conditionalFreq[ClassIdx];
		}


		/**
		 * @return Returns the conditionalFreq for a specified class
		 */
		public double[] getUncondFrequencies() {
			return uncondFrequencies;
		}


		/**
		 * @return Returns the conditionalFreq for a specified class, computed as
		 *counts of those records where the class is the one required and the value of the given MAR feature
		 *is not missing
		 */
		double getClassCountAsMar(int ClassIdx) 
		{
			double[] tmpArr= ArrayUtils.arraySum(conditionalFreq[ClassIdx]);
			return tmpArr[0];
		}


		/**
		 * @return Returns the conditionalFreq for a specified class, and for 
		 * a specified class (range of values) defined within the feature domain
		 */
		double  getConditionalFrequencies(int ClassIdx, int FeatureClassIdx) {
			return conditionalFreq[ClassIdx][FeatureClassIdx];
		}




		/**
		 * @return Returns the log of cond probabilities for a specified class
		 */
		double[] getLogProbability(int ClassIdx) {
			return logProbability[ClassIdx];
		}

		/**
		 * @return Returns the log of cond probabilities for a specified class and
		 * for a specific value of the feature
		 */
		double getLogProbability(int ClassIdx, int FeatureValue) {
			return logProbability[ClassIdx][FeatureValue];
		}

		/**
		 * @return Returns the whole log of cond probability table 
		 */
		double[][] getLogProbability() {
			return logProbability;
		}

		/**
		 * @return Returns the missing.
		 */
		int[] getMissing() {
			return missing;
		}

		/**
		 * @return Returns the number of missing data
		 * for a given output class
		 */
		int getMissing(int OutputClass) {
			return missing[OutputClass];
		}

		/**
		 * @return Returns the name.
		 */
		String getName() {
			return name;
		}

	}
	//==END NESTED CLASS Feature



	//==NESTED CLASS: OutputClass
	/**
	 * Helper class for Naive Classifiers, that implements the output class of the classification problem.
	 */
	protected class OutputClass{


		//constructor 
		OutputClass (String SuppliedClassName, double SuppliedClassFrequency, double suppliedLogProbability) 
		{
			name=SuppliedClassName;
			frequency=SuppliedClassFrequency;
			logProbability=suppliedLogProbability;
		}

		double getFrequency() {
			return frequency;
		}
		String getName() {
			return name;
		}

		double getLogProbability()
		{
			return logProbability;
		}

		double frequency;

		/**Stores log-probability of each cless
		 */
		double logProbability;


		/**names of the output classes*/
		String name;

	}

}


/**
 * Implements the Naive Bayes Classifier (NBC) with Laplace prior
 */

class NaiveBayes extends NaiveClassifier{


	/**
	 * Initializes all features and output classes; trains the classifier on TrainingSet.
	 */
	NaiveBayes(ArrayList<int[]> trainingSet, ArrayList<String> featureNames, ArrayList<String>classNames, ArrayList<Integer> numClassForEachFeature){
		//the classical NBC is implemented, with Laplace prior as done in WEKA
		super(trainingSet, featureNames, classNames, numClassForEachFeature,1);
	}


	/**Classifies all the instances of the supplied TestingSet, writing the results of the computation into
	 * EstimatedProbabilities and PredictedInstances
	 */
	void classifyInstances(ArrayList<int[]> TestingSet)
	{
		predictions= new int[TestingSet.size()];
		probabilities=new double[TestingSet.size()][numClasses]; 
		int i;

		for (i=0; i<TestingSet.size(); i++)
			classifyInstance(TestingSet.get(i), i);
	}


	
	/**
	 * Classify a single instance, writing the computed probabilities at position InstanceIdx
	 * of probabilities, and the predicted class at position InstanceIdx of predictions
	 */
	private void classifyInstance (int[] suppliedInstance, int  instanceIdx)
	{
		int i,j; 
		//double logProb;
		double[] logProbArray=new double[numClasses];
		double maxLogProb=-Double.MAX_VALUE;
		int maxProbIdx=-1;

		//probabilities of each class
		for (i=0; i<numClasses; i++)
		{

			logProbArray[i]=0;

			//get a priori log-probality
			logProbArray[i]+=outputClasses[i].getLogProbability();

			//add conditioned log-probability
			for (j=0; j<numFeatures; j++)
			{
				//marginalization for missing data
				if (suppliedInstance[j] == -9999){
					continue;
				}

				logProbArray[i]+=featureSet[j].getLogProbability(i,suppliedInstance[j]);

			}//==Log probability computed


			if (logProbArray[i]>maxLogProb)
			{
				maxLogProb=logProbArray[i];
				maxProbIdx=i;
			}
		}
		predictions[instanceIdx] = maxProbIdx;

		//****compute the probabilities in a numerical robust way
		 
		 //first, compute shift
		 double shift=logProbArray[maxProbIdx];
		 double sumProb=0;
		 double[] tmpArr = new double [numClasses];
			
		 for (i=0; i<numClasses; i++)	{
			 tmpArr[i]=Math.exp(logProbArray[i]-shift);
			 sumProb+=tmpArr[i];
		 }
			 
		 for (i=0; i<numClasses; i++)	
				probabilities[instanceIdx][i]=tmpArr[i]/sumProb;
		 
	}//==END classify instance
	
	int[] getPredictions() {
		return predictions;
	}	

	public double[][] getProbabilities() {
		return probabilities;
	}

	//DATA MEMBERS
	/**
	 * Index of the class predicted for each instance
	 */
	private int[] predictions;
}