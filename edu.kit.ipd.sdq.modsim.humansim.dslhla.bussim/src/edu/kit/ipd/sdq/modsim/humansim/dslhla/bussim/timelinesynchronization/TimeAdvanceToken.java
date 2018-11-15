package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;

import hla.rti1516e.exceptions.RTIexception;

public class TimeAdvanceToken extends SynchroniseToken {

	public TimeAdvanceToken(AbstractSimEventDelegator returnEvent,
			AbstractSimEntityDelegator entity, 
			double timestep) {
		super(returnEvent, entity, SynchronisedActionTypen.ADVANCE_TIME, timestep, entity.getModel().getSimulationControl().getCurrentSimulationTime(), timestep);
	}

	@Override
	public void executeAction() {
			BusModel m = (BusModel)getEntity().getModel();

			double fedTime = m.getComponent().getCurrentFedTime();
			double resTime = this.getResultingTimepoint();
			double targetTime = fedTime + this.getTimeStep();
			double diff = 0; 
			
			if(fedTime >= resTime) {
				return;
			}
			
			if(targetTime > resTime) {
				diff = targetTime - resTime;
			}
			
			m.getComponent().synchronisedAdvancedTime(getTimeStep() - diff);
	}
}
