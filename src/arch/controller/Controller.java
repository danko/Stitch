package arch.controller;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GLAutoDrawable;

import arch.model.Observable;

public abstract class Controller extends Observable implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	public Controller(Controllable v) {
		v.addController(this);
	}

	public void draw(GLAutoDrawable glD) {}

	public void postDraw(GLAutoDrawable glD) {}
	
}
