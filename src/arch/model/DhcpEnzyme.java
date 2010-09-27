package arch.model;

public class DhcpEnzyme extends Enzyme<IPAddress> {

	//use to avoid exception throwing
	static public DhcpEnzyme wrap(IPAddress e) {
		try { return new DhcpEnzyme(e); }
		catch (InvalidKeyException e1) { e1.printStackTrace(); }
		return null;
	}

	//type cast override
	IPAddress entity;
	
	public DhcpEnzyme(IPAddress e) throws InvalidKeyException {
		super(e);
		this.entity = e;
	}
	
}
