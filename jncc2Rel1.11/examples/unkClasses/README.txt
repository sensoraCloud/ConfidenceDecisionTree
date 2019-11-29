The arff files are distributed with JNCC2 to allow one to become familiar with
the command-line invocation of Jncc2.
In particular, the examples in this directory involve classification of
instances of testing set, whose actual classes are unknown.

There are 2 examples: iris and glass; their files are stored in two
different directories.
For each example, we provide a training and a testing file, from which the
actual classes have been removed.


HOW TO WORK WITH THE EXAMPLES 

Open a command-line prompt and reach the directory where you have copied the
example files.

If you have defined the CLASSPATH variable as explained in the user manual,
you can run the classifier as follows:

java jncc20.Jncc . <currentExample>.training.arff
<currentExample>.testingUnkClasses.arff unknownclasses 

Alternatively, if you did not define the CLASSPATH, you can run the classifier
as follows:

java -jar </path/to/jncc.jar> . <currentExample>.training.arff
<currentExample>.testingUnkClasses.arff unknownclasses 

where </path/to/jncc.jar> is the address of the jar file of jncc.jar on your
filesystem.

Since the actual class of the testing file are unknown, it is not possible to
evaluate the classifier performance in this case.
The software produces as output a single file, which contains the predictions
issued by NBC and NCC2 on the instances of the testing set. 

You might want check that the files you have produced are identical to 
those present in the directory checkResults.
This ensures that the classifier is working as expected.


If you need to produce scientific results, please do not use these ARFF files, 
which have have been arbitrarily divided into training and testing set.
Instead, download the original ARFF files from
http://weka.sourceforge.net/wiki/index.php/Datasets.

For further information about JNCC2, please visit:
http://www.idsia.ch/~giorgio/jncc2.html
