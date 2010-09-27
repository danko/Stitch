package arch.view;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.media.opengl.GLAutoDrawable;

import arch.model.AnimatedValue;

public class Scaffold extends Entity {

	class Item {
		AnimatedValue offset = new AnimatedValue();
		float expandedOffset;
		Entity entity;
		
		void expand() { offset.setGoal(expandedOffset); }
		void contract() { offset.setGoal(Math.signum(expandedOffset)); }
	}
	
	final private Entity centerpiece;
	List<Item> items = new LinkedList<Item>(); //depth sorted (stack groups?)
	boolean expanded = false;

	public Scaffold(Entity centerpiece) {
		super(null);
		this.centerpiece = centerpiece;
	}
	
	@Override
	public void live() {
		super.live();
		getCenterpiece().live();
		for(Item i : items) {
			i.offset.live();
			i.entity.extrusionDepth = i.offset.get();
			i.entity.live();
		}
	}

	public void insert(Entity e, float offset) {
		Item i = new Item();
		i.offset.setGoal(Math.signum(offset));
		i.offset.snapTo();
		i.expandedOffset = offset;
		i.entity = e;
		insert(i);
	}
	
	public void insert(Item item) {
		//in decreasing sorted order by offset (draw order)
		ListIterator<Item> i = items.listIterator();
		while(i.hasNext()) if(item.offset.get() > i.next().offset.get()) { i.previous(); break; }
		i.add(item);
	}
	
	@Override
	public void draw(GLAutoDrawable glD) {
		getCenterpiece().draw(glD);
		for(Item i : items) i.entity.draw(glD);
	}
	
	public void expand() {
		expanded = true;
		for(Item i : items) i.expand();
	}
	
	public void contract() {
		expanded = false;
		for(Item i : items) i.contract();
	}

	public Entity getCenterpiece() { return centerpiece; }
	
}
