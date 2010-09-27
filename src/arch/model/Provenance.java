package arch.model;

public class Provenance {

	public Entity source;

	public Provenance(Entity e) {
		source = e;
	}
	
	@Override
	public boolean equals(Object o) {
		if(Provenance.class.isInstance(o))
			return ((Provenance)o).source == source;
		return false;
	}

}
