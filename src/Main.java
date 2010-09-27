public class Main {

	static { //Initialize other classes (execute static blocks)
		arch.view.PhysicalHost			.poke();
		arch.view.IPAddress				.poke();
		arch.view.ProcessInstance		.poke();
		arch.view.ExecutableBinaryImage	.poke();
		arch.view.Project				.poke();
		arch.view.DataFormat			.poke();
		arch.view.DataSource			.poke();
		arch.view.Timeline				.poke();
		arch.view.Domain				.poke();
		arch.view.Server				.poke();
		arch.view.Selection				.poke();
		arch.view.DataPool				.poke();
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		arch.model     .Stitch model      = new arch.model     .Stitch();
		arch.view      .Stitch view       = new arch.view      .Stitch(model); // Separate thread
		arch.controller.Stitch controller = new arch.controller.Stitch(view );

		apps.App netVis = new apps.netVis.NetVis(model);
		netVis.loadWorkspace("U:\\Data\\Stitch\\");
	
		view.show();
	}

}
