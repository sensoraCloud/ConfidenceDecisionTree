/**
 * NaiveCredalClassifier2.java
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
 * JNCC is free software; you can redistribute it and/or modify it under the 
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

/**Implementation  of the Naive Credal Classifier 2 (NCC2).<br>
 * The constructor build the object and learns the classifier.<br>
 * The function classifyInstances() use the learned NCC2 to classify the supplied instances,
 * while the issued predictions can be obtained via the function getPredictions().
 * <p>
 */

class NaiveCredalClassifier2 extends NaiveClassifier{


	/**Builds feature and output class, and computes the relevant counts for MAR and NON-MAR features
	 */
	NaiveCredalClassifier2(ArrayList<int[]> TrainingSet, ArrayList<String> FeatureNames, ArrayList<String> classNames,
			ArrayList<Integer> numClassForEachFeature, ArrayList<Integer> SuppliedNonMarInTraining, ArrayList<Integer> SuppliedNonMarInTesting,
			ArrayList<Integer> SuppliedNumClassesNonMarTesting){


		//last parameter, which we set to 0, is the number of Lapalce additional counts (we set it to 1 in case of naive Bayes)
		super(TrainingSet, FeatureNames, classNames, numClassForEachFeature,0);

		s=1;
		nonMarTestingIdx=SuppliedNonMarInTesting;
		nonMarTrainingIdx=SuppliedNonMarInTraining;
		numClassesNonMarTesting=SuppliedNumClassesNonMarTesting;
	}

	/**
	 * Classify all the instances of the supplied TestingSet; stores the predictions into
	 * CredalPredictedInstances
	 */
	void classifyInstances(ArrayList<int[]> TestingSet)
	{

		predictions= new int[TestingSet.size()][];

		//should be already sorted, just for the sake fo safety
		Collections.sort(nonMarTrainingIdx);
		Collections.sort(nonMarTestingIdx);

		int i;

		int[] instance;

		for (i=0; i<TestingSet.size(); i++){
			instance=TestingSet.get(i).clone();
			predictions[i]= classifyInstance(instance);
		}
	}  







	/** Classifies a single instance, returning the list of predicted classes  
	 */
	private int[] classifyInstance (int[] CurrentInstance) {


		int i,j;
		ArrayList<Integer> MissingNonMarIdx=new ArrayList<Integer>(nonMarTestingIdx.size());


		//detect missing non mar.
		for (i=0; i<numFeatures; i++)
		{
			if (CurrentInstance[i] != -9999){
				continue;
				//missing variable is Mar on testing
			}
			if (nonMarTestingIdx.indexOf(i)<0){
				continue;
			}
			MissingNonMarIdx.add(i);
		}

		ArrayList<Integer> UndominatedClasses = new ArrayList<Integer>(numClasses);

		for (i=0; i<numClasses; i++)
			UndominatedClasses.add(i);

		for (i=0; i<numClasses; i++)
		{
			//if class i is already dominated, no need to check it
			if (! UndominatedClasses.contains(i)){
				continue;
			}


			for (j=0; j<numClasses; j++)
			{
				if (i==j) {
					continue;
				}

				if (! UndominatedClasses.contains(j)){
					continue;
				}

				double Test;

				if  (MissingNonMarIdx.isEmpty())
				{
					Test=checkCredalDominanceCIR(i, j, CurrentInstance,0,s);

					if (Test > 1)
						UndominatedClasses.remove(UndominatedClasses.indexOf(j));
				}//END ==if  (MissingNonMarIdx.isEmpty())


				// we have some missing NonMar data
				else{
					boolean ZeroFound=findPartitionPoints (i, j, MissingNonMarIdx);
					if (ZeroFound)
					{
						Test=0;
						continue;
					}


					double nextCrossingPoint;
					//RUN THE TEST LOCALLY ON EACH FOUND PARTITION
					for (int kk=0; kk<partitionPoints.size(); kk++)
					{
						for (Integer tmpInt : MissingNonMarIdx)
							CurrentInstance[tmpInt]=partitionPoints.get(kk).getMinimizingTupleLeft()[MissingNonMarIdx.indexOf(tmpInt)];

						//the local minimizing tuple is known and put into the CurrentInstance

						if (kk==0)
						{
							//this manages the very case in which no PartitionPoints have been found at all. In this case,
							//crossingX=-9999
							if (partitionPoints.get(0).getCrossingX() == -9999)
							{
								Test=checkCredalDominanceCIR(i, j, CurrentInstance,0,s);
								if (Test>1) {
									UndominatedClasses.remove(UndominatedClasses.indexOf(j));
								}
								break;
							}
							else
							{
								Test=checkCredalDominanceCIR(i, j, CurrentInstance,0,partitionPoints.get(kk).getCrossingX());
								if (Test<1) 
									break; 
							}
						} //end== if (kk==0)

						else
						{	
							nextCrossingPoint=partitionPoints.get(kk+1).getCrossingX();
							Test=checkCredalDominanceCIR(i, j, CurrentInstance,partitionPoints.get(kk).getCrossingX(),
									nextCrossingPoint);
							if (Test<1) 
								break;
						}

						//in case we are at the end of the ArrayList, we should run the test also in [LastPoint, s]
						if (kk==partitionPoints.size()-1)
						{
							Test=checkCredalDominanceCIR(i, j, CurrentInstance,partitionPoints.get(kk).getCrossingX(),s);
							//if we're here, all the previous tests have been bigger than 1
							if (Test>1) 
								UndominatedClasses.remove(UndominatedClasses.indexOf(j));
						}

					}//==for (int kk=0; kk<PartitionPoints.size(); kk++)
				}//END == else : we  have missing data	
			}
		}
		return ArrayUtils.arrList2Array(UndominatedClasses); 
	}


