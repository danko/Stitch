package arch.view;

import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.j2d.TextRenderer;

public class DataPool extends Entity {
	
	public static void poke() {}
	static { EntityViewFactory.register(DataPool.class, arch.model.DataPool.class); }
	
	static FontDescriptor heading = new FontDescriptor("SansSerif", Font.BOLD, 32);

	arch.model.DataPool model;
	
	public DataPool(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.DataPool) model;
		refresh();
	}
	
	public void refresh() {
		TextRenderer tr = Stitch.getTextRenderer(heading);
		size.x = (float) tr.getBounds(model.getLabel()).getWidth()  + 10f;
		size.y = (float) tr.getBounds(model.getLabel()).getHeight() + 10f;
	}

	public void draw(GLAutoDrawable glD) {
		super.draw(glD);
		final GL gl = glD.getGL();

		TextRenderer tr = Stitch.getTextRenderer(heading);
		
		gl.glPushMatrix();
			gl.glTranslatef(pos.x(), pos.y(), getDepth());
		
			tr.begin3DRendering();
			tr.setColor(0,0,0,0.6f);
			tr.draw3D(model.getLabel(), 5f, 5f, 1f, 1f);
			tr.end3DRendering();
	
		gl.glPopMatrix();
	}
}
