package apps;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
//import java.util.Collection;
//import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;

import arch.model.DataPool;
import arch.model.InvalidKeyException;

public abstract class App {

	protected arch.model.Stitch stitchModel;
	
	static protected DocumentBuilder docBuilder;
	
	public App(arch.model.Stitch stitchModel) {
		this.stitchModel = stitchModel;
		stitchModel.install(this);
	}
	
	public void loadWorkspace(String path) {
		File[] files = new File(path).listFiles(new FilenameFilter() {
			public boolean accept(File f, String n) {
				return !n.startsWith(".");
			}
		});
		
		for(File f: files) {
			if(f.isDirectory()) {
				DataPool pool;
				try {
					pool = new DataPool(f);
					stitchModel.addEntity(pool);
				}
				catch (InvalidKeyException e) { e.printStackTrace(); }
				catch (IOException e) { e.printStackTrace(); }
	
				File[] projects = f.listFiles(new FilenameFilter() {
					public boolean accept(File f, String n) {
						return !n.startsWith(".");
					}
				});
				
				for(File p : projects)
					scanFile(p);
			}
			
			if(f.isFile()) {
				scanFile(f);
				loadFile(f);
			}
		}
		//for(File f: files) loadFile(f);
	}
	
	abstract public void scanFile(File f);
	abstract public void loadFile(File f);

}
