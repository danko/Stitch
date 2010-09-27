package arch.controller;

import arch.model.Point2d;
import arch.view.Entity;

public class SelectionItem {
	
	public Entity entity;
	public Point2d anchor;

	public SelectionItem(Entity e, Point2d a) {
		entity = e;
		anchor = (Point2d) a.clone();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!SelectionItem.class.isInstance(obj)) return false;
		return ((SelectionItem) obj).entity == entity;
	}
	
}
