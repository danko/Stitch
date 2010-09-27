package arch.model;

import java.util.Date;

public class TimeValuePair<T> implements TimeProfile<T> {
	
	public enum Type { Atomic, UpUntil, FromNowOn, PastAndFuture }
	
	public Date timeStamp;
	public T value;
	public Type characterization = Type.PastAndFuture;
	
	public TimeValuePair(Date t, T v) { timeStamp = t; value = v; }
	public TimeValuePair(Date t, T v, Type c) { timeStamp = t; value = v; characterization = c; }

	@Override
	public TimeValuePair<T>[] getTimeValuePairs() {
		@SuppressWarnings("unchecked")
		TimeValuePair<T>[] a = new TimeValuePair[1];
		a[1] = this;
		return a;
	}
	
	@Override
	public String getTimeProfileLabel() {
		return value.toString();
	}
	
}
