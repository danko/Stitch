package arch.view;

import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.Point3d;

import com.sun.opengl.util.j2d.TextRenderer;

public class DataSource extends Entity {

	static TextRenderer textRenderer;

	static public String getTitle() { return "File"; }
	
	public static void poke() {}
	static {
		EntityViewFactory.register(DataSource.class, arch.model.DataSource.class);
		
		textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 9), true, false);
	}

	arch.model.DataSource model;
	
	public DataSource(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.DataSource) model;
		
		extrusionDepth = 300f;
		
		color = java.awt.Color.white;
		size.x = 8.5f*2;
		size.y = 11.0f*2;
	}
	
	@Override
	public void draw(GLAutoDrawable glD) {
		final GL gl = glD.getGL();
		final arch.model.DataSource m = (arch.model.DataSource) model;
		
		super.draw(glD);
		
		if(isSelected) {
			String text = m.key;
			textRenderer.begin3DRendering();
			textRenderer.setColor(0,0,0,1f);
			textRenderer.draw3D(text, pos.x()+5, pos.y()+5, 5f, 1f);
			textRenderer.end3DRendering();
		}
		
		if(m.getFormat() != null) {
			Entity format = Stitch.currentStitchView.getEntity(m.getFormat().key);
			if(format != null) {
				Point3d formatCenter = format.interpolateExtrusion(.5f, .5f);
				Point3d sourceCenter = interpolateExtrusion(.5f, .5f);
				gl.glBegin(GL.GL_LINES);
					gl.glVertex3f(pos.x()+sourceCenter.x, pos.y()+sourceCenter.y, pos.z()-extrusionDepth -5);
					gl.glVertex3f(format.pos.x()+formatCenter.x, format.pos.y()+formatCenter.y, format.pos.z()-format.extrusionDepth +5);
				gl.glEnd();
			}
		}
	}
	
	@Override
	public void hide() {
		arch.model.DataFormat df = model.getFormat();
		if(df != null) {
			Entity e = Stitch.currentStitchView.getEntity(df.key);
			if(e != null) e.store(this);
		}
	}
}
