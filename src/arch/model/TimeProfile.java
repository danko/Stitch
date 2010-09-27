package arch.model;

public interface TimeProfile<T> {

	TimeValuePair<T>[] getTimeValuePairs();
	String getTimeProfileLabel();
	
}
