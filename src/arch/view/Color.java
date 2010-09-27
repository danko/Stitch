package arch.view;

public class Color extends java.awt.Color {

	private static final long serialVersionUID = 1084756513493307271L;

	public Color(int r, int g, int b) {
		super(r, g, b);
	}
	
	public Color(float r, float g, float b) {
		super(r, g, b);
	}
	
	public Color mix(Color fg, float a) {
		return new Color(
				getRed  ()/255f*(1f-a) + fg.getRed  ()/255f*a,
				getGreen()/255f*(1f-a) + fg.getGreen()/255f*a,
				getBlue ()/255f*(1f-a) + fg.getBlue ()/255f*a);
	}

	public Color mix(java.awt.Color c, float a) {
		return mix(new Color(c.getRed(), c.getGreen(), c.getBlue()), a);
	}

}
