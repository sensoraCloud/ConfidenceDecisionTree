/*
 *    KearnsSplitCriterion.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
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
 */
package moa.classifiers.core.splitcriteria;

import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.tasks.TaskMonitor;
import weka.core.Utils;

/**
 * Class for computing splitting criteria using Gini
 * with respect to distributions of class values.
 * The split criterion is used as a parameter on
 * decision trees and decision stumps.
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 7 $
 */
public class KearnsSplitCriterion extends AbstractOptionHandler implements
        SplitCriterion {

    private static final long serialVersionUID = 1L;

    @Override
    public double getMeritOfSplit(double[] preSplitDist,
            double[][] postSplitDists) {
        double totalWeight = 0.0;
        double[] distWeights = new double[postSplitDists.length];
        
        for (int i = 0; i < postSplitDists.length; i++) {
            distWeights[i] = Utils.sum(postSplitDists[i]);
            totalWeight += distWeights[i];
        }
        double kearns = 0.0;
        for (int i = 0; i < postSplitDists.length; i++) {
            kearns += (distWeights[i] / totalWeight)
                    * computeKearns(postSplitDists[i], distWeights[i]);
        }
        return  Math.sqrt( (1.0 - kearns) /2 ) ;
    }

    @Override
    public double getRangeOfMerit(double[] preSplitDist) {
        return 1.0;
    }

    public static double computeKearns(double[] dist, double distSumOfWeights) {
        double kearns = 1.0;
        for (int i = 0; i < dist.length; i++) {
            double relFreq = dist[i] / distSumOfWeights;
            kearns -= relFreq * relFreq;
        }
        return kearns;
    }

    public static double computeKearns(double[] dist) {
        return computeKearns(dist, Utils.sum(dist));
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
            ObjectRepository repository) {
        // TODO Auto-generated method stub
    }
}
