package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Server;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;


public class UnloadingFinishedEvent extends AbstractSimEventDelegator<Server> {

    public UnloadingFinishedEvent(double unloadingTime,ISimulationModel model, String name) {
        super(model, name);
    }

    @Override
    public void eventRoutine(Server bus) {
    	BusModel m = (BusModel)this.getModel();

    	
    	if(HumanSimValues.WORKLOAD_OPEN && m.getUnloadCounter() >= HumanSimValues.NUM_HUMANS) {
			m.getSimulationControl().stop();
			return;
		}
		
        // schedule load passengers event
        LoadPassengersEvent e = new LoadPassengersEvent(this.getModel(), "Load Passengers");
        e.schedule(bus, 0);
//        m.getComponent().synchronisedAdvancedTime(timestep, e, bus);
    }

}
