package arch.model;

import java.util.Collection;
import java.util.LinkedList;

public class Observable {

	Collection<Observer> observers = new LinkedList<Observer>();
	
	public void subscribe(Observer o) { observers.add(o); }
	public void unsubscribe(Observer o) { observers.remove(o); }

	protected void changed(Observable who, String command, Object obj) {
		for(Observer o: observers) o.update(who, command, obj);
	}
	
}
