package arch.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import arch.controller.SelectionItem;
import arch.model.PhysicalHost.TimeProfileID;

public class Timeline extends Entity implements Observer {

	public enum UpdateMessage { TimeSpan, Participants }
	
	List<TimelineParticipant> participants = new LinkedList<TimelineParticipant>();
	public Date min, max;
	
	public Timeline(String key) throws InvalidKeyException {
		super(key);
		min = new Date(System.currentTimeMillis());
		max = new Date(System.currentTimeMillis());
	}

	public void addParticipant(Entity e, TimeProfile<?> tp) {
		if(tp == null) return;
		TimelineParticipant par = null;
		for(TimelineParticipant p : participants) if(p.entity == e) { par = p; break; }
		if(par == null) {
			par = new TimelineParticipant(e);
			participants.add(par);
		}
		par.submit(tp);
		changed(this, UpdateMessage.Participants.name(), null);
	}

	public List<TimelineParticipant> getParticipants() { return participants; }
	
	public void refresh() {
		boolean firstEntry = true;
		for(TimelineParticipant p : participants)
			for(TimeProfile<?> tp : p.getSubmissions()) {
				TimeValuePair<?>[] tvp = tp.getTimeValuePairs();
				if(tvp.length > 0) {
					if(firstEntry) { min = max = null; firstEntry = false; }
					if(min == null || tvp[0           ].timeStamp.before(min)) min = (Date) tvp[0           ].timeStamp.clone();
					if(max == null || tvp[tvp.length-1].timeStamp.after (max)) max = (Date) tvp[tvp.length-1].timeStamp.clone();
				}
			}
		changed(this, UpdateMessage.TimeSpan.name(), null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, String signal, Object obj) {
		if(arch.controller.Stitch.class.isInstance(o)) {
			if(signal.equals(arch.controller.Stitch.UpdateMessage.Selection.name())) {
				participants.clear();
				Set<SelectionItem> sel = (Set<SelectionItem>) obj;
				for(SelectionItem s : sel)
					if(arch.view.PhysicalHost.class.isInstance(s.entity)) {
						addParticipant(s.entity.model, ((PhysicalHost) s.entity.model).getTimeProfile(TimeProfileID.Projects));
						addParticipant(s.entity.model, ((PhysicalHost) s.entity.model).getTimeProfile(TimeProfileID.IPs));
						addParticipant(s.entity.model, ((PhysicalHost) s.entity.model).getTimeProfile(TimeProfileID.Gateways));
						addParticipant(s.entity.model, ((PhysicalHost) s.entity.model).getTimeProfile(TimeProfileID.DHCPs));
						addParticipant(s.entity.model, ((PhysicalHost) s.entity.model).getTimeProfile(TimeProfileID.IPLeases));
					}
				refresh();
				changed(this, UpdateMessage.Participants.name(), null);
			}
		}
	}
	
}
