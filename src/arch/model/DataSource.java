package arch.model;

import java.io.File;

public class DataSource extends Entity {

	File file;
	DataFormat format;
	Project owner;
	
	public DataSource(String filePath) throws InvalidKeyException {
		super(filePath);
	}

	public void setFile(File f) { file = f; }

	public void associate(Entity e) {
		if(DataFormat.class.isInstance(e)) {
			format = (DataFormat) e;
			addProvenance(new Provenance(format));
			init(); //initialize position new format entity
		}
		else if(Project.class.isInstance(e)) {
			assert(owner == null);
			owner = (Project) e;
		}
		else super.associate(e);
	}

	public DataFormat getFormat() { return format; }

	public void load() {
		format.read(this);
	}
	
	public Project getOwner() { return owner; }

}
