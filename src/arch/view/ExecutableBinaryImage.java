package arch.view;

import javax.media.opengl.GLAutoDrawable;

public class ExecutableBinaryImage extends Entity {

	public static void poke() {}
	static {
		EntityViewFactory.register(ExecutableBinaryImage.class, arch.model.ExecutableBinaryImage.class);
	}
	
	public ExecutableBinaryImage(arch.model.Entity model) {
		super(model);
		
		color = java.awt.Color.blue;
		size.x = 10;
		size.y = 300;
	}

}
