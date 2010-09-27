package apps.netVis;

import java.util.HashMap;
import java.util.Map;

class Metadata {
	static Map<String, java.lang.reflect.Field> mapping = new HashMap<String, java.lang.reflect.Field>();

	static {
		try {
			Metadata.mapping.put("PROJECT",		Metadata.class.getField("project"));
			Metadata.mapping.put("COPILOT_ID",	Metadata.class.getField("copilotID"));
			Metadata.mapping.put("ACQUISITION_DATETIME", Metadata.class.getField("acquisition"));
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public String project;
	public String copilotID;
	public String acquisition;
}
