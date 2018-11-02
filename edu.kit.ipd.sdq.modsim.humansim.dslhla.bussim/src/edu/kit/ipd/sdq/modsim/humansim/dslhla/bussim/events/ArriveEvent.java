package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;


import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;



public class ArriveEvent extends AbstractSimEventDelegator<Bus> {

    private double travelingTime;

    public ArriveEvent(double travelingTime, ISimulationModel model, String name) {
        super(model, name);
        this.travelingTime = travelingTime;
    }

    @Override
    public void eventRoutine(Bus bus) {
        // arrive at the target station
    	BusModel m = (BusModel)this.getModel();
        BusStop currentStation = bus.arrive();
        
        Utils.log(bus, "Arrived at station " + currentStation);
        // schedule unloading event
        UnloadPassengersEvent e = new UnloadPassengersEvent(this.getModel(), "Unload Passengers");
        //
        if(HumanSimValues.FULL_SYNC) {
        	m.getComponent().synchronisedAdvancedTime(0, e, bus);
        } else {
        	e.schedule(bus, 0);
        }
        
    }

}
