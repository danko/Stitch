package apps.netVis;

public class InvalidTimeFormat extends Exception {

	private static final long serialVersionUID = -6350496167869274412L;
	
	public final String sample;
	
	public InvalidTimeFormat(String sample) {
		this.sample = sample;
	}

}
