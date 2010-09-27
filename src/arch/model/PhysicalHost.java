package arch.model;

import arch.model.TimeValuePair.Type;

public class PhysicalHost extends Entity {

	public enum UpdateMessage { IP, DHCP, Gateway }
	public enum TimeProfileID { Projects, IPs, Gateways, DHCPs, IPLeases }
	
	TimeVaryingField<Project>	project	= new TimeVaryingField<Project>("Project ID");
	TimeVaryingField<IPAddress> IP		= new TimeVaryingField<IPAddress>("IP");
	TimeVaryingField<IPAddress> gateway	= new TimeVaryingField<IPAddress>("Gateway");

	TimeVaryingField<IPAddress> dhcp	= new TimeVaryingField<IPAddress>("DHCP");
	TimeVaryingField<DhcpLease> dhcpLease = new TimeVaryingField<DhcpLease>("IP Lease");
	
	public PhysicalHost(String MAC) throws InvalidKeyException {
		super(MAC);
		
		project  .setDefaultType(Type.PastAndFuture);
		IP       .setDefaultType(Type.PastAndFuture);
		gateway  .setDefaultType(Type.PastAndFuture);
		dhcp     .setDefaultType(Type.PastAndFuture);
		dhcpLease.setDefaultType(Type.PastAndFuture);
	}
	
	public IPAddress	getIP()			{ return IP.get(); }
	public IPAddress	getGateway()	{ return gateway.get(); }
	public Project		getProject()	{ return project.get(); }
	public IPAddress	getDHCP()		{ return dhcp.get(); }

	public void associate(Entity e) {
		if(Enzyme.class.isInstance(e)) {
			if(GatewayEnzyme.class.isInstance(e)) {
				gateway.insert( ((GatewayEnzyme) e).entity, Stitch.currentStitchModel.getTimeContext());
				changed(this, UpdateMessage.Gateway.name(), ((GatewayEnzyme) e).entity);
			}
			if(DhcpEnzyme.class.isInstance(e)) {
				dhcp.insert(((DhcpEnzyme)e).entity, Stitch.currentStitchModel.getTimeContext());
				changed(this, UpdateMessage.DHCP.name(), ((DhcpEnzyme) e).entity);
			}
		}
		else if(IPAddress.class.isInstance(e)) {
			IPAddress ip = (IPAddress) e;
			IP.insert(ip, Stitch.currentStitchModel.getTimeContext());
			changed(this, UpdateMessage.IP.name(), ip);
		}
		else if(Project.class.isInstance(e)) {
			project.insert( (Project) e, Stitch.currentStitchModel.getTimeContext());
		}
		else super.associate(e);
	}
	
	public void associate(DhcpLease lease) {
		dhcpLease.insert(lease, Stitch.currentStitchModel.getTimeContext());
	}

	public TimeProfile<?> getTimeProfile(TimeProfileID profile) {
		switch(profile) {
			case Projects:	return project;
			default:
			case IPs:		return IP;
			case Gateways:	return gateway;
			case DHCPs:		return dhcp;
			case IPLeases:	return dhcpLease.get();
		}
	}

}
