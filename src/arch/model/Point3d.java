package arch.model;

public class Point3d {

	public float x, y, z;
	
	public Point3d() {}
	public Point3d(float x, float y) { this.x = x; this.y = y; }
	public Point3d(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
	
	public Point3d minus(Point3d rhs) {
		return new Point3d(x-rhs.x, y-rhs.y, z-rhs.z);
	}

	public Point3d plus(Point3d rhs) {
		return new Point3d(x+rhs.x, y+rhs.y, z+rhs.z);
	}
	
	public void plusEquals(Point3d p) {
		x += p.x; y += p.y; z += p.z;
	}
	
	public void plusEquals(Point2d p) {
		x += p.x; y += p.y;
	}
	
	public Point3d times(float f) {
		return new Point3d(x*f, y*f, z*f);
	}
	
	public Point2d point2d() {
		return new Point2d(x, y);
	}
	
	public float distance() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	public float distance2d() {
		return (float) Math.sqrt(x*x + y*y);
	}
	public Point3d plus(float dx, float dy) {
		return new Point3d(x+dx, y+dy, z);
	}
	
	public Object clone() {
		return new Point3d(x, y, z);
	}
	public void match(Point3d p) {
		x = p.x; y = p.y; z = p.z;
	}
}
