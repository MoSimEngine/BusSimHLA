package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.IEntity;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;

import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;

public class TimeAdvanceSynchronisationEvent extends AbstractSimEventDelegator{

	private AbstractSimEventDelegator event;
	private double timestep;
	
	public TimeAdvanceSynchronisationEvent(ISimulationModel model, String name, AbstractSimEventDelegator event, double timestep) {
		super(model, name);
		this.event = event;
		this.timestep = timestep;
	}

	@Override
	public void eventRoutine(IEntity entity) {
		
		AbstractSimEntityDelegator e = (AbstractSimEntityDelegator)entity;
		
		BusModel m = (BusModel)e.getModel();
		if(m.getTimelineSynchronizer().checkAndExecute()) {
			
		}
		
	}

}
