package arch.view;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.j2d.TextRenderer;

import arch.controller.SelectionItem;
import arch.model.Observable;
import arch.model.Point2d;

public class Selection extends Entity {
	
	public static void poke() {}
	static { EntityViewFactory.register(Selection.class, arch.model.Selection.class); }

	static String title = "Selection";
	static FontDescriptor heading;
	
	static {
		heading = (FontDescriptor) FontDescriptor.Default.clone();
		heading.style |= Font.BOLD;
		heading.size += 4;
	}
	
	
	arch.model.Selection model;
	String[] label, count;
	Map<Class<? extends Entity>, int[]> types;
	float columnWidth, columnHeight;
	
	public Selection(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.Selection) model;
		this.model.subscribe(this);
		refresh();
	}
	
	public void refresh() {
		
		types = new HashMap<Class<? extends Entity>, int[]>();
		Set<SelectionItem> sel = model.getAll();
		
		for(SelectionItem i : sel) {
			Class<? extends Entity> c = i.entity.getClass();
			if(!types.containsKey(c)) types.put(c, new int[1]);
			types.get(c)[0]++;
		}
		
		Set<Class<? extends Entity>> keys = types.keySet();
		label = new String[keys.size()];
		count = new String[keys.size()];
		int i = 0;
		for(Class<? extends Entity> k : keys) {
			try { label[i] = (String) k.getMethod("getTitle", (Class<?>[]) null).invoke(null, (Object[]) null); }
			catch (IllegalArgumentException e) { e.printStackTrace(); }
			catch (SecurityException e) { e.printStackTrace(); }
			catch (IllegalAccessException e) { e.printStackTrace(); }
			catch (InvocationTargetException e) { e.printStackTrace(); }
			catch (NoSuchMethodException e) { label[i] = k.getSimpleName(); }
			
			count[i] = Integer.toString( types.get(k)[0] );
			
			++i;
		}

		
		FontDescriptor fd = FontDescriptor.Default;
		TextRenderer   tr = Stitch.getTextRenderer(fd);
		TextRenderer   heading = Stitch.getTextRenderer(Selection.heading);

		// Update columnWidth (find widest label)
		columnWidth = (float) heading.getBounds(title).getWidth();
		for(String s : label) {
			float w = (float) tr.getBounds(s).getWidth();
			if(w > columnWidth) columnWidth = w;
		}
		
		columnHeight = fd.lineHeight() * label.length;
		
		size.x = 10f + columnWidth + 50f;
		size.y = 10f + Selection.heading.lineHeight() + columnHeight;
	}
	
	@Override
	public void draw(GLAutoDrawable glD) {
		final GL gl = glD.getGL();
		
		super.draw(glD);
		
		FontDescriptor fd = FontDescriptor.Default;
		TextRenderer   tr = Stitch.getTextRenderer(fd);
		TextRenderer   heading = Stitch.getTextRenderer(Selection.heading);
		
		gl.glPushMatrix();
		gl.glTranslatef(pos.x(), pos.y(), getDepth());
		
		float x = extrudedPoints[3].x + 5, y = extrudedPoints[3].y - fd.lineHeight() - 5f,
		z = 1f, x2 = x + columnWidth + 10f;

		heading.begin3DRendering();
		heading.setColor(0f, 0f, 0f, 1f);
		heading.draw3D(title, x, y, z, 1f);
		y -= Selection.heading.lineHeight();
		heading.end3DRendering();

		if(label != null) {
			tr.begin3DRendering();
			tr.setColor(0f, 0f, 0f, 1f);
			for(int i = 0; i < label.length; ++i) {
				tr.draw3D(label[i], x, y, z, 1f);
				tr.draw3D(count[i], x2, y, z, 1f);
				y -= fd.lineHeight();
			}
			tr.end3DRendering();
		}
		
		gl.glPopMatrix();
	}
	
	@Override
	public void update(Observable o, String signal, Object obj) {
		if(arch.model.Selection.class.isInstance(o)) {
			if(signal.equals(arch.model.Selection.UpdateMessage.Selection.name())) {
				refresh();
			}
		}
		else super.update(o, signal, obj);
	}

	public void mouseClicked(arch.controller.Stitch controller, MouseEvent event, Point2d p) {
		FontDescriptor fd = FontDescriptor.Default;

		float topOfList = extrudedPoints[3].y - 5f - Selection.heading.lineHeight();
		float row = ((topOfList-p.y) / fd.lineHeight());
		
		if( row >= 0 && row < label.length) {
			//System.out.println(label[(int)row]);
			Iterator<Class<? extends Entity>> itr = types.keySet().iterator();
			Class<? extends Entity> c = null;
			for(int i = 0; i < row; ++i) c = itr.next();
			
			Set<SelectionItem> sel = model.getAll(), newSel = new HashSet<SelectionItem>();
			for(SelectionItem e : sel)
				if(event.isControlDown()) {
					if(!e.entity.getClass().equals(c)) newSel.add(e);
				}
				else {
					if(e.entity.getClass().equals(c)) newSel.add(e);
				}
			model.clearSelection(false);
			model.add(newSel);
		}
	}

}
