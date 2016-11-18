/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011-05-15
 **/
package dice.data;

import java.io.Serializable;
import java.util.Iterator;

/**
 *
 * An Instances stores a data set in memory.
 * @author Zhang Xiatian
 * @see Instance
 *
 */
public interface Instances extends Serializable {

    /**
     * Get the arrays, whose elements indicate the types of attributes.
     * 0 indicates the attribute is numeric or real attribute.
     * The number is greater than 0 indicate the numbers of values of each attributes.
     *
     * @return The attribute type indicator array.
     */
    int[] getAttributes();

    /**
     * Return a Iterator of the {@link dice.data.Instance}
     * @return The iteraotr of Instance.
     */
    Iterator<Instance> iterator();

    /**
     * Get the number of {@link dice.data.Instance}s.
     * @return The number of {@link dice.data.Instance}s.
     */
    int size();

    /**
     * Get the number of attributes.
     * @return The number of attributes.
     */
    int getAttrSize();

    /**
     * Get the relation name of the data set.
     * @return The relation name of the data set.
     */
    String getRelation();

    /**
     * Get an instance by the assigned index or position.
     * @param index The index or position of the {@link Instance} wanted.
     * @return The instance wanted.
     */
    Instance get(int index);

    /**
     * Get the storage status of the Instances.
     * @return If the data storage form is sparse, then return true. Otherwise, return false.
     */
    boolean isSparse();

    /**
     * Get the index matrix.
     * @return The index matrix. If the {@link #isSparse()} return
     * false, the return will be null.
     */
    int[][] getIds();

    /**
     * Get the data matrix.
     * @return The data matrix.
     */
    double[][] getMat();


    /**
     * Put the index and data matrix into it.
     * @param ids The index matrix.
     * @param mat The data matrix.
     */
    void setData(int[][] ids, double[][] mat);

    /**
     * Set the default value for the ignored element
     * in the sparse storage form.
     * @param miss
     */
    void setMiss(double miss);

}
