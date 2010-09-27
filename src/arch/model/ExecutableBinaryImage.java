package arch.model;

public class ExecutableBinaryImage extends Entity {

	public enum UpdateMessage { Label };
	
	String highLevelLabel; //i.e. "Adobe Acrobat Reader"
	
	public ExecutableBinaryImage(String imageFileName) throws InvalidKeyException {
		super(imageFileName);
	}
	
	public void setLabel(String label) {
		highLevelLabel = label;
		changed(this, "Label", label);
	}

	public String getLabel() {
		if(highLevelLabel == null) return key;
		return highLevelLabel;
	}

	public boolean hasHighLevelLabel() {
		return highLevelLabel != null;
	}

}
