package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;


import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Server;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Queue;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;



public class ArriveEvent extends AbstractSimEventDelegator<Server> {

    public ArriveEvent(double travelingTime, ISimulationModel model, String name) {
        super(model, name);
    }

    @Override
    public void eventRoutine(Server bus) {
    	Queue currentStation = bus.arrive();
//        Utils.log(bus, "Arrived at station " + currentStation);
        UnloadPassengersEvent e = new UnloadPassengersEvent(this.getModel(), "Unload Passengers");
        e.schedule(bus,0);
    }
}
