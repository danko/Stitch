package arch.model;

public class Enzyme<T extends Entity> extends Entity {

	static long UID = 0;
	
	public T entity;
	
	public Enzyme(T e) throws InvalidKeyException {
		super("Enzyme" + UID++);
		entity = e;
	}

}
