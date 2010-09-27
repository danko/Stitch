package arch.view;

import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.j2d.TextRenderer;

import arch.model.Observable;
import arch.model.Point3d;

public class PhysicalHost extends Entity {

	static TextRenderer textRenderer;
	
	public static void poke() {}
	static {
		EntityViewFactory.register(PhysicalHost.class, arch.model.PhysicalHost.class);

		textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 16), true, false);
	}
	
	static public String getTitle() { return "MAC"; }

	arch.model.PhysicalHost model;
	
	public PhysicalHost(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.PhysicalHost) model;
		
		extrusionDepth = 200f;
		
		size.x = 130;
		color = java.awt.Color.lightGray;
		
		refresh();
	}
	
	private void refresh() {
		size.y = 20;
		if(model.getGateway() != null) size.y += 20;
		if(model.getDHCP() != null) size.y += 20;
	}

	@Override
	public void live() {
		super.live();
		
		arch.model.IPAddress ip = model.getIP();
		if(ip != null) {
			IPAddress IPView = (IPAddress) Stitch.currentStitchView.getEntity(ip.key);
			IPView.pos.plusEquals(pos.minus(IPView.pos).plus(0f, size.y-20).times(.05f)); //Pull IPAddress node 3% closer
		}
		
		arch.model.IPAddress gateway = model.getGateway();
		if(gateway != null) {
			IPAddress view = (IPAddress) Stitch.currentStitchView.getEntity(gateway.key);
			view.pos.plusEquals( pos.plus(0, size.y-40).minus(view.pos.point3d()).times(.05f).point2d() );
		}
		
		arch.model.IPAddress dhcp = model.getDHCP();
		if(dhcp != null) {
			IPAddress view = (IPAddress) Stitch.currentStitchView.getEntity(dhcp.key);
			view.pos.plusEquals( pos.plus(0, size.y-60).minus(view.pos.point3d()).times(.05f).point2d() );
		}
		
		arch.model.Project proj = model.getProject();
		if(proj != null) {
			Project view = (Project) Stitch.currentStitchView.getEntity(proj.key);
			view.pos.plusEquals( pos.plus(0, getHeight()-view.getHeight()).minus(view.pos.point3d()).times(.05f).point2d() );
		}
	}
	
	@Override
	public void draw(GLAutoDrawable glD) {
		final GL gl = glD.getGL();

		super.drawExtruded(glD);

		gl.glPushMatrix();
			gl.glTranslatef(pos.x(), pos.y(), getDepth());
			textRenderer.begin3DRendering();
			textRenderer.setColor(0,0,0,1f);
			textRenderer.draw3D(model.key, extrudedPoints[3].x + 5, extrudedPoints[3].y - 20, 5f, 1f);
			if(model.getGateway() != null)
				textRenderer.draw3D("Gateway:", extrudedPoints[3].x + 5, extrudedPoints[3].y - 40, 5f, 1f);
			if(model.getDHCP() != null)
				textRenderer.draw3D("DHCP:", extrudedPoints[3].x + 5, extrudedPoints[3].y - 60, 5f, 1f);
			textRenderer.end3DRendering();
		gl.glPopMatrix();
		
		//Point3d center = interpolateExtrusion(.5f, .5f);
		
		drawConnectionBox(glD, model.getIP());
		drawConnectionBox(glD, model.getProject());
		
		gl.glColor3f(0f, 0f, 0f);

		drawConnection(glD, model.getGateway(), size.y-20f-10f);
		drawConnection(glD, model.getDHCP(), size.y - 40f - 10f);
	}

	@Override
	public void update(Observable o, String signal, Object obj) {
		super.update(o, signal, obj);
		
		if(arch.model.PhysicalHost.class.isInstance(o)) {
			if(signal.equals(arch.model.PhysicalHost.UpdateMessage.IP.name())) {
			}
			refresh();
		}
	}

}
