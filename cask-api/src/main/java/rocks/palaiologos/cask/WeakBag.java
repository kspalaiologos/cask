package rocks.palaiologos.cask;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public final class WeakBag {

    private HashSet set = new HashSet();
    private ReferenceQueue refQueue = new ReferenceQueue();

    public WeakBag() {
    }

    public WeakReference add(Object o) {
        WeakReference ans = new WeakReference(o, refQueue);
        set.add(ans);
        return ans;
    }

    public void remove(Reference o) {
        set.remove(o);
    }

    public int size() {
        return set.size();
    }

    public List values() {
        ArrayList a = new ArrayList(set.size());
        for (Iterator i = set.iterator(); i.hasNext(); ) {
            WeakReference r = (WeakReference)i.next();
            Object o = r.get();
            if (o != null) a.add(o);
        }
        return a;
    }

    public void clean() {
        for (;;) {
            Object r = refQueue.poll();
            if (r == null) return;
            set.remove(r);
        }
    }
}

