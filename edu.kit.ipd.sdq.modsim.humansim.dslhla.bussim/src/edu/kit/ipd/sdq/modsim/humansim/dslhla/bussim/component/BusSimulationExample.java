package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;

public class BusSimulationExample implements IApplication {

	private BusSimConfig config;
	private BusModel model;
	private ISimulationControl simControl;
	private BusFederate component;
	
	
	public static Duration MAX_SIMULATION_TIME = HumanSimValues.MAX_SIM_TIME;
	
	
	public BusSimulationExample() {
		this.config = new BusSimConfig();
		this.model = BusModel.create(config);
		this.simControl = model.getSimulationControl();
		if(HumanSimValues.WORKLOAD_OPEN) {
			MAX_SIMULATION_TIME = Duration.hours(Double.MAX_VALUE);
			Utils.log("Setting Time to DoubleValue.max");
		}
		this.simControl.setMaxSimTime((long) MAX_SIMULATION_TIME.toSeconds().value());
		this.component = new BusFederate(model);
		this.model.setComponent(component);
		
	}
	public Object start(IApplicationContext context) throws Exception {
		
		BasicConfigurator.configure();
		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
		loggers.add(LogManager.getRootLogger());
		for ( Logger logger : loggers ) {
		    logger.setLevel(Level.OFF);
		}
        // run the simulation
        model.getSimulationControl().start();

        return EXIT_OK;
	}


	public void stop() {
		// nothing to do;
	}

}
