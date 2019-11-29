The examples in this directory are designed to allow one to
become familiar with the command-line invocation of Jncc2.

There are 3 examples: iris, contact-lenses and glass.
For each example, we provide a training and a testing file.

Open a command-line console and reach the dirsectory where you have copied the
example files. If you have defined the CLASSPATH variable as explained in the
user manual, you can run the classifier as follows:

java jncc20.Jncc . <currentExample>.training.arff  <currentExample>.testing.arff 

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
those present in the directory checkResults; this ensures that the classifier is
working as expected.

If you need to produce scientific results, please do not use these ARFF files, 
which have have been arbitrarily divided into training and testing set.
Instead, download the original ARFF files from
http://weka.sourceforge.net/wiki/index.php/Datasets.

For further information about JNCC2, please visit:
http://www.idsia.ch/~giorgio/jncc2.html
