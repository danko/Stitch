package arch.view;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.LineMetrics;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import arch.controller.Controllable;
import arch.controller.Controller;
import arch.model.AnimatedValue;
import arch.model.Observable;
import arch.model.Observer;
import arch.model.Point2d;
import arch.view.world.DataWorld;
import arch.view.world.World;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

public class Stitch implements GLEventListener, Observer, Controllable {

	static Stitch currentStitchView;
	static GLU glu = new GLU();
	
	static private Map<FontDescriptor, TextRenderer> textRendererLibrary = new HashMap<FontDescriptor, TextRenderer>();

	static public TextRenderer getTextRenderer(FontDescriptor fd) {
		if(!textRendererLibrary.containsKey(fd)) {
			TextRenderer tr = new TextRenderer(new Font(fd.fontName, fd.style, fd.size), true, false);
			LineMetrics lm = tr.getFont().getLineMetrics("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", tr.getFontRenderContext());
			fd.setMetrics(lm);
			textRendererLibrary.put( fd, tr );
		}
		return textRendererLibrary.get(fd);
	}

	
	final public arch.model.Stitch model;
	
	Collection<Entity> entities = new LinkedList<Entity>(); //used for live and draw cycles
	HashMap<String, Entity> entitiesMap = new HashMap<String, Entity>();
	Collection<Entity> entitiesOnLoan = new LinkedList<Entity>();
	Collection<Controller> controllers = new LinkedList<Controller>();
	
	Collection<Entity> checkOutCart = new LinkedList<Entity>();
	Collection<Entity> checkInCart  = new LinkedList<Entity>();
	
	Camera camera = new Camera();

    GLCanvas canvas;
    Frame frame;
    Animator animator;
    boolean fullscreen = true; //set to true to start in fullscreen
    float angle = 0.0f;
    public final Color background = new Color(166, 202, 240); //sky blue
    
    public Timeline timeline;
    public Selection selection;
    public SearchBox searchBox;
    
	Decal cloudLogo;

	public Stitch(arch.model.Stitch m) {
		currentStitchView = this;

		model = m;
		model.subscribe(this);
		
		//timeline = (Timeline) EntityViewFactory.createViewFor(model.getEntity("Timeline"));
		searchBox = new SearchBox(model);
		
		frame = new Frame("Stitch");
		GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(false);
		caps.setAlphaBits(8);
		canvas = new GLCanvas(caps);
		animator = new FPSAnimator(canvas, 50);
		canvas.addGLEventListener(this);

		//rebuildFrame(fullscreen);
		//animator.start();
		
		new DataWorld(this); //.linkSuperWorld(new UserWorld());
	}
	
	public void show() {
		rebuildFrame(fullscreen);
		animator.start();
	}
	
	public boolean isFullscreen() { return fullscreen; }
	
	public int getHeight() { return canvas.getHeight(); }
	
	public Entity getEntity(String key) { return entitiesMap.get(key); }
	
	public void addEntity(Entity e) {
		if(e == null) return;
		entities.add(e);
		if(e.model != null)
			entitiesMap.put(e.model.key, e);
	}
	
	public void removeEntity(Entity e) {
		if(entities.contains(e)) entities.remove(e);
		if(entitiesMap.containsValue(e)) entitiesMap.remove(e);
		if(entitiesOnLoan.contains(e)) entitiesOnLoan.remove(e);
	}

	public boolean checkOut(Entity e) {
		//temporarily excludes entity from view.Stitch live and draw cycle
		if(!entities.contains(e)) return false;
		if(liveMutex) { checkOutCart.add(e); return true; } //postpone checkout until after live loop
		entities.remove(e);
		entitiesOnLoan.add(e);
		return true;
	}
	
	public boolean checkIn(Entity e) {
		if(checkOutCart.contains(e)) { checkOutCart.remove(e); return true; }
		if(!entitiesOnLoan.remove(e)) return false;
		if(liveMutex) { checkInCart.add(e); return true; } //postpone checkin until after live loop
		entities.add(e);
		return true;
	}
	
	public final Camera getCamera() { return camera; }
	
