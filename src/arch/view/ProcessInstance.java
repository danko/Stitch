package arch.view;

import java.awt.Font;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.Observable;
import arch.model.Point3d;

import com.sun.opengl.util.j2d.TextRenderer;

public class ProcessInstance extends Entity {
	
	public static void poke() {}
	static { EntityViewFactory.register(ProcessInstance.class, arch.model.ProcessInstance.class); }

	static FontDescriptor mainFont = new FontDescriptor("SansSerif", Font.PLAIN, 16);
	
	final arch.model.ProcessInstance model;
	String label;
	LineMetrics metrics;
	
	public ProcessInstance(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.ProcessInstance) model;
		
		Color bg = Stitch.currentStitchView.background;
		color = new Color(1f, .6f, .6f).mix(bg, .6f); //java.awt.Color.red.brighter();
		alpha = 1f;

		size.x = 10;
		size.y = 10;
	}
	
	Point3d[] rear = { new Point3d(), new Point3d(), new Point3d(), new Point3d() };
	public void draw(GLAutoDrawable glD) {
		final GL gl = glD.getGL();
		TextRenderer tr = Stitch.getTextRenderer(mainFont);

		if(model.getExe().hasHighLevelLabel()) {
			float oldDepth = extrusionDepth;
			float oldAlpha = alpha;
			extrusionDepth += 200f;
			alpha = .3f;
			
			super.draw(glD);
			for(int i = 0; i < 4; ++i) rear[i].match(extrudedPoints[i]);
			tr.begin3DRendering();
			tr.setColor(0,0,0,1f);
			tr.draw3D(model.getExe().key,
					pos.x()+extrudedPoints[0].x+5, pos.y()+extrudedPoints[0].y+metrics.getDescent(), getDepth()+1, 1f);
			tr.end3DRendering();
			
			extrusionDepth = oldDepth;
			alpha = oldAlpha;
		}

		super.draw(glD);
		tr.begin3DRendering();
		tr.setColor(0,0,0,1f);
		tr.draw3D(label, pos.x()+5, pos.y()+metrics.getDescent(), +5f, 1f);
		tr.end3DRendering();
		
		if(model.getExe().hasHighLevelLabel()) {
			gl.glPushMatrix();
			gl.glTranslatef(pos.x(), pos.y(), getDepth());

			gl.glColor4f(0f, 0f, 0f, .3f);
			gl.glBegin(GL.GL_LINES);
			for(int i = 0; i < 4; ++i) {
				gl.glVertex3f(extrudedPoints[i].x, extrudedPoints[i].y, 0f);
				gl.glVertex3f(rear          [i].x, rear          [i].y, -200f);
			}
			gl.glEnd();
			
			gl.glPopMatrix();
		}
	}
	
	@Override
	public void update(Observable o, String signal, Object obj) {
		super.update(o, signal, obj);
		
		if(arch.model.ProcessInstance.class.isInstance(o)) {
			if(signal.equals(arch.model.ProcessInstance.UpdateMessage.Label.name())) {
				TextRenderer tr = Stitch.getTextRenderer(mainFont);
				label = model.getExe().getLabel();
				metrics = tr.getFont().getLineMetrics(label, tr.getFontRenderContext());

				Rectangle2D r = tr.getBounds(label);
				size.x = (float) r.getWidth() + 10;
				size.y = (float) r.getHeight() + metrics.getDescent();
			}
		}
	}

	@Override
	public void hide() {
		
	}
	
}
