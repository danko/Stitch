package arch.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Project extends Entity {

	PhysicalHost host;
	TimeVaryingField<String> copilotID = new TimeVaryingField<String>();
	TimeVaryingField<ProcessInstance> processes = new TimeVaryingField<ProcessInstance>();
	Set<DataSource> owns = new LinkedHashSet<DataSource>();
	
	public Project(String key) throws InvalidKeyException {
		super(key);
	}

	public void addCopilotID(Stitch stitch, String id) {
		copilotID.insert(id, stitch.getTimeContext());
	}
	
	@Override
	public void associate(Entity e) {
		if(ProcessInstance.class.isInstance(e)) {
			ProcessInstance proc = (ProcessInstance) e;
			processes.insert(proc, proc.creationTime);
		}
		else if(DataSource.class.isInstance(e)) {
			owns.add((DataSource) e);
		}
		else if(PhysicalHost.class.isInstance(e)) {
			assert(host == null); //should only ever have one host
			host = (PhysicalHost) e;
		}
		else super.associate(e);
	}

	public Collection<TimeValuePair<ProcessInstance>> getProcesses() {
		return processes.getAll();
	}

	public Set<DataSource> getOwnership() {
		return owns;
	}

}
