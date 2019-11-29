/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.classifiers;

import java.util.Random;
import weka.core.Instance;
import weka.core.Utils;

/**
 *
 * @author Rocco De Rosa
 */
public class FilteringHoeffdingTreeV1 extends HoeffCesaBoundTree {

    protected Random randomTest = new Random();
    protected double selNumerator;
    protected int pos_class_index;

    public int getPos_class_index() {
        return pos_class_index;
    }

    public void setPos_class_index(int pos_class_index) {
        this.pos_class_index = pos_class_index;
    }

    public FilteringHoeffdingTreeV1(int seed, double c1, int pos_class_index) {
        super(seed);
        this.selNumerator = c1;
        this.pos_class_index=pos_class_index;
    }

    public double getSelNumerator() {
        return selNumerator;
    }

    public boolean isPositive(Node node) {

        double[] dis = node.getObservedClassDistribution();

        //if negative class (first class prob) check consistency 

        if (dis.length > 1) {

            double p_neg = dis[Math.abs(pos_class_index-1)] / Utils.sum(dis);

            double p_pos = dis[pos_class_index] / Utils.sum(dis);

            if (p_pos > p_neg) {
                return true;
            } else {

                double leafBound = computeConsistentBound(1.0,
                        this.splitConfidenceOption.getValue(),
                        Utils.sum(dis));

                double t = randomTest.nextDouble();

                //return ((p_neg - p_pos > leafBound) || (leafBound < this.tieThresholdOption.getValue()) || (t <= calculateTrainingBound(node)));

                return ((p_neg - p_pos > leafBound) || (t <= calculateTrainingBound(node)));

            }
        } else {
            return true;
        }




    }

    public double computeConsistentBound(double range, double confidence,
            double n) {
        return Math.sqrt(((range * range) * Math.log(4 / confidence))
                / (2.0 * n));
    }

    public double calculateTrainingBound(Node node) {
        return this.selNumerator / Math.sqrt(Utils.sum(node.getObservedClassDistribution()));
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {

        super.getVotesForInstance(inst);
        //positive is the second class.. [-1 1] or [1 2]
        if (lastFoundNode != null && isPositive(lastFoundNode)) {
            lastChosenClass = 1;
            if (pos_class_index==0)
                return new double[]{1.0, 0.0};
            else
                return new double[]{0.0, 1.0};
        } else {
            lastChosenClass = 0;
           if (pos_class_index==0)
                return new double[]{0.0, 1.0};
            else
                return new double[]{1.0, 0.0};
        }

    }

    @Override
    protected boolean passesFilterCondition() {
        return this.lastChosenClass == 1;
    }

    @Override
    public String getName() {
        return String.format("FHTV1[C=%.1f,s=%d]",
                this.selNumerator,
                this.seed);

    }
}
