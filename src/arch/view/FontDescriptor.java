package arch.view;

import java.awt.Font;
import java.awt.font.LineMetrics;

public class FontDescriptor implements Cloneable {

	public String fontName;
	public int style, size;
	
	public LineMetrics metrics = null; 
	
	static public final FontDescriptor Default = new FontDescriptor(null, -1, -1);
	
	public FontDescriptor(String fontName, int style, int size) {
		if(fontName == null || fontName.equals("")) fontName = "SansSerif";
		if(style == -1) style = Font.PLAIN;
		if(size == -1) size = 12;

		this.fontName = fontName;
		this.style = style;
		this.size = size;
	}
	
	public void setMetrics(LineMetrics lm) { metrics = lm; }
	
	public int lineHeight() {
		if(metrics == null) return 20;
		return (int) (metrics.getHeight() + metrics.getDescent() + .5f); //round up
	}
	
	@Override
	public Object clone() {
		return new FontDescriptor(fontName, style, size);
	}
	
}
