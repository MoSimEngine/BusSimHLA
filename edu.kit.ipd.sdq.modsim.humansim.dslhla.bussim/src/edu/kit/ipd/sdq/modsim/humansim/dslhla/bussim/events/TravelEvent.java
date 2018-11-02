package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Route.RouteSegment;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;


public class TravelEvent extends AbstractSimEventDelegator<Bus> {

    public TravelEvent(ISimulationModel model, String name) {
        super(model, name);
    }

    @Override
    public void eventRoutine(Bus bus) {
    	BusModel m = (BusModel)this.getModel();
        RouteSegment segment = bus.travel();

        Utils.log(bus, "Travelling to station " + segment.getTo(), true);
        //System.out.println("RTITime:" + m.getComponent().getCurrentFedTime());
        double drivingTime = Duration.hours(segment.getDistance() / (double) segment.getAverageSpeed()).toSeconds()
                .value();

//        System.out.println("DrivingTime" + drivingTime);
        // wait for the bus to arrive at the next station^
        ArriveEvent e = new ArriveEvent(drivingTime, this.getModel(), "Arrive Event");
        //
        
        if(HumanSimValues.FULL_SYNC) {
        	m.getComponent().synchronisedAdvancedTime(drivingTime, e, bus);
        } else {
        	e.schedule(bus, drivingTime);
        }
    }

}
