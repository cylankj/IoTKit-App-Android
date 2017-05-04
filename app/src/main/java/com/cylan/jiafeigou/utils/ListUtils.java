package com.cylan.jiafeigou.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * List Utils
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-7-22
 */
public class ListUtils {

    /**
     * default join separator *
     */
    public static final String DEFAULT_JOIN_SEPARATOR = ",";

    /**
     * get size of list
     * <p>
     * <pre>
     * getSize(null)   =   0;
     * getSize({})     =   0;
     * getSize({1})    =   1;
     * </pre>
     *
     * @param <V>
     * @param sourceList
     * @return if list is null or empty, return 0, else return {@link List#size()}.
     */
    public static <V> int getSize(List<V> sourceList) {
        return sourceList == null ? 0 : sourceList.size();
    }

    /**
     * is null or its size is 0
     * <p>
     * <pre>
     * isEmpty(null)   =   true;
     * isEmpty({})     =   true;
     * isEmpty({1})    =   false;
     * </pre>
     *
     * @param <V>
     * @param sourceList
     * @return if list is null or its size is 0, return true, else return false.
     */
    public static <V> boolean isEmpty(List<V> sourceList) {
        return (sourceList == null || sourceList.size() == 0);
    }

    /**
     * compare two list
     * <p>
     * <pre>
     * isEquals(null, null) = true;
     * isEquals(new ArrayList&lt;String&gt;(), null) = false;
     * isEquals(null, new ArrayList&lt;String&gt;()) = false;
     * isEquals(new ArrayList&lt;String&gt;(), new ArrayList&lt;String&gt;()) = true;
     * </pre>
     *
     * @param <V>
     * @param actual
     * @param expected
     * @return
     */
    public static <V> boolean isEquals(ArrayList<V> actual, ArrayList<V> expected) {
        if (actual == null) {
            return expected == null;
        }
        if (expected == null) {
            return false;
        }
        if (actual.size() != expected.size()) {
            return false;
        }

        for (int i = 0; i < actual.size(); i++) {
            if (!ObjectUtils.isEquals(actual.get(i), expected.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * join list to string, separator is ","
     * <p>
     * <pre>
     * join(null)      =   "";
     * join({})        =   "";
     * join({a,b})     =   "a,b";
     * </pre>
     *
     * @param list
     * @return join list to string, separator is ",". if list is empty, return ""
     */
    public static String join(List<String> list) {
        return join(list, DEFAULT_JOIN_SEPARATOR);
    }

    /**
     * join list to string
     * <p>
     * <pre>
     * join(null, '#')     =   "";
     * join({}, '#')       =   "";
     * join({a,b,c}, ' ')  =   "abc";
     * join({a,b,c}, '#')  =   "a#b#c";
     * </pre>
     *
     * @param list
     * @param separator
     * @return join list to string. if list is empty, return ""
     */
    public static String join(List<String> list, char separator) {
        return join(list, new String(new char[]{separator}));
    }

    /**
     * join list to string. if separator is null, use {@link #DEFAULT_JOIN_SEPARATOR}
     * <p>
     * <pre>
     * join(null, "#")     =   "";
     * join({}, "#$")      =   "";
     * join({a,b,c}, null) =   "a,b,c";
     * join({a,b,c}, "")   =   "abc";
     * join({a,b,c}, "#")  =   "a#b#c";
     * join({a,b,c}, "#$") =   "a#$b#$c";
     * </pre>
     *
     * @param list
     * @param separator
     * @return join list to string with separator. if list is empty, return ""
     */
    public static String join(List<String> list, String separator) {
        if (isEmpty(list)) {
            return "";
        }
        if (separator == null) {
            separator = DEFAULT_JOIN_SEPARATOR;
        }

        StringBuilder joinStr = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            joinStr.append(list.get(i));
            if (i != list.size() - 1) {
                joinStr.append(separator);
            }
        }
        return joinStr.toString();
    }

    /**
     * add distinct entry to list
     *
     * @param <V>
     * @param sourceList
     * @param entry
     * @return if entry already exist in sourceList, return false, else add it and return true.
     */
    public static <V> boolean addDistinctEntry(List<V> sourceList, V entry) {
        return (sourceList != null && !sourceList.contains(entry)) ? sourceList.add(entry) : false;
    }

    /**
     * add all distinct entry to list1 from list2
     *
     * @param <V>
     * @param sourceList
     * @param entryList
     * @return the count of entries be added
     */
    public static <V> int addDistinctList(List<V> sourceList, List<V> entryList) {
        if (sourceList == null || isEmpty(entryList)) {
            return 0;
        }

        int sourceCount = sourceList.size();
        for (V entry : entryList) {
            if (!sourceList.contains(entry)) {
                sourceList.add(entry);
            }
        }
        return sourceList.size() - sourceCount;
    }

    /**
     * remove duplicate entries in list
     *
     * @param <V>
     * @param sourceList
     * @return the count of entries be removed
     */
    public static <V> int distinctList(List<V> sourceList) {
        if (isEmpty(sourceList)) {
            return 0;
        }

        int sourceCount = sourceList.size();
        int sourceListSize = sourceList.size();
        for (int i = 0; i < sourceListSize; i++) {
            for (int j = (i + 1); j < sourceListSize; j++) {
                if (sourceList.get(i).equals(sourceList.get(j))) {
                    sourceList.remove(j);
                    sourceListSize = sourceList.size();
                    j--;
                }
            }
        }
        return sourceCount - sourceList.size();
    }

    /**
     * add not null entry to list
     *
     * @param sourceList
     * @param value
     * @return <ul>
     * <li>if sourceList is null, return false</li>
     * <li>if value is null, return false</li>
     * <li>return {@link List#add(Object)}</li>
     * </ul>
     */
    public static <V> boolean addListNotNullValue(List<V> sourceList, V value) {
        return (sourceList != null && value != null) ? sourceList.add(value) : false;
    }

    /**
     * @see {@link ArrayUtils#getLast(Object[], Object, Object, boolean)} defaultValue is null, isCircle is true
     */
    @SuppressWarnings("unchecked")
    public static <V> V getLast(List<V> sourceList, V value) {
        return (sourceList == null) ? null : (V) ArrayUtils.getLast(sourceList.toArray(), value, true);
    }

    /**
     * @see {@link ArrayUtils#getNext(Object[], Object, Object, boolean)} defaultValue is null, isCircle is true
     */
    @SuppressWarnings("unchecked")
    public static <V> V getNext(List<V> sourceList, V value) {
        return (sourceList == null) ? null : (V) ArrayUtils.getNext(sourceList.toArray(), value, true);
    }

    /**
     * invert list
     *
     * @param <V>
     * @param sourceList
     * @return
     */
    public static <V> List<V> invertList(List<V> sourceList) {
        if (isEmpty(sourceList)) {
            return sourceList;
        }

        List<V> invertList = new ArrayList<V>(sourceList.size());
        for (int i = sourceList.size() - 1; i >= 0; i--) {
            invertList.add(sourceList.get(i));
        }
        return invertList;
    }

    public static String ListStr2Str(List<String> list, String interFlag) {
        StringBuilder builder = new StringBuilder();
        for (String str : list) {
            builder.append(str);
            builder.append(interFlag);
        }
        return builder.toString();
    }

    public static <V> void clear(List<V> list) {
        if (isEmpty(list))
            return;
        list.clear();
    }

    /**
     * @param list : String list
     * @return
     */
    public static byte[] convert2ByteArray(List<String> list) {
        if (isEmpty(list))
            return null;
        // write to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try {
            for (String element : list) {
                out.writeUTF(element);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(out);
            CloseUtils.close(baos);
        }
        return baos.toByteArray();
    }

    public static List<String> byteArray2List(byte[] bytes) {
        // read from byte array
        if (bytes == null || bytes.length == 0)
            return null;
        List<String> list = new ArrayList<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);
        try {
            while (in.available() > 0) {
                String element = in.readUTF();
                list.add(element);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(in);
            CloseUtils.close(bais);
        }
        return list;
    }

    public static byte[] intArray2Byte(List<Integer> ints) throws IOException {
        // write to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        for (int element : ints) {
            out.writeUTF(Integer.toString(element));
        }
        return baos.toByteArray();
    }

    /**
     * @param bytes
     * @return
     */
    public static List<Integer> byteArray2ListInt(byte[] bytes) {
        if (bytes == null)
            return null;
        // read from byte array
        final int len = bytes.length;
        Integer[] result = new Integer[len];
        for (int i = 0; i < len; i++) {
            result[i] = (int) bytes[i];
        }
        return Arrays.asList(result);
    }

    /**
     * the last maxSize elements are valid
     * so :[0, getSize(sourceList)-maxSize]is bad
     *
     * @param sourceList
     * @param maxSize
     * @param <V>
     * @return
     */
    public static <V> List<V> cutTop(List<V> sourceList, int maxSize) {
        int size = getSize(sourceList);
        if (size == 0) return null;
        size = size - maxSize;
        for (int i = 0; i < size; i++) {
            sourceList.remove(0);
        }
        return sourceList;
    }

    public static <V> V getLast(List<V> sourList) {
        int size = getSize(sourList);
        if (size == 0)
            return null;
        return sourList.get(size - 1);
    }

    /**
     * @param listA
     * @param listB
     * @param <V>
     * @return
     */
    public static <V> List<V> getDiff(List<V> listA, List<V> listB) {
        List<V> rawList = new ArrayList<>(listA);
        rawList.removeAll(listB);
        return rawList;
    }
}
