package arch.view;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.AnimatedPoint3d;
import arch.model.Observable;
import arch.model.Observer;
import arch.model.Point2d;
import arch.model.Point3d;
import arch.model.Provenance;

public class Entity implements Observer {

	final static float inv255 = 1f/255f;

	static {
		EntityViewFactory.register(Entity.class, arch.model.Entity.class);
	}
	
	final public arch.model.Entity model;
	AnimatedPoint3d pos = new AnimatedPoint3d();
	float extrusionDepth = 0f;
	Point3d extrudedPoints[];

	Point3d size = new Point3d();
	java.awt.Color color;
	float alpha = .4f;

	boolean hasDoneInit = false;
	boolean isSelected = false;
	
	Entity guardian = null;
	List<Entity> cargo = new LinkedList<Entity>();
	private List<Entity> incoming = new LinkedList<Entity>();
	
	public Entity(arch.model.Entity model) {
		this.model = model;
		if(model != null)
			model.subscribe(this);
		
		pos.x ( (int) (Math.random()*(800)) );
		pos.y ( (int) (Math.random()*(600)) );
		pos.snap();
		
		extrudedPoints = new Point3d[4];
		for(int i = 0; i < extrudedPoints.length; ++i) extrudedPoints[i] = new Point3d();
		
		size.x = 60;
		size.y = 40;
		
		color = new java.awt.Color(1f, 1f, 1f);
	}
	
	public void init() {
		Provenance prov = model.getProvenance();
		if(prov != null) {
			Entity e = Stitch.currentStitchView.getEntity(prov.source.key);
			if(e != null) {
				if(!hasDoneInit) {
					pos.x (e.pos.x());
					pos.y (e.pos.y());
					pos.snap();
				}
				
				//pos.x ( e.pos.x() + (int) (Math.random()*(200)-100) );
				//pos.y ( e.pos.y() + (int) (Math.random()*(200)-100) );
				
				double a = Math.random()*2*Math.PI;
				pos.x ( e.pos.x() + (int) (Math.cos(a)*200) );
				pos.y ( e.pos.y() + (int) (Math.sin(a)*200) );
			}
		}
		hasDoneInit = true;
	}

	public boolean hasGuardian() { return guardian != null; }
	
	public float getDepth() {
		if(guardian != null) return guardian.getDepth();
		return pos.z() - extrusionDepth;
	}
	
	protected Point3d interpolateExtrusion(float xP, float yP) {
		// xP, yP : percentages ('texture coods')
		Point3d p[] = extrudedPoints;
		float x = p[0].x + (p[1].x-p[0].x)*xP;
		float y = p[0].y + (p[3].y-p[0].y)*yP;
		return new Point3d(x, y, extrudedPoints[0].z);
	}

	public void setSelection(boolean b) { isSelected = b; }
	public boolean isSelected() { return isSelected; }
	
	public void live() { //Override me & call super.live();
		pos.live();

		//pull incoming cargo
		Point3d center = getCenter();
		for(ListIterator<Entity> i = incoming.listIterator(); i.hasNext(); ) {
			Entity e = i.next();
			Point3d p = e.getCenter();
			e.pos.plusEquals( center.minus(p).times(.09f).point2d() );
			e.pos.snap();
			if(p.minus(center).distance2d() < 5f) {
				i.remove();
				capture(e);
			}
		}
	}

	public void draw(GLAutoDrawable glD) {
		drawExtruded(glD);

//		final GL gl = glD.getGL();
//		
//		gl.glPushMatrix();
//		gl.glTranslatef(pos.x(), pos.y(), pos.z());
//		
//		//gl.glColor3fv(color.getRGBComponents(null), 0);//(color.get, 1.0f, 1.0f);
//		gl.glColor4f(color.getRed()/255f, color.getGreen()*inv255, color.getBlue()*inv255, alpha);
//		//gl.glColor3f(1, 1, 1);
//		gl.glBegin(GL.GL_QUADS);
//			gl.glVertex2f(0f, 0f);
//			gl.glVertex2f(size.x, 0f);
//			gl.glVertex2f(size.x, size.y);
//			gl.glVertex2f(0f, size.y);
//		gl.glEnd();
//
//		gl.glColor3f(0.0f, 0.0f, 0.0f);
//		gl.glBegin(GL.GL_LINE_LOOP);
//			gl.glVertex3f(0f, 0f, 1f);
//			gl.glVertex3f(size.x, 0f, 1f);
//			gl.glVertex3f(size.x, size.y, 1f);
//			gl.glVertex3f(0f, size.y, 1f);
//		gl.glEnd();
//		
//		//gl.glTranslatef(0f, 0f, 5f);
//		//textRenderer.begin3DRendering();
//		//textRenderer.setColor(0,0,0,1f);
//		//textRenderer.draw(model.key, 5, 5);
//		//textRenderer.end3DRendering();
//		
//		gl.glPopMatrix();
	}
	
