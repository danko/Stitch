package arch.view;

import java.awt.event.KeyEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.j2d.TextRenderer;

import arch.model.Point2d;

public class SearchBox {
	
	static FontDescriptor mainFont;
	static {
		mainFont = (FontDescriptor) FontDescriptor.Default.clone();
		mainFont.size += 6;
	}
	
	static int blinkTime = 500; //ms
	static final String prompt = "Search";
	
	final arch.model.Stitch model;
	boolean active = false;
	
	Point2d pos = new Point2d();
	Point2d size = new Point2d();
	
	String contents = prompt;
	
	boolean showCursor = true;
	int cursorTimer = blinkTime;
	long lastTime = System.currentTimeMillis();
	
	public SearchBox(arch.model.Stitch model) {
		this.model = model;
		
		size.x = 200f;
		size.y = 25f;
	}
	
	public void init() { Stitch.getTextRenderer(mainFont); refresh(); }
	
	public void refresh() {
		size.y = mainFont.metrics.getHeight() + 4f;
	}
	
	public void setPos(Point2d p) { pos.x = p.x; pos.y = p.y; }

	public void activate() {
		active = true;
		cursorTimer = blinkTime; showCursor = true;
		if(contents == prompt) contents = "";
	}
	
	public void deactivate() {
		active = false;
		if(contents.equals("")) contents = prompt;
	}
	
	public boolean isActive() { return active; }
	
	public void live() {
		if(active) {
			cursorTimer -= System.currentTimeMillis() - lastTime;
			if(cursorTimer < 0) { showCursor = !showCursor; cursorTimer = blinkTime; }
			lastTime = System.currentTimeMillis();
		}
	}
	
	public void draw(GLAutoDrawable glD) {
		final GL gl = glD.getGL();
		
		gl.glPushMatrix();
			gl.glTranslatef(pos.x, pos.y, 0f);
			gl.glColor4f(0f, 0f, 0f, .3f);
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex2f(6f       ,       -4f);
				gl.glVertex2f(size.x+6f,       -4f);
				gl.glVertex2f(size.x+6f, size.y-4f);
				gl.glVertex2f(6f       , size.y-4f);
			gl.glEnd();
			gl.glColor3f(1f, 1f, 1f);
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex2f(0f    , 0f );
				gl.glVertex2f(size.x, 0f );
				gl.glVertex2f(size.x, size.y);
				gl.glVertex2f(0f    , size.y);
			gl.glEnd();
			gl.glColor3f(0f, 0f, 0f);
			gl.glBegin(GL.GL_LINE_LOOP);
				gl.glVertex2f(0f    , 0f );
				gl.glVertex2f(size.x, 0f );
				gl.glVertex2f(size.x, size.y);
				gl.glVertex2f(0f    , size.y);
			gl.glEnd();
			
			TextRenderer tr = Stitch.getTextRenderer(mainFont);
			tr.begin3DRendering();
			tr.setColor(0f, 0f, 0f, .7f);
			tr.draw3D(contents, 5f, 2f+mainFont.metrics.getDescent(), 0f, 1f);
			tr.end3DRendering();
			
			if(active && showCursor) {
				float x = 5f + 1f + (float) tr.getBounds(contents).getWidth();
				gl.glColor4f(0f, 0f, 0f, 1f);
				gl.glBegin(GL.GL_LINES);
					gl.glVertex2f(x, 2f);
					gl.glVertex2f(x, 2f + mainFont.metrics.getHeight());
				gl.glEnd();
			}
			
		gl.glPopMatrix();
	}
	
	public void keyTyped(KeyEvent event) {
		if(event.getKeyChar() == KeyEvent.VK_ESCAPE) {
			deactivate();
		}
		else if(event.getKeyChar() == '\n') {
			model.search(contents);
			deactivate();
		}
		else if(event.getKeyChar() == '\b') {
			if(contents.length() > 0)
				contents = contents.substring(0, contents.length()-1);
		}
		else
			contents += event.getKeyChar();
		
		cursorTimer = blinkTime;
		showCursor = true;
	}

	public boolean isWithin(Point2d p) {
		if(p.x >= pos.x && p.x <= pos.x+size.x)
			if(p.y >= pos.y && p.y <= pos.y+size.y)
				return true;
		return false;
	}

}
