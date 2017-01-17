/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011.05.19
 **/
package dice.tree.structure;

import java.util.Arrays;

import dice.util.BiArrays;

/**
 * Leaf node of the a decision tree.
 * @author Zhang Xiatian
 *
 */
public class Leaf implements Node {

    private static final long serialVersionUID = -4275978923452814810L;

    /**
     * The index array of the distribution of the classes.
     */
    public int[] distIndex;

    /**
     * The array of the distribution of the classes.
     */
    public double[] dist;

    /**
     * The number of the training instances.
     */
    public int size;

    /**
     * The average number of the responding variable.
     * For regression.
     */
    public double v;

    @Override
    public void clear() {
        if (distIndex == null) {
            return;
        }

        int c = 0;
        for (int index : distIndex) {
            if (index == -1) {
                c++;
            } else {
                break;
            }
        }

        int[] newDIndex = new int[distIndex.length - c];
        System.arraycopy(distIndex, c, newDIndex, 0, distIndex.length - c);

        distIndex = newDIndex;

        double[] newDist = new double[dist.length - c];
        System.arraycopy(dist, c, newDist, 0, dist.length - c);

        dist = newDist;

    }

    /**
     * /**
     * Create {@link #distIndex} and {@link #dist} according
     * the assigned possible number of values of the split attribute.
     * This function not create the exactly the same number of
     * the parameter. It will claim a small array t avoid space waste.
     * In leaf, in some situation, the number of instances
     * may be less than the number of the possible values of the split
     * attributes. The array creation strategy is design to avoid that
     * situation.
     *
     * @param classSize The possible number of values of the split attribute.
     */
    public void addDists(int classSize) {

        int distSize = classSize < 3 ? 3 : (classSize / 10 + 3);

        dist = new double[distSize];
        distIndex = new int[distSize];
        for (int i = 0; i < distIndex.length; i++) {
            distIndex[i] = -1;
        }
    }

    /**
     * Add a new appearance class into
     * the distribution arrays.
     * @param c The class id.
     */
    private void addDist(int c) {

        int index = Arrays.binarySearch(distIndex, c);

        if (index > -1) {
            return;
        }

        if (distIndex[0] > -1) {//There are no available position


            int newLength = (int) (distIndex.length / frac) + 1;
            //System.out.println("Attr Size:"+attr+" new length:"+newLength);
            int[] newIndex = new int[newLength];
            for (int i = 0; i < newLength - distIndex.length; i++) {
                newIndex[i] = -1;
            }
            System.arraycopy(distIndex, 0, newIndex, newLength - distIndex.length, distIndex.length);

            distIndex = newIndex;

            double[] newDist = new double[newLength];
            System.arraycopy(dist, 0, newDist, newLength - dist.length, dist.length);

            dist = newDist;

        }

        distIndex[0] = c;

        BiArrays.sort(distIndex, dist);
    }

    /**
     * Increase a count for the given class.
     * @param c The
     */
    public void incDist(int c) {
        int index = Arrays.binarySearch(distIndex, c);
        if (index > 0) {
            dist[index]++;
        } else {
            addDist(c);
            dist[Arrays.binarySearch(distIndex, c)]++;
        }
    }

    /**
     * Get the distribution for the given class.
     * @param c The class id.
     * @return
     */
    public double getDist(int c) {
        int index = Arrays.binarySearch(distIndex, c);
        if (index < 0) {
            return 0;
        }
        return dist[index];
    }

    /**
     * Add a responding variable's value.
     * For regression.
     * @param v The value of responding variable.
     */
    public void addValue(double v) {
        this.v += v;
    }

    /**
     * Get the average value of the
     * responding variable in this leaf.
     * For regression.
     * @return
     */
    public double getValue() {
        if (size > 0) {
            return v / size;
        } else {
            return 0;
        }
    }

}
