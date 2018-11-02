package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;


public class LoadPassengersEvent extends AbstractSimEventDelegator<Bus> {

    public static final Duration LOADING_TIME_PER_PASSENGER = Duration.seconds(3);

    public LoadPassengersEvent(ISimulationModel model, String name) {
        super(model, name);
    }

    @Override
    public void eventRoutine(Bus bus) {
    	BusModel m = (BusModel)this.getModel();
        BusStop position = bus.getPosition();
//        System.out.println(position.getName());
        int waitingPassengers = position.getPassengersInQueue();

        int servedPassengers = Math.min(waitingPassengers, bus.getTotalSeats());
        if(servedPassengers < waitingPassengers){
        	Utils.log(bus, "could not serve all passengers!!!");
        }
        //Utils.log(bus, "Loading " + servedPassengers + " passengers at bus stop " + position + "...");
        bus.load(servedPassengers);

        int remainingPassengers = waitingPassengers - servedPassengers;
        
        double totalLoadingTime = 0;
        totalLoadingTime = 0;
        for (int i = 0; i < servedPassengers; i++){
        	//System.out.println("Found Passenger");
        	Human h = position.getPassenger();
        	bus.transportHuman(h);
//           	HumanPickupEvent e = new HumanPickupEvent(this.getModel(), "Human Pickup", bus);
//           	e.schedule(h, Bus.LOADING_TIME_PER_PASSENGER.toSeconds().value());
        	double loadingTime = Bus.LOADING_TIME_PER_PASSENGER.toSeconds().value();
        	//picks up human from home busstop
        	if(HumanSimValues.USE_SPIN_WAIT){
        		try {
    				m.getComponent().modifyHumanCollected(h, true, loadingTime);
    			} catch (RTIexception e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			}
        	} else {
        		try {
        			//System.out.println();
					m.getComponent().sendHumanEnterInteraction
					(h, position, loadingTime);
				} catch (RTIexception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}
        	
        	h.setCollected(true);
        	
        	totalLoadingTime += loadingTime;
        	Utils.log(bus, "Loading " + h.getName() + " at position + " + position.getName());
        }
        //position.setWaitingPassengers(remainingPassengers);

        // wait until all passengers have entered the bus
        //double loadingTime = servedPassengers * LOADING_TIME_PER_PASSENGER.toSeconds().value();

        // schedule load finished event
        LoadFinishedEvent e = new LoadFinishedEvent(totalLoadingTime, remainingPassengers, this.getModel(), "LoadFinished");
        //
       
        if(HumanSimValues.FULL_SYNC) {
        	 m.getComponent().synchronisedAdvancedTime(totalLoadingTime, e, bus);
        } else {
        	e.schedule(bus, totalLoadingTime);
        }
        
    }

}
