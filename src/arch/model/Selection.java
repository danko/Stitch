package arch.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import arch.controller.SelectionItem;
import arch.controller.Stitch.UpdateMessage;

public class Selection extends Entity implements Cloneable {
	
	public enum UpdateMessage { Selection /*Set<Selection> obj*/ };
	
	public boolean selectionBox = false;
	public boolean holding = false;
	public Point2d dragOrigin;
	public Point2d boxOrigin;
	private Set<SelectionItem> selections = new HashSet<SelectionItem>();
	
	public Selection(String key) throws InvalidKeyException {
		super(key);
	}

	public boolean hasSelection() { return selections.size() > 0; }
	
	public boolean isSingle() { return selections.size() == 1; }
	
	public SelectionItem getFirst() { return selections.iterator().next(); }
	
	public Set<SelectionItem> getAll() { return selections; }
	
	public void startSelectionBox(Point2d start) { clearSelection(true); boxOrigin = start.clone(); selectionBox = true; }
	
	public void continueSelectionBox(Point2d start) { boxOrigin = start.clone(); selectionBox = true; reAnchor(); }
	
	public void stopSelectionBox() { selectionBox = false; }
	
	public void clearSelection(boolean sendMessage) {
		boolean wasNonEmpty = hasSelection();
		Iterator<SelectionItem> i = selections.iterator();
		while(i.hasNext()) i.next().entity.setSelection(false);
		selections.clear();
		if(sendMessage && wasNonEmpty) changed(this, UpdateMessage.Selection.name(), getAll());
	}
	
	public void add(arch.view.Entity e, boolean sendMessage) {
		if(e != null && !isSelected(e)) {
			selections.add(new SelectionItem(e, e.getPos().point2d().minusEquals(dragOrigin)));
			e.setSelection(true);
		}
		//use dragOrigin because this might not be a box selection (they are identical during a box drag)
		//selectionBox ? boxOrigin : dragOrigin
		if(sendMessage) changed(this, UpdateMessage.Selection.name(), getAll());
	}
	
	public void add(Set<SelectionItem> all) {
		for(SelectionItem s : all) add(s.entity, false);
		refresh();
	}

	public void remove(arch.view.Entity e) {
		Iterator<SelectionItem> i = selections.iterator();
		while(i.hasNext())
			if(i.next().entity == e) {
				e.setSelection(false);
				i.remove();
				changed(this, UpdateMessage.Selection.name(), getAll());
				break;
			}
	}
	
	public boolean isSelected(arch.view.Entity e) {
		for(SelectionItem s : selections) if(s.entity == e) return true;
		return false;
	}
	
	public void reAnchor() {
		for(SelectionItem s : selections) s.anchor = s.entity.getPos().point2d().minusEquals(boxOrigin);
	}
	
	public boolean isHoldingSelection() {
		return holding;
	}
	
	public Collection<arch.view.Entity> getEntities() {
		Collection<arch.view.Entity> ents = new LinkedList<arch.view.Entity>();
		for(SelectionItem s : selections) ents.add(s.entity);
		return ents;
	}
	
	public void selectAll(arch.view.Stitch view) {
		if(dragOrigin == null) dragOrigin = new Point2d(0f, 0f);
		if(boxOrigin == null) boxOrigin = new Point2d(0f, 0f);
		clearSelection(false);
		for(arch.view.Entity e : view.getAll()) add(e, false);
		changed(this, UpdateMessage.Selection.name(), getAll());
	}

	public void refresh() {
		changed(this, UpdateMessage.Selection.name(), getAll());
	}
	
	public Point2d centerOfMass() {
		Point2d average = new Point2d(0f, 0f);
		float width = 0f;
		Collection<arch.view.Entity> ents = getEntities();
		for(arch.view.Entity e : ents) {
			average.plusEquals(e.getPos().point2d());
			width += e.getWidth();
		}
		int n = ents.size();
		average.divideEquals(n);
		return average;
	}
	
	public Selection clone() {
		Selection s = null;
		String k = key;
		boolean done = false;
		do { k += "Clone";
			try {
				s = new Selection(k);
				done = true;
				s.selections.addAll(selections);
			} catch (InvalidKeyException e) { e.printStackTrace(); }
		} while(!done);
		return s;
	}
}
