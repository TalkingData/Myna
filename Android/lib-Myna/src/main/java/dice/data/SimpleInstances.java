/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011.05.13
 **/
package dice.data;

import java.util.Iterator;

/**
 * A simple implementation of the interface {@link Instances}.
 *
 * @author Zhang Xiatian
 */
public class SimpleInstances implements Instances {

    private static final long serialVersionUID = 8836910060167142716L;

    /**
     * Keep the reference to this object.
     */
    Instances thiz = this;

    /**
     * Indicate the storage form. True is sparse, false is dense.
     */
    boolean isSparse;

    /**
     * The array holds the meta information of attributes.
     * @see Instances#getAttributes()
     */
    int[] attributes;

    /**
     * The index matrix for the data set. Only used for sparse data.
     * @see Instances#getIds()
     */
    int[][] ids;//

    /**
     * The data matrix for the data set.
     * @see Instances#getMat()
     */
    double[][] mat;

    /**
     * The object of {@link Instance} hold the data for a record,
     * which is determined by {@link #cursor}.
     */
    Instance instance;

    /**
     * The indicator indicates which record can be get from the {@link #instance}
     */
    int cursor;

    /**
     * An {@link Iterator} for {@link Instance}
     */
    Iterator<Instance> iterator;

    /**
     * The name of the data set.
     */
    String relation;

    /**
     * For sparse storage form, it keep the default value for the element ignored.
     */
    double miss;

    /**
     * Create a SimpleInstances.
     * @param attrs The attribute type array, @see {@link #attributes}
     * @param matrix The data matrix, @see {@link #mat}
     * @param indexes The index matrix, @see {@link #ids}
     * @param relation The name of the data set, @see {@link #relation}
     */
    public SimpleInstances(int[] attrs, double[][] matrix, int[][] indexes, String relation) {

        if (indexes == null) {
            isSparse = false;
        } else {
            isSparse = true;
        }
        this.attributes = attrs;
        this.ids = indexes;
        this.mat = matrix;

        this.relation = relation;
        cursor = -1;

        instance = new Instance() {

            public double getValue(int attrId) {

                return mat[cursor][attrId];
            }

            public double[] getValues() {
                return mat[cursor];
            }

            public void setValue(int attrId, double value) {
                mat[cursor][attrId] = value;

            }

            public void setValues(double[] values) {
                mat[cursor] = values;
            }

            @Override
            public int getIndex() {
                // TODO Auto-generated method stub
                return cursor;
            }

            @Override
            public int[] getIndexs() {
                return ids[cursor];
            }

            @Override
            public Instances getInstances() {
                return thiz;
            }

            @Override
            public void setIndexs(int[] indexs) {
                // TODO Auto-generated method stub

            }
        };

        //Create the iterator.
        iterator = new Iterator<Instance>() {
            public boolean hasNext() {
                return cursor < mat.length - 1;
            }

            public Instance next() {
                cursor++;
                return instance;
            }

            public void remove() {
                //Do nothing by now;
            }
        };
    }

    /**
     * Get the iterator.
     */
    public Iterator<Instance> iterator() {
        cursor = -1;
        return iterator;

    }

    /**
     * @see Instances#getAttributes()
     */
    public int[] getAttributes() {
        return attributes;
    }


    /**
     * @see Instances#getRelation()
     */
    public String getRelation() {
        return relation;
    }

    /**
     * @see Instances#getAttrSize()
     * @return The number of attributes.
     */
    public int getAttrSize() {
        return attributes.length;
    }

    /**
     * @see Instances#size()
     */
    public int size() {
        return mat.length;
    }


    @Override
    /**
     * @see Instances#get()
     */
    public Instance get(int index) {
        cursor = index;
        return instance;
    }

    @Override
    /**
     * @see Instances#isSparse()
     */
    public boolean isSparse() {
        return isSparse;
    }

    @Override
    /**
     * @see Instances#getIds()
     */
    public int[][] getIds() {
        return ids;
    }

    @Override
    /**
     * @see Instances#getMat()
     */
    public double[][] getMat() {
        return mat;
    }

    @Override
    /**
     * @see Instances#setMiss(double miss)
     * @param miss
     */
    public void setMiss(double miss) {
        this.miss = miss;
    }

    /**
     * @see Instances#setData(int[][], double[][])
     */
    public void setData(int[][] ids, double[][] mat) {
        this.ids = ids;
        this.mat = mat;
    }


}