	/**Given a sub-partion (xmin,xmax) of[0,s], returns the value of feature FeatureIdx, which minimizes 
	 * the ratio (lowercount(feature,c1)/(uppercount(feature,c2)+x)) in the interval.
	 */
	private int findMinimizingValue(int FeatureIdx, int NumValues, int c1, int c2, double xmin, double xmax){

		double evalPoint=(xmin+xmax)/2;
		double CurrentMin=Double.MAX_VALUE;
		int MinimizingClass=-9999;
		double tmp;

		for (int jj=0; jj<NumValues; jj++)
		{
			tmp=super.featureSet[FeatureIdx].getConditionalFrequencies(c1,jj)/(super.featureSet[FeatureIdx].getConditionalFrequencies(c2,jj)+
					super.featureSet[FeatureIdx].getMissing(c2) + evalPoint);
			if (tmp<CurrentMin)
			{
				CurrentMin=tmp;
				MinimizingClass=jj;
				if (CurrentMin ==0)
					break;
			}
		}//end == for (jj=0; jj<NumValues; jj++)
		return MinimizingClass;
	}



	/**If there are missing data in the NonMar part of the units to be classified, this function
	 * identifies the intervals in which the range [0,s] has to be sub-partitioned.
	 * Later, the test of dominance has to performed separately on each sub-partition. 
	 */
	private boolean findPartitionPoints (int c1, int c2, ArrayList<Integer> MissingNonMarIdx)
	{

		int i,j,kk;
		double tmp1, tmp2, tmp3;
		double crossingx;


		//values for missing NonMar features whose minimizing value doesn't change over [0,s]
		ArrayList<Integer> ConstantMinimizingValues = new ArrayList<Integer> (MissingNonMarIdx.size());
		partitionPoints=new ArrayList <PartitionPoint>();


		//==DETECT THE PARTITION POINTS VARIABLE BY VARIABLE
		for (i=0; i<MissingNonMarIdx.size(); i++)
		{
			ArrayList<Double> CrossingPoints = new ArrayList<Double>();
			//get the number of possible classes for the current variable
			int idx =nonMarTestingIdx.indexOf(MissingNonMarIdx.get(i));
			int NumValues=numClassesNonMarTesting.get(idx);


			//in some cases, datasets discretized outside jncc might contain features with a unique class
			if (NumValues>1)
			{
				idx=MissingNonMarIdx.get(i);
				//==compare class j against class k
				for (j=0; j<NumValues; j++)
				{

					for (kk=j+1; j<NumValues; j++)
					{

						tmp2 = super.featureSet[idx].getConditionalFrequencies(c1,j);

						//if there is a value of the missing NonMar feature which has conditional count=0 with respect to class c1, 
						//this value minimizes the function. No further computation is required, we can safely conclude that c1 cannot dominate
						//c2
						if (tmp2==0)
						{
							boolean FoundZero=true;
							return FoundZero;
						}

						tmp2 *=super.featureSet[idx].getConditionalFrequencies(c2,kk)+ super.featureSet[idx].getMissing(c2);
						tmp1=super.featureSet[idx].getConditionalFrequencies(c2,j)+ super.featureSet[idx].getMissing(c2);
						tmp1 *= super.featureSet[idx].getConditionalFrequencies(c1,kk);
						tmp3=super.featureSet[idx].getConditionalFrequencies(c1,j) - featureSet[idx].getConditionalFrequencies(c1,kk);

						//if the two classes have the same conditionalFreq, we cannot define any partition point.
						if (tmp3!=0) 
						{crossingx = (tmp1-tmp2)/tmp3;
						if ((crossingx > 0) &  (crossingx<s))
							CrossingPoints.add((double)(tmp1-tmp2)/tmp3);
						}
					}
				}

				Collections.sort(CrossingPoints);
				//=AT THIS POINT, ALL THE CROSSING POINTS HAVE BEEN FOUND AND ARE 
				//SORTED WITHIN CROSSINGPOINTS
			}

			//if there are no crossing points, let's find the minizing value which is constant over [0,s]
			if (CrossingPoints.isEmpty())
			{
				ConstantMinimizingValues.add(i,
						findMinimizingValue(idx, NumValues, c1, c2, 0, s));
			}

			else
				//LET'S FIND THE MINIZING VALUE REGION-BY-REGION
			{
				//we have to manage the case when all the crossing points do not involve the lower envelope
				boolean NoCrossingPointsInLowerEnvelope=true;

				int[] minimizingValues = new int [CrossingPoints.size()+1];
				minimizingValues[0]=findMinimizingValue(idx, NumValues, c1, c2, 0, CrossingPoints.get(0));
				int numCrossingPoints=CrossingPoints.size();

				for (kk=0; kk<numCrossingPoints-1; kk++)
				{
					minimizingValues[kk+1]=findMinimizingValue(idx, NumValues, c1, c2, CrossingPoints.get(kk), CrossingPoints.get(kk+1));
					if (minimizingValues[kk+1] != minimizingValues[kk])
						NoCrossingPointsInLowerEnvelope=false;
				}

				minimizingValues[numCrossingPoints]=findMinimizingValue(idx, NumValues, c1, c2, CrossingPoints.get(numCrossingPoints-1),s);
				if (minimizingValues[numCrossingPoints] != minimizingValues[numCrossingPoints-1])
					NoCrossingPointsInLowerEnvelope=false;

				if (NoCrossingPointsInLowerEnvelope){
					ConstantMinimizingValues.add(i,minimizingValues[0]);
				}

				else //allocate a partition point for each CrossingPoint involving the lower envelope
				{

					for (kk=0; kk<minimizingValues.length-1; kk++)
					{
						if  (minimizingValues[kk+1] != minimizingValues[kk])
						{
							int[] minimizingTupleLeft=new int[MissingNonMarIdx.size()];
							int[] minimizingTupleRight=new int[MissingNonMarIdx.size()];
							Arrays.fill(minimizingTupleLeft,-9999);
							Arrays.fill(minimizingTupleRight,-9999);
							minimizingTupleLeft[i]=minimizingValues[kk];
							minimizingTupleRight[i]=minimizingValues[kk+1];
							PartitionPoint novelPartitionPoint= new PartitionPoint(CrossingPoints.get(kk),minimizingTupleLeft, minimizingTupleRight);
							partitionPoints.add(novelPartitionPoint);
						}
					}					
				}//end else ==allocate a partition point for each CrossingPoint involving the lower envelope
			}//end else (finding the minimizing value region by region)
		}//end for (i=0; i<MissingNonMarIdx.size(); i++) 




		if (partitionPoints.isEmpty())
		{
			PartitionPoint NewPoint = new PartitionPoint(-9999,ArrayUtils.arrList2Array(ConstantMinimizingValues), 
					ArrayUtils.arrList2Array(ConstantMinimizingValues));
			partitionPoints.add(NewPoint);
			boolean FoundZero=false;
			return FoundZero;
		}

		//sort the partition Points according to the value of x
		Collections.sort(partitionPoints);

		//now, we have to fill the minizing tuple of each Partition Point

		//first, take values from ConstantMinimizingValues
		if (!ConstantMinimizingValues.isEmpty())
		{
			for (kk=0; kk<ConstantMinimizingValues.size(); kk++)
			{
				if (ConstantMinimizingValues.get(kk) != -9999)
					for (PartitionPoint CurrentPoint : partitionPoints)
					{
						CurrentPoint.setMinimizingTupleLeft (ConstantMinimizingValues.get(kk), 
								kk);
						CurrentPoint.setMinimizingTupleRight (ConstantMinimizingValues.get(kk),
								kk);
					}
			}
		}

		//now, fill the empty values in the PartitionPoints

		int[][] MinimizingTuplesMatrix = new int [2*partitionPoints.size()][MissingNonMarIdx.size()]  ; 
		int counter=0;

		for (kk=0; kk<partitionPoints.size(); kk++)
		{
			MinimizingTuplesMatrix[counter++]=partitionPoints.get(kk).getMinimizingTupleLeft();
			MinimizingTuplesMatrix[counter++]=partitionPoints.get(kk).getMinimizingTupleRight();
		}		

		//== Fill all the missing values in the minizing tuples
		for (i=0; i<MissingNonMarIdx.size(); i++)
		{
			int position=0;
			int currentValue=-9999;
			boolean firstFound = false;

			while (position<2*partitionPoints.size())
			{
				if (MinimizingTuplesMatrix[position][i] == -9999)
				{
					if (firstFound){
						MinimizingTuplesMatrix[kk][i]=currentValue;
					}
					else {
						continue;
					}
				}


				else //value is missing
				{
					if (firstFound){
						MinimizingTuplesMatrix[kk][i]=currentValue;
					}
					else {
						continue;
					}
				}

				position++;	
			}//end ==while (position<2*PartitionPoints.size())
		}//end ==for (i=0; i<MissingNonMarIdx.size(); i++)


		//==Copy the built values for minimizing tuples within the actual PartitionPoints.
		counter=0;
		for (PartitionPoint CurrPartPoint : partitionPoints)
		{
			CurrPartPoint.setMinimizingTupleLeft(MinimizingTuplesMatrix[counter++]);
			CurrPartPoint.setMinimizingTupleRight(MinimizingTuplesMatrix[counter++]);
		}

		//AT THIS POINTS, ALL PARTITION POINTS ARE FULLY BUILT
		boolean FoundZero=false;
		return FoundZero;
	}//end findPartitionPoints







