package arch.model;

import java.util.Collection;
import java.util.LinkedList;

public class TimelineParticipant {

	public final Entity entity;
	private Collection<TimeProfile<?>> submissions = new LinkedList<TimeProfile<?>>();
	private Collection<TimeProfile<?>[]> cache;
	
	public TimelineParticipant(Entity e) { entity = e; }
	public void submit(TimeProfile<?> tp) {
		if(tp == null) return;
		submissions.add(tp);
		cache = null;
	}
	public void revoke(TimeProfile<?> tp) { submissions.remove(tp); cache = null; }
	public void clear() { submissions.clear(); cache = null; }
	public Collection<TimeProfile<?>> getSubmissions() { return submissions; }

}