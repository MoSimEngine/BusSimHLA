package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;


import java.util.LinkedList;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimulationModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.events.LoadPassengersEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.processes.BusProcess;



public class BusModel extends AbstractSimulationModel{

	 private boolean PROCESS_ORIENTED = false;
	 
	 private BusStop stop1;
	 private BusStop stop2;
	 private BusStop stop3;
	 private Bus bus;
	 private double startTime; 
	 private BusStop[] stops;
	 public int modelRun;
	 public LinkedList<Double> durations;
	 
	 private LinkedList<Human> humans;
	 
	 private BusFederate component;
	 
	public BusModel(ISimulationConfig config, ISimEngineFactory factory) {
		super(config, factory);
		humans = new LinkedList<Human>();
	}
	
	public void init() {
		
		startTime = System.nanoTime();
		 // define bus stops
        stop1 = new BusStop(this, "Stop1");
        stop2 = new BusStop(this, "Stop2");
        stop3 = new BusStop(this, "Stop3");
        // define bus stops
		 stops = new BusStop[]{stop1, stop2, stop3};
	        
	        // define route
	        Route lineOne = new Route();
	        lineOne.addSegment(stop1, stop2, 10, 35);
	        lineOne.addSegment(stop2, stop3, 20, 50);
	        lineOne.addSegment(stop3, stop1, 30, 50);

	        // define buses
	        bus = new Bus(40, stop1, lineOne, this, "Bus 1");
	   
	       
	        
	        try {
				component.runFederate("BusFed");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   
	}
	public void finalise() {
		
	
	 	
		try {
			component.cleanFederate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
		System.out.println("Closed Fed");
//		 for (Human human : this.humans) {
//			 	
//			 	//Duration[] away = (Duration[]) human.getAwayFromHomeTimes().toArray();
//			 	
//			 	ArrayList<Duration> away = human.getAwayFromHomeTimes();
//			 	ArrayList<Duration> driven = human.getDrivingTimes();
//			 	ArrayList<Duration> waited = human.getBusWaitingTimes();
//			 	
//			 	
//			 	for (int i = 0; i < away.size(); i++){
//			 		
//			 		
//			 		System.out.println("Day " + i + ": Away From Home (Hours)" + Math.round(away.get(i).toHours().value()*100.00) /100.00 
//			 				+ "; Driven (Minutes): " + Math.round(driven.get(i).toMinutes().value() * 100.00)/100.00 
//			 				+ "; Waiting at Stations (Minutes): " + waited.get(i).toMinutes().value());
//				}
//			 	
//			 	
//		        System.out.println("-----------------------------");
//		}
	
	       	
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
		
		System.out.println("Start bus at " + component.getCurrentFedTime());
	     if (PROCESS_ORIENTED) {
	            // schedule a process for each bus
	            new BusProcess(bus).scheduleAt(component.getCurrentFedTime());
	        } else { // event-oriented
	            // schedule intitial event for the bus
	            LoadPassengersEvent e = new LoadPassengersEvent(this, "Load Passengers");
	            component.synchronisedAdvancedTime(0.0, e, bus);
	            

	            
	            
	            //new PassengerArrivalEvent(Duration.seconds(2.0), this, "BS").schedule(stop1, 0);
	        }
	}

	public BusStop[] getStops() {
		return stops;
	}
	
	public Bus getBus(){
		return bus;
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
	
	public boolean registerHumanAtBusStop(String humanName, String busStop){
		
		for (Human humanBS : getHumans()) {
			if(humanBS.getName().equals(humanName)){
				//this.log("Human Found while register");
				for(int i = 0; i < getStops().length; i++){
					if(getStops()[i].getName().equals(busStop.toString())){
						
						getStops()[i].setPassenger(humanBS);
						//log("Register Action  with HumanName:" + register.getName() + " for BusStop " + simulation.getStops()[i].getName());
						//Utils.log(simulation.getBus "Bus is in State:" + bus.getState().toString());
						return true;
					}
				}
			}
		}
		return false;
	}
}
