package arch.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Domain extends Entity {

	public enum UpdateMessage { ServerAdded };
	
	String name;
	Set<Server> servers = new LinkedHashSet<Server>();
	
	public Domain(String name) throws InvalidKeyException {
		super(name);
		this.name = name;
	}
	
	public String		getName() { return name; }
	public int			numServers() { return servers.size(); }
	public Set<Server>	getServers() { return servers; }

	@Override
	public void associate(Entity e) {
		if(Server.class.isInstance(e)) { servers.add((Server) e); changed(this, UpdateMessage.ServerAdded.name(), e); }
	}

	@Override
	public void subscribe(Observer o) {
		super.subscribe(o);

		//Get the newcomer up to date

		for(Server s : servers)
			o.update(this, UpdateMessage.ServerAdded.name(), s);
	}
	
}
