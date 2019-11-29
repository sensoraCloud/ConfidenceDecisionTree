/**
 * jncc.java
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.StringTokenizer;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;




/**
 *Main class of the project, which loads the data set from file and then trains and validates the classifiers.
 *It loads data from the file specified by the user; then,
 *it trains and validates NBC and NCC according to the validation method specified by the user.
 *Jncc implementes three validation methods:
 * 1) 10 runs of <em>stratified</em> 10-folds cross-validation; 
 * 2) validation via testing file (single training/testing experiment)
 * 3)testing file with unknown classes.
 *In the first two cases, accuracy stats are reported to file (via ResultsReporter objects),
 *as the true classes are known.
 *In the last case, NCC predictions only are reported to file, as the true classes are unknown.
 *Numerical features are discretized via MDL-entropy-based supervised discretization (using MdlDiscretizer 
 *objects. Note that discretization intervals
 *are computed on the training set, and then applied unchanged on the testing set.
 */

public class Jncc {

	/**
	 * Initializes the necessary data members, scans the main Arff file and then
	 * instantiates the data members FeatureNames, 
	 * NumFlags, CategoryNames and rawDataset.
	 */
	Jncc(String UserSuppliedWorkingPath,
			String UserSuppliedArffName, String UserSuppliedValidationName, int numArgs) {

		if (!UserSuppliedArffName.endsWith(".arff")) {
			System.out.println("Data file " + UserSuppliedArffName
					+ " has different extension from the expected" + ".arff");
			System.exit(0);
		}


		try
		{
			File WorkingPathFile;
			WorkingPathFile = new File (UserSuppliedWorkingPath);
			workPath=WorkingPathFile.getCanonicalPath();

			if (!(WorkingPathFile.getCanonicalPath().endsWith(System.getProperty("file.separator"))))
				workPath +=System.getProperty("file.separator");

		}
		catch (IOException e)
		{
			System.err.println(e);
		}


		//time tracking (computation starts from here)
		mxbean= (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean() ;
		startTime=mxbean.getProcessCpuTime();


		if (UserSuppliedValidationName.equalsIgnoreCase("cv")) {
			//setting for 10-folds-cross-validation
			numCvFolds = 10;
			numCvRuns = 10;
			foldsSize=new int[numCvFolds];
			arffTestingFile="none";
			arffTestingFileName = "none";
			validationMethod = "CV";
		} 
		else if (!UserSuppliedValidationName.endsWith("arff")) {
			System.out.println("Third arguments should be either 'cv'");
			System.out.println("or a testing file with .arff extension");
			System.exit(0);
		}
		// regular Arff filename supplied for testing
		else {
			//we see a single testing experiment as a unique cv-run with a single fold
			numCvFolds = 1;
			numCvRuns = 1;
			arffTestingFile = UserSuppliedValidationName;
			arffTestingFileName=workPath +arffTestingFile;
			validationMethod = "TestingFile";
		}


		//scan ARFF file and get information
		arffFileAddress = workPath + UserSuppliedArffName;
		parseArffFile();
		discretLog=new int[featNames.size()];
		unknownClasses=(numArgs==4);
		//this init is useful also when working with testing file (see function savePredictionsToFile)
		currentCvFold=0;
	};

	/**
	 * Arguments of the main: (1) the working path; (2) the name of the main ArrfFile;
	 * (3) "cv" or the name of the testing ArffFile;
	 * (4)[OPTIONAL] "unknownClasses", in case the
	 * actual classes of the testing set are unknown.
	 */
	public static void main(String[] args) {

		Jncc.checkArgs(args);
		Jncc dm = new Jncc(args[0], args[1], args[2], args.length);



		//cross-validation
		if (dm.validationMethod.equals("CV"))
			dm.validateCV(args);

		//testing file, whose classes are unknown
		else if (dm.unknownClasses)
			dm.validateTFileUnkClasses ();

		// testing file, whose classes are known
		else
			dm.validateTFile(args[2]);

		dm.printElapsedTime();
	}

	private void printElapsedTime(){
		double elapsedCputime=(double) ((mxbean.getProcessCpuTime()-startTime)/Math.pow(10,9));	
		System.out.println("Elapsed Cpu time: "+ elapsedCputime +"seconds");
	}

	/**Sanity-check of the parameters supplied by the user
	 */
	private static void checkArgs(String[] args){

		if  (args.length == 1) {
			if		((args[0].equalsIgnoreCase("help")) | 
					(args[0].equalsIgnoreCase("-help")) | 
					(args[0].equalsIgnoreCase("--help"))){
				printHelp();
			}
			else
				printArgError();
		}

		if ((args.length != 3)& (args.length != 4)){
			printArgError();}

		if  (args.length == 4)
		{
			if (args[3].compareToIgnoreCase("unknownClasses")!= 0){
				printArgError();}
		}
	}
	 
	
	private static void printArgError(){
		System.out.println("You have supplied a wrong set of arguments");
		System.out.println ("Type java jncc20.Jncc --help for further information.");
		System.exit(0);
	}
	
	 /**
	  * Writes an help message to the user, specifying the syntax to be used with JNCC2.
	  */
	 private static void printHelp(){
		 	System.out.println();
			System.out.println("JNCC2 USAGE");
			System.out.println("==Parameters:");
			System.out.println("<workDir>: the directory where the ARFF file(s) are stored;");
			System.out.println("<trFile.arff>: the ARFF file containing the data set;");
			System.out.println("<teFile.arff>: the optional ARFF file containing the testing data set;");
			System.out.println();
			System.out.println("==How to start experiments:");
			System.out.println("1)Cross-validation:");
			System.out.println("java jncc20.Jncc <workDir> <trFile.arff> cv");
			System.out.println();
			System.out.println("2) Testing file:");
			System.out.println("java jncc20.Jncc <workDir> <trFile.arff> <teFile.arff>");
			System.out.println();
			System.out.println("3) Testing file with unknown classes:");
			System.out.println("java jncc20.Jncc <workDir> <trFile.arff> unknownclasses");
			System.out.println();
			System.out.println("Remark"); 
			System.out.println("If you are actually typing from directory <workDir>, the above commands simplify as follows:");
			System.out.println("java jncc20.Jncc . <trFile.arff> cv");
			System.out.println("java jncc20.Jncc . <trFile.arff> <teFile.arff>");
			System.out.println("java jncc20.Jncc . <trFile.arff> unknownclasses");
			System.out.println();
			System.out.println("==Further information");
			System.out.println("Please visit the website:");
			System.out.println("http://www.idsia.ch/~giorgio/jncc2.html");
			System.out.println();
			System.exit(0);
	 }


	/**
	 *Validates NBc and NCC via 10 runs of 10-folds cross-validation.
	 *Reports to file the relevant accuracy measures.
	 */
	private void validateCV(String[] args)

	{

		//==various initalizations, and console messages
		System.out.println("Validating via cross-validation");
		initResultsFiles(args[1]);


		for (int i = 0; i < numCvRuns; i++) {
			System.out.println("--Run "+(i+1)+"/"+numCvRuns+" of cross-validation");
			drawCVindexes();

			//==LOOP IMPLEMENTING 10-FOLD CV
			//j indexes the current fold used as testing

			for (int j = 0; j <numCvFolds; j++) {

				currentCvFold=j;

				//System.out.println("Fold n. "+(j+1)+"/"+numCvFolds);
				prepareTrainTestSet(j);
				trainValidClassifiers();
			}//END ==10 FOLDS			
		}//END ==10-CV-EXPERIMENTS

		writePerfIndicators();
		System.out.println();
		System.out.println();
		System.out.println("CLASSIFICATION SUCCESSFULLY PERFORMED");
	}

	/**Validates  NBC and  NCC via testing file.
	 * Reports to file the relevant accuracy measures.
	 */
	private void validateTFile (String TestingFile)
	{

		//various init. and console messages
		System.out.println("Validating via testing file");
		initResultsFiles(TestingFile);

		//we have to pass a dummy argument to the function, as we are not running cross-validation
		prepareTrainTestSet();
		trainValidClassifiers();
		writePerfIndicators();
		//writePredictions();
		System.out.println("CLASSIFICATION SUCCESSFULLY PERFORMED");	
	}

	/**
	 * Initializes file where to store predictions (which are only temporary) and performance indicators;
	 * validationFile is  the unique available Arff file (in
	 * case of CV), the testing file in case of validation via testing file; it is not defined in case 
	 * of unknownclasses. 
	 *
	 */
	private void initResultsFiles(String validationFile){
		
		resultsFile = "ResultsTable";
		resultsFile =workPath+resultsFile+".csv";
		
		if (validationMethod.equalsIgnoreCase("cv"))
		{
			predictionsFile = "Predictions-CV-"+validationFile;
			predictionsFile=predictionsFile.substring(0,predictionsFile.lastIndexOf("."));
			predictionsFile=workPath+predictionsFile+".csv";

			probabilitiesFile = "Probabilities-CV-"+validationFile;
			probabilitiesFile=probabilitiesFile.substring(0,probabilitiesFile.lastIndexOf("."));
			probabilitiesFile=workPath+""+probabilitiesFile+".csv";
		}
		
		//testing file
		else 
		{
			if (!unknownClasses)
			{
				predictionsFile = "Predictions-Testing-"+validationFile;
				predictionsFile=predictionsFile.substring(0,predictionsFile.lastIndexOf("."));
				predictionsFile=workPath+predictionsFile+".csv";

				probabilitiesFile = "Probabilities-Testing-"+validationFile;
				probabilitiesFile=probabilitiesFile.substring(0,probabilitiesFile.lastIndexOf("."));
				probabilitiesFile=workPath+probabilitiesFile+".csv";
			}
		}

		deleteFileIfExisting(predictionsFile);
	}


	private void deleteFileIfExisting(String file){

		//remove possible copy of the file referring to a previous run
		boolean exists = (new File(file)).exists();

		if (exists) {
			boolean success = (new File(file)).delete();
			if (!success) {
				// Deletion failed
				System.out.println("Impossible removing  file"+ file);
				System.exit(0);
			}
		} 




	}

	/**Trains classfier on the training set, validates them on the testing set and save predictions 
	 * to a temporary file
	 */
	private void trainValidClassifiers(){

		nbc = new NaiveBayes(trainingSet, usedFeaturesNames, classNames, 
				numClassForEachUsedFeature);
		nbc.classifyInstances(testingSet);

		ncc2= new NaiveCredalClassifier2(trainingSet, usedFeaturesNames, classNames, 
				numClassForEachUsedFeature, nonMarTraining, nonMarTesting, 
				numClassesNonMarTesting);
		ncc2.classifyInstances(testingSet);

		saveTmpPredictions ();
	}



	/**Prepares training and testing sets for cross-validation, discretizing also numerical variables. 
	 */
	private void prepareTrainTestSet(int currentFold)
	{

		//==BUILD TRAINING AND TESTING SET
		//collect RawData, i.e. numerical features have to be discretized
		ArrayList<double[]> RawTrainingData=new ArrayList<double[]>(rawDataset.size()-foldsSize[currentFold]);
		ArrayList<double[]> RawTestingData=new ArrayList<double[]>(foldsSize[currentFold]);
		for (int k=0; k<rawDataset.size(); k++)
		{
			if (cvFoldsIdx[k] != currentFold)
				RawTrainingData.add(rawDataset.get(k).clone());

			else
				RawTestingData.add(rawDataset.get(k).clone());
		}
		discretizeNumFeats(RawTrainingData);
		trainingSet=new ArrayList<int[]>(RawTrainingData.size());
		testingSet=new ArrayList<int[]>(RawTestingData.size());

		/*copy nominal features, and the numerical ones as discretized, 
 			discarding features discretized into a unique bin */
		prepareDataSetFromRawData(RawTrainingData, trainingSet);
		prepareDataSetFromRawData(RawTestingData, testingSet);

		//==TRAINING AND TESTING SET ARE NOW READY
	}

	/**Prepares training and testing sets for validation via testing set, discretizing also numerical variables. 
	 */
	private void prepareTrainTestSet()
	{
		//The computation starts here
		//rawDataset contains in this case the training data
		discretizeNumFeats(rawDataset);

		/*copy nominal features, and the numerical ones as discretized, 
		 discarding features discretized into a unique bin */
		trainingSet=new ArrayList<int[]>(rawDataset.size());
		prepareDataSetFromRawData(rawDataset, trainingSet);

		if (unknownClasses)
			parseArffTestingFile(true);
		else
			parseArffTestingFile(false);
	}			


	/**
	 * Dumps to file the predictions issued by the classifiers on the testing set(s); they will
	 * be later analyzed to compute the indicators, and eventually deleted.
	 * It produces a file which contains the features (apart from the ones discretized
	 * into a unique bin, which can change between different runs of CV), the actual class, the NCC classification
	 * (i.e., a number of columns equal to the number of classes containing either the outputted class, or 6666 to mean that
	 * not all classes have been outputted by NCC), and then NBC and Bma prediction
	 */
	private  void saveTmpPredictions()
	{

		int[]   nbcPredictions=nbc.getPredictions();
		int[][] nccPredictions=ncc2.getPredictions();

		int NumClasses=classNames.size();
		try 
		{
			BufferedWriter out=new BufferedWriter(new FileWriter(predictionsFile,true));

			for (int k=0; k<testingSet.size(); k++)
			{
				//fold number 
				out.write(currentCvFold+",");

				//actual class
				int jj=testingSet.get(0).length-1;
				out.write(testingSet.get(k)[jj]+",");

				for (int tmpint : nccPredictions[k]){
					out.write(tmpint+",");
				}

				for (int tmpint=0; tmpint<NumClasses-nccPredictions[k].length; tmpint++ ){
					out.write("6666"+",");
				}
				
				out.write(nbcPredictions[k]+"");
				out.newLine();
			}

			out.close();
		}
		catch (IOException e)
		{
			System.out.println();
			System.out.println("Problem in writing CD predictions to file PredFile");
			System.out.println();
		}
	}



	/**
	 *Learns NCC; classifies the instances of the testing file via NCC, and writes the classifications to file.
	 */
	private void validateTFileUnkClasses ()
	{
		//==various initalizations, and console messages
		System.out.println("Using testing file with unknown classes");

		//we have to pass a dummy argument to the function, as we are not running cross-validation
		prepareTrainTestSet();

		nbc = new NaiveBayes(trainingSet, usedFeaturesNames, classNames, 
				numClassForEachUsedFeature);
		nbc.classifyInstances(testingSet);
		
		ncc2= new NaiveCredalClassifier2(trainingSet, usedFeaturesNames, classNames, 
				numClassForEachUsedFeature, nonMarTraining, nonMarTesting, 
				numClassesNonMarTesting);				
		ncc2.classifyInstances(testingSet);	
		
		writePredictions();
		System.out.println("CLASSIFICATION SUCCESSFULLY PERFORMED");
		System.out.println();
	}

	/**Once classifiers have been validated (either via CV or single testing file), save to file all 
	 * the relevant information
	 *
	 */
	private void writePerfIndicators()
	{
		ResultsReporter reporter= new ResultsReporter();
		reporter.analyzePredictionsFile();
		reporter.writeOutputFiles();
	}

	/**Write to file the instances, actual classes, probability distribution computed by NBC and non-dominated classes 
	 *identified by NCC2.
	 *when the actual classes are unknown, this constitutes 
	 * the only output of the classifier.
	 * Note that, because of the discretization, different number of features might appear in
	 * different runs of CV 
	 */
	private void writePredictions()
	{
		int i,j;
		String ResultsFile = "Predictions-Testing-"+arffTestingFile;
		int tmpidx=ResultsFile.lastIndexOf('.');
		ResultsFile=ResultsFile.substring(0,tmpidx);
		ResultsFile =workPath+ResultsFile+".csv";

		int[] nbcPredictions= nbc.getPredictions();
		double[][] nbcProbabilities= nbc.getProbabilities();

		int[][] nccPredictions = ncc2.getPredictions();
		int nccOutSize;

		DecimalFormat formatter= new DecimalFormat("#0.00");

		try{
			int numFeats=featNames.size();
			BufferedWriter out=new BufferedWriter(new FileWriter(ResultsFile,false));

			if (usedFeatures.size()<featNames.size()){
				out.write("Features discretized into a single bin:");
				out.newLine();
				for (i=0; i<numFeats; i++){
					if (usedFeatures.indexOf(i)<0){
						out.write(featNames.get(i));
						out.newLine();
					}
				}
				out.newLine();out.newLine();
			}
			
			out.write ("INSTANCES AND PREDICTIONS");
			out.newLine();out.newLine();
			
			for (i=0; i<numFeats; i++){
				if (usedFeatures.indexOf(i)>=0){
					out.write(featNames.get(i)+",");
				}
			}

			if (!unknownClasses){
				out.write("ActualClass,");
			}

			out.write("NbcPosteriorDistribution,");
			for (i=0;i<((classNames.size())-1);i++){
				out.write(",");
			}
			out.write("NbcPrediction,");
			out.write("Ncc2Prediction");
			out.newLine();

			int numTabs=usedFeatures.size()+1;
			if (unknownClasses)
				numTabs--;
			for (i=0;i<(numTabs);i++){
				out.write(",");
			}
			
			
			for (i=0;i<classNames.size();i++){
				out.write(classNames.get(i));
				out.write(",");
			}
			out.newLine();


			//INSTANCES AND PREDICTIONS
			for (i=0; i<rawTestingSet.size(); i++){

				//features	
				for (j=0; j<usedFeatures.size(); j++){
						out.write(rawTestingSet.get(i)[j]+",");
				}

				//class
				if (!unknownClasses){
					out.write(rawTestingSet.get(i)[numFeats]+",");
				}

				//nbc distribution of probability
				for (j=0; j<classNames.size();j++){
					out.write(formatter.format(nbcProbabilities[i][j])+",");
				}
				//nbc prediction
				out.write(classNames.get(nbcPredictions[i])+",");


				//ncc2 prediction
				nccOutSize=nccPredictions[i].length;
				for (j=0; j<nccOutSize;j++){
					out.write(classNames.get(nccPredictions[i][j])+",");
				}
				for (j=0;j<classNames.size()-nccOutSize;j++){
					out.write("-"+",");
				}
				out.newLine();
			}
			out.close();
		}//end try
		catch (IOException e)
		{
			System.out.println("Problems in writing predictions to file");
		}
		catch (Exception e)
		{
			System.out.println("Problems in writing predictions to file");
		}
	}


	/**
	 * Draws <em>stratified</em>  folders for cross-validation, instantiating CvFoldsIdx.
	 */
	private void drawCVindexes() {

		cvFoldsIdx = new int[rawDataset.size()];

		/*
		 * (N x 1) array. Filled with the indexes of the rows, sorted according
		 * to the class, and previously randomized class-by-class
		 */
		ArrayList<Integer> IdxArray = new ArrayList<Integer>(rawDataset.size());
		ArrayList<Integer> bufferArrayList;
		int i, j;
		/*
		 * note that the random seed is initialized every time the function is
		 * called, to avoid generate the same random sequences over the
		 * different runs
		 */
		Random RandGenerator = new Random();
		int currRandIdx; 

		// now let's randomize them class by class
		for (i = 0; i < rowsClassIdx.length; i++) {
			bufferArrayList = new ArrayList<Integer>();

			for (int kk = 0; kk < rowsClassIdx[i].size(); kk++) {
				bufferArrayList.add(rowsClassIdx[i].get(kk));
			}

			while (!bufferArrayList.isEmpty()) {
				// choose randomly an index between 0 and (number of elements-1)
				// remember the nextInt generates Int up to (argument - 1)
				currRandIdx = RandGenerator.nextInt(bufferArrayList.size());
				IdxArray.add(bufferArrayList.get(currRandIdx));
				bufferArrayList.remove(currRandIdx);
			}// == end while
		}// end== for (int i=0; i<RawDatasetClassIdx.lenght; i++)

		/*
		 * At this point, IdxArray contain the indexes randomized class by
		 * class, and the array presents all the randomized indexes of class 1,
		 * then those of class 2, etc. Let's fill the CVFolderIdx
		 */

		int start = 0;
		int counter = 0;
		Arrays.fill(foldsSize,0);


		while (counter < IdxArray.size()) {
			j = start;

			while (j < IdxArray.size()) {
				cvFoldsIdx[IdxArray.get(j)] = start;
				j += numCvFolds;
				counter++;
				foldsSize[start]++;
			}// ==end of while

			start++;
		}// ==end while (counter<IdxArray.length)
	}// end== drawCVindexes


	/**Take a raw set of data (undiscretized features) and put them into 
	 * a dataset to be accessed by classifiers; categorical variables are copied unchanged, while
	 * numerical variables are converted to categorical according to DiscretizationIntervals;
	 * numerical variables discretized into a unique bin (and hence listed 
	 * in NonUsedFeatures) are discarded.
	 */
	private void prepareDataSetFromRawData(ArrayList<double[]> SourceData, ArrayList<int[]> DestinationData){

		int i,j;

		int MaxColumns=SourceData.get(0).length;
		//TrainingSet= new int[SourceData.length][];

		int[] tmpArr;
		int rowLength=usedFeatures.size()+1;

		for (i = 0; i < SourceData.size(); i++) {
			tmpArr=new int[rowLength];
			for (j=0; j<rowLength-1; j++)
			{
				if (! numFlags.get(usedFeatures.get(j)))
					tmpArr[j]=(int) SourceData.get(i)[usedFeatures.get(j)];

				else
					//numerical feature to discretize
				{
					if (SourceData.get(i)[usedFeatures.get(j)]!= -9999)
						tmpArr[j]=getDiscretizationIdx(SourceData.get(i)[usedFeatures.get(j)], usedFeatures.get(j));
					else
						tmpArr[j]=-9999;
				} 
			}//==Features have been copied

			//class copy
			tmpArr[usedFeatures.size()]=(int)SourceData.get(i)[MaxColumns-1];
			DestinationData.add(tmpArr);
		}

	}


	/**Prepares the NonMarInCurrentDataset data member.
	 */	

	private void findNonMarInCurrentDataset(){

		nonMarTraining= new ArrayList<Integer>(nonMarFeatsTraining.size()) ;
		nonMarTesting= new ArrayList<Integer>(nonMarFeatsTesting.size()) ;
		numClassesNonMarTesting= new ArrayList<Integer>(nonMarFeatsTesting.size()) ;
		int IdxFullDataset=-1;
		int i;

		for (String tmpStr : nonMarFeatsTraining)
		{
			//==get the index, unfortunately the array is unsorted
			for (i=0; i<usedFeaturesNames.size(); i++)
			{
				if (usedFeaturesNames.get(i).equalsIgnoreCase(tmpStr))
				{
					nonMarTraining.add(i);
					break;
				}
			}
		}


		for (String tmpStr : nonMarFeatsTesting)
		{
			//==get the index, unfortunately the array is unsorted
			for (i=0; i<usedFeaturesNames.size(); i++)
			{
				if (usedFeaturesNames.get(i).equalsIgnoreCase(tmpStr))
				{
					nonMarTesting.add(i);
					break;
				}
			}

			//we have to search by hand, as the array is not sorted!	
			//numerical feature

			for (i=0; i<featNames.size(); i++)
			{
				if (featNames.get(i).equalsIgnoreCase(tmpStr))
				{
					IdxFullDataset=i;
					break;
				}
			}

			if (numFlags.get(IdxFullDataset))
			{
				if (discretizationIntervals[IdxFullDataset][0]==Double.MAX_VALUE)
					continue;
				numClassesNonMarTesting.add((discretizationIntervals[IdxFullDataset].length)+1);
			}
			//categorical feature
			else
				numClassesNonMarTesting.add(categoryNames.get(IdxFullDataset).length);	
		}
	}



	/** 
	 * Discretizes all the numerical features on the Training Set, and instantiates DiscretizationIntervals,
	 * UsedFeatures, UsedFeaturesNames, NumClassForEachUsedFeature; updates DiscretizationLog.
	 */
	private void discretizeNumFeats (ArrayList<double[]> trainingData)
	{
		discretizationIntervals=new double[numFlags.size()][];
		//avoid effects of previous CV-runs
		MdlDiscretizer Discretizer;

		usedFeatures=new ArrayList<Integer>(featNames.size());
		notUsedFeatures=new ArrayList<Integer>(featNames.size());

		
		for (int j = 0; j < numFlags.size(); j++)
		{
			if (numFlags.get(j)) 
			{
				ArrayList<Integer> ClassValues=new ArrayList<Integer>(trainingData.size());
				ArrayList<Double> FeatureValues = new ArrayList<Double>(trainingData.size());
				for (int k = 0; k < trainingData.size(); k++) {
					if (trainingData.get(k)[j] != -9999) {
						ClassValues.add((int)trainingData.get(k)[numFlags.size()]);
						FeatureValues.add(trainingData.get(k)[j]);
					}
				}

				int NonDiscretizedCounter=0;
				Discretizer= new MdlDiscretizer(FeatureValues, ClassValues,classNames.size());

				if (Discretizer.getCutPoints().size()>0)
					discretizationIntervals[j]=ArrayUtils.arrList2Array(Discretizer.getCutPoints());
				else
					discretizationIntervals[j]=new double[]{Double.MAX_VALUE};

				if (Discretizer.getCutPoints().size()>0) 
					usedFeatures.add(j);
				else 
				{
					notUsedFeatures.add(j);
					NonDiscretizedCounter++;
				}
			}//end if numerical

			else 
			{ 
				//remove categorical features which have a single category only
				if (categoryNames.get(j).length >1)
					usedFeatures.add(j);
				else 
					notUsedFeatures.add(j);

			}
		}//end ==for on features

		Collections.sort(usedFeatures);
		Collections.sort(notUsedFeatures);
		usedFeaturesNames=new ArrayList<String>(usedFeatures.size());
		numClassForEachUsedFeature=new ArrayList<Integer>(usedFeatures.size());
		int i;
		for (i=0; i<usedFeatures.size(); i++)
		{
			usedFeaturesNames.add(i, featNames.get(usedFeatures.get(i)));
			if (numFlags.get(usedFeatures.get(i)))
				numClassForEachUsedFeature.add(i,(discretizationIntervals[usedFeatures.get(i)].length)+1);
			else
				numClassForEachUsedFeature.add(i,categoryNames.get(usedFeatures.get(i)).length);
		}

		findNonMarInCurrentDataset();

		for (int z=0; z<notUsedFeatures.size(); z++)
			//categorical feature with a single category don't go here;
			//we look for numerical feat discretized into a single bin
			if (numFlags.get(notUsedFeatures.get(z)))
				discretLog[notUsedFeatures.get(z)]++;
	}




//	=====PRIVATE HELPER FUNCTIONS=====




	/**Given a numerical value of a certain discretized feature, 
	 * returns the index of the bin in which the value falls
	 */
	private int getDiscretizationIdx(Double currentValue,
			int FeatureIdx) {

		int NextIdx=1;
		int PreviousIdx=0;
		int NumCutPoints=discretizationIntervals[FeatureIdx].length;

		if (currentValue<discretizationIntervals[FeatureIdx][0])
			return 0;

		if (currentValue>discretizationIntervals[FeatureIdx][NumCutPoints-1])
			return NumCutPoints;

		//on testing data, it may happen that a datum is actually equal to a cutpoint
		int MatchingCutPoint=Arrays.binarySearch(discretizationIntervals[FeatureIdx], currentValue);

		if (MatchingCutPoint < 0)
		{
			//we have an intermediate value
			while (! (currentValue<discretizationIntervals[FeatureIdx][NextIdx]) &
					(currentValue>discretizationIntervals[FeatureIdx][PreviousIdx]) )
			{
				PreviousIdx=NextIdx++;
			}
			return 	NextIdx;
		}
		else
			return MatchingCutPoint;
	}



	/**
	 * Reads the file NonMar.txt, containing the list of nonMar variables; if no
	 * file is found, all variables are assumed to be MAR.
	 * If the name of the variable is not preceeded by any token, the feature is supposed to be NonMar on
	 * both training and testing set; if it is preceeded by "training" ["testing"], then it is managed as NonMar on training
	 * [testing] only, and hence as Mar on testing [training].
	 * <p>
	 * Then, put the names of NonMar variables in TrainingNonMarFeatureNames and TestingNonMarFeatureNames.
	 */
	private void parseNonMar() {

		BufferedReader bfr = null;
		String NonMarFile = workPath + "NonMar.txt";
		nonMarFeatsTraining = new ArrayList<String>();
		nonMarFeatsTesting  = new ArrayList<String>();

		try {
			bfr = new LineNumberReader(new InputStreamReader(new FileInputStream(
					NonMarFile)));
			String line = "";
			try {
				String tmpStr;
				while ((line = bfr.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line);

					if (st.countTokens()>2)
					{
						System.out.println("Error in file "+NonMarFile);
						System.out.println("Maximum of two tokens per line is allowed");
						System.out.println("Check that every line  is formed either as either:");
						System.out.println();
						System.out.println("training[or testing] <name of the feature>");
						System.out.println("or:");
						System.out.println("<name of the feature>");
						System.out.println("In the latter case, the feature is assumed NonMar both on training and testing");
						System.exit(0);
					}
					tmpStr=st.nextToken();
					if  (tmpStr.compareToIgnoreCase("nonmar")==0) {
						if (((nonMarFeatsTesting.size()==0 & nonMarFeatsTraining.size()==0)) & ((line = bfr.readLine()) == null))
						{
							nonMarFeatsTesting=featNames;
							nonMarFeatsTraining=featNames;
							System.out.println("All features assumed to be NonMAR!");
							return;
						}
						else 
						{
							System.out.println("Possible inconsistency in NonMAR declarations:");
							System.out.println("I don't expect any further line in file if you use the token 'nonmar' ");
							System.out.println("Please setup properly file: "+NonMarFile);
							System.exit(0);
						}

					}


					if (tmpStr.equalsIgnoreCase("training"))
					{
						tmpStr=st.nextToken();
						if (nonMarFeatsTraining.contains(tmpStr))
						{
							System.out.println("Feature "+tmpStr+" declared more than once as NonMar in training. Please fix file "+workPath+ " NonMar.txt");
							System.exit(0);
						}
						else
							nonMarFeatsTraining.add(tmpStr);
					}

					else if (tmpStr.equalsIgnoreCase("testing"))
					{
						tmpStr=st.nextToken();
						if (nonMarFeatsTesting.contains(tmpStr))
						{
							System.out.println("Feature "+tmpStr+" declared more than once as NonMar in testing. Please fix file "+workPath+ " NonMar.txt");
							System.exit(0);
						}
						else
							nonMarFeatsTesting.add(tmpStr);
					}

					//if it is the feature name, no further tokens are allowed
					else if (st.countTokens()>0)
					{
						System.out.println("Error in file "+NonMarFile);
						System.out.println();
						System.out.println("Check that every line  is formed either as either:");
						System.out.println();
						System.out.println("training[or testing] <name of the feature>");
						System.out.println();
						System.out.println("or:");
						System.out.println("<name of the feature>");
						System.out.println();
						System.out.println("In the latter case, the feature is assumed NonMar both on training and testing");
						System.exit(0);
					}


					//NonMar on both training and testing set
					else
					{
						if (nonMarFeatsTesting.contains(tmpStr))
						{
							System.out.println("Feature "+tmpStr+" declared more than once as NonMar in testing. Please fix file "+workPath+ " NonMar.txt");
							System.exit(0);
						}

						if (nonMarFeatsTraining.contains(tmpStr))
						{
							System.out.println("Feature "+tmpStr+" declared more than once as NonMar in training. Please fix file "+workPath+ " NonMar.txt");
							System.exit(0);
						}
						nonMarFeatsTesting.add(tmpStr);
						nonMarFeatsTraining.add(tmpStr);
					}

				}
			} catch (Exception e) {
			}
		} 
		catch (FileNotFoundException exc) {

			//is perhaps the case wrong?
			File CurrentDir= new File (workPath); 
			String[] fileNames = CurrentDir.list();
			for (String tmpString:fileNames)
			{
				if (tmpString.compareToIgnoreCase("NonMar.txt")==0)
				{
					System.out.println("Missing file NonMar.txt in directory "+ workPath);
					System.out.println("Found file: "+tmpString);
					System.out.println("Please rename it as NonMar.txt if it contains declarations of NonMAR features for current experiment"); 
					System.out.println("Otherwise remove it, if has not to do with the experiment");
					System.exit(0);
				}
			}
		}
	}


	/**
	 * Scans the main Arff file.
	 * Initializes data members; than, scans the Arff file, checking the formal correctness 
	 * of variable declarations, and  the coherence of the data with the declarations; stores the information and the data
	 * loaded from file.
	 * In particular, it instantiates the data members FeatureNames, NumFlags(whether every feature is numerical or not), 
	 * CategoryNames(names of categories for each
	 * categorical featrue) and rawDataset (a matrix of double which contains the data as read from file, with missing values substitued by -9999, 
	 * and category names substituted by numerical indexes, and numerical values unchanged.)
	 * Moreover, reads the list of NonMar variables, which are then stored in NonMarFeatureNamesTraining and
	 * NonMarFeatureNamesTesting. 
	 */
	private void parseArffFile() {

		try {

			LineNumberReader lnr;
			String line = "";
			String currentToken = "";
			String[] tmpStrAr;
			boolean DataFound = false;
			int i, j;
			int FeatureIdx = 0;
			int tmpIndex = 0;

			// dynamic structures for managing data of unknown size
			featNames = new ArrayList<String>();
			categoryNames = new ArrayList<String[]>();
			numFlags = new ArrayList<Boolean>();
			datasetName=null;

			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(arffFileAddress)));

			// begin scan file header
			while (DataFound == false) {
				line = lnr.readLine();
				// skip blank lines
				if (line.trim().equals(""))
					continue;
				StringTokenizer st = new StringTokenizer(line);
				currentToken = st.nextToken();

				// skip comments
				if (currentToken.charAt(0) == '%')
					continue;

				if (currentToken.equalsIgnoreCase("@RELATION")) {
					if (datasetName==null){
						datasetName = st.nextToken();
						System.out.println();
						System.out.println("Dataset: "+datasetName);
					}
					else{
						System.out.println("Name of the relation is defined multiple times: "+datasetName+", "+st.nextToken());
						System.out.println("Fix the ARFF file by providing a single name for the relation.");
						System.exit(0);
					}

				} else if (currentToken.equalsIgnoreCase("@ATTRIBUTE"))
				{	
					currentToken = st.nextToken();

					if (! (featNames.indexOf(currentToken)==-1))
					{
						System.out.println("File: "+arffFileAddress); 
						System.out.println("Line: "+lnr.getLineNumber());
						System.out.println("Duplicate attribute name "+currentToken);
						System.exit(0);
					}


					featNames.add(currentToken);
					currentToken = st.nextToken();

					// we have a nominal feature? in this case, tokenizer
					// should
					// use commas as delimiters

					if (currentToken.charAt(0) == '{') {

						tmpIndex = line.indexOf('{');
						currentToken = line.substring(tmpIndex).trim();

						if (!(currentToken
								.charAt(currentToken.length() - 1) == '}')) {
							System.out.println("Line: "
									+ lnr.getLineNumber() + " of file:"
									+ arffFileAddress);
							System.out
							.println("List of values not enclosed into brackets, exit");
							System.exit(0);
						}

						// remove brackets from token
						currentToken = currentToken.substring(1,
								(currentToken.length() - 1)).trim();
						tmpStrAr = (currentToken.split(",", -1));

						for (i = 0; i < tmpStrAr.length; i++) {
							tmpStrAr[i] = tmpStrAr[i].trim();
						}
						categoryNames.add(tmpStrAr);
						numFlags.add(false);
						FeatureIdx++;

					}

					else {

						// we have a numerical feature
						if (currentToken.equalsIgnoreCase("real")
								| currentToken.equalsIgnoreCase("numeric") 
								| currentToken.equalsIgnoreCase("integer")) {
							String[] Empty = { "empty" };
							categoryNames.add(Empty);
							numFlags.add(true);
							FeatureIdx++;
						} else {
							System.out.println("Line "
									+ lnr.getLineNumber() + " of file: "
									+ arffFileAddress);
							System.out
							.println("Non nominal features should be declared either as "
									+ "real or numeric");
							System.out
							.println("Or, if the feature is a nominal one, its values should be enclosed "
									+ " into { } brackets");
							System.out
							.println("If this is not the problem, check that names enclosed into quotes do NOT include white spaces");

							System.exit(0);
						}

					}//==END NUMERICAL FEATURE
				}//==END "@ATTRIBUTE"

				// we do not accept tokens other than @attribute, @relation,
				// @data,
				else if (!(currentToken.equalsIgnoreCase("@DATA"))) {
					System.out.println("Wrong token in line "
							+ lnr.getLineNumber() + " of file " + arffFileAddress);
					System.exit(0);
				}

				else if (currentToken.equalsIgnoreCase("@DATA")) {

					//we have to remove the last feature from the feature set, as it is going to be the class.

					//Previously, however, we have to check that it is not numerical, otherwise the problem cannot be addressed.
					if (numFlags.get(numFlags.size()-1)){
						System.out.println("Last declared feature is numerical, and therefore cannot be used for a classification problem");
						System.exit(0);
					}

					int lastIdx=featNames.size()-1;
					//now that the last feature is known to be a nominal one, we have to use it as a class and to drop it from the feature set.
					classNames = new ArrayList<String>();
					classNames= new ArrayList<String>(Arrays.asList(categoryNames.get(lastIdx)));
					featNames.remove(lastIdx);
					numFlags.remove(lastIdx);
					categoryNames.remove(lastIdx);
					DataFound = true;
				}
			}// end (while DataFound==false)

			// At this point, the header has been entirely parsed
			// Let's load the name of NonMar variables and check if they are
			// consistent with the names
			// of the variables already parsed
			// Execution is halted if no correspondence is found.
			parseNonMar();

			if (!(nonMarFeatsTraining.isEmpty())) {
				for (String tmpStrn : nonMarFeatsTraining) {
					if (!findFeatName(tmpStrn)) {
						System.out.println();System.out.println();
						System.out.println("NonMar variable " + tmpStrn.toUpperCase()+
								" declared in file: "+workPath+"NonMar.txt");
						System.out.println("but NOT existent in data-file " + arffFileAddress);
						System.exit(0);
					}
				}
			}

			if (!(nonMarFeatsTesting.isEmpty())) {
				for (String tmpStrn : nonMarFeatsTesting) {
					if (!findFeatName(tmpStrn)) {
						System.out.println();System.out.println();
						System.out.println("NonMar variable " + tmpStrn.toUpperCase()+
								" declared in file: "+workPath+"NonMar.txt");
						System.out.println("but NOT existent in data-file " + arffFileAddress);
						System.exit(0);
					}
				}
			}


			double[] CurrentRow;
			boolean catNameChecked;
			boolean classNameChecked;
			rawDataset = new ArrayList<double[]>();

			// ==TO DO ONLY IF CV
			if (validationMethod.equals("CV"))
			{
				rowsClassIdx = new ArrayList[classNames.size()];
				for (int ii=0;ii<classNames.size();ii++)
					rowsClassIdx[ii]=new ArrayList<Integer>();
			}

			// ===END ONLY CV

			// ==PARSE THE COMMA SEPARATED VALUES
			while ((line = lnr.readLine()) != null) {

				// current row: all the features and the class as last value
				CurrentRow = new double[(featNames.size()) + 1];
				// skip blank lines
				if (line.trim().equals(""))
					continue;

				// parse data
				StringTokenizer st = new StringTokenizer(line, ",");
				currentToken = st.nextToken().trim();

				// skip commented line
				if (currentToken.charAt(0) == '%')
					continue;


				if (st.countTokens() != featNames.size())
				{
					int tmpInt=featNames.size()+1;
					int foundTokens=st.countTokens()+1;
					System.out.println("File: "+arffFileAddress);
					System.out.println("Line: "+lnr.getLineNumber());
					System.out.println	("Expected "+ tmpInt + " tokens");
					System.out.println("Found "+foundTokens+" tokens" );
					System.exit(0);
				}

				// ==PARSE FEATURES VALUES IN THE CURRENT ROW
				for (i = 0; i < featNames.size(); i++) {

					if (currentToken.equals("?"))
						CurrentRow[i] = -9999;

					// numerical feature
					else if (numFlags.get(i))
						try {
							CurrentRow[i] = Double.parseDouble(currentToken);
						}

					catch (NumberFormatException e) {
						System.out.println("Line " + lnr.getLineNumber()
								+ " of file " + arffFileAddress);
						System.out
						.println("Value "
								+ currentToken
								+ " incompatible with the numerical attribute "
								+ featNames.get(i));
						System.exit(0);
					}

					// nominal feature
					else {
						catNameChecked = false;
						tmpStrAr = categoryNames.get(i);

						for (j = 0; j < tmpStrAr.length; j++) {
							if (currentToken.equalsIgnoreCase(tmpStrAr[j])) {
								catNameChecked = true;
								CurrentRow[i] = j;
								break;
							}
						} // end **for (j=0; j<tmpStrAr.length; j++)

						if (catNameChecked == false) {
							System.out.println("Line " + lnr.getLineNumber()
									+ " of file " + arffFileAddress);
							System.out
							.println("Value "
									+ currentToken
									+ " does not match any of the possible values"
									+ " of attribute "
									+ featNames.get(i));
							System.exit(0);
						}

					}// end else (nominal feature)
					currentToken = st.nextToken().trim();
				}// end **for (int i=0; i<FeatureNames.size()(); i++)
				// ==FEATURES IN THE CURRENT ROW HAVE BEEN PARSED

				// == READ AND STORE CLASS VALUES
				classNameChecked = false;

				for (j = 0; j < classNames.size(); j++) {
					if (currentToken.equalsIgnoreCase(classNames.get(j))) {
						classNameChecked = true;
						CurrentRow[featNames.size()] = j;
						break;
					}
				}
				if (classNameChecked == false) {
					System.out.println("Line " + lnr.getLineNumber()
							+ " of file " + arffFileAddress);
					if (currentToken.equalsIgnoreCase("?")){
						System.out.println("Missing value not allowed for the class variable");
						System.exit(0);
					}
					else
					{
						System.out.println("Value " + currentToken
								+ " does not match any of the possible values"
								+ " for the output class");
						System.exit(0);
					}
				}
				// ==CLASS PARSED

				rawDataset.add(CurrentRow);

				// ==ONLY FOR CV CASE!!
				if (validationMethod.equals("CV")) 
					rowsClassIdx[j].add(rawDataset.size()-1);

			}// end parsing data (while line != null)


			lnr.close();
		}// end try

