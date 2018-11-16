package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.LoadToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;

public class LoadPassengersEvent extends AbstractSimEventDelegator<Bus> {

	public static final Duration LOADING_TIME_PER_PASSENGER = Duration.seconds(3);
	private double timestep = 0;

	public LoadPassengersEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Bus bus) {
		BusModel m = (BusModel) this.getModel();
		BusStop position = bus.getPosition();

		int waitingPassengers = position.getPassengersInQueue();

		int servedPassengers = Math.min(waitingPassengers, bus.getTotalSeats());
		if (servedPassengers < waitingPassengers) {
			Utils.log(bus, "could not serve all passengers!!!");
		}
		
		bus.load(servedPassengers);
		int remainingPassengers = waitingPassengers - servedPassengers;
		double totalLoadingTime = 0;
		totalLoadingTime = 0;
		
		for (int i = 0; i < servedPassengers; i++) {
			Human h = position.getPassenger();
			bus.transportHuman(h);
			double loadingTime = Bus.LOADING_TIME_PER_PASSENGER.toSeconds().value();

			// picks up human from home busstop
			LoadToken loadToken = new LoadToken(bus, loadingTime, position, h);
			m.getTimelineSynchronizer().putToken(loadToken, false);

			h.setCollected(true);
			totalLoadingTime += loadingTime;
			Utils.log(bus, "Loading " + h.getName() + " at position" + position.getName());
		}

		timestep = totalLoadingTime;
		// schedule load finished event
		LoadFinishedEvent e = new LoadFinishedEvent(totalLoadingTime, remainingPassengers, this.getModel(),
				"LoadFinished");
		TimeAdvanceToken token = new TimeAdvanceToken(e, bus, timestep);
		m.getTimelineSynchronizer().putToken(token, false);
	}

}