	/**
	 * Computes the CIR test of dominance between class c1 and c2 (if the returned value is >1, c1 dominates c2)
	 */
	private double checkCredalDominanceCIR(int c1, int c2, int[] currentInstance, double xmin, double xmax){

		alphaArr=new ArrayList<Integer>(numFeatures);
		betaArr=new ArrayList<Integer>(numFeatures);		
		deltaTildeArr=new ArrayList<Integer>(numFeatures);
		gammaTildeArr=new ArrayList<Integer>(numFeatures);
		deltaArr=new ArrayList<Integer>(numFeatures);
		gammaArr=new ArrayList<Integer>(numFeatures);

		int i;
		ArrayList<Integer> MissingMarIdx=new ArrayList<Integer>(numFeatures-nonMarTestingIdx.size());

		//remember that frequencies of the super class have been computed without any prior additional count, so these casts
		//do not involve any information loss
		alpha=(int)super.getOutputClasses()[c1].getFrequency();
		beta=(int)super.getOutputClasses()[c2].getFrequency();


		//get the conditional counts 
		for (i=0; i<numFeatures; i++)
		{	
			//missing variables are marginalized
			if (currentInstance[i]==-9999)
			{
				MissingMarIdx.add(i);
				continue;
			}

			//NonMar features
			if (nonMarTrainingIdx.indexOf(i)>=0)
			{
				//empirical frequencies of superclass have been built without adding any prior, so they ARE integer and the following casts do not
				//involve any information loss
				alphaArr.add((int)super.featureSet[i].getConditionalFrequencies(c1,currentInstance[i]));
				betaArr.add((int)super.featureSet[i].getConditionalFrequencies(c2,currentInstance[i]) + super.featureSet[i].getMissing(c2));
			}

			//MAR features
			else 
			{
				gammaArr.add((int)super.featureSet[i].getConditionalFrequencies(c1,currentInstance[i]));
				deltaArr.add((int)super.featureSet[i].getConditionalFrequencies(c2,currentInstance[i]));
				gammaTildeArr.add((int)super.featureSet[i].getClassCountAsMar(c1));				
				deltaTildeArr.add((int)super.featureSet[i].getClassCountAsMar(c2));
			}
		}


		//note that NonMar features are always non missing at this stage, because occurring missingness are managed
		//via replacement of all possible values.
		k=alphaArr.size();




		double InfHx=-999;
		//init necessary to compile the code
		double DerivLnH0=0;
		double DerivLnHs;
		double minimizingX;


		//if there is j such that n(aM j , cM ) = 0 or l such that n(ˆM l , cM ) = 0, inf h(t(cM )) = 0;
		if (  (alphaArr.indexOf(0)>=0) | (gammaArr.indexOf(0)>=0) ) 			
			return InfHx=0;


		//if k = 0 and r = 0, inf h(t(cM )) = h(xmax);
		if ( (k==0) & (MissingMarIdx.size()==gammaArr.size()) )
			return InfHx=computeHxCIR(xmax);

		//from here on, the function can be assumed to be convex


		if ( (beta==0) | (betaArr.indexOf(0)>=0) ) 
			DerivLnH0=Double.NEGATIVE_INFINITY;

		for (i=0; i<deltaArr.size(); i++)
		{
			if ((deltaArr.get(i)==0) & (deltaTildeArr.get(i)>0))
			{
				DerivLnH0=Double.NEGATIVE_INFINITY;
				break;
			}
		}

		if (!(DerivLnH0==Double.NEGATIVE_INFINITY))
			DerivLnH0=computeDerivLnHxCIR (xmin);


		DerivLnHs=computeDerivLnHxCIR (xmax);

		if (DerivLnH0>=0)
			InfHx=computeHxCIR(xmin);

		else if (DerivLnHs<=0)
			InfHx=computeHxCIR(xmax);

		else if ((DerivLnH0<=0) & (DerivLnHs>=0) )
		{
			//numerical approximation
			minimizingX=findZeroCIR(xmin,xmax);
			InfHx=computeHxCIR(minimizingX);
		}
		return InfHx;
	}

