package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;

public class BusSimulationExample implements IApplication {

	private BusSimConfig config;
	private BusModel model;
	private ISimulationControl simControl;
	private BusFederate component;
	
	
	 private static final Duration MAX_SIMULATION_TIME = HumanSimValues.MAX_SIM_TIME;
	
	
	public BusSimulationExample() {
		this.config = new BusSimConfig();
		this.model = BusModel.create(config);
		this.simControl = model.getSimulationControl();
		this.simControl.setMaxSimTime((long) MAX_SIMULATION_TIME.toSeconds().value());
		this.component = new BusFederate(model);
		this.model.setComponent(component);
		
	}
	public Object start(IApplicationContext context) throws Exception {
		
		BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);

        // run the simulation
        model.getSimulationControl().start();

        return EXIT_OK;
	}


	public void stop() {
		// nothing to do;
	}

}