	public void rebuildFrame(boolean fullscreen) {
		this.fullscreen = fullscreen;

		boolean wasAnimating = animator.isAnimating();
		if(wasAnimating) animator.stop();
		
		frame.dispose();
		frame = new Frame("Stitch");

		canvas.setSize(1024, 768);
		frame.add(canvas);
		//frame.setSize(1024, 768);

		if(fullscreen) {
			frame.setUndecorated(true);
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		} else {
			frame.pack();
		}
		
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) { exit(); }
		    });

		frame.setVisible(true);
		canvas.requestFocus();
		if(wasAnimating) animator.start();
	}
	
    public void exit(){
		if(animator.isAnimating()) animator.stop();
		frame.dispose();
		System.exit(0);
    }

    private boolean liveMutex = false;
    public void live() {
    	camera.live();
    	
    	liveMutex = true;
		for(Entity e : entities) e.live();
		liveMutex = false;
		for(Entity e : checkOutCart) checkOut(e); checkOutCart.clear();
		for(Entity e : checkInCart ) checkIn (e); checkInCart .clear();
		
		World.headWorld.live();
		
		searchBox.live();
    }
    
	@Override
	public void init(GLAutoDrawable glD) {
		//glD.addKeyListener(this);
		glD.setAutoSwapBufferMode(false);
	
		GL gl = glD.getGL();
	
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
	
		float bgClr[] = {background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 0f};
		//gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		gl.glClearColor(bgClr[0], bgClr[1], bgClr[2], bgClr[3]);
		gl.glClearDepth(1.0f);

		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_DEPTH_TEST);
	
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_BLEND);
	
		//gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		gl.glEnable(GL.GL_LINE_SMOOTH);
		
		gl.glFogi(GL.GL_FOG_MODE, GL.GL_LINEAR);
		gl.glFogfv(GL.GL_FOG_COLOR, bgClr, 0);
		gl.glFogf(GL.GL_FOG_DENSITY, .01f);
		gl.glFogf(GL.GL_FOG_START, 1f);
		gl.glFogf(GL.GL_FOG_END,   2000f);
		
		//FloatBuffer mat = FloatBuffer.wrap(bgClr); //FloatBuffer.allocate(3);
		//mat.put(0, 0.1745f); mat.put(1, 0.01175f); mat.put(2, 0.01175f);
		//gl.glEnable(GL.GL_LIGHT0);
		//gl.glMaterialfv (GL.GL_FRONT, GL.GL_AMBIENT, mat);
		//gl.glMaterialfv (GL.GL_FRONT, GL.GL_DIFFUSE, mat);
		//gl.glMaterialfv (GL.GL_FRONT, GL.GL_SPECULAR, mat);

		//cloudLogo = new Decal("Cloud Logo.bmp");
		searchBox.init();
	}

	AnimatedValue cloudY = new AnimatedValue();
	int cloudTimer = 200;
	private static boolean displaying = false;
	@Override
	public void display(GLAutoDrawable glD) {
		if(displaying) return;
		displaying = true;
		live();
		
		final GL gl = glD.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		//gl.glLoadIdentity();

		if(World.headWorld != null) {
			World.headWorld.preDraw(glD);
			World.headWorld.draw(glD);
		}
		
		// HUD
		gl.glDisable(GL.GL_DEPTH_TEST);
			//non-centered elements
			gl.glPushMatrix();
				gl.glTranslatef(-canvas.getWidth()/2, -canvas.getHeight()/2, 0f);
				searchBox.draw(glD);
				selection.draw(glD);
			gl.glPopMatrix();
			
			//cloud logo
			//cloudY.live(); if(--cloudTimer == 0) cloudY.setGoal(cloudLogo.getHeight());
			//cloudLogo.drawAsOverlay(glD, -cloudLogo.getWidth()/2, getHeight()/2 - cloudLogo.getHeight() + cloudY.get());
		gl.glEnable(GL.GL_DEPTH_TEST);
		
		for(Controller c : controllers) c.postDraw(glD);

		//gl.glFlush();
		//glD.swapBuffers();
		displaying = false;
	}
	
	public void draw(GLAutoDrawable glD) {
		final GL gl = glD.getGL();

		//gl.glEnable(GL.GL_FOG);
		//gl.glEnable(GL.GL_LIGHTING);
		
		gl.glPushMatrix();
		
			camera.transform(glD);
	
			try {
				for(Entity e : entities) e.draw(glD);
			} catch(java.util.ConcurrentModificationException e) {
				System.out.println("Frame skipped: concurrent modification");
			}
	
			for(Controller c : controllers) c.draw(glD);
			
		gl.glPopMatrix();

		//gl.glDisable(GL.GL_FOG);
		//gl.glDisable(GL.GL_LIGHTING);
	}

	@Override
	public void displayChanged(GLAutoDrawable glD, boolean modeChanged, boolean deviceChanged) {}

	@Override
	public void reshape(GLAutoDrawable glD, int x, int y, int width, int height) {
		float fovy = 45f;
		camera.setPerspective(glD, glu, fovy, width, height);

		searchBox.setPos(new Point2d(15f, height-15f-25f));
		
		World.headWorld.reshape(width, height, glD.getGL());
	}

	static boolean haveSeenSuppressionWarning = false;
	@Override
	public void update(Observable o, String signal, Object obj) {
		if(arch.model.Stitch.class.isInstance(o)) {
			
			if(signal.equals(arch.model.Stitch.UpdateMessage.AddEntity.name())) {
				arch.model.Entity m = (arch.model.Entity) obj;
				
				if(arch.model.ExecutableBinaryImage.class.isInstance(obj)) {
					if(!haveSeenSuppressionWarning) {
						System.out.println("view.Stitch Reminder: generation of view for ExecutableBinaryImage suppressed");
						haveSeenSuppressionWarning = true;
					}
					return;
				}
				
				Entity v = EntityViewFactory.createViewFor(m);
				if(v != null) {
					entities.add(v);
					entitiesMap.put(v.model.key, v);
					
					if(arch.view.Selection.class.isInstance(v)) {
						v.snapTo(((arch.model.Selection)v.model).centerOfMass());
					}

					if(m.key.equals("Timeline")) timeline = (Timeline) v;
					if(m.key.equals("Selection")) {
						selection = (Selection) v;
						checkOut(v);
						v.snapTo(new Point2d(0f, 0f));
					}
				}
				
			}
			
		}
	}

	@Override
	public void addController(Controller c) {
		controllers.add(c);
		//frame.addKeyListener(c);
		canvas.addKeyListener(c);
		canvas.addMouseListener(c);
		canvas.addMouseMotionListener(c);
		canvas.addMouseWheelListener(c);
	}

	public Entity getEntityAt(Point2d xy) {
		for(Entity e : entities)
			if(e.isWithin(xy)) return e;
		return null;
	}

	public Collection<Entity> getEntitiesWithin(Point2d p1, Point2d p2) {
		Point2d min = new Point2d(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
		Point2d max = new Point2d(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
		Collection<Entity> group = new LinkedList<Entity>();
		for(Entity e : entities) if(e.isWithin(min, max)) group.add(e);
		return group;
	}

	public Collection<Entity> getAll() {
		return entities;
	}

	public void setCursor(Cursor c) {
		frame.setCursor(c);
	}
	
}