		catch (FileNotFoundException exc) {
			System.out.println("Missing File " + arffFileAddress);
			System.out.println("Please check path and file name!");
			System.exit(0);
		} catch (IOException ioexc) {
			System.out.println("Unexpected exception reading file " + arffFileAddress);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Unexpected exception reading file " + arffFileAddress);
			System.exit(0);
		}

	}// end parseArffFile


	private boolean findFeatName (String tmpString){
		int i;
		boolean found = false;
		for (i=0;i<featNames.size(); i++){
			if (featNames.get(i).equalsIgnoreCase(tmpString)){
				found=true;
				break;
			}
		}
		return found;
	}
	


	
	
	/**
	 * Parses the testing file, checking that all declarations are coherent with those already loaded from the training
	 * Arff file; if the classes are unknown, it reads only the instances, without looking for the classes.
	 * Data are stored in TestingSet: nominal features are simply stored, while 
	 * numerical features are discretized using the bins available from DiscretizationIntervals.
	 */

	private void parseArffTestingFile(boolean UnknownClasses) {

		try {
//			System.out.println("Parsing testing file: "+arffTestingFileName+"...");
			LineNumberReader lnr;
			String line = "";
			String currentToken = "";
			String[] tmpStrAr;
			boolean ClassFound = false;
			boolean DataFound = false;
			int i, j;
			int tmpIndex;

			//num of features found in testing set which match, both as for order and name, those of training set.
			//for the testing file to be accepted, it is necessary to have eventually foundFeatures=featuresNames.size()
			int foundFeatures=0;

			lnr = new LineNumberReader(new InputStreamReader(
					new FileInputStream(arffTestingFileName)));

			// begin scan file header
			while (!DataFound) {
				line = lnr.readLine();
				// skip blank lines
				if (line.trim().equals(""))
					continue;
				StringTokenizer st = new StringTokenizer(line);
				currentToken = st.nextToken();

				// skip comments
				if (currentToken.charAt(0) == '%'){
					continue;
				}

				if (currentToken.equalsIgnoreCase("@RELATION")) {
					continue;
				}

				if (currentToken.equalsIgnoreCase("@ATTRIBUTE"))

				{
					if (ClassFound)
					{
						System.out.println("File: "+arffTestingFileName);
						System.out.println("Line: "+lnr.getLineNumber());
						System.out.println("Feature "+st.nextToken()+" exceeds the features declared in training file.");
						System.exit(0);
					}
					currentToken = st.nextToken();


					//if this condition is verified, then we are going to parse the class
					if  (foundFeatures==featNames.size())
					{
						tmpIndex = line.indexOf('{');
						currentToken = line.substring(tmpIndex).trim();

						if (!(currentToken
								.charAt(currentToken.length() - 1) == '}')) {
							System.out.println("Line: "
									+ lnr.getLineNumber() + " of file:"
									+ arffTestingFileName);
							System.out
							.println("List of values not enclosed into {} brackets");
							System.exit(0);
						}

						// remove brackets from token
						currentToken = currentToken.substring(1,
								(currentToken.length() - 1)).trim();
						tmpStrAr = (currentToken.split(",", -1));

						if (tmpStrAr.length != classNames.size())
						{
							System.out.println("Number of categories of last attribute (assumed to be the class) " +
									"does not match between files");
							System.out.println(arffFileAddress +  "and " + arffTestingFileName);
							System.exit(0);
						}

						for (i = 0; i < tmpStrAr.length; i++) {
							tmpStrAr[i] = tmpStrAr[i].trim();
							if (! (tmpStrAr[i].equalsIgnoreCase(classNames.get(i))))
							{
								System.out.println ("Problem with categories of the last attribute (assumed to represent the class):"); 
								System.out.println ("class" + tmpStrAr[i] + " declared in file"); 
								System.out.println();
								System.out.println(arffTestingFileName);
								System.out.println("does not match class" + 
										classNames.get(i) + " declared in ");
								System.out.println(arffFileAddress);
								System.exit(0);
							}
						}
						ClassFound=true;
					}// end analysis of the class

					//if instead a feature is to be parsed, the following procedure has to be followed
					else
					{

						if  (featNames.get(foundFeatures).equalsIgnoreCase(currentToken))
							foundFeatures+=1;
						else
						{//file rejected because variables supplied in the wrong order
							System.out.println("File: "+arffTestingFileName);
							System.out.println("Line: " + lnr.getLineNumber());
							System.out.println("Variable " + currentToken +" found"); 
							System.out.println("Expected variable: "+ featNames.get(foundFeatures));
							System.exit(0);
						}

						currentToken = st.nextToken();

						// we have a nominal feature? in this case, tokenizer
						// should
						// use commas as delimiters

						if (currentToken.charAt(0) == '{') {

							//note that if we want to use foundFeature as an index, we have to decrement it of an unit,
							//as array are indexed starting from zero
							if (numFlags.get(foundFeatures-1))
							{
								System.out.println("File: "+arffTestingFileName+"\nLine: "
										+lnr.getLineNumber()+"\n**Feature: "+featNames.get(foundFeatures-1)
										+" declared as categorical.\nThis is incoherent with the declaration" +
										" for the same feature in file:\n"  +
										arffFileAddress);

								System.exit(0);
							}


							tmpIndex = line.indexOf('{');
							currentToken = line.substring(tmpIndex).trim();

							if (!(currentToken
									.charAt(currentToken.length() - 1) == '}')) {
								System.out.println("Line: "
										+ lnr.getLineNumber() + " of file:"
										+ arffTestingFileName);
								System.out
								.println("List of values not enclosed into {} brackets");
								System.exit(0);
							}

							// remove brackets from token
							currentToken = currentToken.substring(1,
									(currentToken.length() - 1)).trim();
							tmpStrAr = (currentToken.split(",", -1));

							if (tmpStrAr.length != categoryNames.get(foundFeatures-1).length)
							{
								System.out.println("Feature: "+ featNames.get(foundFeatures-1)+
										"\n incoherent class names declarations between files \n"+
										arffFileAddress + " and " + arffTestingFileName); 
								System.exit(0);

							}


							for (i = 0; i < tmpStrAr.length; i++) {
								tmpStrAr[i] = tmpStrAr[i].trim();
								if (! (tmpStrAr[i].equalsIgnoreCase(categoryNames.get(foundFeatures-1)[i])))
								{
									System.out.println ("Feature " + featNames.get(foundFeatures-1) + " : \n"
											+"Category " + tmpStrAr[i] + " declared in file \n " + 
											arffTestingFileName + "\n does not match category " + 
											categoryNames.get(foundFeatures-1)[i] 
											                                   + " declared in \n" + arffFileAddress );
									System.exit(0);
								}
							}
						}

						else {

							// we have a numerical feature

							if (! numFlags.get(foundFeatures-1))
							{

								System.out.println("File: "+arffTestingFileName+"\nLine: "
										+lnr.getLineNumber()+"\n**Feature: "+featNames.get(foundFeatures-1)
										+"declared as numerical.\nThis is incoherent with the declaration"+
										"for the same feature in file:\n" + arffFileAddress);

								System.exit(0);
							}

							if (!(currentToken.equalsIgnoreCase("real")
									| currentToken.equalsIgnoreCase("numeric") 
									| currentToken.equalsIgnoreCase("integer"))) 
							{	
								System.out.println("Line "
										+ lnr.getLineNumber() + " of file: "
										+ arffTestingFileName);
								System.out
								.println("Non nominal features should be declared either as "
										+ "real or numeric");								
								System.exit(0);
							}

						}//==END numerical feature
					}
				}//==END (if @attribute)


				// we do not accept tokens other than @attribute, @relation,
				// @data,
				else if (!(currentToken.equalsIgnoreCase("@DATA"))) {
					System.out.println("Wrong token in line "
							+ lnr.getLineNumber() + " of file " + arffTestingFileName);
					System.exit(0);
				}

				else if (currentToken.equalsIgnoreCase("@DATA")) {
					if ((!ClassFound) & (!UnknownClasses)) {
						System.out.println("Problem in file " + arffTestingFileName);
						System.out
						.println("Missing declaration of output class before the '@DATA' tag");
						System.exit(0);
					}
					DataFound = true;
				}
			}// end (while DataFound==false)
			// At this point, the header has been entirely parsed.

			//We now know that all the variables in the testing file are present and 
			//coherently ordered with those in the training file. 
			//We should still check that all the training features are
			//present in the testing file.

			if (foundFeatures != numFlags.size())
			{
				System.out.println(numFlags.size() + " features  in " + arffFileAddress);
				System.out.println(foundFeatures + " features in " + arffTestingFileName);
				System.out.println("\n**List of missing feature in testing file:");
				for (int kk=foundFeatures; kk<numFlags.size(); kk++)
				{
					System.out.println("\n"+featNames.get(kk));
				}		
				System.exit(0);

			}


			//==END HEADER PARSING AND CHECKING


			//===PARSE THE DATA 
			int[] currentRow;
			String[] currentRawRow;
			boolean catNameChecked;
			boolean classNameChecked;

			//I approximately suppose that it will have more or less the same number of data than 
			//the training
			testingSet = new ArrayList<int[]>(rawDataset.size());
			rawTestingSet = new ArrayList<String[]>(rawDataset.size());
			int TotalVariables;
			int UsedVariables;

			if (UnknownClasses)
			{
				UsedVariables=usedFeatures.size();
				TotalVariables=featNames.size();
			}
			else
			{
				UsedVariables=(usedFeatures.size())+1;
				TotalVariables=featNames.size()+1;
			}



			// ==PARSE THE COMMA SEPARATED VALUES
			while ((line = lnr.readLine()) != null) {


				currentRow = new int[UsedVariables];
				currentRawRow = new String[TotalVariables];

				// skip blank lines
				if (line.trim().equals(""))
					continue;

				// parse data
				StringTokenizer st = new StringTokenizer(line, ",");
				currentToken = st.nextToken().trim();

				// skip commented line
				if (currentToken.charAt(0) == '%')
					continue;

				if (st.countTokens() != (TotalVariables-1))
				{
					int foundTokens=st.countTokens()+1;
					System.out.println("File: "+arffTestingFileName+"\nLine: "+lnr.getLineNumber()
							+"\nExpected "+ TotalVariables + " tokens \nFound " +
							foundTokens+" tokens" );
					System.exit(0);
				}

				int counter=0;
				// ==PARSE FEATURES VALUES IN THE CURRENT ROW
				for (i = 0; i < featNames.size(); i++) {

					if (usedFeatures.indexOf(i)<0)
					{
						if (unknownClasses){
							if (i!=featNames.size()-1)
							currentToken = st.nextToken().trim();
						}
						else
							currentToken = st.nextToken().trim();
						continue;
					}


					currentRawRow[counter]=currentToken;

					if (currentToken.equals("?"))
						currentRow[counter] = -9999;

					// numerical feature
					else if (numFlags.get(i))
						try {
							currentRow[counter]=getDiscretizationIdx(Double.parseDouble(currentToken),i);
						}

					catch (NumberFormatException e) {
						System.out.println("Line " + lnr.getLineNumber()
								+ " of file " + arffTestingFileName);
						System.out
						.println("Value "
								+ currentToken
								+ " incompatible with the numerical attribute "
								+ featNames.get(i));
						System.exit(0);
					}

					// nominal feature
					else {
						catNameChecked = false;
						for (j = 0; j < categoryNames.get(i).length; j++) {
							if (currentToken.equalsIgnoreCase(categoryNames.get(i)[j])) {
								catNameChecked = true;
								currentRow[counter] = j;
								break;
							}
						} // end **for (j=0; j<tmpStrAr.length; j++)

						if (catNameChecked == false) {
							System.out.println("Line " + lnr.getLineNumber()
									+ " of file " + arffTestingFileName);
							System.out
							.println("Value "
									+ currentToken
									+ " does not match any of the possible values"
									+ " of attribute "
									+ featNames.get(i));
							System.exit(0);
						}

					}// end else (nominal feature)


					if ((!UnknownClasses)| (i < featNames.size()-1))
						currentToken = st.nextToken().trim();

					counter++;
				}// end **for (int i=0; i<FeatureNames.size()(); i++)
				// ==FEATURES IN THE CURRENT ROW HAVE BEEN PARSED

				if (!UnknownClasses)
				{
					// == READ AND STORE CLASS VALUES
					classNameChecked = false;

					for (j = 0; j < classNames.size(); j++) {
						if (currentToken.equalsIgnoreCase(classNames.get(j))) {
							classNameChecked = true;
							currentRow[UsedVariables-1] = j;
							currentRawRow[TotalVariables-1]=currentToken;
							break;
						}
					}
					if (classNameChecked == false) {
						System.out.println("Line " + lnr.getLineNumber()
								+ " of file " + arffTestingFileName);
						System.out.println("Value " + currentToken
								+ " does not match any of the possible values"
								+ " for the output class");
						System.exit(0);
					}
					// ==CLASS PARSED
				}

				testingSet.add(currentRow);
				rawTestingSet.add(currentRawRow);
			}// end parsing data (while line != null)

			lnr.close();

		}// end try

		catch (FileNotFoundException exc) {
			System.out.println("Missing File " + arffTestingFileName);
			System.exit(0);
		} catch (IOException ioexc) {
			System.out.println("Unexpected exception reading file " + arffTestingFileName);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Unexpected exception reading file " + arffTestingFileName);
			System.exit(0);
		}
	}//end parseTestingFile



