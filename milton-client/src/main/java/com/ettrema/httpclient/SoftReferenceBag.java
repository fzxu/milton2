package com.ettrema.httpclient;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author brad
 */
public class SoftReferenceBag<T> implements Iterable<T>{

    private final List<SoftReference<T>> list = new ArrayList<SoftReference<T>>();

    public int size() {
        flush();
        return list.size();
    }

    public boolean isEmpty() {
        flush();
        return list.isEmpty();
    }

    public Iterator<T> iterator() {
        return values().iterator();
    }

    public List<T> values() {
        ArrayList<T> l = new ArrayList<T>();
        Iterator<SoftReference<T>> it = list.iterator();
        while(it.hasNext()) {
            SoftReference<T> ref = it.next();
            if( ref.isEnqueued() ) {
                list.remove(ref);
            } else {
                T t = ref.get();
                if( t != null ) {
                    l.add(t);
                }
            }
        }
        return l;
    }

    public boolean add(T e) {
        return list.add(new SoftReference<T>(e));
    }

    public boolean remove(T o) {
        Iterator<SoftReference<T>> it = list.iterator();
        while(it.hasNext()) {
            SoftReference<T> ref = it.next();
            if( ref.isEnqueued() ) {
                list.remove(ref);
            } else {
                if( o == ref.get() ) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    private void flush() {
        ArrayList<T> l = new ArrayList<T>();
        Iterator<SoftReference<T>> it = list.iterator();
        while(it.hasNext()) {
            SoftReference<T> ref = it.next();
            if( ref.isEnqueued() ) {
                list.remove(ref);
            }
        }
    }

    public void clear() {
        list.clear();
    }


}
