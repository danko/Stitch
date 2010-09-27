package arch.controller;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.InvalidKeyException;
import arch.model.Observable;
import arch.model.Observer;
import arch.model.Point2d;
import arch.model.Point3d;
import arch.model.Selection;
import arch.view.DataFormat;
import arch.view.DataSource;
import arch.view.Entity;
import arch.view.Layout;
import arch.view.Scaffold;
import arch.view.world.UserWorld;
import arch.view.world.World;

public class Stitch extends Controller implements Observer {

	public enum UpdateMessage { Selection /*Set<Selection> obj*/ };
	
	class PivotInfo {
		Entity selection;
		Scaffold scaffold;
	} PivotInfo pivotInfo = new PivotInfo();
	
	final static long doubleClickRate = 450; // ms between clicks 
	
	final arch.view.Stitch view;

	Point2d cursorPos; float cursorZ;
	Point2d hudPos = new Point2d();
	Point2d worldPos = new Point2d();
	boolean leftButtonDown = false, rightButtonDown = false, middleButtonDown = false;
	
	long timeOfLastClick = 0;
	boolean movedSinceClick = false;

	Selection selectionModel;
	
	public Stitch(arch.view.Stitch v) {
		super(v);
		view = v;
		
		subscribe(view.model.timeline);
		
		selectionModel = view.model.selection;
		selectionModel.subscribe(this);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(view.searchBox.isActive()) {
			
		}
		else {
			switch(e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE: view.exit();
				case KeyEvent.VK_F: // Fullscreen
					view.rebuildFrame(!view.isFullscreen());
					break;
				case KeyEvent.VK_L: // List
					if(selectionModel.hasSelection())
						Layout.line(selectionModel.getEntities());
					break;
				case KeyEvent.VK_G: // Grid
					if(selectionModel.hasSelection())
						Layout.grid(selectionModel.getEntities());
					break;
				case KeyEvent.VK_R: // Row
					if(selectionModel.hasSelection())
						Layout.row(selectionModel.getEntities());
					break;
				case KeyEvent.VK_C: // Circle
					if(selectionModel.hasSelection())
						Layout.circle(selectionModel.getEntities());
					break;
				case KeyEvent.VK_A: // All
					if(e.isControlDown()) selectionModel.selectAll(view);
					break;
				case KeyEvent.VK_H: // Hide
					for(SelectionItem s : selectionModel.getAll())
						s.entity.hide();
					selectionModel.clearSelection(true);
					break;
				case KeyEvent.VK_EQUALS: view.getCamera().scaleBy(1f +.15f); break;
				case KeyEvent.VK_MINUS:  view.getCamera().scaleBy(1f -.15f); break;
				case KeyEvent.VK_0:      view.getCamera().setScale(1f); break;
				case KeyEvent.VK_SPACE: World.headWorld.linkSuperWorld(new UserWorld()); break;
				case KeyEvent.VK_S:		view.model.addEntity(view.model.selection.clone()); break; //"Save" selection
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent event) {
		if(view.searchBox.isActive()) {
			view.searchBox.keyTyped(event);
		}
		else {
			switch(event.getKeyChar()) {
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if(movedSinceClick == false && System.currentTimeMillis()-timeOfLastClick < doubleClickRate) {
			mouseDoubleClicked(event);
			timeOfLastClick = System.currentTimeMillis();
			return;
		}
		
		if(event.getButton() == MouseEvent.BUTTON1) {
			if(view.selection.isWithin(hudPos)) {
				view.selection.mouseClicked(this, event, hudPos.minus(view.selection.getPos()));
			}
			else if(selectionModel.hasSelection()) {
				if(selectionModel.isSingle() && !event.isControlDown() && selectionModel.getFirst().entity.isWithin(worldPos)) {
					arch.view .Entity v = selectionModel.getFirst().entity;
					arch.model.Entity m = v.model;
					
					System.out.println(selectionModel.getFirst().entity.getClass() + " " + m.key);
					
					if(arch.view.Domain.class.isInstance(v)) {
						arch.view.Domain d = (arch.view.Domain) v;
						if(d.hasCargo()) d.evacuate();
						else d.congregate();
					}
					if(arch.view.DataFormat.class.isInstance(v)) {
						if(v.hasCargo()) v.evacuate();
						else v.congregate();
					}
					if(arch.view.Project.class.isInstance(v)) {
						if(v.hasCargo()) v.evacuate();
						else v.congregate();
					}
					if(arch.view.Selection.class.isInstance(v)) {
						selectionModel.clearSelection(false);
						selectionModel.add( ((Selection) m).getAll() );
					}
				}
			}
		}
		
		if(event.getButton() == MouseEvent.BUTTON2) {
			view.getCamera().tiltTo(0f, 0f);
		}
		
		if(event.getButton() == MouseEvent.BUTTON3) {
			view.getCamera().moveTo(worldPos);
		}

		timeOfLastClick = System.currentTimeMillis();
		movedSinceClick = false;
	}

	public void mouseDoubleClicked(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON1) {
			if(selectionModel.hasSelection()) {
				if(selectionModel.isSingle() && !event.isControlDown()) {
					arch.view .Entity v = selectionModel.getFirst().entity;
					arch.model.Entity m = v.model;
					System.out.println(v.getClass() + " " + m.key);
					if(arch.model.DataSource.class.isInstance(m))
						((arch.model.DataSource) m).load();
					if(arch.model.DataFormat.class.isInstance(m))
						((arch.model.DataFormat) m).loadAll();
					if(arch.model.IPAddress.class.isInstance(m)) {
						arch.model.IPAddress ip = (arch.model.IPAddress) m;
						//selectionModel.clearSelection(false);
						for(arch.model.PhysicalHost h : ip.getHosts()){
							selectionModel.add(view.getEntity(h.key), false);
						}
						for(arch.model.Server s : ip.getServers()){
							selectionModel.add(view.getEntity(s.key), false);
						}
						selectionModel.refresh();
					}
						
				} else { // !isSingle()
					for(SelectionItem i : selectionModel.getAll()) {
						arch.view .Entity v = i.entity;
						arch.model.Entity m = v.model;
						if(arch.model.DataSource.class.isInstance(m))
							((arch.model.DataSource) m).load();
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent event) {
		worldPos = view.getCamera().untransform(cursorPos, cursorZ);

		if(event.getButton() == MouseEvent.BUTTON1) leftButtonDown = true;
		if(event.getButton() == MouseEvent.BUTTON2) middleButtonDown = true;
		if(event.getButton() == MouseEvent.BUTTON3) rightButtonDown = true;

		if(view.searchBox.isActive()) {
			if(!view.searchBox.isWithin(hudPos))
				view.searchBox.deactivate();
		}
		
		//Mutual exclusion section
		
		if(view.searchBox.isWithin(hudPos)) {
			view.searchBox.activate();
		}
		
		else if(view.selection.isWithin(hudPos)) {

		}
		
		else if(event.getButton() == MouseEvent.BUTTON1) {
			selectionModel.dragOrigin = worldPos.clone();

			Entity e = view.getEntityAt(worldPos);
			if(e == null) { //hit nothing; start selection box
				if(event.isControlDown()) selectionModel.continueSelectionBox(worldPos);
				else {
					boolean had = selectionModel.hasSelection();
					selectionModel.startSelectionBox(worldPos);
					if(had) changed(this, UpdateMessage.Selection.name(), selectionModel.getAll());
				}
			} else { //hit something; select it; ready to drag
				if(event.isControlDown()) {
					selectionModel.continueSelectionBox(worldPos);
					if(selectionModel.isSelected(e)) selectionModel.remove(e);
					else selectionModel.add(e, true);
				} else {
					selectionModel.holding = true;
					if(!selectionModel.isSelected(e)) {
						selectionModel.clearSelection(false);
						selectionModel.add(e, true);
					}
					else if(selectionModel.isSingle()) { //re-add it to update anchor by dragOrigin
						selectionModel.clearSelection(false);
						selectionModel.add(e, true);
					} else {
						selectionModel.reAnchor();
					}
				}
			}
		}
		
		else if(event.getButton() == MouseEvent.BUTTON2) { 
			pivotInfo.selection = view.getEntityAt(worldPos);
			if(pivotInfo.selection != null) {
				view.checkOut(pivotInfo.selection);
				pivotInfo.scaffold = new Scaffold(pivotInfo.selection);
				view.addEntity(pivotInfo.scaffold);
			}
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		worldPos = view.getCamera().untransform(cursorPos, cursorZ);

		if(event.getButton() == MouseEvent.BUTTON1) {
			leftButtonDown = false;

			selectionModel.holding = false;
			if(selectionModel.selectionBox) {
				Collection<Entity> group = view.getEntitiesWithin(selectionModel.boxOrigin, worldPos);
				int n = selectionModel.getAll().size();
				for(Entity e : group) selectionModel.add(e, false);
				if(selectionModel.getAll().size() != n) {
					selectionModel.refresh();
					//changed(this, UpdateMessage.Selection.name(), selectionInfo.getAll());
				}
				selectionModel.stopSelectionBox();
			} else {
				if(selectionModel.hasSelection() && !selectionModel.isSingle()) {
					selectionModel.boxOrigin.plusEquals(worldPos.minus(selectionModel.dragOrigin));
				}
			}
		}

		if(event.getButton() == MouseEvent.BUTTON2) {
			middleButtonDown = false;
			
			//view.getCamera().tiltTo(0f, 0f);

			if(pivotInfo.selection != null) {
				view.removeEntity(pivotInfo.scaffold);
				view.checkIn(pivotInfo.scaffold.getCenterpiece());
				pivotInfo.scaffold = null;
				pivotInfo.selection = null;
			}
		}

		if(event.getButton() == MouseEvent.BUTTON3) rightButtonDown = false;
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		movedSinceClick = true;
		worldPos = view.getCamera().untransform(cursorPos, cursorZ);
		Point2d diff = new Point2d(event.getPoint()); diff.x -= cursorPos.x; diff.y -= cursorPos.y;

		if(leftButtonDown) {
			if(selectionModel.isHoldingSelection()) {
				if(selectionModel.isSingle()) {
					selectionModel.getFirst().entity.snapTo( worldPos.plus(selectionModel.getFirst().anchor) );
				} else {
					for(SelectionItem s : selectionModel.getAll())
						s.entity.snapTo( worldPos.minus(selectionModel.dragOrigin) .plusEquals(selectionModel.boxOrigin).plusEquals(s.anchor) );
				}
			}
		}
		
		if(middleButtonDown) {
			view.getCamera().tiltBy(diff.x*.5f, diff.y*.5f);
		}
		
		if(rightButtonDown) {
			view.getCamera().panBy(-diff.x, diff.y);
		}

		cursorPos = new Point2d(event.getPoint());
	}

	boolean searchBoxHovered = false;
	@Override
	public void mouseMoved(MouseEvent event) {
		movedSinceClick = true;
		cursorPos = new Point2d(event.getPoint());
		hudPos.x = cursorPos.x; hudPos.y = view.getHeight() - cursorPos.y;
		worldPos = view.getCamera().untransform(cursorPos, cursorZ);
		
		if(view.searchBox.isWithin(hudPos)) {
			view.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			searchBoxHovered = true;
		} else if(searchBoxHovered) {
			view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			searchBoxHovered = false;
		}
		
		if(view.timeline.isWithin(worldPos)) {
			view.timeline.hover(worldPos.minus(view.timeline.getPos()));
		} else {
			if(view.timeline.isHovered())
				view.timeline.exited();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		//int ds = event.getUnitsToScroll(); //accounts for OS user preferences
		int ds = event.getWheelRotation(); //literal
		view.timeline.zoomBy(1f + ds*-.25f);
		//view.getCamera().scaleBy(1f + ds*-.15f);
	}
	
	@Override
	public void draw(GLAutoDrawable glD) {
		GL gl = glD.getGL();
		
		gl.glDepthFunc(GL.GL_ALWAYS);
		
		if(selectionModel.selectionBox) {
			gl.glColor4f(.3f, .3f, 1f, .3f);
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex2f(selectionModel.boxOrigin.x, selectionModel.boxOrigin.y);
				gl.glVertex2f(worldPos.x               , selectionModel.boxOrigin.y);
				gl.glVertex2f(worldPos.x               , worldPos.y               );
				gl.glVertex2f(selectionModel.boxOrigin.x, worldPos.y               );
			gl.glEnd();

			gl.glColor4f(.3f, .3f, 1f, .9f);
			gl.glBegin(GL.GL_LINE_LOOP);
				gl.glVertex2f(selectionModel.boxOrigin.x, selectionModel.boxOrigin.y);
				gl.glVertex2f(worldPos.x               , selectionModel.boxOrigin.y);
				gl.glVertex2f(worldPos.x               , worldPos.y               );
				gl.glVertex2f(selectionModel.boxOrigin.x, worldPos.y               );
			gl.glEnd();
		}

		if(selectionModel.hasSelection()) {
			for(SelectionItem s : selectionModel.getAll()) {
				Point3d p = s.entity.getPos();
				float z = s.entity.getDepth();
				Point3d[] b = s.entity.getBoundary();
				gl.glColor4f(1f, 1f, .3f, .2f);
				gl.glBegin(GL.GL_QUADS);
					gl.glVertex3f(p.x + b[0].x, p.y + b[0].y, z);
					gl.glVertex3f(p.x + b[1].x, p.y + b[1].y, z);
					gl.glVertex3f(p.x + b[2].x, p.y + b[2].y, z);
					gl.glVertex3f(p.x + b[3].x, p.y + b[3].y, z);
				gl.glEnd();
			}
		}
		
		//Draw an ugly test cursor
//		gl.glColor3f(0f, 0f, 0f);
//		gl.glBegin(GL.GL_QUADS);
//			gl.glVertex2f(worldPos.x-10, worldPos.y-10);
//			gl.glVertex2f(worldPos.x+10, worldPos.y-10);
//			gl.glVertex2f(worldPos.x+10, worldPos.y+10);
//			gl.glVertex2f(worldPos.x-10, worldPos.y+10);
//		gl.glEnd();
		
		gl.glDepthFunc(GL.GL_LEQUAL);
	}
	
	@Override
	public void postDraw(GLAutoDrawable glD) {
		final GL gl = glD.getGL();
		
		if(cursorPos != null) {
			FloatBuffer buffer = FloatBuffer.allocate(1);
			gl.glReadPixels((int)cursorPos.x, view.getHeight()-(int)cursorPos.y, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, buffer); //sample z-buffer
			cursorZ = buffer.get(0);
		}
	}

	//@Reification
	public void update(Observable o, String signal, Object obj) {
		if(arch.model.Selection.class.isInstance(o)) {
			changed(this, UpdateMessage.Selection.name(), obj); //re-transmit to our observers
		}
	}
	
}
