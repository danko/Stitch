package arch.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import arch.model.Provenance;

public class Entity extends Observable {
	
	public enum UpdateMessage { Init }

	List<Provenance> provenance = new LinkedList<Provenance>();
	public final String key;
	boolean hasDoneInit = false;
	
	Collection<Entity> generalAssociates = new LinkedList<Entity>();
	
	public Entity(String key) throws InvalidKeyException {
		if(key == null || key.equals(""))
			throw new InvalidKeyException();
		this.key = key;
	}
	
	@Override
	public String toString() { return key; }
	
	public void addProvenance(Provenance prov) {
		for(Provenance p : provenance)
			if(p.equals(prov)) return;
		provenance.add(prov);
	}

	public static void associateMutually(Entity a, Entity b) {
		a.associate(b);
		b.associate(a);
	}

	public void associate(Entity e) {
		System.out.println("Warning: Entity associate fallback used (" + getClass() + ", " + e.getClass() + ")");
		generalAssociates.add(e);
	}

	public void init() {
		hasDoneInit = true;
		changed(this, UpdateMessage.Init.name(), null);
	}
	
	public Provenance getProvenance() {
		return provenance.get(provenance.size()-1); //return last (most recently added)
	}
	
	public boolean hasProvenance(Entity e) {
		for(Provenance p : provenance)
			if(p.source == e) return true;
		return false;
	}
	
	public void subscribe(Observer o) {
		super.subscribe(o);
		
		//Get the newcomer up to date
		
		if(hasDoneInit)
			o.update(this, UpdateMessage.Init.name(), null);
	}
	
}
