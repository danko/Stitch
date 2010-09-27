package arch.model;

import java.util.LinkedList;
import java.util.List;

public class IPAddress extends Entity {

	static int maxServerAssociations = 0;
	
	int subnetMask = -1;
	TimeVaryingField<Server> server = new TimeVaryingField<Server>();
	List<PhysicalHost> hosts = new LinkedList<PhysicalHost>();
	
	public IPAddress(String ip) throws InvalidKeyException {
		super(ip);
	}

	public void setSubnetMask(String mask) {
		int[] n = {0,0,0,0};
		int p = 0, pp = -1;
		int ones = 0;
		for(int i = 0; i < 4; ++i) {
			p = mask.indexOf(".", pp+1);
			if(p == -1) p = mask.length();
			n[i] = Integer.parseInt(mask.substring(pp+1, p));
			pp = p;
			
			for(int b = 0; b < 8; ++b)
				ones += (n[i]>>b) & 1;
		}
		setSubnetMask(ones);
	}
	
	public void setSubnetMask(int w) {
		subnetMask = w;
	}
	
	public int getSubnetMask() { return subnetMask; }
	
	public Server getServer() { return server.get(); }
	
	@Override
	public void associate(Entity e) {
		if(Server.class.isInstance(e)) {
			server.insert((Server) e, Stitch.currentStitchModel.getTimeContext());
			
			if(server.count() > maxServerAssociations) {
				maxServerAssociations = server.count();
				System.out.println("IPAddress:: max(ServerAssociations) = " + server.count());
			}
		}
		else if(PhysicalHost.class.isInstance(e)){
			hosts.add((PhysicalHost) e);
		}
		else super.associate(e);
	}

	public List<PhysicalHost> getHosts() {return hosts;}

	public List<Server> getServers() {
		List<Server> s = new LinkedList<Server>();
		for(TimeValuePair<Server> p : server.getAll() )
			s.add(p.value);
		return s;
	}
	
}
