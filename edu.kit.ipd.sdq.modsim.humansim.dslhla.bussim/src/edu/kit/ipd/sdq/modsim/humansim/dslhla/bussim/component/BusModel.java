package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;


import java.util.ArrayList;
import java.util.LinkedList;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimulationModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events.LoadPassengersEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.RTITimelineSynchronizer;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.TimeAdvanceSynchronisationEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;





public class BusModel extends AbstractSimulationModel{

	 public int modelRun;
	 public LinkedList<Double> durations;
	 private RTITimelineSynchronizer timelineSynchronizer;
	 private LinkedList<Human> humans;
	 private LinkedList<Bus> buses;
	 private ArrayList<BusStop> stops;
	 private BusFederate component;
	 
	public BusModel(ISimulationConfig config, ISimEngineFactory factory) {
		super(config, factory);
		humans = new LinkedList<Human>();
		buses = new LinkedList<Bus>();
		stops = new ArrayList<BusStop>();
	}
	
	public void init() {
		
        // define bus stops
        
        int numStops = 6;
        
        for(int i = 0; i < numStops; i++) {
        	stops.add(new BusStop(this, "Stop" + i));
        }
        
        
        
        
	        // define route
        // define route
	     Route lineOne = new Route();
	        lineOne.addSegment(stops.get(0), stops.get(1), 30, 50);
	        lineOne.addSegment(stops.get(1), stops.get(0), 30, 50);
	        
	        Route lineTwo = new Route();
	        lineTwo.addSegment(stops.get(2), stops.get(3), 40, 50);
	        lineTwo.addSegment(stops.get(3), stops.get(2), 40, 50);
	        
	        Route lineThree = new Route();
	        lineThree.addSegment(stops.get(4), stops.get(5), 50, 50);
	        lineThree.addSegment(stops.get(5), stops.get(4), 50, 50);
	        
       
       //define busses
       buses.add(new Bus(1000, stops.get(0), lineOne, this, "Bus0"));
       buses.add(new Bus(1000, stops.get(1), lineOne, this, "Bus1"));
       buses.add(new Bus(1000, stops.get(2), lineTwo, this, "Bus2"));
       buses.add(new Bus(1000, stops.get(3), lineTwo, this, "Bus3"));
       buses.add(new Bus(1000, stops.get(4), lineThree, this, "Bus4"));
       buses.add(new Bus(1000, stops.get(5), lineThree, this, "Bus5"));
	        
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
     * @param config
     *            the simulation configuration
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

	public void startSimulation(){
		
		this.timelineSynchronizer = new RTITimelineSynchronizer(this);
		
		System.out.println("Start busses at " + component.getCurrentFedTime());
	
//		for (Bus bus : busses) {
//			new LoadPassengersEvent(this, "Load Passengers").schedule(bus, component.getCurrentFedTime());
//		}
//	
	
		for (int i = 0; i < buses.size(); i++) {
			double timestep = component.getCurrentFedTime();
			if(i == 3 || i == 7) {
				timestep += Duration.minutes(10).toSeconds().value();
			}
			
			TimeAdvanceToken tok = new TimeAdvanceToken(new LoadPassengersEvent(this, "Load Passengers"), buses.get(i), timestep);
			timelineSynchronizer.putToken(tok, false);
		}
		
		TimeAdvanceSynchronisationEvent e = new TimeAdvanceSynchronisationEvent(this, "AdvanceTime", null, 0.0);
		e.schedule(buses.get(0), 0);
	}

	public ArrayList<BusStop> getStops() {
		return stops;
	}
	
	public Bus getBus(String name){
		for (Bus bus : buses) {
			if(bus.getName().equals(name)) {
				return bus;
			}
		}
		
		return null;
	}
	
	public void addHuman(Human hu){
		humans.add(hu);
	}

	public LinkedList<Human> getHumans() {
		return humans;
	}
	
	public BusFederate getComponent(){
		return component;
	}
	
	public void setComponent(BusFederate component){
		this.component = component;
	}
	
	public boolean registerHumanAtBusStop(String humanName, String busStop, String destination){

		boolean foundCurrentBS = false;
		boolean setDestination = false;
		
		for (Human humanBS : getHumans()) {
			if(humanBS.getName().equals(humanName)){
//				Utils.log(humanBS, "Registering Human");
				for (BusStop bs : getStops()) {
					
					if(bs.getName().equals(busStop)){
						bs.setPassenger(humanBS);
						foundCurrentBS = true;
					}
					
					if(bs.getName().equals(destination)){
						humanBS.setDestination(bs);
						setDestination = true;
					}
					
					if(foundCurrentBS && setDestination){
						return true;
					}
				}	
			}
		}
		return false;
	}
	
	public boolean unregisterHumanAtBusStop(String humanName, String busStop){
	
		for (Human humanBS : getHumans()) {
			if(humanBS.getName().equals(humanName)){
				for (BusStop bs : getStops()) {
					if(bs.getName().equals(busStop)){
						bs.removePassenger(humanBS);
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

	public LinkedList<Bus> getBusses() {
		return buses;
	}
}
