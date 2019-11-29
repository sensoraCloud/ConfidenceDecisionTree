/**
 * ArrayUtils.java
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

/**
 * Utilities for computing average, sum,
 * standard deviation etc. of arrays, ignoring missing data, denoted as -9999. 
 */
public final class ArrayUtils {


	/**Converts an ArrayList into an  array.*/
	static int[] arrList2Array(ArrayList<Integer> ProvidedArrayList) {
		int[] ReturnedArray = new int[ProvidedArrayList.size()];
		for (int i = 0; i < ProvidedArrayList.size(); i++) {
			ReturnedArray[i] = ProvidedArrayList.get(i);
		}
		return ReturnedArray;
	}

	/**Converts an ArrayList into  an array.*/
	static double[] arrList2Array(ArrayList<Double> ProvidedArrayList) {
		double[] ReturnedArray = new double[ProvidedArrayList.size()];
		for (int i = 0; i < ProvidedArrayList.size(); i++) {
			ReturnedArray[i] = ProvidedArrayList.get(i);
		}
		return ReturnedArray;
	}
	
	/**Returns the maximum of an array*/
	static double max(double[] providedArray) {
		double max=-Double.MAX_VALUE;
		for (int i = 0; i < providedArray.length; i++) {
			if (max<providedArray[i])
				max=providedArray[i];
			}
		return max;
	}




	/**Computes the sum of the array, ignoring missing data (denoted as -9999). Returns an array of two values: the sum (pos. 0) and number of data 
	 * used for the computation (pos. 1). 
	 */
	static int[] arraySum (int[] ProvidedArray)
	{
		int[]Arr= new int[2];
		int sum=0;
		int suitableData=ProvidedArray.length;

		for (int tmpint : ProvidedArray)
		{
			if (tmpint != -9999)
				sum+=tmpint;
			else
				suitableData--;
		}
		Arr[0]=sum; Arr[1]=suitableData;
		return Arr;
	}

	/**Computes Sum_i(Arr1[i]*Arr2[i]); term Arr1[j]*Arr2[j] is not included in the computation if 
	 * Arr1[j] or Arr2[j] are -9999. 
	 * Values -9999 in position j of either Arr1 or Arr2 prevents term Arr1[j]*Arr2[j] to be included.
	 * Returns the computed value [pos.0 in the returned array] and the number of data used [pos 1].
	 */
	static int[] arraySumProduct (int[] Array1, int[] Array2)
	{
		int[]Arr= new int[2];
		int sum=0;
		int suitableData=Array1.length;

		for (int i=0; i<Array1.length; i++)
		{
			if ((Array1[i] != -9999) & (Array2[i] != -9999))
				sum+=Array1[i]*Array2[i];
			else
				suitableData--;
		}
		Arr[0]=sum; Arr[1]=suitableData;
		return Arr;
	}

	/**Computes Sum_i(Arr1[i]*Arr2[i]); term Arr1[j]*Arr2[j] is not included in the computation if 
	 * Arr1[j] or Arr2[j] are -9999. 
	 * Returns the computed value [pos.0 in the returned array] and the number of data used [pos 1].
	 */
	static double[] arraySumProduct (double[] Array1, double[] Array2)
	{
		double[]Arr= new double[2];
		double sum=0;
		int suitableData=Array1.length;

		for (int i=0; i<Array1.length; i++)
		{
			if ((Array1[i] != -9999) & (Array2[i] != -9999)){
				sum+=Array1[i]*Array2[i];
			}
			else{
				suitableData--;
			}
		}
		Arr[0]=sum; Arr[1]=suitableData;
		return Arr;
	}

	/**
	 * Computes the average of the array, ignoring missing data (denoted as -9999); 
	 * if all data are missing, returns -9999.
	 */
	static double arrayAvg(double[] ProvidedArray)
	{
		double[] Arr;
		Arr=ArrayUtils.arraySum(ProvidedArray);
		double avg;
		if (Arr[1]==0)
			avg= -9999;
		else
			avg=Arr[0]/Arr[1];
		return avg;
	}


	/**
	 * Computes the average of the array, ignoring missing data (denoted as -9999). 
	 */
	static double arrayAvg(int[] ProvidedArray)
	{
		int[] Arr;
		Arr=ArrayUtils.arraySum(ProvidedArray);
		double avg;
		if (Arr[1]==0){
			avg=-9999;
		}
		else
		{
			avg=Arr[0]/Arr[1];
		}
		return avg;
	}


	/**
	 * Computes the StdDev of the array, ignoring missing data (denoted as  -9999); 
	 * if all data are missing, returns -9999.
	 */
	static double arrayStDev (double[] ProvidedArray)
	{
		double stdev=0;
		double[] Arr;
		Arr=ArrayUtils.arraySum(ProvidedArray);

		double avg=Arr[0]/Arr[1];
		double n=Arr[1];

		//if all data are missing
		if (n==0)
			return -9999;

		for (double tmp : ProvidedArray)
		{
			//if data are missing, they are not to be counted
			if (tmp != -9999)
				stdev+=Math.pow((tmp-avg),2);
		}

		stdev/=(n-1);
		stdev=Math.sqrt(stdev);
		return stdev;
	}

	/**
	 * Computes the StdDev of the array, ignoring missing data (denoted as -9999). 
	 */
	static double arrayStDev (int[] ProvidedArray)
	{
		double stdev=0;
		int[] Arr;
		Arr=ArrayUtils.arraySum(ProvidedArray);
		double avg=(double) Arr[0]/Arr[1];
		double n=Arr[1];

		//if all data are missing
		if (n==0)
			return -9999;

		for (double tmp : ProvidedArray)
		{
			if (tmp != -9999){
				stdev+=Math.pow((tmp-avg),2);
			}
		}

		stdev/=(n-1);
		stdev=Math.sqrt(stdev);
		return stdev;
	}


	/**Computes the sum of the array, ignoring missing data (dentoed as -9999). Returns an array of two values: the sum (pos. 0) and number of data 
	 * used for the computation (pos. 1).
	 */
	static double[] arraySum (double[] ProvidedArray)
	{

		double[] Arr= new double[2];
		double sum=0;
		double suitableData=ProvidedArray.length;

		for (double tmp : ProvidedArray)
		{
			if (tmp != -9999)
				sum+=tmp;
			else
				suitableData-= 1;

		}
		Arr[0]=sum; Arr[1]=suitableData;
		return Arr;
	}



}
