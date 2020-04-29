package net.ponomar.utility;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrderedHashtable<V> extends LinkedHashMap<String,V> implements IOrderedHashtable<String, V> {

	@Override
	public Iterator<String> iterateKeys() {
        return this.keySet().iterator();
	}

	@Override
	public Enumeration<String> enumerateKeys() {
		return Collections.enumeration(this.keySet());
	}
	
	@Override
	public IOrderedHashtable<?, ?> clone(){
		return (IOrderedHashtable<?, ?>) super.clone();
	}

	@Override
	public void putAll(Map m) throws NullPointerException {
		if(m instanceof IOrderedHashtable) 
		{
			IOrderedHashtable c = (IOrderedHashtable)m;
			Iterator<String> itr = c.iterateKeys();
			while (itr.hasNext())
			{
				Object o = itr.next();
				put(o.toString(), (V) c.get(o));
			}
		}
		else // the map is not ordered, so add at will
		{

			for (Object o : m.entrySet()) {
				Map.Entry e = (Map.Entry) o;
				// Optimize in case the Entry is one of our own.
				put(e.getKey().toString(), (V) e.getValue());
			}
		}
	}

	@Override
	public Enumeration keys() {
		return Collections.enumeration(this.keySet());
	}

}
