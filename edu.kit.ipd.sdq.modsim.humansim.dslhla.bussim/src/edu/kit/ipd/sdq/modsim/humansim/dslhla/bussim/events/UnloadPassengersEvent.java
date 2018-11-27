package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import java.util.LinkedList;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Server;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Queue;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.UnloadToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;

public class UnloadPassengersEvent extends AbstractSimEventDelegator<Server> {
	private double timestep = 0;

	protected UnloadPassengersEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Server bus) {
		BusModel m = (BusModel) this.getModel();
		Queue position = bus.getPosition();
		bus.unload();

		// wait for the passengers to leave the bus
		int numTransportedHumanSize = bus.getNumTransportedHumans();
		double totalUnloadingTime = 0.0;
		double unloadingTime = Server.UNLOADING_TIME_PER_PASSENGER.toSeconds().value();
		int unloadCounter = 0;
		for (int i = 0; i < numTransportedHumanSize; i++) {
			Token h = bus.unloadHuman();
			if (h.getDestination().equals(bus.getPosition())) {

				UnloadToken unloadToken = new UnloadToken(bus, 1.0, position, h);
				m.getTimelineSynchronizer().putToken(unloadToken, false);
				
				h.setCollected(false);
				totalUnloadingTime += unloadingTime;
				unloadCounter++;
//				Utils.log(bus, "Unloading " + h.getName() + " at position " + position.getName(), true);
			} else {
				bus.transportHuman(h);
			}
		}
		
		m.incrementUnloadCounterBy(unloadCounter);

		timestep = totalUnloadingTime;
		UnloadingFinishedEvent e = new UnloadingFinishedEvent(totalUnloadingTime, this.getModel(), "Unload Finished");
		TimeAdvanceToken token = new TimeAdvanceToken(e, bus, 1.0);
		m.getTimelineSynchronizer().putToken(token, false);
		

	}
}