	/**Numerical approximation of the min of Ln(Hx) via Newton-Raphson method.
	 */
	private double findZeroCIR (double x1, double x2)
	{
		double eps=Math.pow(10,-7);
		int j,maxiter=200;
		double df,dx,dxold,f,fl;
		double temp,xh,xl,rts;

		fl=computeDerivLnHxCIR(x1);

		//Orient the search so that f(xl) < 0.
		if (fl < 0.0)
		{	
			xl=x1;
			xh=x2;
		} 
		else 
		{
			xh=x1;
			xl=x2;
		}


		rts=0.5*(x1+x2); //Initialize the guess for root,
		dxold=Math.abs(x2-x1); //the stepsize ,
		dx=dxold; //and the last step.


		f=computeDerivLnHxCIR(rts);
		df=computeDeriv2LnHxCIR (rts);

		for (j=1;j<=maxiter;j++) 

		{
			if ((((rts-xh)*df-f)*((rts-xl)*df-f) > 0.0) // Bisect if Newton out of range,
					|| (Math.abs(2.0*f) > Math.abs(dxold*df))) // or not decreasing fast enough.
			{
				dxold=dx;
				dx=0.5*(xh-xl);
				rts=xl+dx;
				if (xl == rts) return rts; //Change in root is negligible.
			} 
			//Newton step acceptable. Take it.
			else { 
				dxold=dx;
				dx=f/df;
				temp=rts;
				rts -= dx;
				if (temp == rts) return rts;
			}
			if (Math.abs(dx) < eps) return rts; //Convergence criterion.

			f=computeDerivLnHxCIR(rts);
			df=computeDeriv2LnHxCIR (rts);

			if (f < 0.0) //Maintain the bracket on the root.
				xl=rts;
			else
				xh=rts;
		}
		System.out.println("Maximum number of iterations exceeded in rtsafe");
		return rts; //Never get here.
	}


