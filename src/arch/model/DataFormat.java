package arch.model;

import java.util.Collection;
import java.util.LinkedList;

import apps.App;

public class DataFormat extends Entity {

	public static enum Tag { EXE_TABLE, METADATA, IP_CONFIG, ARP_CONFIG, NETMAP, PROCESS_LIST };
	public static final String[] Labels = {"EXE Database", "File Metadata", "IP Configuration", "ARP Configuration", "Network Mapping", "Process List"};

	public enum UpdateMessage { DataSourceAdded };
	
	App app;
	Tag formatTag;
	Collection<DataSource> knownInstances = new LinkedList<DataSource>();
	
	public DataFormat(App app, String tag) throws InvalidKeyException {
		super(tag);
		this.app = app;
		formatTag = Tag.valueOf(tag);
	}

	@Override
	public void associate(Entity e) {
		if(DataSource.class.isInstance(e)) {
			knownInstances.add((DataSource)e);
			changed(this, UpdateMessage.DataSourceAdded.name(), e);
		}
	}
	
	public void read(DataSource ds) {
		app.loadFile(ds.file);
	}

	public void loadAll() {
		for(DataSource ds : knownInstances)
			ds.load();
	}

	public int numInstances() {
		return knownInstances.size();
	}

	public Collection<DataSource> getInstances() {
		return knownInstances;
	}
	
}
