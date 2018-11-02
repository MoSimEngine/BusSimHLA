package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import java.util.LinkedList;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;


public class UnloadPassengersEvent extends AbstractSimEventDelegator<Bus> {

    protected UnloadPassengersEvent(ISimulationModel model, String name) {
        super(model, name);
    }

    @Override
    public void eventRoutine(Bus bus) {
    	BusModel m = (BusModel)this.getModel();
        BusStop position = bus.getPosition();
        int occupiedSeats = bus.getOccupiedSeats();
        LinkedList<Human> tmpHumans = new LinkedList<Human>();
        //Utils.log(bus, "Unloading " + occupiedSeats + " passengers at station " + position + "...");
        bus.unload();

        // wait for the passengers to leave the bus
        int numTransportedHumanSize = bus.getNumTransportedHumans();
        double totalUnloadingTime = 0.0;
        double unloadingTime = Bus.UNLOADING_TIME_PER_PASSENGER.toSeconds().value();
        for(int i = 0; i < numTransportedHumanSize; i++){
        	Human h = bus.unloadHuman();
        	if(h.getDestination().equals(bus.getPosition())){
        		//Utils.log(bus, "Unloading " + h.getName() + " at position " + position.getName());

        		if(HumanSimValues.USE_SPIN_WAIT){
	        		try {
						m.getComponent().modifyHumanCollected(h, false, unloadingTime);
					} catch (RTIexception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	        		}else {
	        			try {
							m.getComponent().sendHumanExitsInteraction(h, position, unloadingTime);
						} catch (RTIexception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	        		}
	        		//getFederate().sendHumanExitsInteraction(h, position, unloadingTime);
	        		h.setCollected(false);
	        		totalUnloadingTime += unloadingTime;
	        		Utils.log(bus, "Unloading " + h.getName() + " at position " + position.getName(), true);
    			} else {
    				bus.transportHuman(h);
    			}
        	}
        
        UnloadingFinishedEvent e = new UnloadingFinishedEvent(totalUnloadingTime, this.getModel(), "Unload Finished");
//        
        
        if(HumanSimValues.FULL_SYNC) {
        	m.getComponent().synchronisedAdvancedTime(totalUnloadingTime, e, bus);
        } else {
        	m.getComponent().synchronisedAdvancedTime(totalUnloadingTime, e, bus);
        }
    
    }
}