	/**Computes  Hx for a given value of x, alpha, beta ecc. (see Corani and Zaffalon, 2007)
	 */
	private double computeHxCIR(double x)

	{
		double value;		
		double tmp=0;

		tmp= (beta+x)/(alpha+s-x);
		tmp=Math.pow(tmp,(k-1));

		int i;
		double MarProduct=0, NonMarProduct=0;

		//==NON MAR COMPUTATION
		//computation done on logarithms to avoid overflows
		for (i=0; i<alphaArr.size(); i++)
			NonMarProduct += Math.log(alphaArr.get(i)/(betaArr.get(i)+x));  
		//exponential to return to the true computation
		NonMarProduct=Math.exp(NonMarProduct);


		//==MAR COMPUTATION
		//computation done on logarithms to avoid overflows
		for (i=0; i<deltaTildeArr.size(); i++)
		{
			MarProduct += Math.log( (deltaTildeArr.get(i) + x)/(gammaTildeArr.get(i)+s-x));  
			MarProduct += Math.log( (gammaArr.get(i))/(deltaArr.get(i)+x));
		}

		//exponential to return to the true computation
		MarProduct=Math.exp(MarProduct);

		value=tmp*NonMarProduct*MarProduct;
		return value;
	}


	/**Computes the derivative of Ln(Hx) (see Corani and Zaffalon, 2007)
	 */

