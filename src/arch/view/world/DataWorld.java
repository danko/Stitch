package arch.view.world;

import javax.media.opengl.GLAutoDrawable;

import arch.view.Stitch;

public class DataWorld extends World {

	final Stitch view;
	
	public DataWorld(Stitch view) {
		super(0);
		this.view = view;
	}

	@Override
	public void draw(GLAutoDrawable glD) {
		view.draw(glD);
		postDraw(glD);
	}

}
