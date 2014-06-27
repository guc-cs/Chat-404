/************************************
 * Title: 	FixedArrayIndex
 * Date:	10.16.2012
 * Purpose: A generic data structure 
 * 			simulating an expandable 
 * 			array where	items added 
 * 			retain the same index till 
 * 			deleted
 ************************************/

package engine;

import java.util.Iterator;
import java.util.Stack;

public class FixedIndexArray <T> implements Iterable<T>{

	private Object [] array;
	private Stack<Integer> emptyPos;
	int nEmptyPos;
	int nElem;
	
	public FixedIndexArray(int initial)
	{
		array = new Object[initial]; 
		emptyPos = new Stack<Integer>();
		
	}
	
	public int addItem(T item)
	{
		if (nEmptyPos > 0)
		{
			nEmptyPos--;
			int index = emptyPos.pop();
			array[index] = item;
			return index;		
		}
		if (nElem == array.length)
			expand();
		array[nElem] = item;
		return nElem++;
	}
	
	public int removeItem(int index)
	{
		nEmptyPos++;
		emptyPos.push(index);
		array[index] = null;
		return index;
	}
	
	public int removeItem(T item)
	{
		
		for(int i = 0; i < array.length; i++)
		{
			if (item.equals(array[i]))
			{
				nEmptyPos++;
				emptyPos.push(i);
				array[i] = null;
				return i;
			}
		}
		
		return -1;
	}
	
	private void expand()
	{
		Object [] tempArray = new Object[2*array.length];
		for (int i = 0; i < nElem; i++)
		{
			tempArray[i] = array[i];
		}
		array = tempArray;
	}
	
	public int size()
	{
		return nElem;
	}
	
	@SuppressWarnings("unchecked")
	public T get(int i)
	{
		return (T)array[i];
	}
	
	public Iterator<T> iterator()
	{
		return new FixedArrayIndexIterator();
	}
	
	public int numberOfElements()
	{
		return nElem - nEmptyPos;
	}
	//==============Iterator==============
	
	private class FixedArrayIndexIterator implements Iterator<T>
	{
		private int current;
		
		public boolean hasNext()
		{
			return (current < nElem) ? true : false;
		}

		@SuppressWarnings("unchecked")
		public T next() {
			return (T)array[current++];
		}

		public void remove() {
			
			
		}	
	}
	
}