	private double computeDerivLnHxCIR (double x)

	{


		//if X==0, let's first do some preliminary checks. It might be necessary
		//setting manually the derivative to -Inf to avoid numerical issues with zeros

		if (x==0)
		{

			if ( (beta==0) | (betaArr.indexOf(0)>=0) ) 
				return Double.NEGATIVE_INFINITY;

			for (int i=0; i<deltaArr.size(); i++)
			{
				if ((deltaArr.get(i)==0) & (deltaTildeArr.get(i)>0))
					return Double.NEGATIVE_INFINITY;
			}
		}


		double value;		
		value= (k-1)/(beta+x);
		value += (k-1)/(alpha+s-x);

		int i;

		for (i=0; i<k; i++)
			value -= 1/(betaArr.get(i)+x);

		for (i=0; i<deltaArr.size(); i++)
			value += 1/(deltaTildeArr.get(i)+x);

		for (i=0; i<deltaArr.size(); i++)
			value -= 1/(deltaArr.get(i)+x);

		for (i=0; i<deltaArr.size(); i++)
			value += 1/(gammaTildeArr.get(i)+s-x);

		return value;
	}


	/**Computes the second derivative of Ln(Hx) (see Corani and Zaffalon, 2007) 
	 */
	private double computeDeriv2LnHxCIR (double x)
	{
		double value;		
		int i;


		value = - (k-1)/(Math.pow(beta+x,2));		
		value +=(k-1)/Math.pow((alpha+s-x),2);

		for (i=0; i<k; i++)
			value += 1/Math.pow(betaArr.get(i)+x,2);

		for (i=0; i<gammaArr.size(); i++)
			value -= 1/Math.pow(deltaTildeArr.get(i)+x,2);

		for (i=0; i<gammaArr.size(); i++)
			value += 1/Math.pow(deltaArr.get(i)+x,2);

		for (i=0; i<gammaArr.size(); i++)
			value += 1/Math.pow(gammaTildeArr.get(i)+s-x,2);

		return value;
	}



	/**
	 * Returns the matrix of the predictions
	 */
	int[][] getPredictions()
	{
		return predictions;
	}


	/**
	 * Returns the vector, which contains the prediction for instance in position idx
	 */
	int[] getPredictions(int idx)
	{
		return predictions[idx];
	}

	/**overall occurrences of class c1*/ 
	private int alpha;
	
	/**overall occurrences of class c2*/ 
	private int beta;
	
	/**alphaArr is defined for NonMar features only; it contains the conditional count after having dropped missing data*/
	private ArrayList<Integer> alphaArr;
	
	/**BetaArr is defined for NonMar features only; it contains conditional count  to which the number of missing records for the given feature is added*/
	private ArrayList<Integer> betaArr;
	
