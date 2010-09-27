package arch.model;

public class AnimatedPoint3d {

	AnimatedValue x, y, z;
	
	public AnimatedPoint3d() {
		x = new AnimatedValue();
		y = new AnimatedValue();
		z = new AnimatedValue();
	}
	
	public float x() { return x.get(); }
	public float y() { return y.get(); }
	public float z() { return z.get(); }
	
	public float goalX() { return x.getGoal(); }
	public float goalY() { return y.getGoal(); }
	public float goalZ() { return z.getGoal(); }

	public void x(float v) { x.setGoal(v); }
	public void y(float v) { y.setGoal(v); }
	public void z(float v) { z.setGoal(v); }
	
	public void set(Point3d p) {
		x.setGoal(p.x);
		y.setGoal(p.y);
		z.setGoal(p.z);
	}
	
	public void live() {
		x.live();
		y.live();
		z.live();
	}
	
	public void snap() {
		x.snapTo();
		y.snapTo();
		z.snapTo();
	}
	
	public Point3d point3d() {
		return new Point3d(x.get(), y.get(), z.get());
	}
	
	public Point3d minus(AnimatedPoint3d rhs) {
		return new Point3d(x.getGoal()-rhs.goalX(), y.getGoal()-rhs.goalY(), z.getGoal()-rhs.goalZ());
	}
	
	public void plusEquals(Point3d rhs) {
		x.setGoal(x.getGoal() + rhs.x);
		y.setGoal(y.getGoal() + rhs.y);
		z.setGoal(z.getGoal() + rhs.z);
	}

	public Point3d plus(Point3d p) {
		return new Point3d(x.get()+p.x, y.get()+p.y, y.get()+p.z);
	}

	public Point3d plus(float xd, float yd) {
		return new Point3d(x.get()+xd, y.get()+yd, y.get());
	}

	public void plusEquals(Point2d rhs) {
		x.setGoal(x.getGoal() + rhs.x);
		y.setGoal(y.getGoal() + rhs.y);
	}
	
}
