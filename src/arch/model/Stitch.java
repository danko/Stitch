package arch.model;

import apps.App;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class Stitch extends Observable {

	public enum UpdateMessage {
		TimeContext,		//(Entity)		An entity was added to the model (The entity that was added)
		AddEntity			//(Date)		The model's current time context has changed (The new time)
	}
	
	static Stitch currentStitchModel;
	
	HashMap<String, Entity> entities = new HashMap<String, Entity>();
	Stack<Date> timeContexts = new Stack<Date>();
	
	public Timeline timeline;
	public Selection selection;
	
	Collection<App> apps = new LinkedList<App>();
	
	public Stitch() {
		currentStitchModel = this;
		
		try { timeline = new Timeline("Timeline"); addEntity(timeline); }
		catch(InvalidKeyException e) {}

		try { selection = new Selection("Selection"); addEntity(selection); }
		catch(InvalidKeyException e) {}

		timeContexts.push(new Date(System.currentTimeMillis()));
	}

	@Override
	public void subscribe(Observer o) {
		super.subscribe(o);
		
		//Get the newcomer up to date
		
		o.update(this, UpdateMessage.TimeContext.name(), timeContexts.peek());

		for(java.util.Map.Entry<String, Entity> e : entities.entrySet())
			o.update(this, UpdateMessage.AddEntity.name(), e.getValue());
	}
	
	public void install(App a) {
		apps.add(a);
	}

	public void pushTimeContext(Date t) { if(t == null) throw new NullPointerException(); timeContexts.push(t); }
	public void popTimeContext() { timeContexts.pop(); }

	public void setTimeContext(Date t) { timeContexts.peek().setTime(t.getTime()); }
	public Date getTimeContext() { return (Date) timeContexts.peek().clone(); }
	
	public void setTimeContextWithUpdate(Date t) {
		setTimeContext(t);
		changed(this, UpdateMessage.TimeContext.name(), timeContexts.peek());
	}
	
	public void addEntity(Entity e) {
		entities.put(e.key, e);
		changed(this, UpdateMessage.AddEntity.name(), e);
	}

	public Entity getEntity(String key) {
		return entities.get(key);
	}
	
	public Collection<Entity> getEntities() { return entities.values(); }

	public void search(String terms) {
		// TODO Auto-generated method stub
		System.out.println("Not searching for " + terms);
	}

}
