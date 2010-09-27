package arch.view;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import arch.model.AnimatedValue;
import arch.model.Point2d;
import arch.model.Point3d;

public class Camera {

	boolean animating = false;
	Point3d pos = new Point3d();
	Point3d goalPos = new Point3d();
	AnimatedValue pitch = new AnimatedValue(), yaw = new AnimatedValue();
	float roll;
	float scale = 1f;
	
	float focalDepth, zNear, zFar;
	int halfViewWidth, halfViewHeight;
	
	public void setGoalPos(Point3d p) {
		goalPos = p;
		animating = true;
	}
	
	public void jumpTo(Point3d p) {
		pos.x = p.x; pos.y = p.y; pos.z = p.z;
		goalPos.x = p.x; goalPos.y = p.y; goalPos.z = p.z;
	}
	
	public void live() {
		if(!animating) return;
		
		float dx = goalPos.x-pos.x;
		float dy = goalPos.y-pos.y;
		float dz = goalPos.z-pos.z;
		
		pos.x += dx*0.09f;
		pos.y += dy*0.09f;
		pos.z += dz*0.07f;
		
		pitch.live();
		yaw.live();
		//roll += .03f;
		//scale *= 0.995f;
		
		if(pitch.isDone() && yaw.isDone())
			if(Math.abs(dx) < .01f && Math.abs(dy) < .01f && Math.abs(dz) < .01f) {
				pos.x = goalPos.x; pos.y = goalPos.y; pos.z = goalPos.z;
				animating = false;
			}
	}
	
	public void transform(GLAutoDrawable glD) {
		GL gl = glD.getGL();
		
		gl.glScalef(scale, scale, scale);
		gl.glRotatef(pitch.get(), 1f, 0f, 0f);
		gl.glRotatef(yaw.get(), 0f, 1f, 0f);
		gl.glTranslatef(-pos.x, -pos.y, -pos.z);
		gl.glRotatef(roll, 0f, 0f, 1f);
		gl.glTranslatef(-halfViewWidth, -halfViewHeight, 0f);
	}
	
	public Point2d untransform(Point2d cursorPos, float z) {
		//final GL gl = glD.getGL();
		//gl.glTranslatef(pos.x, pos.y, 0f);		

		// Initialize
//		FloatBuffer buffer = FloatBuffer.allocate(1);
//		gl.glReadPixels((int)cursorPos.x, (int)cursorPos.y, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, buffer); //sample z-buffer
//		z = buffer.get(0);
//		System.out.println(" " + z);
		//if(z >= 1f-.001f) z = 0.0f; //over background = on default plane
		
		//System.out.print("z " + z + " = ");
		//z = z*(zFar-zNear)+zNear - focalDepth - 2325.582f;
		z = (z - .72758f) * 150/(.7635692f - .72758f);
		//System.out.print("" + z + "\n");
		
		//z = (z - GL.GL_DEPTH_BIAS) / GL.GL_DEPTH_SCALE; 
		Point2d p = new Point2d(cursorPos.x, halfViewHeight*2 - cursorPos.y); //flip Y (window -> GL)
		p.x -= halfViewWidth; p.y -= halfViewHeight; //0,0 at screen center

		// Un-scale
		p.x /= scale; p.y /= scale;

		if(z != 1f) {
			//System.out.println(z);
			float c, s, t;
			// Un-pitch
			t = (float) Math.toRadians(pitch.get());
			c = (float) Math.cos(t); s = (float) Math.sin(t);
			t = p.y*c - z*s;
			z = p.y*s + z*c;
			p.y = t;
			
			// Un-yaw
			//-yaw.get()
			t = (float) Math.toRadians(yaw.get());
			c = (float) Math.cos(t); s = (float) Math.sin(t);
			t = p.x*c - z*s;
			z = p.x*s + z*c;
			p.x = t;
		} else {
			z = 0f;
		}
		
		// Un-translate
		p.x += pos.x; p.y += pos.y;

		// un-roll
		double angle = Math.toRadians(-roll);
		double cos = Math.cos(angle), sin = Math.sin(angle);
		double x = p.x, y = p.y;
		p.x = (float) (x*cos-y*sin); p.y = (float) (x*sin+y*cos); //rotate by negative roll about origin
		
		// Un-center
		p.x += halfViewWidth; p.y += halfViewHeight;
		
		return p;
	}

	public void setPerspective(GLAutoDrawable glD, GLU glu, float FoVy, int width, int height) {
		halfViewWidth  = width  / 2;
		halfViewHeight = height / 2;

		if(height <= 0) { height = 1; }
		float aspect = (float)width / (float)height;

		GL gl = glD.getGL();

		double angle = Math.toRadians(FoVy/2.0);
		focalDepth = (float) ( Math.cos(angle) * halfViewHeight/Math.sin(angle) );
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		
		//glu.gluPerspective(50.0f, h, 1.0, 1000.0);

		zNear = 500f;
		zFar = 5000f;
		glu.gluPerspective(FoVy, aspect, zNear, zFar);
		gl.glTranslatef(0.0f, 0.0f, -focalDepth);
		
		//glu.gluPerspective(0.2f, h, 1000.0, 2000.0);
		//gl.glTranslatef(0.0f, 0.0f, -1000.0f);
		
		//			(left, right, bottom, top)
		//glu.gluOrtho2D(0.0, width, 0.0, height);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public Point3d halfSize() {
		return new Point3d(halfViewWidth, halfViewHeight);
	}

	public void panBy(float dx, float dy) {
		pos.plusEquals(new Point3d(dx/scale, dy/scale)); //snap
		goalPos.x = pos.x; goalPos.y = pos.y; goalPos.z = pos.z; //and stay there
	}

	public void tiltBy(float x, float y) {
		pitch.setGoal(pitch.getGoal() + y);
		yaw.setGoal(yaw.getGoal() + x);
		animating = true;
	}
	
	public void tiltTo(float x, float y) {
		pitch.setGoal(y);
		yaw.setGoal(x);
		animating = true;
	}

	public void extrude(Point3d[] p, Point3d ePos, float fromDepth, float toDepth) {
		Point3d pov = pos.minus(ePos).plus(halfSize()); //relative
		float ratio = toDepth / (pov.z + focalDepth - fromDepth);
		
		for(int i = 0; i < p.length; ++i) {
			p[i].x += (p[i].x-pov.x)*ratio;
			p[i].y += (p[i].y-pov.y)*ratio;
		}
	}

	public void moveTo(Point2d p) {
		goalPos.x = p.x-halfViewWidth; goalPos.y = p.y-halfViewHeight;
		animating = true;
	}

	public void moveTo(Point3d p) {
		goalPos.x = p.x-halfViewWidth; goalPos.y = p.y-halfViewHeight;
		goalPos.z = p.z;
		animating = true;
	}

	public void setTilt(float pitch, float yaw) {
		this.pitch.setGoal(pitch); this.pitch.snapTo();
		this.yaw.setGoal(yaw); this.yaw.snapTo();
	}

	public boolean isMoving() {
		return animating;
	}
	
	public void scaleBy(float f) {
		scale *= f;
		if(scale < .0001f) scale = .0001f;
		if(Math.abs(scale-1f) < .1f) scale = 1f;
	}

	public void setScale(float s) {
		scale = s;
	}
	
}
