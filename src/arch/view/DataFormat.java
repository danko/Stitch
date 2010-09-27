package arch.view;

import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.Observable;

import com.sun.opengl.util.j2d.TextRenderer;

public class DataFormat extends Entity {

	static FontDescriptor large;
	
	public static void poke() {}
	static {
		EntityViewFactory.register(DataFormat.class, arch.model.DataFormat.class);
		
		large = (FontDescriptor) FontDescriptor.Default.clone();
		large.size += 10;
	}
	
	static public String getTitle() { return "File Type"; }
	
	final arch.model.DataFormat model;

	public DataFormat(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.DataFormat) model;
		
		extrusionDepth = 400f;
		
		color = java.awt.Color.white;
		size.x = 85;
		size.y = 110;
	}
	
	public void draw(GLAutoDrawable glD) {
		super.draw(glD);
		
		String text = arch.model.DataFormat.Labels[arch.model.DataFormat.Tag.valueOf(model.key).ordinal()];
		
		GL gl = glD.getGL();

		TextRenderer tr = Stitch.getTextRenderer(large);
		
		gl.glPushMatrix();
		gl.glTranslatef(pos.x(), pos.y(), getDepth());
		tr.begin3DRendering();
		tr.setColor(0,0,0,1f);
		tr.draw3D(text, extrudedPoints[0].x+5, extrudedPoints[0].y+5, 5f, 1f);
		tr.draw3D(Integer.toString(model.numInstances()) + " File(s)", extrudedPoints[0].x + 5, extrudedPoints[0].y + 100 + 5, 5f, 1f);
		tr.end3DRendering();
		gl.glPopMatrix();
	}
	
	@Override
	public void update(Observable o, String signal, Object obj) {
		super.update(o, signal, obj);
		
		if(o == model) {
			if(signal.equals(arch.model.DataFormat.UpdateMessage.DataSourceAdded.name())) {
				DataSource view = (DataSource) Stitch.currentStitchView.getEntity( ((arch.model.Entity)obj).key );
				store(view);
			}
		}
	}
	
	@Override
	public void congregate() {
		for(arch.model.DataSource s : model.getInstances()) {
			store( Stitch.currentStitchView.getEntity(s.key) );
		}
	}

}
