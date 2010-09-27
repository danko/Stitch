package arch.model;

public class GatewayEnzyme extends Enzyme<IPAddress> {

	//use to avoid exception throwing
	static public GatewayEnzyme wrap(IPAddress e) {
		try { return new GatewayEnzyme(e); }
		catch (InvalidKeyException e1) { e1.printStackTrace(); }
		return null;
	}

	//type cast override
	IPAddress entity;
	
	public GatewayEnzyme(IPAddress e) throws InvalidKeyException {
		super(e);
		this.entity = e;
	}
	
}