	/**gamma array is defined for Mar features only; it contains conditional count with respect to class c1 after having dropped missing data*/
	private ArrayList<Integer> gammaArr;
	
	/**delta array is defined for Mar features only; it contains conditional count with respect to class c2 after having dropped missing data*/
	private ArrayList<Integer> deltaArr;
	
	/**Sum of occurrences of class c1, considering only those instances of the learning set where
	the NonMar feature is non missing. A different value for every feature.*/
	private ArrayList<Integer> gammaTildeArr;
	
	/**Sum of occurrences of class c2, considering only those instances of the learning set where
	the NonMar feature is non missing. A different value for every feature.*/
	private ArrayList<Integer> deltaTildeArr;


	/**Number of NonMar features in training*/
	private int k;


	/**Indexes of nonMarFeature in training 
	 */
	private ArrayList<Integer> nonMarTrainingIdx; 

	/**Indexes of nonMarFeature in testing 
	 */
	private ArrayList<Integer> nonMarTestingIdx; 

	/**
	 * Number of classes of each NonMar variable in Testing
	 */
	private ArrayList<Integer> numClassesNonMarTesting;


	/**Stores NCC predictions; as every prediction can be imprecise and hence contain several
	 * value, it is implemented as a matrix. 
	 * */
	private int[][] predictions;

	/**Partition Points, used when classyfing instances with missing units in the NonMar part.
	 * 
	 */
	private ArrayList<PartitionPoint> partitionPoints;

	/**number of "hidden" observations, that rules the weights of the prior with respect to the likelihood.
	 * It is a safe choice to set it as 1 or 2.
	 */
	private int s;


	/**
	 *Helper class for NaiveCredal Classifier, used to store crossing points and minimizing tuples; it is used 
	 *to deal with missing data in the NonMar part of the testing instances.
	 */
	private static class PartitionPoint  implements Comparable  {


		protected PartitionPoint(double suppliedCrossingX, int[] minimizingTupleLeft, int[] minimizingTupleRight) {
			crossingX = suppliedCrossingX;
			minTupleLeft = minimizingTupleLeft.clone();
			minTupleRight = minimizingTupleRight.clone();
		}


		//Constructor used whenever the partition points is external to [0,s]
		protected  PartitionPoint(double suppliedCrossingX, int NumMissingNonMar) {
			crossingX = suppliedCrossingX;	
			minTupleLeft = new int[NumMissingNonMar];
			minTupleRight = new int[NumMissingNonMar];
			Arrays.fill (minTupleLeft, -9999);
			Arrays.fill (minTupleRight, -9999);
		}



		/**
		 * Value of the crossing point
		 */
		private double crossingX;

		/**
		 * Tuple (i.e., realization of NonMar variables missing in the istance to classify), that minimize the objective 
		 * function for values lower than crossingX
		 */
		private int[] minTupleLeft;

		/**
		 * Tuple (i.e., realization of NonMar variables missing in the istance to classify), that minimize the objective 
		 * function for values higher than crossingX
		 */
		private int[] minTupleRight;


		//getter and setters
		protected double getCrossingX() {
			return crossingX;
		}
		protected void setCrossingX(double crossingX) {
			this.crossingX = crossingX;
		}
		protected int[] getMinimizingTupleLeft() {
			return minTupleLeft;
		}
		protected void setMinimizingTupleLeft(int[] minimizingTupleLeft) {
			minTupleLeft = minimizingTupleLeft.clone();
		}
		protected int[] getMinimizingTupleRight() {
			return minTupleRight;
		}
		protected void setMinimizingTupleRight(int[] minimizingTupleRight) {
			minTupleRight = minimizingTupleRight.clone();
		}

		protected void setMinimizingTupleRight(int value, int idx) {
			minTupleRight[idx] = value;
		}

		protected void setMinimizingTupleLeft(int value, int idx) {
			minTupleLeft[idx] = value;
		}

		public int compareTo(Object SecondPartitionPoint) throws ClassCastException {
			if (!(SecondPartitionPoint instanceof PartitionPoint))
				throw new ClassCastException("PartitionPoint object expected.");
			double difference = this.crossingX - ((PartitionPoint) SecondPartitionPoint).getCrossingX(); 
			if (difference > 0 )
				return 1;
			else return -1;
		}

	}

}