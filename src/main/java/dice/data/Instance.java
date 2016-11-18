/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011-05-12
 **/
package dice.data;


/**
 *
 * An Instance stores a data record in memory.
 * @author Zhang Xiatian
 * @see Instances
 *
 */
public interface Instance {

    /**
     * Get the array holds the values of attributes of
     * the Instance. Note, for sparse storage form,
     * the returned array is a compact model which
     * only keep the non-zero (or any other default value)
     * in the array. In order to determine the values
     * belong to which attribute, you should to check
     * the index array (can be gotten from {@link #getIndexs()})
     * function.
     * @return The value array.
     */
    double[] getValues();

    /**
     * Get the value of the assigned attribute.
     * @param attrId Attribute id.
     * @return The value of the assined attribute.
     */
    double getValue(int attrId);

    /**
     * Set the value array
     * @param values Value array.
     */
    void setValues(double[] values);

    /**
     * Set the value of a attribute
     * @param attrId Attribute id.
     * @param value  The value to be set.
     */
    void setValue(int attrId, double value);

    /**
     * Get the position or index of
     * @return The index of current instance.
     */
    int getIndex();

    /**
     * Set the index array of the values.
     * The index array is only used for sparse storage form.
     * @param indexs Index array.
     */
    void setIndexs(int[] indexs);

    /**
     * Get the index array of the values.
     * The index array is only used for sparse storage form.
     * @return The index array.
     */
    int[] getIndexs();

    /**
     * Return the {@link Instances} hold the current Instance.
     * @return The Instances.
     */
    Instances getInstances();

}
