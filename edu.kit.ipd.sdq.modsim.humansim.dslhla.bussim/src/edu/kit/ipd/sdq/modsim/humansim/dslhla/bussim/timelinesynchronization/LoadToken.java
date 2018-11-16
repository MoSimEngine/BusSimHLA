package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Human;
import hla.rti1516e.exceptions.RTIexception;

public class LoadToken extends SynchroniseToken{

	BusStop position;
	Human human;
	
	public LoadToken(AbstractSimEntityDelegator entity, double timestep, BusStop position, Human human) {
		super(null, entity, SynchronisedActionTypen.RTI_ACTION, timestep, entity.getModel().getSimulationControl().getCurrentSimulationTime(), timestep);
		this.position = position;
		this.human = human;
	}

	@Override
	public void executeAction() {
		BusModel m = (BusModel) getEntity().getModel();
		
		try {
			m.getComponent().sendHumanEnterInteraction(human, position, getTimeStep());
		} catch (RTIexception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
