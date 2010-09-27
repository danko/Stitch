package arch.view.world;

import java.awt.Font;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.Point3d;
import arch.view.Camera;
import arch.view.FontDescriptor;
import arch.view.Stitch;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;

public class UserWorld extends World {

	Camera cam = new Camera();
	Portal monitor;
	float border = 45f;
	private boolean enteringMonitor = false;
	//Texture tex;
	static FontDescriptor labels = new FontDescriptor("SansSerif", Font.BOLD, 72);
	
	public UserWorld() {
		super(1); //one sub-world
		
		portals[0] = new Portal();
		portals[0].pos = new Point3d(800f, 1200f, -500f);
		portals[0].rotation.y = -25f;
		monitor = portals[0]; //alias
		
		cam.jumpTo(monitor.pos);
		cam.setTilt(-monitor.rotation.x, -monitor.rotation.y);
		cam.setGoalPos(new Point3d(0f, 1500f, 2000f));
		cam.tiltTo(0, 20);
	}

	int t = 200;
	@Override
	public void live() {
		super.live();
		cam.live();
		
		if(t-- == 0) enterMonitor();
		
		if(enteringMonitor && !cam.isMoving()) monitor.world.unlinkSuperWorld(parentWorld);
	}
	
	public void enterMonitor() {
		enteringMonitor = true;
		cam.setGoalPos(monitor.pos);
		cam.tiltTo(-monitor.rotation.y, -monitor.rotation.x);
	}
	
	public void draw(GLAutoDrawable glD) {
		final GL gl = glD.getGL();

		gl.glPushMatrix();
			cam.transform(glD);

			float room = 1800f;

//			gl.glShadeModel(GL.GL_SMOOTH);
//			gl.glEnable(GL.GL_LIGHTING);
//			gl.glEnable(GL.GL_LIGHT0);
//			float black[] = { 0f, 0f, 0f, 1f };
//			float white[] = { 1f, 1f, 1f, 1f };
//			float position[] = { 0f, room, 0f, 1f };
//			gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, FloatBuffer.wrap(position));
//			gl.glMaterialfv (GL.GL_FRONT, GL.GL_AMBIENT, FloatBuffer.wrap(black));
//			gl.glMaterialfv (GL.GL_FRONT, GL.GL_DIFFUSE, FloatBuffer.wrap(white));
//			gl.glMaterialfv (GL.GL_FRONT, GL.GL_SPECULAR, FloatBuffer.wrap(white));
//			float spot_direction[] = { -0.2f, -1.0f, 0.0f };
//			gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPOT_DIRECTION, FloatBuffer.wrap(spot_direction));
//			gl.glLightf(GL.GL_LIGHT0, GL.GL_QUADRATIC_ATTENUATION, 0.0001f);
//			gl.glLightf(GL.GL_LIGHT0, GL.GL_SPOT_CUTOFF, 45.0f);
			
			gl.glColor3f(254/255f, 235/255f, 169/255f);
			gl.glBegin(GL.GL_QUADS);
				//back wall
				gl.glNormal3f(0, 0, 1);
				gl.glVertex3f(-room,   0f, -room);
				gl.glVertex3f(+room,   0f, -room);
				gl.glVertex3f(+room, room, -room);
				gl.glVertex3f(-room, room, -room);
				
				//floor
//				gl.glNormal3f(0, 1, 0);
//				gl.glVertex3f(-room, 0f, -room);
//				gl.glVertex3f(+room, 0f, -room);
//				gl.glVertex3f(+room, 0f, +room);
//				gl.glVertex3f(-room, 0f, +room);
			gl.glEnd();
			gl.glColor3f(0f, 0f, 0f);
			gl.glBegin(GL.GL_LINES);
				gl.glVertex3f(-room, 0f+5, -room+5);
				gl.glVertex3f(+room, 0f+5, -room+5);
			gl.glEnd();
			
			float height = 500f, width = 2500f, depth = 400f;
			gl.glColor3f(88/255f, 60/255f, 5/255f);
			gl.glBegin(GL.GL_QUADS);
				gl.glNormal3f(0, 1, 0);
				gl.glVertex3f(monitor.pos.x-width*.6f, height, monitor.pos.z-depth);
				gl.glVertex3f(monitor.pos.x+width*.4f, height, monitor.pos.z-depth);
				gl.glVertex3f(monitor.pos.x+width*.4f, height, monitor.pos.z+depth);
				gl.glVertex3f(monitor.pos.x-width*.6f, height, monitor.pos.z+depth);
			gl.glEnd();
			
//			gl.glDisable(GL.GL_LIGHT0);
//			gl.glDisable(GL.GL_LIGHTING);
			
			gl.glPushMatrix();
				monitor.transform(gl);
				gl.glBegin(GL.GL_QUADS);
					float hw = monitor.width * .5f, hh = monitor.height * .5f;
					gl.glColor3f(.2f, .2f, .2f);

					//bottom
					gl.glVertex2f(-hw-border, -hh-border);
					gl.glVertex2f(+hw+border, -hh-border);
					gl.glVertex2f(+hw+border, -hh);
					gl.glVertex2f(-hw-border, -hh);

					//top
					gl.glVertex2f(-hw-border, +hh);
					gl.glVertex2f(+hw+border, +hh);
					gl.glVertex2f(+hw+border, +hh+border);
					gl.glVertex2f(-hw-border, +hh+border);

					//left
					gl.glVertex2f(-hw-border, -hh);
					gl.glVertex2f(-hw, -hh);
					gl.glVertex2f(-hw, +hh);
					gl.glVertex2f(-hw-border, +hh);

					//right
					gl.glVertex2f(+hw, -hh);
					gl.glVertex2f(+hw+border, -hh);
					gl.glVertex2f(+hw+border, +hh);
					gl.glVertex2f(+hw, +hh);
				gl.glEnd();
			gl.glPopMatrix();
			
			TextRenderer tr = Stitch.getTextRenderer(labels);
			tr.begin3DRendering();
			tr.setColor(1f, 1f, 1f, .8f);
			tr.draw3D("CTRL, S, L, H, C, G, SPACE, ESC, 0, -, =/+", -450f, 550f, -400f, 1f);
			tr.draw3D("Select, Tilt, Pan", +1000f, 550f, -400f, 1f);
			tr.end3DRendering();
			
			postDraw(glD);
		gl.glPopMatrix();
	}
		
}
