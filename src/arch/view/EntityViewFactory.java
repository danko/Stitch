package arch.view;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EntityViewFactory {

	private static Map<Class<? extends arch.model.Entity>, Class<? extends Entity>> registeredViewSubClasses
		=  new HashMap<Class<? extends arch.model.Entity>, Class<? extends Entity>>();
	
	public static void register(Class<? extends Entity> view, Class<? extends arch.model.Entity> model) {
		registeredViewSubClasses.put(model, view);
	}

	public static Entity createViewFor(arch.model.Entity model) {
		try {
			return registeredViewSubClasses.get(model.getClass())
				.getDeclaredConstructor(arch.model.Entity.class)
					.newInstance(model);
		}
		catch (SecurityException e) { e.printStackTrace(); }
		catch (NoSuchMethodException e) { e.printStackTrace(); }
		catch (IllegalArgumentException e) { e.printStackTrace(); }
		catch (InstantiationException e) { e.printStackTrace(); }
		catch (IllegalAccessException e) { e.printStackTrace(); }
		catch (InvocationTargetException e) { e.printStackTrace(); }
		
		
		//try { return registeredViewSubClasses.get(model.getClass()).newInstance().setModel(model); }
		//catch (InstantiationException e) { e.printStackTrace(); }
		//catch (IllegalAccessException e) { e.printStackTrace(); }
		catch (NullPointerException e) {
			System.err.println("Missing registration of view.Entity subclass for " + model.getClass().getName());
			return new Entity(model);
		}
		return null;
	}
	
}
