package arch.model;

public class Server extends Entity {

	String name, localName;
	TimeVaryingField<IPAddress> ip = new TimeVaryingField<IPAddress>();
	
	public Server(String name) throws InvalidKeyException {
		super(name);
		this.name = name;
	}
	
	public void setLocalName(String ln) { localName = ln; }
	
	@Override
	public void associate(Entity e) {
		if(IPAddress.class.isInstance(e)) {
			ip.insert((IPAddress) e, Stitch.currentStitchModel.getTimeContext());
		}
		
	}

	public IPAddress getIP() {
		return ip.get();
	}

}
