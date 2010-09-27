package arch.model;

import java.util.Date;

public class ProcessInstance extends Entity implements Observer {

	public enum UpdateMessage { ImageChanged, Label }
	
	ExecutableBinaryImage image;
	Date creationTime;
	
	public ProcessInstance(String key) throws InvalidKeyException {
		super(key);
	}

	public static String genKey(String URI, Integer pid) {
		return URI + ";" + pid.toString();
	}

	public void setCreationTime(Date t) { creationTime = t; }
	
	public ExecutableBinaryImage getExe() { return image; }

	@Override
	public void associate(Entity e) {
		if(ExecutableBinaryImage.class.isInstance(e)) {
			image = (ExecutableBinaryImage) e;
			image.subscribe(this);
			changed(this, UpdateMessage.Label.name(), image);
		}
	}

	@Override
	public void update(Observable o, String signal, Object obj) {
		//super.update(o, signal, obj);
		
		if(ExecutableBinaryImage.class.isInstance(o)) {
			
			if(signal.equals(ExecutableBinaryImage.UpdateMessage.Label.name())) {
				changed(this, UpdateMessage.Label.name(), obj);
			}
			
		}
	}
	
}