//	=====DATA MEMBERS=====

	/**
	 * Dataset Name as read from the field "@relation" in the Arff file
	 */
	private String datasetName;



	/** Set either to "CV" or to the name of the testing Arff file */
	private String validationMethod;


	/**
	 * Names of  NonMar features in training
	 */
	private ArrayList<String> nonMarFeatsTraining;

	/**
	 * Names of  NonMar features in testing
	 */
	private ArrayList<String> nonMarFeatsTesting;

	private int currentCvFold;

	/**
	 * Index of NonMar features positions in the current 
	 * training set (position might change during CV, as different variables can get discretized into a single bin)
	 */
	private ArrayList<Integer> nonMarTraining;

	/**
	 * Index of NonMar features positions in the current 
	 * testing set (position might change during CV, as different variables can get discretized into a single bin)
	 */	private ArrayList<Integer> nonMarTesting;

	 /**Number of classes of variables NonMar in the testing set. 
	  * Useful when the NCC builds all the possible realizations of 
	  * the NonMar variables
	  */
	 private ArrayList<Integer>  numClassesNonMarTesting;

	 /** Names of input features */
	 private ArrayList<String> featNames;

	 /** Flags array, regarding wheter Features are numerical (1) or not (0)*/
	 private ArrayList<Boolean> numFlags;

	 /**Variables used in the current experiment, hence excluding those
	 discretized in a single bin. Indexes refer to rawDataset
	  * */
	 private ArrayList<Integer> usedFeatures;

	 /**Names of the variables used in the current experiment, hence excluding those
	 discretized in a single bin.
	  * */
	 private ArrayList<String> usedFeaturesNames;


	 /**Whether classes of the testing set are known or not */
	 private boolean unknownClasses;

	 /**
	  *Number of classes for each used feature  
	  */
	 private ArrayList<Integer> numClassForEachUsedFeature;

	 /**Variables not used in the current experiment,
	   because discretized in a single bin;
	   indexes refer to rawDataset
	  * */
	 private ArrayList<Integer> notUsedFeatures;


	 /**
	  * Copy of the data read from Arff file (having hence -9999 as marker for
	  * missing data), and category names substituted by the corresponding
	  * indexes.)
	  */
	 private ArrayList<double[]> rawDataset;

	 /**
	  * Raw testing set exactly as read from file.
	  * Used when a testing file with unknown classes is provided, to eventually 
	  * dump to file the values of the instances.
	  * Being declared as String[][], it  hosts number as well as
	  * categories.
	  */
	 private ArrayList<String[]> rawTestingSet;


	 /**
	  * Matrix with rows of different length; stores the bin ranges for numerical
	  * features
	  */
	 private double[][] discretizationIntervals;

	 /**
	  * Matrix of String with rows of different lenght; stores the name of the categories 
	  * (each row corresponds to a different feature); meaningful for categorical features only.
	  * <p>
	  */
	 private ArrayList<String[]> categoryNames;

	 /**
	  * How many times each feature has been discretized in a single bin,
	  * over the different training/testing experiments.
	  */
	 private int[] discretLog;

	 /**
	  * File that reports avg and std dev of performance indicators; this is the ultimate output file
	  */
	 private String resultsFile;

	 /**
	  * File that reports the estimated probabilities by precise classifiers and whether the imprecise classifier is precise or not; used to
	  * compute the curve of precision vs. accuracy
	  */
	 private String probabilitiesFile;

	 /**
	  * Path where the files for the given experiment (Arff files, NonMar.txt) are found,
	  * and where output files will be saved.
	  */
	 private String workPath;


	 /** Absolute path of the main Arff file */
	 private String arffFileAddress;

	 /** Absolute path of the testing Arff file */
	 private String arffTestingFile;
	 /** Absolute path of the temporary predictions file */
	 private String predictionsFile;

	 /** Name of the testing Arff file */
	 private String arffTestingFileName;





	 /**
	  * Names of the output classes.
	  */
	 private ArrayList<String> classNames;

	 /**
	  * Indexes of the rows, in rawDataset, which have the same output class.
	  * For instance, the first row collects the indexes of all the rows in rawDataset
	  * having output
	  * class c1, and so on.
	  */
	 private ArrayList<Integer>[] rowsClassIdx;

	 /**
	  * Indexes for cross validation: in which fold each row of 
	  * rawDataset falls
	  */
	 private int[] cvFoldsIdx;

	 /**
	  * How many instances are in each fold
	  */
	 private int[] foldsSize;

	 /** Number of folds used by cross-validation */
	 private int numCvFolds;

	 /** time at which program is started*/
	 private double startTime;

	 /**needed to track execution time*/
	 OperatingSystemMXBean mxbean;

	 /** Number of Cross validation Runs*/
	 private int numCvRuns;

	 /**
	  * Training set, accessed by the classifier: numerical
	  * variables are discretized, while category names and classes are substituted by 
	  * indexes; missing data denoted as -9999.
	  */
	 private ArrayList<int[]> trainingSet;

	 /**
	  * Testing set, accessed by the classifier: numerical
	  * variables are discretized, while category names and classes are substituted by 
	  * indexes; missing data denoted as -9999.
	  */
	 private ArrayList<int[]> testingSet;

	 /**Naive Bayes classifier */
	 private NaiveBayes nbc;
	 
	 /**NCC2 classifier */
	 private NaiveCredalClassifier2 ncc2;


	 //==INNER NESTED CLASS: ResultsReporter
	 /**
	  * Helper class for jncc, which accomplishes the following tasks: 
	  * reads the temporary file where NBC and NCC predictions are stored; 
	  * computes performances indexes; produces the output files, i.e., ResultsTable.csv (performance indicators),
	  * ConfMatrices.txt (confusion matrices) and, if a testing file is supplied, Prediction-<TestingFileName>.csv (instances and predictions 
	  * of the testing file).
	  */
	 private  class ResultsReporter {

		 /**
		  * Constructor
		  */
		 ResultsReporter(){			 

			 numRuns=numCvFolds*numCvRuns;
			 numClasses=classNames.size();
			 aNbcAcc = new double[numRuns];
			 aNccPrec = new double[numRuns];
			 aNccSingleAcc = new double[numRuns];
			 aNccSetAcc = new double[numRuns];
			 aNccOutputSize = new double[numRuns];
			 aNccP = new double[numRuns];
			 aNccI = new double[numRuns];
			 indicatorsFile =workPath+"indicators.csv";
		 }


		 private void parseIndicatorFile(){
			 try
			 {
				 //skip first line, containing  comments
				 LineNumberReader lnr = new LineNumberReader(new InputStreamReader(
						 new FileInputStream(indicatorsFile)));
				 String line = "";

				 int i = -1;

				 while ((line = lnr.readLine()) != null) {

					 i++;
					 //parse data
					 StringTokenizer st = new StringTokenizer(line, ",");
					 Double tmpDouble;
					 aNbcAcc[i]=Double.parseDouble(st.nextToken());
					 aNccPrec[i]=Double.parseDouble(st.nextToken());
					 tmpDouble=Double.parseDouble(st.nextToken());
					 if (tmpDouble.isNaN())
						 aNccSingleAcc[i]=-9999;
					 else
						 aNccSingleAcc[i]=tmpDouble;


					 tmpDouble=Double.parseDouble(st.nextToken());
					 if (tmpDouble.isNaN())
						 aNccSetAcc[i]=-9999;
					 else
						 aNccSetAcc[i]=tmpDouble;


					 tmpDouble=Double.parseDouble(st.nextToken());
					 if (tmpDouble.isNaN())
						 aNccOutputSize[i]=-9999;
					 else
						 aNccOutputSize[i]=tmpDouble;

					 tmpDouble=Double.parseDouble(st.nextToken());
					 if (tmpDouble.isNaN())
						 aNccP[i]=-9999;
					 else
						 aNccP[i]=tmpDouble;


					 tmpDouble=Double.parseDouble(st.nextToken());
					 if (tmpDouble.isNaN())
						 aNccI[i]=-9999;
					 else
						 aNccI[i]=tmpDouble;

				 }
				 lnr.close();
			 }
			 catch (IOException ioexc) {
				 System.out.println("Unexpected exception reading file " + indicatorsFile);
				 System.exit(0);
			 } 
		 }
		 
		 /**Writes confusion matrix to file, preceeding it by a title which depends on the classifier
		  * parameter (nbc or ncc2); the confusion matrix is appended into a an already existing file (if the file is
		  * not existing, it is created)
		  */
		 private void writeConfMatrix(String classifier){
			 
			 int[][] matrix=nbcConfMatrix;
			 String confFile;
			 confFile=workPath+"ConfMatrices.txt";
			 int i,j;
			 
			 try{
				 BufferedWriter out=new BufferedWriter(new FileWriter(confFile,true));
				 
				 //write 4 white lines if nbc, to separate from previous experiment
				 if (classifier.equalsIgnoreCase("nbc")) {
					 for (i=1;i<4; i++){
						 out.newLine();
					 }
				 }
				 out.newLine();
				 out.write("Dataset: "+datasetName);
				 out.newLine();
				 
				 //write validation method
				 if (validationMethod.equalsIgnoreCase("cv"))
					 out.write("Validation method: cv");
				 else
					 out.write("Validation method: t&t");
				 out.newLine();
				 
				 
				 //write classifier
				 if (classifier.equalsIgnoreCase("nbc")){
					 out.write("Classifier: NBC");
					 out.newLine();
				 }
				 else if (classifier.equalsIgnoreCase("ncc2")){
					 out.write("Classifier: NCC2 (matrix refers to determinate instances)");
					 out.newLine();
					 matrix=nccConfMatrix;
				 }
				 else{
					 System.out.println("Wrong classifier typer provided.");
					 System.exit(0);
				 }
				 
				 
				 for (i=0; i<classNames.size(); i++){
					 out.write(classNames.get(i)+"\t");
				 }
				 out.write("\t <--classified as");
				 out.newLine();
				 
				 if (validationMethod.equalsIgnoreCase("cv"))
				 {	
					 DecimalFormat formatter= new DecimalFormat("#0");
					 for (i=0; i<matrix.length; i++)
					 {
						 for (j=0; j<matrix[i].length-1; j++)
							 out.write(formatter.format(Math.round((double)(matrix[i][j]/numCvRuns)))+"\t"); 
						 out.write(formatter.format (Math.round((double)(matrix[i][j]/numCvRuns)))
								 +"\t"+classNames.get(i));
						 out.newLine();
					 }
				 }
				 
				 //Testing File
				 else
				 {
					 for (i=0; i<matrix.length; i++)
					 {
						 for (j=0; j<matrix[i].length-1; j++){
							 out.write(matrix[i][j]+"\t");}
						 out.write(matrix[i][j]+"\t"+classNames.get(i));
						 out.newLine();
					 }
				 }
				 out.close();
			 }

			 catch (IOException ioexc) {
				 System.out.println("Unexpected exception writing Confusion Matrix file " +  confFile);
				 System.out.println("Please check directory permission.");
				 System.exit(0);
			 } 
		 }







		 /**
		  * Parses the temporary file where performance indicators (measured fold by fold) have been stored , 
		  * and produces the output files, i.e, resultsTable and confusionMatrix. 
		  * The resultsTable creates , a row of indicators for each data set;
		  * confusion matrices are instead appended into a single file.
		  */
		 private void writeOutputFiles()
		 {			 
			 
			 writeConfMatrix("nbc");
			 writeConfMatrix("ncc2");
				
			 try{

				 boolean exists=new File(resultsFile).exists();
				 BufferedWriter out=new BufferedWriter(new FileWriter(resultsFile,true));
				 //write header if file does not exists
				 if (!exists) {
					 out.write("Dataset,Instances,Features,Classes,ValMethod,NbcAcc,(StdDev),NccDeterm,(StdDev),NccSingleAcc,(StdDev)" +
							 ",NccSetAcc,(StdDev),NccIndetOutSize,(StdDev),Nbc(Ncc-P),(StdDev),Nbc(Ncc-I),(StdDev)");
					 out.newLine();
				 }

				 out.write(datasetName+",");
				 out.write(rawDataset.size()+",");
				 out.write(featNames.size()+",");
				 out.write(classNames.size()+",");
				 if (validationMethod.equalsIgnoreCase("cv"))
					 out.write("cv"+",");
				 else
					 out.write("t&t"+",");


				 DecimalFormat formatter= new DecimalFormat("#0.00%");
				 out.write(formatter.format(ArrayUtils.arrayAvg(aNbcAcc))+",");
				 writeStdDev(aNbcAcc,out,formatter);



				

				 out.write(formatter.format(ArrayUtils.arrayAvg(aNccPrec))+",");
				 writeStdDev(aNccPrec,out,formatter);


				 if (ArrayUtils.arrayAvg(aNccSingleAcc)==-9999)
				 out.write("n.a.,n.a.,");
				 else
				 {
				 out.write(formatter.format(ArrayUtils.arrayAvg(aNccSingleAcc))+",");
				 writeStdDev(aNccSingleAcc,out,formatter);
				 }

				 DecimalFormat formatter2= new DecimalFormat("#0.0");
				 if (ArrayUtils.arrayAvg(aNccSetAcc)==-9999)
				 out.write("n.a.,n.a.,n.a.,n.a.,");
				 else{
				 out.write(formatter.format(ArrayUtils.arrayAvg(aNccSetAcc))+",");
				 writeStdDev(aNccSetAcc,out,formatter);


				 out.write(formatter2.format(ArrayUtils.arrayAvg(aNccOutputSize))+",");	
				 if (numCvRuns>1)
				 out.write(formatter2.format(ArrayUtils.arrayStDev(aNccOutputSize))+",");
				 else 
				 out.write("n.a.,");
				 }

				 if (ArrayUtils.arrayAvg(aNccSingleAcc)==-9999) 
				 out.write("n.a,n.a,");
				 else
				 {
				 out.write(formatter.format(ArrayUtils.arrayAvg(aNccP))+",");
				 writeStdDev(aNccP,out,formatter);
				 }
				 if (ArrayUtils.arrayAvg(aNccSetAcc)==-9999)
				 out.write("n.a,n.a,");
				 else{
				 out.write(formatter.format(ArrayUtils.arrayAvg(aNccI))+",");
				 writeStdDev(aNccI,out,formatter);
				 }
				 out.newLine();
				 out.close();
			 }


			 catch (IOException ioexc) {
				 System.out.println("Unexpected exception reading file " + predictionsFile+
				 " or writing results to file. Please check directory permission. ");
				 System.exit(0);
			 } 
		 }



		 private void writeStdDev(double[] array, BufferedWriter out, DecimalFormat formatter){
			 try{
				 if (numCvRuns>1){
					 out.write(formatter.format(ArrayUtils.arrayStDev(array)));
				 }
				 else {
					 out.write("n.a.");
				 }
				 out.write(",");
			 }
			 catch (IOException ioexc) {
				 System.out.println("Unexpected exception reading file " + predictionsFile+
				 " or writing results to file. Please check directory permission. ");
				 System.exit(0);
			 } 
		 }


		 /**
		  *Computes statistics accuracy of NBC NCC and Bma by analyzing the predictions saved on a temporary file; then,
		  *saves to file the computed indicators (fold by fold); parse the indicator file and allocates all the indicators into arrays.
		  *Then, both prediction file and indicator file are deleted.
		  *The file is expected to be made up by many rows arranged as follows:<p>
		  *FoldNumber | Actual Class  | NBC prediction  | NCC prediction (nc cols) | Bma prediction(1 col) | Bma num of significant models (1 col)
		  *<p>
		  *where nc is the number of classes.

		  */
		 private void analyzePredictionsFile ()
		 {

			 try {

				 String line;
				 int currentFold=-9999;
				 int previousFold=-9999;
				 LineNumberReader lnr = new LineNumberReader(new InputStreamReader(
						 new FileInputStream(predictionsFile)));
				 BufferedWriter out=new BufferedWriter(new FileWriter(indicatorsFile,false));

				 nbcConfMatrix= new int [numClasses][numClasses];
				 nccConfMatrix= new int [numClasses][numClasses];
				
				 resetCounters();

				 //==PARSE PREDICTIONS IN FILE
				 while ((line = lnr.readLine()) != null) {


					 //parse data
					 StringTokenizer st = new StringTokenizer(line, ",");
					 currentFold=Integer.parseInt(st.nextToken());
					 if (previousFold==-9999)
						 previousFold=currentFold;

					 //folder is changed, save to file the stats on the current fold 
					 //and set to zero the various counters
					 if ((currentFold!=previousFold)) 
					 {
						 saveTmpStats(out);
						 resetCounters();
						 previousFold=currentFold;
					 }

					 actualClass=Integer.parseInt(st.nextToken());
					 parseNbcNccPrediction(st);
					 instancesCounter++;
				 }

				 saveTmpStats(out);

				 //==PREDICTIONS HAVE BEEN ANALYZED
				 out.close();
				 parseIndicatorFile();


				 //delete temporary prediction file
				 boolean success = (new File(predictionsFile)).delete();
				 if (!success) {
					 // Deletion failed
					 System.out.println("Impossible deleting file "+predictionsFile+" from filesystem.");
				 }

				 //delete temporary indicators file
				 success = (new File(indicatorsFile)).delete();
				 if (!success) {
					 // Deletion failed
					 System.out.println("Impossible deleting file "+indicatorsFile+" from filesystem.");
				 }

			 }



			 catch (FileNotFoundException exc) {
				 System.out.println("Missing File " + predictionsFile);
				 System.out.println("Please check path and file name!");
				 System.exit(0);
			 } catch (IOException ioexc) {
				 System.out.println("Unexpected exception reading file " + predictionsFile+
				 " or writing results to file. Please check directory permission. ");
				 System.exit(0);
			 } 


		 }


		 /**
		  * Saves to a temporary file the performance indicators (a row for each single training/testing experiment), to be later averaged
		  */
		 private  void saveTmpStats(BufferedWriter out){
			 try{
				 //NBC accuracy
				 out.write ((double)nbcAccurate/instancesCounter+",");
				 //NCC precision
				 out.write ((double)nccPrecise/instancesCounter+",");
				 //NCC single acc
				 out.write ((double)nccPreciseAccurate/nccPrecise+",");
				 //NCC set acc
				 out.write ((double)nccSetAccurate/(instancesCounter-nccPrecise)+",");
				 //NCC out size
				 out.write((double)nccImpreciseOutputSize/(instancesCounter-nccPrecise)+",");
				 //NCCP
				 out.write((double)nbcAccNccprecise/nccPrecise+",");
				 //NCCI
				 out.write((double)nbcAccNccImprecise/nccImprecise+"");
				 out.newLine();
			 }
			 catch (IOException ioexc) {
				 System.out.println("Unexpected exception reading file " + predictionsFile+
				 " or writing results to file. Please check directory permission. ");
				 System.exit(0);
			 } 
		 }

		 /**
		  * Reads and evaluates a single Ncc prediction retrieved from the prediction file, and updates the indexes
		  * referring to NBC accuracy when NCC precise/imprecise
		  */
		 private void parseNbcNccPrediction(StringTokenizer st){
			 String currentToken;
			 int i;
			 int currentOutputSize=0;
			 Integer NCCPredictedClass=new Integer(0);
			 int NCCFirstPrediction=new Integer(0);
			 boolean NCCAccurate=false;

			 for (i =0; i<numClasses; i++)
			 {
				 currentToken=st.nextToken();
				 NCCPredictedClass = new Integer(currentToken);
				 if (i==0)
					 NCCFirstPrediction=new Integer(currentToken);
				 if (NCCPredictedClass !=6666)
					 currentOutputSize++;
				 if (NCCPredictedClass.compareTo(actualClass)==0)
					 NCCAccurate=true;
			 }

			 parseNbcPrediction(st.nextToken());

			 //updating indicators
			 if (currentOutputSize>1)
			 {
				 nccImprecise++;
				 nccImpreciseOutputSize+=currentOutputSize;
				 if (nbcAccCurrentInst)
					 nbcAccNccImprecise++;
				 if (NCCAccurate)
					 nccSetAccurate++;
			 }

			 else

			 {
				 nccPrecise++;
				 nccConfMatrix[actualClass][NCCFirstPrediction]++;
				 if (nbcAccCurrentInst)
					 nbcAccNccprecise++;
				 if (NCCAccurate)
					 nccPreciseAccurate++;
			 }
		 }


		 /**
		  * Reads and assesses a single Nbc prediction retrieved from the prediction file; its accuracy or not on the supplied instance
		  * is tracked by the member variable nbcAccCurrentInst
		  */
		 private void parseNbcPrediction(String currentToken){

			 int NBCPrediction=Integer.parseInt(currentToken);

			 nbcConfMatrix[actualClass][NBCPrediction]++;
			 if (NBCPrediction==actualClass){
//				 nbcPerClassAccurate[actualClass]++;
				 nbcAccurate++;
			 }

			 nbcAccCurrentInst=(NBCPrediction==actualClass);
		 }




		 /**
		  *Set all counters to 0 
		  */
		 private void resetCounters(){
			 nccImprecise=0;
			 nccPrecise=0;
			 nccPreciseAccurate=0;
			 nccSetAccurate=0;
			 nccImpreciseOutputSize=0;
			 nbcAccNccImprecise=0;
			 nbcAccNccprecise=0;
			 instancesCounter=0;
			 nbcAccurate=0;
		 }


		 //==DATA MEMBERS 


		 //various counters
		 private int nccImprecise;
		 private int nccPrecise;
		 private int nccPreciseAccurate;
		 private int nccSetAccurate;
		 private int nccImpreciseOutputSize;
		 private int nbcAccNccImprecise;
		 private int nbcAccNccprecise;

		 private int instancesCounter;
		 private int numClasses;
		 private int nbcAccurate;
		 private int actualClass;

		 private Boolean nbcAccCurrentInst;


		 //array of indicators (for each indicator, an array of values, each values referring to a different CV run)
		 private double[] aNbcAcc;
		 private double[] aNccPrec;
		 private  double[] aNccSingleAcc ;
		 private  double[] aNccSetAcc ;
		 private  double[] aNccOutputSize ;
		 private  double[] aNccP ;
		 private double[] aNccI ;


		 /**
		  * Temporary file, where the performance indicators are saved fold-by-fold 
		  */
		 private String indicatorsFile;

		 /**
		  * how many training/testing experiments (1 for testing, num runs*num folds for CV) the predictions saved to file reg
		  */
		 private int numRuns;
		 
		 private int[][] nbcConfMatrix;
		 private int[][] nccConfMatrix;
	 }

}