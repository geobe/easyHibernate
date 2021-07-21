/*
 *  The MIT License (MIT)
 *
 *                            Copyright (c) 2021. Georg Beier
 *
 *                            Permission is hereby granted, free of charge, to any person obtaining a copy
 *                            of this software and associated documentation files (the "Software"), to deal
 *                            in the Software without restriction, including without limitation the rights
 *                            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *                            copies of the Software, and to permit persons to whom the Software is
 *                            furnished to do so, subject to the following conditions:
 *
 *                            The above copyright notice and this permission notice shall be included in all
 *                            copies or substantial portions of the Software.
 *
 *                            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *                            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *                            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *                            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *                            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *                            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *                            SOFTWARE.
 *
 *
 */

package de.geobe.architecture.persist;

import java.io.Serializable;
import java.util.*;

/**
 * @author georg beier
 * 
 */
public class DaoTransientList<PersistType> implements DataAccess<PersistType> {

	/**
	 * transient store for domain objects
	 */
	private static Map<Class<?>, LinkedHashSet<Object>> objects =
			new HashMap<>();

	public static Set<Object> getObjects(Class<?> cl) {
		return objects.get(cl);
	}

	public static void clearObjectStore() {
		objects = new HashMap<>();
	}
	
	public static void clear() {
		for (Set<Object> set : objects.values()) {
			set.clear();
		}
	}

	private Class<?> key;

	public DaoTransientList(Class<?> clazz) {
		key = clazz;
		if (!objects.containsKey(key)) {
			objects.put(key, new LinkedHashSet<>());
		}
	}

	@Override
	public boolean save(PersistType obj) {
		return getStore().add(obj);
	}

	@Override
	public PersistType fetch(Serializable id) {
		if (id instanceof Number) {
			int hash = ((Number) id).intValue();
			for (PersistType p : getStore()) {
				if (p.hashCode() == hash)
					return p;
			}
		}
		return null;
	}

	@Override
	public List<PersistType> fetchAll() {
		return new ArrayList<>(getStore());
	}

	@Override
	public List<Object> find(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> find(String query, Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PersistType> findByExample(PersistType sample) {
		return new ArrayList<>();
	}

	@Override
	public List<PersistType> findByExample(PersistType sample,
			Collection<String> excluded) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(PersistType obj) {
		getStore().remove(obj);
	}

	@Override
	public void deleteAll() {
		getStore().clear();
	}

	@SuppressWarnings("unchecked")
	private Set<PersistType> getStore() {
		return (Set<PersistType>) objects.get(key);
	}

}
