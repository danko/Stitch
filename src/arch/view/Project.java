package arch.view;

import java.awt.Font;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.Point3d;
import arch.model.TimeValuePair;

import com.sun.opengl.util.j2d.TextRenderer;

public class Project extends Entity {
	
	static TextRenderer textRenderer;

	public static void poke() {}
	static {
		EntityViewFactory.register(Project.class, arch.model.Project.class);
		
		textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 12), true, false);
	}

	arch.model.Project model;
	
	public Project(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.Project) model;

		extrusionDepth = -350f;
		
		//color = java.awt.Color.LIGHT_GRAY.brighter();
		//Color bg = Stitch.currentStitchView.background;
		//color = new Color(bg.getRed()/255.0f*.6f + .95f*.4f, bg.getGreen()/255.0f*.6f + .95f*.4f, bg.getBlue()/255.0f*.6f + .95f*.4f);
		color = new Color(.95f, .95f, .95f).mix(Stitch.currentStitchView.background, .4f);
		alpha = 1f;

		size.x = 130;
		size.y = 20;
	}
	
	@Override
	public void live() {
		super.live();
		
//		//Pull process nodes 3% closer
//		arch.model.Project m = (arch.model.Project) model;
//		for(TimeValuePair<arch.model.ProcessInstance> p : m.getProcesses()) {
//			ProcessInstance proc = (ProcessInstance) Stitch.currentStitchView.getEntity(p.value.key);
//			if(proc != null) {
//				proc.pos.plusEquals(pos.minus(proc.pos).times(.03f));
//			}
//		}
	}
	
	public void draw(GLAutoDrawable glD) {
		super.draw(glD);
		GL gl = glD.getGL();
		
		textRenderer.begin3DRendering();
		textRenderer.setColor(0,0,0,1f);
		textRenderer.draw3D(model.key, pos.x()+extrudedPoints[0].x+5, pos.y()+extrudedPoints[0].y+5, getDepth() + 1, 1f);
		textRenderer.end3DRendering();
		
		Point3d center = pos.plus(interpolateExtrusion(.5f, .5f));

		for(TimeValuePair<arch.model.ProcessInstance> p : model.getProcesses()) {
			ProcessInstance proc = (ProcessInstance) Stitch.currentStitchView.getEntity(p.value.key);
			if(proc != null && !proc.hasGuardian()) {
				gl.glBegin(GL.GL_LINES);
					gl.glVertex3d(center.x, center.y, getDepth() -5f);
					gl.glVertex3d(proc.pos.x()+proc.size.x/2, proc.pos.y()+proc.size.y/2, proc.getDepth() +5f);
				gl.glEnd();
			}
		}
		
		if(isSelected) {
			gl.glColor4f(0f, 0f, 0f, .4f);
			for(arch.model.DataSource ds : model.getOwnership()) {
				DataSource dsView = (DataSource) Stitch.currentStitchView.getEntity(ds.key);
				Point3d dsCenter = dsView.getCenter();
				gl.glBegin(GL.GL_LINES);
					gl.glVertex3f(center.x, center.y, pos.z() - extrusionDepth -5);
					gl.glVertex3f(dsCenter.x, dsCenter.y, dsView.getDepth() -5);
				gl.glEnd();
			}
		}
	}

	@Override
	public void congregate() {
		arch.model.Project m = (arch.model.Project) model;
		for(TimeValuePair<arch.model.ProcessInstance> p : m.getProcesses()) {
			ProcessInstance proc = (ProcessInstance) Stitch.currentStitchView.getEntity(p.value.key);
			if(proc != null) {
				//proc.pos.plusEquals(pos.minus(proc.pos).times(.03f));
				store(proc);
			}
		}
	}
	
}
