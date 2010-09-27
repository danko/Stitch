package arch.view;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.j2d.TextRenderer;

public class Server extends Entity {

	static TextRenderer textRenderer;

	public static void poke() {}
	static {
		EntityViewFactory.register(Server.class, arch.model.Server.class);
		
		textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 12), true, false);
	}
	
	static public String getTitle() { return "Hostname"; }
	
	arch.model.Server model;
	
	public Server(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.Server) model;
		
		extrusionDepth = -200;
		
		color = Stitch.currentStitchView.background.mix(Color.blue.brighter(), .4f);
		size.x = 130;
		size.y = 20;
		alpha = 1f;
	}

	@Override
	public void live() {
		super.live();
		
		arch.model.IPAddress m = model.getIP();
		if(m != null) {
			if(m.getHosts().size() == 0) { //only pull IP if no hosts are pulling it
				Entity view = Stitch.currentStitchView.getEntity(m.key);
				view.pos.plusEquals(pos.minus(view.pos).plus(0f, size.y-20).times(.05f)); //Pull IPAddress node 5% closer
			}
		}
	}

	@Override
	public void draw(GLAutoDrawable glD) {
		super.drawExtruded(glD);
		GL gl = glD.getGL();
		
		gl.glPushMatrix();
		gl.glTranslatef(pos.x(), pos.y(), pos.z() - extrusionDepth + 1f);
		textRenderer.begin3DRendering();
		textRenderer.setColor(0,0,0,1f);
		textRenderer.draw(model.key, (int) extrudedPoints[0].x+5, (int) extrudedPoints[0].y+5);
		textRenderer.end3DRendering();
		gl.glPopMatrix();
		
		drawConnectionBox(glD, ((arch.model.Server) model).getIP());
	}

}
