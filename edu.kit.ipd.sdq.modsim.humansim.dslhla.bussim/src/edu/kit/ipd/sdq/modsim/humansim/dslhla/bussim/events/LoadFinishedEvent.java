package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;


public class LoadFinishedEvent extends AbstractSimEventDelegator<Bus> {

    private int remainingPassengers;

    private double loadingTime;

    public LoadFinishedEvent(double loadingTime, int remainingPassengers, ISimulationModel model, String name) {
        super(model, name);
        this.loadingTime = loadingTime;
        this.remainingPassengers = remainingPassengers;
    }

    @Override
    public void eventRoutine(Bus bus) {
    	BusModel m = (BusModel)this.getModel();
//    	if(loadingTime > 0.0){
//        Utils.log(bus, "Loading finished. Took " + loadingTime + " seconds.");
//    	}
//        if (remainingPassengers > 0) {
//            Utils.log(bus, "Bus is full. Remaining passengers at bus station: "
//                    + bus.getPosition().getPassengersInQueue());
//        }
      
       
        TravelEvent e = new TravelEvent(this.getModel(), "Travel");
//       
        if(HumanSimValues.FULL_SYNC) {
        	 m.getComponent().synchronisedAdvancedTime(0, e, bus);
        } else {
        	 e.schedule(bus, 0);
        }
       
    }

}
