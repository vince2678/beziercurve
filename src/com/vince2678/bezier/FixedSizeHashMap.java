package com.vince2678.bezier;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import lombok.Getter;

public class FixedSizeHashMap<K, V> extends HashMap<K, V>
{
	private Deque<K> deque;

	@Getter
	private int capacity;

	public FixedSizeHashMap(int capacity)
	{
		super(capacity);
		this.capacity = capacity;
		deque = new ArrayDeque<K>(capacity);
	}

	public void setCapacity(int capacity)
	{
		this.capacity = capacity;
		while (deque.size() > capacity)
		{
			remove(deque.getFirst());
		}
	}

	@Override
	public V put(K k, V v)
	{
		if (!containsKey(k))
		{
			while (deque.size() >= capacity)
			{
				remove(deque.getFirst());
			}
			deque.addLast(k);
		}
		return super.put(k, v);
	}

	@Override
	public V remove(Object k)
	{
		V v = super.remove(k);
		if (v != null)
		{
			deque.remove(k);
		}
		return v;
	}

	@Override
	public void clear()
	{
		super.clear();
		deque.clear();
	}

	@Override
	public boolean remove(Object k, Object v)
	{
		boolean retval = super.remove(k, v);
		if (retval)
		{
			deque.remove(k);
		}

		return retval;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) throws NullPointerException
	{
		if (map == null)
		{
			throw new NullPointerException();
		}

		Set<? extends K> keySet = map.keySet();

		for (K k : keySet)
		{
			put(k, map.get(k));
		}
	}

	@Override
	public V putIfAbsent(K k, V v)
	{
		V oldV = get(k);
		if (containsKey(k) && oldV != null)
		{
			return oldV;
		}
		return put(k, v);
	}

	@Override
	public V merge(K k, V v, BiFunction<? super V, ? super V, ? extends V> biFunction)
	{
		V oldValue = get(k);

		V newValue;
		if (oldValue == null)
		{
			newValue = v;
		}
		else
		{
			newValue = biFunction.apply(oldValue, v);
		}

		if (newValue == null)
		{
			remove(k);
		}
		else
		{
			put(k, newValue);
		}

		return newValue;
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> biFunction)
	{
		for (K k : keySet())
		{
			V v = get(k);

			V newV = biFunction.apply(k, v);
			put(k, newV);
		}
	}
}
