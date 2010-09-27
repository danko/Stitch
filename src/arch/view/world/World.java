package arch.view.world;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.BufferUtil;

import arch.model.Point3d;

abstract public class World {

	static public World headWorld;
	
	class Portal {
		World world;
		Point3d pos = new Point3d();
		Point3d rotation = new Point3d();
		int width = 1024, height = 768;
		public void transform(GL gl) {
			gl.glTranslatef(pos.x, pos.y, pos.z);
			gl.glRotatef(rotation.x, 1f, 0f, 0f);
			gl.glRotatef(rotation.y, 0f, 1f, 0f);
			gl.glRotatef(rotation.z, 0f, 0f, 1f);
		}
	}
	
	World parentWorld;

	int viewWidth=1024, viewHeight=768;
	
	Portal[] portals;
	int[] subWorldTextures;
	
	public World(int numSubWorlds) {
		portals = new Portal[numSubWorlds];
		subWorldTextures = new int[numSubWorlds];
		for(int i = 0; i < numSubWorlds; ++i) subWorldTextures[i] = -1;
		if(headWorld == null) headWorld = this;
	}
	
	public boolean linkSuperWorld(World w) {
		if(parentWorld != null && !parentWorld.linkWorld(w, this)
				|| !w.linkWorld(this, this)) {
			return false;
		}
		if(headWorld == this || headWorld == null) headWorld = w;
		parentWorld = w;
		return true;
	}
	
	public boolean unlinkSuperWorld(World newParent) {
		if(parentWorld != null) {
			if(parentWorld.parentWorld != null)
				parentWorld.parentWorld.unlinkWorld(parentWorld);
			parentWorld.unlinkWorld(this);
		}
		if(newParent != null && !newParent.linkWorld(this, parentWorld)) return false;
		if(headWorld == parentWorld) headWorld = this;
		parentWorld = newParent;
		return true;
	}
	
	public boolean linkWorld(World w, World c) {
		// link w between this and c
		for(int i = 0; i < portals.length; ++i)
			if(portals[i].world == c) {
				if(!c.linkSuperWorld(w)) return false;
				portals[i].world = w;
				portals[i].width = w.viewWidth;
				portals[i].height = w.viewHeight;
				return true;
			}
		if(w == c) // match not found, start a new child
			for(int i = 0; i < portals.length; ++i)
				if(portals[i].world == null) {
					portals[i].world = w;
					portals[i].width = w.viewWidth;
					portals[i].height = w.viewHeight;
					return true;
				}
		return false;
	}
	
	public void unlinkWorld(World w) {
		for(int i = 0; i < portals.length; ++i)
			if(portals[i].world == w) portals[i].world = null; //unlinks all occurrences
	}
	
	public void reshape(int w, int h, GL gl) {
		viewWidth = w; viewHeight = h;
		if(parentWorld != null) parentWorld.childResized(this, gl);
	}
	
	public void childResized(World w, GL gl) {
		for(int i = 0; i < portals.length; ++i)
			if(portals[i].world == w) deleteTexture(i, gl);
	}
	
	private void genTexture(int index, GL gl) {
		World sw = portals[index].world;

		ByteBuffer data = BufferUtil.newByteBuffer(sw.viewWidth * sw.viewHeight * 4);
		data.limit(data.capacity());
		
		gl.glGenTextures(1, subWorldTextures, index);
		gl.glBindTexture(GL.GL_TEXTURE_2D, subWorldTextures[index]);
		
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 4, portals[index].world.viewWidth, portals[index].world.viewHeight,
				0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, data);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
	}
	
	private void deleteTexture(int index, GL gl) {
		if(subWorldTextures[index] == -1) return;
		gl.glDeleteTextures(1, subWorldTextures, index);
		subWorldTextures[index] = -1;
	}
	
	public void dispose(GL gl) {
		gl.glDeleteTextures(subWorldTextures.length, subWorldTextures, 0);
		for(int i = 0; i < subWorldTextures.length; ++i)
			subWorldTextures[i] = -1;
	}
	
	public void live() {
		for(Portal p : portals) if(p.world != null) p.world.live();
	}
	
	public void preDraw(GLAutoDrawable glD) { //render sub-worlds (updates texture buffers)
		GL gl = glD.getGL();
		for(int i = 0; i < portals.length; ++i) {
			World sw = portals[i].world;
			if(sw != null) {
				if(subWorldTextures[i] == -1) genTexture(i, gl);
				gl.glViewport(0, 0, sw.viewWidth, sw.viewHeight);

				sw.draw(glD);
				
				gl.glBindTexture(GL.GL_TEXTURE_2D, subWorldTextures[i]);
				gl.glCopyTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, 0, 0, sw.viewWidth, sw.viewHeight, 0); //no border
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				gl.glViewport(0, 0, viewWidth, viewHeight);
			} else {
				if(subWorldTextures[i] != -1) deleteTexture(i, gl);
			}
		
		}
	}

	public abstract void draw(GLAutoDrawable glD);
	
	public void postDraw(GLAutoDrawable glD) { //draw portals (render texture quads)
		final GL gl = glD.getGL();

		gl.glDisable(GL.GL_TEXTURE_GEN_S);
		gl.glDisable(GL.GL_TEXTURE_GEN_T);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_TEXTURE_2D);
		
		gl.glColor4f(1f, 1f, 1f, 1f);
		for(int i = 0; i < portals.length; ++i) {
			gl.glPushMatrix();
//				gl.glTranslatef(portals[i].pos.x, portals[i].pos.y, portals[i].pos.z);
//				gl.glRotatef(portals[i].rotation.x, 1f, 0f, 0f);
//				gl.glRotatef(portals[i].rotation.y, 0f, 1f, 0f);
//				gl.glRotatef(portals[i].rotation.z, 0f, 0f, 1f);
				portals[i].transform(gl);
	
				float hw = viewWidth * .5f, hh = viewHeight * .5f;
				gl.glBindTexture(GL.GL_TEXTURE_2D, subWorldTextures[i]);
				gl.glBegin(GL.GL_QUADS);
					gl.glTexCoord2f(0f, 0f); gl.glVertex2f(-hw, -hh);
					gl.glTexCoord2f(1f, 0f); gl.glVertex2f(+hw, -hh);
					gl.glTexCoord2f(1f, 1f); gl.glVertex2f(+hw, +hh);
					gl.glTexCoord2f(0f, 1f); gl.glVertex2f(-hw, +hh);
				gl.glEnd();
			gl.glPopMatrix();
		}

		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_TEXTURE_2D);
	}

}
