package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimulationModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Server;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Queue;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events.LoadPassengersEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.RTITimelineSynchronizer;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.TimeAdvanceSynchronisationEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;

public class BusModel extends AbstractSimulationModel {

	private RTITimelineSynchronizer timelineSynchronizer;
	private BusFederate component;

	private LinkedList<Token> tokens;
	private LinkedList<Server> servers;
	private LinkedList<Queue> queues;

	private int unloadCounter = 0;
	private int receivedEventCounter = 0;
	private int sendEventCounter = 0;
	
	public BusModel(ISimulationConfig config, ISimEngineFactory factory) {
		super(config, factory);
		tokens = new LinkedList<Token>();
		servers = new LinkedList<Server>();
		queues = new LinkedList<Queue>();
	}

	public void init() {

		// define bus stops

		int numStops = 6;

		for (int i = 0; i < numStops; i++) {
			queues.add(new Queue(this, "Queue" + i));
		}

		Route lineOne = new Route();

		lineOne.addSegment(queues.get(0), queues.get(2), 25, 50, false);
		lineOne.addSegment(queues.get(2), queues.get(1), 25, 50, HumanSimValues.TRAFFIC_JAM); // Segment to Delay in
																								// TrafficScenario
		lineOne.addSegment(queues.get(1), queues.get(0), 25, 50, false);

		
//		Route lineTwo = new Route();
//		lineTwo.addSegment(queues.get(2), queues.get(3), 50, 50);
//		lineTwo.addSegment(queues.get(3), queues.get(2), 0, 50);
//
//		Route lineThree = new Route();
//		lineThree.addSegment(queues.get(4), queues.get(5), 50, 50);
//		lineThree.addSegment(queues.get(5), queues.get(4), 0, 50);

		// define busses
//		servers.add(new Server(500, queues.get(0), lineOne, this, "Server0"));
//		servers.add(new Server(500, queues.get(0), lineOne, this, "Server1"));
//		servers.add(new Server(100, queues.get(2), lineTwo, this, "Server2"));
//		servers.add(new Server(100, queues.get(2), lineTwo, this, "Server3"));
//		servers.add(new Server(100, queues.get(4), lineThree, this, "Server4"));
//		servers.add(new Server(100, queues.get(4), lineThree, this, "Server5"));


		for (int i = 0; i < HumanSimValues.NUM_SERVERS; i++) {
			Server serv = new Server(HumanSimValues.SERVER_CAPACITY, queues.get(0), lineOne, this, "Server" + i);
			servers.add(serv);
		}
		try {
			component.runFederate("BusFed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void finalise() {
		try {
			component.endExecution();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utils.log("Ended Federation");
	}

	/**
	 * Creates the simulation model for the specified configuration.
	 * 
	 * @param config the simulation configuration
	 * @return the created simulation model
	 */
	public static BusModel create(final BusSimConfig config) {
		// load factory for the preferred simulation engine
		ISimEngineFactory factory = SimulationPreferencesHelper.getPreferredSimulationEngine();
		if (factory == null) {
			throw new RuntimeException("There is no simulation engine available. Install at least one engine.");
		}

		// create and return simulation model
		final BusModel model = new BusModel(config, factory);

		return model;
	}

	public void startSimulation() {
		this.timelineSynchronizer = new RTITimelineSynchronizer(this);
		Utils.log("Start serving at RTI Time " + component.getCurrentFedTime());

		double timestep = component.getCurrentFedTime();
		for (int i = 0; i < servers.size(); i++) {

			TimeAdvanceToken tok = new TimeAdvanceToken(new LoadPassengersEvent(this, "Load Passengers"),
					servers.get(i), timestep);
			timelineSynchronizer.putToken(tok, false);
		}

	}

	public LinkedList<Queue> getStops() {
		return queues;
	}

	public Server getBus(String name) {
		for (Server bus : servers) {
			if (bus.getName().equals(name)) {
				return bus;
			}
		}

		return null;
	}

	public void addToken(Token hu) {
		tokens.add(hu);
	}

	public LinkedList<Token> getTokens() {
		return tokens;
	}

	public BusFederate getComponent() {
		return component;
	}

	public void setComponent(BusFederate component) {
		this.component = component;
	}

	public void registerHumanAtBusStop(String humanName, String busStop, String destination) {;

		Token token = null;

		for (Token availableToken : getTokens()) {
			if (availableToken.getName().equals(humanName)) {
				token = availableToken;
				break;
			}
		}

		if (token == null) {
			token = new Token(this, humanName, null);
			tokens.add(token);
		}

		for (Queue queue : getStops()) {
			if (queue.getName().equals(busStop)) {
				queue.setToken(token);
				continue;
			}

			if (queue.getName().equals(destination)) {
				token.setDestination(queue);
				continue;
			}
		}
		
		incrementReceivedEventCounter();
	}

	public boolean unregisterHumanAtBusStop(String humanName, String busStop) {

		for (Token humanBS : getTokens()) {
			if (humanBS.getName().equals(humanName)) {
				for (Queue bs : getStops()) {
					if (bs.getName().equals(busStop)) {
						bs.removeToken(humanBS);
						return true;
					}
				}
			}
		}
		return false;
	}

	public RTITimelineSynchronizer getTimelineSynchronizer() {
		return timelineSynchronizer;
	}

	public LinkedList<Server> getServers() {
		return servers;
	}
	
	public int getUnloadCounter() {
		return unloadCounter;
	}
	
	public void incrementUnloadCounterBy(int i) {
		unloadCounter += i;
	}
	
	public void incrementSendEventCounter() {
		sendEventCounter++;
	}
	
	public void incrementReceivedEventCounter() {
		receivedEventCounter++;
	}
}
