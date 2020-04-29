package net.ponomar.utility;

import java.io.Serializable;
import java.util.*;

public interface IOrderedHashtable<K, V> extends Map<K, V>, Cloneable, Serializable {

    void clear() throws UnsupportedOperationException;

    Iterator<String> iterateKeys();

    Enumeration<String> enumerateKeys();

    Collection values();

    Set keySet();

    /** Returns a string representation of the OrderedHashtable. */
    String toString();

    /**
    * Copies all of the mappings from the specified Map to this Hashtable
    * These mappings will replace any mappings that this Hashtable had for any
    * of the keys currently in the specified Map.
    * If the map is an instance of OrderedHashtable, then the copy will have
    * the same ordering.
    *
    * @param m Mappings to be stored in this map.
    * @throws NullPointerException if the specified map is null.
    *
    */
    void putAll(Map m) throws NullPointerException;

	IOrderedHashtable clone();

	Enumeration keys();
}
