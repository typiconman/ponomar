package Ponomar;

import java.util.*;
import java.awt.*;

/******************************************************************************
OrderedHashtable.java - A FIFO HASHTABLE
Original notice:

  	This class is used to combine the key-value lookup capabilities of a
	Hashtable along with order preserving capabilities of a Vector.
	Iterator on a Set of Hashtable keys, obtained by keySet() or an
	Enumeration obtained by keys() method, both are not guaranteed to
	iterate in the same order as the values were put in.

 	This class behaves like a queue, (FIFO). Objects are returned in the
 	same order they were put in.

 	Author Animesh Srivastava
	Author Wayne Burgess

THIS CLASS NOW ALSO INCLUDES AN IMPLEMENTATION OF THE values() FUNCTION,
WHICH RETURNS A COLLECTION OBJECT IN THE SAME ORDER AS THE HASHTABLE.

OrderedHashtable.java is part of the Ponomar program.
Copyright 2007, 2008 Aleksandr Andreev.
aleksandr.andreev@gmail.com

Ponomar is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

While Ponomar is distributed in the hope that it will be useful,
it comes with ABSOLUTELY NO WARRANTY, without even the implied warranties of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for details.
******************************************************************/

public class OrderedHashtable extends Hashtable implements Cloneable
{

	//member variables
	private Vector mSerialOrder;

	/** Public Constructor */
	public OrderedHashtable()
	{
		super();
		this.mSerialOrder = new Vector();
	}


	/** Clears this OrderedHashtable so that it has no keys.
	*
	* @exception UnsupportedOperationException - clear is not supported by
	* the underlying Interface java.util.Map.
	*/
	synchronized public void clear() throws UnsupportedOperationException
	{
		super.clear();
		this.mSerialOrder.clear();
	}

        
	/** Removes the key (and its corresponding value) from this OrderedHashtable.
	* Does nothing if key is not in the OrderedHashtable.
	*
	* @param key - the key that needs to be removed.
	* @returns the value to which the key had been mapped in this OrderedHashtable,
	* or null if the key did not have a mapping.
	*/
	synchronized public Object remove(Object key)
	{
		this.mSerialOrder.remove(key);
		return super.remove(key);
	}


	/** Maps the specified key to the specified value in this OrderedHashtable.
	* Neither the key nor the value can be null. If the key already exists
	* then the ordering is not changed. If it does not exists then it is added
	* at the end of the OrderedHastable.
	*
	* @param key - the key.
	* @param value - the value.
	* @exception - NullPointerException, if the key or value is null.
	* @returns the previous value of the specified key in this hashtable, or
	* null if it did not have one.
	*
	*/
	synchronized public Object put(Object key, Object value) throws NullPointerException
	{
		Object toReturn = super.put(key,value);
		if(toReturn == null)
		{
			this.mSerialOrder.add(key);
		}
		return toReturn;
	}

	/** Returns an Iterator to iterate through the keys of the OrderedHashtable.
	* Iteration will occur in the same order as the keys were put in into the
	* OrderedHashtable.
	*
	* The remove() method of Iterator interface is optional in jdk1.3 and hence
	* not implemented.
	*/
	public Iterator iterateKeys() 
	{
		return new Enumerator();
	}


	/** Returns an Enumeration to enumerate through the keys of the OrderedHashtable.
	* Enumeration will occur in the same order as the keys were put in into the
	* OrderedHashtable.
	*
	*/
	public Enumeration enumerateKeys() 
	{
		return new Enumerator();
	}

	public Collection values()
	{
		return new OrderedCollection();
	}

	public Set keySet()
	{
		return new OrderedSet();
	}