	public void drawExtruded(GLAutoDrawable glD) {
		final GL gl = glD.getGL();

		Point3d p[] = extrudedPoints;
		p[0].x = 0f;     p[0].y = 0f;
		p[1].x = size.x; p[1].y = 0f;
		p[2].x = size.x; p[2].y = size.y;
		p[3].x = 0f;     p[3].y = size.y;

		Stitch.currentStitchView.getCamera().extrude(p, pos.point3d(), pos.z(), extrusionDepth);
		
		gl.glPushMatrix();
			gl.glTranslatef(pos.x(), pos.y(), -extrusionDepth);
			gl.glColor4f(color.getRed()*inv255, color.getGreen()*inv255, color.getBlue()*inv255, alpha);
			gl.glBegin(GL.GL_QUADS);
				for(int i = 0; i < 4; ++i)
					gl.glVertex2f(p[i].x, p[i].y);
			gl.glEnd();
	
			gl.glColor3f(0.0f, 0.0f, 0.0f);
			gl.glBegin(GL.GL_LINE_LOOP);
			for(int i = 0; i < 4; ++i)
				gl.glVertex3f(p[i].x, p[i].y, 1f);
			gl.glEnd();
			
		gl.glPopMatrix();

	}
	
	public void drawConnection(GLAutoDrawable glD, arch.model.Entity e, float y) {
		final GL gl = glD.getGL();
		
		if(e != null) {
			Entity view = Stitch.currentStitchView.getEntity(e.key);
			if(view != null) {
				Point3d ipMount = interpolateExtrusion(.5f, y/size.y);
				gl.glBegin(GL.GL_LINES);
					gl.glVertex3d(pos.x() + ipMount.x, pos.y() + ipMount.y, -extrusionDepth+5f);
					gl.glVertex3d(view.pos.x() + view.size.x/2, view.pos.y() + view.size.y/2, -5f);
				gl.glEnd();
			}
		}
	}

	public void drawConnectionBox(GLAutoDrawable glD, arch.model.Entity e) {
		final GL gl = glD.getGL();
		
		if(e != null) {
			Entity view = Stitch.currentStitchView.getEntity(e.key);
			if(view != null) {
				float y = (size.y-view.getHeight())/size.y;
				Point3d[] mount = {
						interpolateExtrusion(0f, y),
						interpolateExtrusion(1f, y),
						interpolateExtrusion(1f, 1f),
						interpolateExtrusion(0f, 1f),
				};
				gl.glColor4f(0f, 0f, 0f, .3f);
				gl.glBegin(GL.GL_LINES);
					for(int i = 0; i < 4; ++i) {
						gl.glVertex3d(pos.x() + mount[i].x, pos.y() + mount[i].y, getDepth());
						gl.glVertex3d(view.pos.x()+view.extrudedPoints[i].x, view.pos.y()+view.extrudedPoints[i].y, view.getDepth());
					}
				gl.glEnd();
			}
		}
	}

	public boolean isWithin(Point2d p) {
		if(p.x >= pos.x() && p.x <= pos.x()+size.x)
			if(p.y >= pos.y() && p.y <= pos.y()+size.y)
				return true;
		return false;
	}

	public boolean isWithin(Point2d min, Point2d max) {
		if(pos.x() >= min.x && pos.y() >= min.y)
			if(pos.x() + size.x <= max.x && pos.y() + size.y <= max.y)
				return true;
		return false;
	}

	public Point3d getPos() {
		if(guardian != null) return guardian.pos.point3d();
		return pos.point3d();
	}

	public void moveTo(Point2d p) {
		pos.x (p.x);
		pos.y (p.y);
	}
	
	public void snapTo(Point2d p) {
		pos.x (p.x);
		pos.y (p.y);
		pos.snap();
	}

	@Override
	public void update(Observable o, String signal, Object obj) {
		if(arch.model.Entity.class.isInstance(o)) {
			if(signal.equals(arch.model.Entity.UpdateMessage.Init.name()))
				init();
		}
	}

	public Point3d[] getBoundary() {
		return extrudedPoints;
	}

	public float getWidth() { return size.x; }
	public float getHeight() { return size.y; }

	public Point3d getCenter() {
		if(guardian != null) return guardian.getCenter();
		return new Point3d(pos.x()+size.x*.5f, pos.y()+size.y*.5f, pos.z());
	}
	
	public void store(Entity e) { if(!incoming.contains(e)) incoming.add(e); }
	public boolean hasCargo() { return !cargo.isEmpty(); }
	public void capture(Entity e) {
		if(e.guardian != null) e.guardian.release(e);
		e.guardian = this;
		cargo.add(e);
		Stitch.currentStitchView.checkOut(e);
	}
	public boolean release(Entity e) {
		if(!cargo.remove(e)) return false;
		e.guardian = null;
		e.snapTo(getCenter().point2d());
		Stitch.currentStitchView.checkIn(e);
		return true;
	}
	public void evacuate() {
		Point3d center = getCenter();
		for(Entity e : cargo) {
			e.guardian = null;
			e.snapTo(center.point2d());
			Stitch.currentStitchView.checkIn(e);
		}
		Layout.circle(cargo);
		cargo.clear();
	}
	
	public void congregate() {}
	public void hide() {}
	
}
