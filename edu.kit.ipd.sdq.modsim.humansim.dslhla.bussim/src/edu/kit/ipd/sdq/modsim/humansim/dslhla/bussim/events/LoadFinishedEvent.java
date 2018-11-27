package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Server;


public class LoadFinishedEvent extends AbstractSimEventDelegator<Server> {


    public LoadFinishedEvent(double loadingTime, int remainingPassengers, ISimulationModel model, String name) {
        super(model, name);
    }

    @Override
    public void eventRoutine(Server bus) {
    	BusModel m = (BusModel)this.getModel();
        TravelEvent e = new TravelEvent(this.getModel(), "Travel");
        e.schedule(bus, 0);
    }

}
