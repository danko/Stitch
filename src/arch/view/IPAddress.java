package arch.view;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.j2d.TextRenderer;

public class IPAddress extends Entity {

	public static void poke() {}
	static {
		EntityViewFactory.register(IPAddress.class, arch.model.IPAddress.class);
	}
	
	static public String getTitle() { return "IP"; }
	
	arch.model.IPAddress model;
	
	public IPAddress(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.IPAddress) model;
		
		size.x = 130;
		size.y = 20;
		color = Stitch.currentStitchView.background.mix(java.awt.Color.yellow.brighter(), .4f);
		alpha = 1f;
	}
	
	@Override
	public void live() {
		super.live();
		
		arch.model.Server m = model.getServer();
		if(m != null) {
			if(model.getHosts().size() != 0) { //only pull Server if we have hosts pulling us
				Entity view = Stitch.currentStitchView.getEntity(m.key);
				view.pos.plusEquals(pos.minus(view.pos).plus(0f, size.y-20).times(.05f));
			}
		}
	}
	
	@Override
	public void draw(GLAutoDrawable glD) {
		super.draw(glD);
		final GL gl = glD.getGL();
		
		TextRenderer tr = Stitch.getTextRenderer(FontDescriptor.Default);
		
		gl.glPushMatrix();
			gl.glTranslatef(pos.x(), pos.y(), getDepth());
			
			String label = model.key;
			if(model.getSubnetMask() > -1) label += " / "+model.getSubnetMask();

			tr.begin3DRendering();
			tr.setColor(0f,0f,0f,1f);
			tr.draw3D(label, extrudedPoints[0].x + 5f, extrudedPoints[0].y + 5f, 5f, 1f);
			tr.end3DRendering();
		gl.glPopMatrix();
	}

}
