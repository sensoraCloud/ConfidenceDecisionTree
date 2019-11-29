The arff files are distributed with JNCC2 to allow one to become familiar with
the command-line invocation of Jncc2.
In particular, the examples in this directory involve special treatment of
missing data.

There are 2 examples: hepatitis and labor; their files are stored in two
different directories, to avoid conflicting versions of file NonMar.txt .

For each example, we provide a training and a testing file.

Moreover, for each example we provide a file NonMar.txt.
Each file NonMar.txt contains 3 declarations:
-a feature is declared as nonMAR in training only;
-a feature declared as nonMAR in testing only;
-a feature declared as nonMAR both in training and testing.


HOW TO WORK WITH THE EXAMPLES 

Open a command-line prompt and reach the directory where you have copied the
example files.

If you want to learn how to declare nonMAR feature, simply read the
declarations contained in file NonMar.txt.

If you have defined the CLASSPATH variable as explained in the user manual,
you can run the classifier as follows:

java jncc20.Jncc . <currentExample>.training.arff <currentExample>.testing.arff 

Alternatively, if you did not define the CLASSPATH, you can run the classifier
as follows:

java -jar </path/to/jncc.jar> . <currentExample>.training.arff
<currentExample>.testing.arff  

where </path/to/jncc.jar> is the address of the jar file of jncc.jar on your
filesystem.


The software should produce two files: 
1) ResultsTable.csv
2) ConfMatrices.txt

You might want check that the files you have produced are identical to 
those present in the directory checkResults.
This ensures that the classifier is working as expected.

Then, you can assess how the NonMAR features affect the performance by changing
the declarations in file NonMar.txt; you can then compare the indicators, which
are written in different rows with file ResultsTable.csv.


If you need to produce scientific results, please do not use these ARFF files, 
which have have been arbitrarily divided into training and testing set.
Instead, download the original ARFF files from
http://weka.sourceforge.net/wiki/index.php/Datasets.

For further information about JNCC2, please visit:
http://www.idsia.ch/~giorgio/jncc2.html
