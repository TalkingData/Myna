/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                : 
 * Version                 : 0.00.0
 * Modification history    : 2011.05.20
 **/

package dice.data.io;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dice.data.SimpleInstances;

/**
 * An {@link DataReader} implementation can read arff file.
 *
 * @author Xiatian Zhang
 * @version 0.000
 * @date 2011-5-20 10:40:56p.m
 * @project Dice
 */
public class ArffReader implements DataReader {

    /**
     * The constant for "@relation" label of arff file.
     */
    private final String RELATION = "@relation";

    /**
     * The constant for "@attribute" label of arff file.
     */
    private final String ATTRIBUTE = "@attribute";

    /**
     * The constant for "@data" label of arff file.
     */
    private final String DATA = "@data";

    /**
     * The constant for "numeric" label of arff file, which indicates the attribute is numeric type.
     */
    private final String NUMERIC = "numeric";

    /**
     * The constant for "real" label of arff file, which indicates the attribute is real type.
     */
    private final String REAL = "real";

    /**
     * In memory, the numeric attribute is represent as "-1".
     */
    private final int N_NUMERIC = -1;

    /**
     * In memory, the numeric attribute is represent as "0".
     */
    private final int N_REAL = 0;

    /**
     * In arff file, "?" means the missing data.
     */
    private final String UNKNOWN = "?";

    /**
     * Split character.
     */
    private final String SPLIT = "\t| ";

    /**
     * Comma character.
     */
    private final String COMM = ",";

    /**
     * The data source file.
     */
    private String filePath;

    /**
     * The number of attributes.
     */
    private int attrSize;

    /**
     * The flag indicate whether the data source is sparse of dense. True means sparse.
     */
    private boolean isSparse;

    /**
     * Check the data storage form in the source file. If it is sparse, the function will set
     * {@link #isSparse} to true, otherwise to false.
     */
    private static Map<Integer, Map<String, Integer>> globalNominalAttrs;
    private static int[] globalAttributes;

