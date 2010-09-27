package arch.model;

import java.awt.Point;

public class Point2d implements Cloneable {

	public float x, y;
	
	public Point2d() {}
	public Point2d(float x, float y) { this.x = x; this.y = y; }
	
	public Point2d(Point p) {
		x = p.x; y = p.y;
	}
	
	@Override
	public Point2d clone() {
		return new Point2d(x, y);
	}
	
	public Point2d minus(Point2d rhs) {
		return new Point2d(x-rhs.x, y-rhs.y);
	}

	public Point2d plus(Point2d rhs) {
		return new Point2d(x+rhs.x, y+rhs.y);
	}
	
	public Point2d plusEquals(Point2d rhs) {
		x += rhs.x; y += rhs.y;
		return this;
	}
	
	public Point2d minusEquals(Point2d rhs) {
		x -= rhs.x; y -= rhs.y;
		return this;
	}

	public Point2d times(float f) {
		return new Point2d(x*f, y*f);
	}
	
	public Point2d minus(Point3d rhs) {
		return new Point2d(x-rhs.x, y-rhs.y);
	}
	
	public void divideEquals(float f) {
		x /= f; y /= f;
	}
	
	public float distance() {
		return (float) Math.sqrt(x*x + y*y);
	}
	
}
