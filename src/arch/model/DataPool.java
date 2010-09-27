package arch.model;

import java.io.File;
import java.io.IOException;

public class DataPool extends Entity {

	File path;
	String label;
	
	public DataPool(File path) throws InvalidKeyException, IOException {
		super(path.getCanonicalPath());
		this.path = path;
		label = "/" + path.getName() + "/";
	}
	
	public String getLabel() { return label; }

}