	/** Returns a string representation of the OrderedHashtable. */
	public String toString()
	{
		StringBuffer s = new StringBuffer();
		s.append("{ ");
		Object key = null;
		int i = 0;
		while(i < this.mSerialOrder.size())
		{
			key = this.mSerialOrder.elementAt(i++);
			s.append(key.toString());
			s.append("=");
			s.append(super.get(key).toString());
			s.append("; ");
		}
		s.append(" }");
		return s.toString();
	}
        public OrderedHashtable clone(){
            OrderedHashtable cloned =new OrderedHashtable();
            OrderedHashtable current=this;

            Enumeration keys=current.enumerateKeys();
            while(keys.hasMoreElements()){
                String key=keys.nextElement().toString();
                if (key.equals("Locale"))
                {
                    Locale item=(Locale)current.get(key);
                    cloned.put(new String(key), item);
                    continue;
                }
                if (key.equals("Orient")){
                    ComponentOrientation item=(ComponentOrientation)current.get(key);
                    cloned.put(new String(key), item);
                    continue;
                }
                if (current.get(key) instanceof Integer){
                    Integer item=(Integer)current.get(key);
                    cloned.put(new String(key), new Integer(item));
                    continue;
                }
                String item=(String)current.get(key);
                cloned.put(new String(key), new String(item));
            }

            return cloned;
        }
        public static void main(String[] argz) {
        //Testing cloning of OrderedHashtables
            OrderedHashtable test=new OrderedHashtable();
            test.put("hippo","large");
            test.put("mouse", "small");
            test.put("lion","roars");
            OrderedHashtable aped=test.clone();
            aped.put("lion", "yellow");
            System.out.println("In test, we have that a lion " + test.get("lion"));
            System.out.println("In aped, we have that a lion is " + aped.get("lion"));

    }

	private class OrderedSet extends AbstractSet
	{
		int COUNT = mSerialOrder.size();

		public boolean equals(Object o)
		{
			synchronized (OrderedHashtable.this)
			{
				return OrderedHashtable.this.containsKey(o);
			}
		}

		public int size()
		{
			return COUNT;
		}

		public boolean removeAll()
		{
			throw new UnsupportedOperationException("OrderedHashtable OrderedSet");
		}

		public Iterator iterator()
		{
			return new Enumerator();
		}
	}

	private class OrderedCollection extends AbstractCollection
	{
		int COUNT = mSerialOrder.size();

		public int size()
		{
			return COUNT;
		}

		public Iterator iterator()
		{
			return new ValuesIterator();
		}
	}

	private class ValuesIterator implements Iterator
	{
		int COUNT = mSerialOrder.size();
		int SERIAL = 0;

		public boolean hasNext()
		{
			return SERIAL < COUNT;
		}

		public Object next()
		{
			synchronized (OrderedHashtable.this)
			{
				if ((COUNT == 0) || (SERIAL == COUNT))
				{
					throw new NoSuchElementException("OrderedHashtable ValuesIterator");
				}
				return OrderedHashtable.this.get(mSerialOrder.elementAt(SERIAL++));
			}
		}

		public void remove()
		{
			throw new UnsupportedOperationException("OrderedHashtable ValuesIterator");
		}
	}

	//inner class,
	private class Enumerator implements Enumeration, Iterator
	{
		int COUNT = mSerialOrder.size(); //number of elements in the Vector
		int SERIAL = 0; //keep track of the current element

		public boolean hasMoreElements() 
		{
			return SERIAL < COUNT;
		}

		public Object nextElement() 
		{
			synchronized (OrderedHashtable.this) 
			{
				if((COUNT == 0) || (SERIAL == COUNT))
				{
					throw new NoSuchElementException("OrderedHashtable Enumerator");
				}
				return mSerialOrder.elementAt(SERIAL++);
			}
		}

		public boolean hasNext() 
		{
			return hasMoreElements();
		}

		public Object next() 
		{
			return nextElement();
		}

		//optional in jdk1.3
		public void remove() 
		{
		}
	}

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
	synchronized public void putAll(Map m) throws NullPointerException
	{
		// if its one of "us" its ordered, make sure copy is ordered too!
		if(m instanceof OrderedHashtable) 
		{
			OrderedHashtable c = (OrderedHashtable)m;
			Iterator itr = c.iterateKeys();
			while (itr.hasNext())
			{
				Object o = itr.next();
				put(o, c.get(o));
			}
		}
		else // the map is not ordered, so add at will
		{
			Iterator itr = m.entrySet().iterator();

			while (itr.hasNext())
			{
				Map.Entry e = (Map.Entry) itr.next();
				// Optimize in case the Entry is one of our own.
				put(e.getKey(), e.getValue());
			}
		}
	}
        

}
