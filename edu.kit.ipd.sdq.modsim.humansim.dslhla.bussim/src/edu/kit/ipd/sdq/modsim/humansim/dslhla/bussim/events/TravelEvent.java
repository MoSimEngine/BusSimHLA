package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Route.RouteSegment;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Server;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;


public class TravelEvent extends AbstractSimEventDelegator<Server> {

	private double timestep = 0;
	
    public TravelEvent(ISimulationModel model, String name) {
        super(model, name);
    }

    @Override
    public void eventRoutine(Server bus) {
    	BusModel m = (BusModel)this.getModel();
        RouteSegment segment = bus.travel();
        BigDecimal actualSpeed = BigDecimal.ZERO;
        
        
//      Utils.log(bus, "Transports " + bus.getNumTransportedHumans());
      
      if(segment.getTrafficJamDanger()) {
      	BigDecimal averageSpeed =  BigDecimal.valueOf((double)segment.getAverageSpeed());
      	BigDecimal numAcceptingServers = BigDecimal.valueOf((double) segment.getFrom().getNumberOfAcceptingServers());
      	actualSpeed = averageSpeed.divide(numAcceptingServers, 0, BigDecimal.ROUND_CEILING); 
      } else {
      	actualSpeed = BigDecimal.valueOf((double) segment.getAverageSpeed());
      }
      
      BigDecimal distance = BigDecimal.valueOf((double)segment.getDistance());
      
      
      double drivingTime = Duration.hours(distance.divide(actualSpeed, 0, BigDecimal.ROUND_CEILING).doubleValue()).toSeconds()
              .value();
    
        segment.getFrom().serverStopsAccepting();
        timestep = drivingTime;
        ArriveEvent e = new ArriveEvent(drivingTime, this.getModel(), "Arrive Event");
        TimeAdvanceToken token = new TimeAdvanceToken(e, bus, timestep);
		m.getTimelineSynchronizer().putToken(token, false);
    }

}
