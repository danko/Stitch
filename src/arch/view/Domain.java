package arch.view;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.Observable;
import arch.model.Point3d;

import com.sun.opengl.util.j2d.TextRenderer;

public class Domain extends Entity {

	static TextRenderer textRenderer;

	public static void poke() {}
	static {
		EntityViewFactory.register(Domain.class, arch.model.Domain.class);
		
		textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 20), true, false);
	}
	
	arch.model.Domain model;
	
	public Domain(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.Domain) model;
		
		color = new Color(.9f, 1f, .9f);
		
		size.x = 200;
		size.y = 200;
		
		extrusionDepth = -250f; //foreground
	}
	
	@Override
	public void draw(GLAutoDrawable glD) {
		super.drawExtruded(glD);
		GL gl = glD.getGL();
		
		gl.glPushMatrix();
			gl.glTranslatef(pos.x(), pos.y(), pos.z() - extrusionDepth + 1f);
			textRenderer.begin3DRendering();
			textRenderer.setColor(0f,0f,0f,.5f);
			textRenderer.draw(model.getName(), (int) extrudedPoints[0].x + 5, (int) extrudedPoints[0].y + 5);
			textRenderer.draw(Integer.toString(model.numServers()) + " Server(s)", (int) extrudedPoints[0].x + 5, (int) extrudedPoints[0].y + 100 + 5);
			textRenderer.end3DRendering();
		gl.glPopMatrix();
		
		if(isSelected) {
			Point3d center = pos.plus(interpolateExtrusion(.5f, .5f));
			gl.glColor3f(0f, 0f, 0f);
			for(arch.model.Server s : model.getServers()) {
				Server serverView = (Server) Stitch.currentStitchView.getEntity(s.key);
				Point3d serverCenter = serverView.pos.plus(serverView.interpolateExtrusion(.5f, .5f));
				gl.glBegin(GL.GL_LINES);
					gl.glVertex3f(center.x, center.y, pos.z() - extrusionDepth -5);
					gl.glVertex3f(serverCenter.x, serverCenter.y, serverView.pos.z() - serverView.extrusionDepth +5);
				gl.glEnd();
			}
		}
	}

	@Override
	public void update(Observable o, String signal, Object obj) {
		super.update(o, signal, obj);
		
		if(o == model) {
			if(signal.equals(arch.model.Domain.UpdateMessage.ServerAdded.name())) {
				Server view = (Server) Stitch.currentStitchView.getEntity( ((arch.model.Server)obj).key );
				store(view);
			}
		}
	}
	
	public void congregate() {
		for(arch.model.Server s : model.getServers()) {
			Server sv = (Server) Stitch.currentStitchView.getEntity(s.key);
			store(sv);
		}		
	}

}
