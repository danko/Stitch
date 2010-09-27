package arch.view;

import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class Decal {

	Texture tex;
	
	public Decal(String filename) {
		try { tex = TextureIO.newTexture(new File(filename), false); }
		catch (GLException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public int getWidth() { return tex.getWidth(); }
	public int getHeight() { return tex.getHeight(); }
	
	public void drawAsOverlay(GLAutoDrawable glD, float x, float y) {
		// Overlay = disable depth test
		final GL gl = glD.getGL();
		
		gl.glDisable(GL.GL_DEPTH_TEST);
		tex.bind();
		gl.glEnable(GL.GL_TEXTURE_2D);
		float w = tex.getWidth(), h = tex.getHeight();
		gl.glColor3f(1f, 1f, 1f);
		gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0f, 0f); gl.glVertex2f(x  , y);
			gl.glTexCoord2f(1f, 0f); gl.glVertex2f(x+w, y);
			gl.glTexCoord2f(1f, 1f); gl.glVertex2f(x+w, y+h);
			gl.glTexCoord2f(0f, 1f); gl.glVertex2f(x  , y+h);
		gl.glEnd();
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glEnable(GL.GL_DEPTH_TEST);
	}
	
}
