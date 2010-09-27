package arch.model;

public interface Observer {

	void update(Observable o, String signal, Object obj);
	
	//Example implementation:
	//  super.update(o, signal, obj);
	//	if(arch.model.Stitch.class.isInstance(o)) {
	//		if(signal.equals(arch.model.Stitch.UpdateMessage.AddEntity.name())) {
	//			Entity e = EntityViewFactory.createViewFor( (arch.model.Entity) obj );
	//			if(e != null) entities.add(e);
	//		}
	//	}
	
}
