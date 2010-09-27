package arch.model;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class TimeVaryingField<T> implements TimeProfile<T> {

	String label = new String("unlabeled");
	List<TimeValuePair<T>> values = new LinkedList<TimeValuePair<T>>();
	//ListIterator<TimeValuePair<T>> current;
	TimeValuePair<T> cached;
	boolean cacheValid = false;
	Date lastDate;
	TimeValuePair.Type typeDefault = null;
	
	public TimeVaryingField() {}
	public TimeVaryingField(String label) { this.label = label; }
	
	public void setDefaultType(TimeValuePair.Type t) { typeDefault = t; }

	public void insert(T val, Date time) {
		ListIterator<TimeValuePair<T>> i = values.listIterator();
		TimeValuePair<T> lastReturned = null;
		while(i.hasNext())
			if(i.next().timeStamp.after(time))
				{ lastReturned = i.previous(); break; }

		TimeValuePair<T> tvp;
		if(typeDefault != null) tvp = new TimeValuePair<T>(time, val, typeDefault);
		else					tvp = new TimeValuePair<T>(time, val);
		
		if(lastReturned != null)
			if(lastReturned.equals(tvp)) return;

		i.add(tvp);

		cacheValid = false;
	}
	
	public boolean insertUnique(T val, Date time) {
		for(TimeValuePair<T> tvp : values)
			if(tvp.value == val) return false;
		insert(val, time);
		return true;
	}
	
	public T getClosest(Date t) {
		lastDate = t;
		if(values.size() == 0) return null;

		if(t == null) { //hmm
			cached = values.get(0);
			cacheValid = true;
			return cached.value;
		}

		int min = Math.abs( t.compareTo(values.get(0).timeStamp) );
		for(TimeValuePair<T> p : values) {
			cached = p;
			int dist = Math.abs( t.compareTo(p.timeStamp) );
			if(dist > min) break;
			min = dist;
		}
		cacheValid = true;
		return cached.value; 
	}
	
	public void setTime(Date t) {
		lastDate = t;
		cacheValid = false;
	}
	
	public T get() {
		if(!cacheValid) return getClosest(lastDate);
		return cached.value;
	}
	
	public Collection<TimeValuePair<T>> getN(Date t1, Date t2) {
		Collection<TimeValuePair<T>> set = new LinkedList<TimeValuePair<T>>();
		// TODO
		return set;
	}

	public int count() {
		return values.size();
	}
	
	public Collection<TimeValuePair<T>> getAll() { return values; }

	@Override
	public TimeValuePair<T>[] getTimeValuePairs() {
		@SuppressWarnings("unchecked")
		TimeValuePair<T>[] a = new TimeValuePair[values.size()];
		int i = 0;
		for(TimeValuePair<T> p : values) a[i++] = p;
		return a;
	}

	@Override
	public String getTimeProfileLabel() { return label; }
	
}
