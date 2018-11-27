package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import java.util.LinkedList;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Server;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Queue;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.LoadToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;

public class LoadPassengersEvent extends AbstractSimEventDelegator<Server> {

	public static final Duration LOADING_TIME_PER_PASSENGER = Duration.seconds(3);
	private double timestep = 0;

	public LoadPassengersEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Server bus) {
		BusModel m = (BusModel) this.getModel();
		Queue position = bus.getPosition();

		int waitingPassengers = position.getTokensInQueue();

		int servedPassengers = Math.min(waitingPassengers, bus.getTotalSeats());
		if (servedPassengers < waitingPassengers) {
//			Utils.log(bus, "could not serve all passengers!!!");
		}

		bus.load(servedPassengers);
		int remainingPassengers = waitingPassengers - servedPassengers;
		double totalLoadingTime = 0;
		
		LinkedList<Token> notPickedup = new LinkedList<Token>();

		for (int i = 0; i < servedPassengers; i++) {
			
			if(position.getTokensInQueue() == 0) {
				break;
			}
			
			Token h = position.getToken();

			if (bus.containsDestinationInRoute(h.getDestination())) {

				bus.transportHuman(h);
				double loadingTime = Server.LOADING_TIME_PER_PASSENGER.toSeconds().value();

				// picks up human from home busstop
				LoadToken loadToken = new LoadToken(bus, 1.0, position, h);
				m.getTimelineSynchronizer().putToken(loadToken, false);

				h.setCollected(true);
				totalLoadingTime += loadingTime;
//				Utils.log(bus, "Loading " + h.getName() + " at position" + position.getName());
			} else {
				notPickedup.add(h);
				i--;
			}
		}
		if (notPickedup.size() != 0) {
			for (int j = notPickedup.size() - 1; j >= 0; j--) {
				position.placeTokensInFront(notPickedup.get(j));
			}
		}

		timestep = totalLoadingTime;
		// schedule load finished event
		LoadFinishedEvent e = new LoadFinishedEvent(1.0, remainingPassengers, this.getModel(),
				"LoadFinished");
		TimeAdvanceToken token = new TimeAdvanceToken(e, bus, 1.0);
		m.getTimelineSynchronizer().putToken(token, false);
	}

}
