package arch.model;

import java.util.Date;

public class DhcpLease implements TimeProfile<String> {

	static String string_obtained = "Obtained", string_expires = "Expires";
	
	Date leaseObtained, leaseExpires;
	IPAddress ip;

	public DhcpLease(IPAddress ip, Date obtained, Date expires) {
		this.ip = ip;
		leaseObtained = obtained;
		leaseExpires = expires;
	}
	
	@Override
	public String toString() { return "IP lease"; }

	@Override
	public TimeValuePair<String>[] getTimeValuePairs() {
		@SuppressWarnings("unchecked")
		TimeValuePair<String>[] lease = new TimeValuePair[2];
		lease[0] = new TimeValuePair<String>(leaseObtained, string_obtained, TimeValuePair.Type.FromNowOn);
		lease[1] = new TimeValuePair<String>(leaseExpires, string_expires, TimeValuePair.Type.UpUntil);
		return lease;
	}

	@Override
	public String getTimeProfileLabel() {
		return "IP Lease from DHCP";
	}
	
}