    private void checkData() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String ln;
            boolean flag = false;
            while ((ln = br.readLine()) != null) {
                if (flag) {
                    if (ln.startsWith("{")) {
                        isSparse = true;
                    } else {
                        isSparse = false;
                    }
                    flag = false;
                }
                if (ln.equals(DATA)) {
                    flag = true;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    /**
     * Get the data instances from the source file.
     */
    public SimpleInstances getInstances() {
        checkData();
        if (isSparse) {
            return getSparseInstances();
        } else {
            return getDenseInstances();
        }
    }

    /**
     * Get the instances from sparse source file.
     * 
     * @return
     */
    private SimpleInstances getSparseInstances() {
        BufferedReader br;
        try {
            String relation = "relation";
            Map<Integer, Map<String, Integer>> nominalAttrs = new HashMap<Integer, Map<String, Integer>>();

            String ln;
            int c = 0;
            int instSize = 0;
            int vSize = 0;
            boolean isHeader = true;
            br = new BufferedReader(new FileReader(filePath));
            while ((ln = br.readLine()) != null) {
                if (ln.length() < 1 || ln.startsWith("%")) {
                    continue;
                }
                if (ln.equals(DATA)) {
                    isHeader = false;

                    continue;
                }
                if (isHeader) {
                    String[] s = ln.split(SPLIT, 3);
                    for (int i = 0; i < s.length; i++) {
                        s[i] = s[i].trim();
                    }
                    if (s.length > 1) {
                        if (s[0].equalsIgnoreCase(RELATION)) {
                            relation = s[1];
                        } else if (s.length >= 3 && s[0].equalsIgnoreCase(ATTRIBUTE)) {
                            if (s[2].startsWith("{")) {
                                s[2] = s[2].substring(1, s[2].length() - 1);
                                String[] ss = s[2].split(COMM);
                                int t = 0;
                                Map<String, Integer> nMap = new HashMap<String, Integer>();
                                for (String e : ss) {
                                    nMap.put(e.trim(), t);
                                    t++;
                                }
                                nominalAttrs.put(c, nMap);
                            }
                            c++;
                        }
                    }
                } else {

                    ln = ln.substring(1, ln.length() - 1);

                    String[] s = ln.split(COMM);

                    vSize += s.length;

                    instSize++;

                }
            }

            br.close();

            int[] attributes = new int[attrSize];
            int[][] ids = new int[instSize][];
            double[][] mat = new double[instSize][];

            isHeader = true;
            c = 0;
            int ic = 0;
            br = new BufferedReader(new FileReader(filePath));
            while ((ln = br.readLine()) != null) {
                if (ln.length() < 1 || ln.startsWith("%")) {
                    continue;
                }
                if (ln.equals(DATA)) {
                    isHeader = false;
                    continue;
                }
                if (isHeader) {
                    String[] s = ln.split(SPLIT, 3);
                    for (int i = 0; i < s.length; i++) {
                        s[i] = s[i].trim();
                    }
                    if (s.length >= 3 && s[0].equals(ATTRIBUTE)) {
                        if (c < attributes.length) {
                            if (s[2].equalsIgnoreCase(NUMERIC)) {
                                attributes[c] = N_NUMERIC;
                            } else if (s[2].equalsIgnoreCase(REAL)) {
                                attributes[c] = N_REAL;
                            } else if (s[2].startsWith("{")) {
                                attributes[c] = nominalAttrs.get(c).size();
                            }
                            c++;
                        }
                    }
                } else {

                    ln = ln.substring(1, ln.length() - 1);

                    String[] s = ln.split(COMM);
                    int[] indexs = new int[s.length];
                    double[] values = new double[s.length];
                    for (int i = 0; i < s.length; i++) {

                        String[] ss = s[i].split(SPLIT);

                        indexs[i] = Integer.parseInt(ss[0]);

                        if (indexs[i] < attributes.length) {
                            if (attributes[indexs[i]] < 1) {
                                values[i] = Double.parseDouble(ss[1]);
                            } else {
                                values[i] = nominalAttrs.get(indexs[i]).get(ss[1]);
                            }
                        }
                    }
                    mat[ic] = values;
                    ids[ic] = indexs;
                    ic++;
                }
            }

            br.close();

            return new SimpleInstances(attributes, mat, ids, relation);

        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }

    // //////////////////////////////////add by Ye////////////////////////////////////////////////
    @SuppressLint("UseSparseArrays")
    public SimpleInstances getTestInstances(String testStr) {

        int[] attributes;
        double[][] matrix;

        String relation = "relation";
        Map<Integer, Map<String, Integer>> nominalAttrs = new HashMap<Integer, Map<String, Integer>>();
        nominalAttrs = ArffReader.globalNominalAttrs;
        attributes = ArffReader.globalAttributes;

        String ln = "";
        matrix = new double[1][attrSize];

        ln = testStr;

        String[] s = ln.split(COMM);
        for (int i = 0; i < s.length; i++) {
            s[i] = s[i].trim();
        }
        for (int i = 0; i < s.length; i++) {
            if(i >= attributes.length)
                break;
            if (attributes[i] < 1) {// Numeric or Real attributes
                if (s[i].equals(UNKNOWN)) {
                    matrix[0][i] = Double.MAX_VALUE;
                } else {
                    matrix[0][i] = Double.parseDouble(s[i]);
                }
            } else {
                if (s[i].equals(UNKNOWN)) {
                    matrix[0][i] = 0;
                } else {
                    Map<String, Integer> temp = nominalAttrs.get(i);
                    if(temp == null){
                        matrix[0][i] = 0;
                    }
                    else{
//                        Log.i("rHAR", String.format("i = %d, s[i] = %s", i, s[i]));
                        Integer tempInt = temp.get(s[i].trim());
                        if(tempInt != null){
                            matrix[0][i] = tempInt;
                        }
                    }
                }
            }
        }

        Log.i("rHAR", "attributes" + Arrays.toString(attributes));
        Log.i("rHAR", "matrix" + Arrays.toString(matrix[0]));
        return new SimpleInstances(attributes, matrix, null, relation);

    }

    /**
     * Get instances from dense source file.
     * 
     * @return
     */
    private SimpleInstances getDenseInstances() {
        BufferedReader br;
        try {

            int[] attributes;
            double[][] matrix;

            String relation = "relation";
            Map<Integer, Map<String, Integer>> nominalAttrs = new HashMap<Integer, Map<String, Integer>>();

            String ln;
            int c = 0;
            int instSize = 0;
            boolean isHeader = true;
            br = new BufferedReader(new FileReader(filePath));
            while ((ln = br.readLine()) != null) {
                if (ln.length() < 1 || ln.startsWith("%")) {
                    continue;
                }
                if (ln.equalsIgnoreCase(DATA)) {
                    isHeader = false;
                    continue;
                }
                if (isHeader) {
                    String[] s = ln.split(SPLIT, 3);
                    for (int i = 0; i < s.length; i++) {
                        s[i] = s[i].trim();
                    }
                    if (s.length > 1) {
                        if (s[0].equalsIgnoreCase(RELATION)) {
                            relation = s[1];
                        } else if (s.length >= 3 && s[0].equalsIgnoreCase(ATTRIBUTE)) {

                            if (s[2].startsWith("{")) {

                                s[2] = s[2].substring(1, s[2].length() - 1);
                                String[] ss = s[2].split(COMM);
                                int t = 0;
                                Map<String, Integer> nMap = new HashMap<String, Integer>();
                                for (String e : ss) {
                                    nMap.put(e.trim(), t);
                                    t++;

                                }
                                nominalAttrs.put(c, nMap);
                            }
                            c++;
                        }
                    }
                } else {
                    instSize++;
                }
            }

            br.close();
            matrix = new double[instSize][attrSize];
            attributes = new int[attrSize];

            c = 0;
            int ic = 0;
            instSize = 0;
            isHeader = true;
            br = new BufferedReader(new FileReader(filePath));
            while ((ln = br.readLine()) != null) {
                if (ln.length() < 1 || ln.startsWith("%")) {
                    continue;
                }
                if (ln.equalsIgnoreCase(DATA)) {
                    isHeader = false;
                    c = 0;
                    continue;
                }
                if (isHeader) {
                    String[] s = ln.split(SPLIT, 3);
                    for (int i = 0; i < s.length; i++) {
                        s[i] = s[i].trim();
                    }
                    if (s.length >= 3 && s[0].equalsIgnoreCase(ATTRIBUTE) && c < attrSize) {
                        if (s[2].equalsIgnoreCase(NUMERIC)) {
                            attributes[c] = N_NUMERIC;
                        } else if (s[2].equalsIgnoreCase(REAL)) {
                            attributes[c] = N_REAL;
                        } else if (s[2].startsWith("{")) {
                            attributes[c] = nominalAttrs.get(c).size();
                        }
                        c++;
                    }
                } else {
                    String[] s = ln.split(COMM);
                    for (int i = 0; i < s.length; i++) {
                        s[i] = s[i].trim();
                    }
                    System.out.print(Arrays.toString(s));
                    for (int i = 0; i < (s.length > attributes.length ? attributes.length
                            : s.length); i++) {

                        if (attributes[i] < 1) {// Numeric or Real attributes
                            if (s[i].equals(UNKNOWN)) {
                                matrix[ic][i] = Double.MAX_VALUE;
                            } else {
                                matrix[ic][i] = Double.parseDouble(s[i]);
                            }
                        } else {
                            if (s[i].equals(UNKNOWN)) {
                                matrix[ic][i] = 0;
                            } else {
                                matrix[ic][i] = nominalAttrs
                                        .get(i)
                                        .get(s[i].trim());
                            }
                        }
                    }

                    if (instSize % 1000 == 0) {
                        // System.out.println("2:"+instSize);
                    }

                    ic++;
                    instSize++;

                    /*
                     * if(instSize>=num){ break; }
                     */
                }
            }

            br.close();
            globalNominalAttrs = nominalAttrs;
            globalAttributes = attributes;
            return new SimpleInstances(attributes, matrix, null, relation);

        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get current source file path.
     * 
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Set current source file path.
     * 
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Set the size of attributes.
     * 
     * @param attrSize
     */
    public void setAttrSize(int attrSize) {
        this.attrSize = attrSize;
    }

}
