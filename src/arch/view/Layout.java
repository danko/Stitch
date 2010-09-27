package arch.view;

import java.util.Collection;

import arch.model.Point2d;

public class Layout {

	static public void line(Collection<Entity> col) {
		final float gap = 10f; //pixel buffer between each
		
		Point2d average = new Point2d(0f, 0f);
		float height = 0f;
		for(Entity e : col) {
			average.plusEquals(e.getPos().point2d());
			height += e.getHeight();
		}
		int n = col.size();
		average.divideEquals(n);
		height += (n-1)*gap;
		
		float baseY = -height/2;
		float widest = 0f;
		float x = 0, y = baseY;
		for(Entity e : col) {
			e.moveTo(average.plus(new Point2d(x, y)));
			if(e.getWidth() > widest) widest = e.getWidth();
			y += e.getHeight() + gap;
		}
	}
	
	static public void grid(Collection<Entity> col) {
		final float gap = 10f; //pixel buffer between each
		
		Point2d average = new Point2d(0f, 0f);
		float height = 0f;
		for(Entity e : col) {
			average.plusEquals(e.getPos().point2d());
			height += e.getHeight();
		}
		int n = col.size();
		average.divideEquals(n);
		
		height += (n-1)*gap;
		float columnHeight = Stitch.currentStitchView.getHeight() * .8f;
		if(height > columnHeight) height = columnHeight;

		float baseY = -height/2;
		float widest = 0f;
		Point2d offset = new Point2d(0, baseY);
		for(Entity e : col) {
			e.moveTo(average.plus(offset));
			if(e.getWidth() > widest) widest = e.getWidth();
			offset.y += e.getHeight() + gap;
			if(offset.y-baseY > columnHeight) {
				offset.x += widest + gap;
				widest = 0f;
				offset.y = baseY;
			}
		}
	}

	static public void row(Collection<Entity> col) {
		final float gap = 10f; //pixel buffer between each
		
		Point2d average = new Point2d(0f, 0f);
		float width = 0f;
		for(Entity e : col) {
			average.plusEquals(e.getPos().point2d());
			width += e.getWidth();
		}
		int n = col.size();
		average.divideEquals(n);
		width += (n-1)*gap;
		
		float baseX = -width/2;
		float x = baseX, y = 0;
		for(Entity e : col) {
			e.moveTo(average.plus(new Point2d(x, y)));
			x += e.getWidth() + gap;
		}
	}
	
	static public void circle(Collection<Entity> col) {
		float maxDim = 10f;
		Point2d average = new Point2d(0f, 0f);
		for(Entity s : col) {
			average.plusEquals(s.getPos().point2d());
			if(s.getWidth() > maxDim) maxDim = s.getWidth();
			if(s.getHeight() > maxDim) maxDim = s.getHeight();
		}
		int n = col.size();
		average.divideEquals(n);

		float a = 0f, r = (float) (Math.sqrt(2*maxDim*maxDim) * n / Math.PI / 2);
		float aInc = (float)(2f*Math.PI / n);
		for(Entity s : col) {
			float x = (float)Math.cos(a)*r, y = (float)Math.sin(a)*r;
			s.moveTo(average.plus(new Point2d(x, y)));
			a += aInc;
		}
	}
	
}
