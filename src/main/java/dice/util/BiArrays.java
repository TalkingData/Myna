/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011.05.20
 **/
package dice.util;

/**
 * An tool class to help sort two related array by one at the same time.
 * In all the <code>sort</code> functions, the first array is used to 
 * sort the two array.
 * @author Zhang Xiatian
 *
 */
public class BiArrays {


    public static void sort(int[] array, Object[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(int[] array, double[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(int[] array, float[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(double[] array, int[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(double[] array, boolean[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(float[] array, boolean[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(double[] array, double[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(float[] array, int[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(int[] array, int[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(int[] array, short[] vArray) {
        sort(0, array.length, array, vArray);
    }


    public static void sort(double[][] array, int col) {
        sort(0, array.length, array, col);
    }

    private static int med3(int[] array, int a, int b, int c) {
        int x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }


    private static void sort(int start, int end, int[] array, Object[] vArray) {
        int temp;
        Object vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        int partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }


    private static void sort(int start, int end, int[] array, double[] vArray) {
        int temp;
        double vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        int partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }


    /////////////////////////////////////////////////////////////////////////


    public static void sort(float[] array, Object[] vArray) {
        sort(0, array.length, array, vArray);
    }

    public static void sort(float[] array, double[] vArray) {
        sort(0, array.length, array, vArray);
    }

    private static int med3(float[] array, int a, int b, int c) {
        float x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }

    private static void sort(int start, int end, float[] array, Object[] vArray) {
        float temp;
        Object vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        float partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }

    private static void sort(int start, int end, float[] array, boolean[] vArray) {
        float temp;
        boolean vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        float partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }


    private static void sort(int start, int end, float[] array, double[] vArray) {
        float temp;
        double vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        float partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }


    ///////////////////////////////////////////////////

    private static int med3(double[] array, int a, int b, int c) {
        double x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }

    private static void sort(int start, int end, double[] array, int[] vArray) {
        double temp;
        int vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        double partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }


    private static void sort(int start, int end, double[] array, boolean[] vArray) {
        double temp;
        boolean vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        double partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }

///////////////////////////////////////////////////

	/*private static int med3(int[] array, int a, int b, int c) {
        double x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }*/

    private static void sort(int start, int end, int[] array, int[] vArray) {
        int temp;
        int vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        double partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }


//////////////////////////////////////////////////

    public static void sort(int start, int end, int[] array, short[] vArray) {
        int temp;
        short vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        double partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }


///////////////////////////////////////////////////


    private static void sort(int start, int end, float[] array, int[] vArray) {
        float temp;
        int vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        double partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }

//////////////////////////////////////////////////////

    public static void sort(int start, int end, int[] array, float[] vArray) {
        int temp;
        float vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        int partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;

            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }

///////////////////

    private static void sort(int start, int end, double[] array, double[] vArray) {
        double temp;
        double vTemp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                    //add by xiatian
                    vTemp = vArray[j];
                    vArray[j] = vArray[j - 1];
                    vArray[j - 1] = vTemp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        double partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;

                    //add by xiatian
                    vTemp = vArray[a];
                    vArray[a++] = vArray[b];
                    vArray[b] = vTemp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;

                    //add by xiatian
                    vTemp = vArray[c];
                    vArray[c] = vArray[d];
                    vArray[d--] = vTemp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

            //add by xiatian
            vTemp = vArray[b];
            vArray[b++] = vArray[c];
            vArray[c--] = vTemp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian
            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

            //add by xiatian

            vTemp = vArray[l];
            vArray[l++] = vArray[h];
            vArray[h++] = vTemp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, vArray);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, vArray);
        }
    }

    //////////////////////////////////
    private static int med3(double[][] array, int a, int b, int c, int col) {
        double x = array[a][col], y = array[b][col], z = array[c][col];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }


    private static void sort(int start, int end, double[][] array, int col) {
        double[] temp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1][col] > array[j][col]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;

                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length), col);
                middle = med3(array, middle - length, middle, middle + length, col);
                top = med3(array, top - (2 * length), top - length, top, col);
            }
            middle = med3(array, bottom, middle, top, col);
        }
        double partionValue = array[middle][col];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b][col] <= partionValue) {
                if (array[b][col] == partionValue) {
                    temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;
                }
                b++;
            }
            while (c >= b && array[c][col] >= partionValue) {
                if (array[c][col] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d] = temp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b] = array[c];
            array[c] = temp;

        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l] = array[h];
            array[h] = temp;

        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array, col);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array, col);
        }
    }

}
