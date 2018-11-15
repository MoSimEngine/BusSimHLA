package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Route;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.Route.RouteSegment;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.SynchroniseToken;


/**
 * This entity represents a bus which transports passengers between different bus stations.
 * 
 * @author J�rg Hen�, Philipp Merkle
 * 
 */
public class Bus extends AbstractSimEntityDelegator {

    private Route route;

    public enum BusState {
        LOADING_PASSENGERS, TRAVELLING, ARRIVED, UNLOADING_PASSENGERS;
    }

    private int totalSeats;

    private int occupiedSeats;

    private BusState state;

    private BusStop position;

    private BusStop destination;

    public static final Duration UNLOADING_TIME_PER_PASSENGER = Duration.seconds(5);

    public static final Duration LOADING_TIME_PER_PASSENGER = Duration.seconds(6);
    private ConcurrentLinkedQueue<Human> transportedHumans;
	private SynchroniseToken currentTAToken;
	private int taTokenIndex = -1;
	
	private LinkedList<SynchroniseToken> regTokens;
    public Bus(int totalSeats, BusStop initialPosition, Route route, ISimulationModel model, String name) {
        super(model, name);
        this.totalSeats = totalSeats;
        this.route = route;

        // start in unloading state
        this.position = initialPosition;
        this.state = BusState.UNLOADING_PASSENGERS;
        transportedHumans = new ConcurrentLinkedQueue<Human>();
    }

    public BusStop arrive() {
        if (isTravelling()) {
            this.state = BusState.ARRIVED;
            this.position = this.destination;
            this.destination = null;
        } else {
            throw new IllegalStateException("Can not arrive without being in TRAVELLING state.");
        }

        return this.position;
    }

    public void load(int numberOfPassengers) {
        if (!isTravelling()) {
            this.occupiedSeats = numberOfPassengers;
            this.state = BusState.LOADING_PASSENGERS;
        } else {
            throw new IllegalStateException("Can not load passengers while TRAVELLING.");
        }
    }

    public void unload() {
        if (!isTravelling()) {
            this.state = BusState.UNLOADING_PASSENGERS;
        } else {
            throw new IllegalStateException("Unloading passengers while TRAVELLING seems not very wise.");
        }
    }

    public RouteSegment travel() {
        RouteSegment s = route.getRouteSegment(this.position);
        


        this.state = BusState.TRAVELLING;
        this.destination = route.getRouteSegment(this.position).getTo();
        this.position = null;

        return s;
    }

    public boolean isTravelling() {
        return state.equals(BusState.TRAVELLING);
    }

    public BusStop getPosition() {
        return this.position;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getOccupiedSeats() {
        return occupiedSeats;
    }

	public void transportHuman(Human human){
		if(!transportedHumans.contains(human))
			transportedHumans.add(human);
		else 
			throw new IllegalStateException("Human is already in Bus");
	}
	
	public ConcurrentLinkedQueue<Human> getTransportedHumans(){
		return transportedHumans;
	}
	
	public int getNumTransportedHumans(){
		if(transportedHumans.isEmpty()){
			return 0;
		} else {
			return transportedHumans.size();
		}
		
	}
	
	
	
	public Human unloadHuman(){
		return transportedHumans.poll();
	}
	
	public SynchroniseToken getTaToken() {
		return currentTAToken;
	}

	public void setTaToken(SynchroniseToken token) {
		this.currentTAToken = token;
	}

	public LinkedList<SynchroniseToken> getRegTokens() {
		return regTokens;
	}

	public void addRegToken(SynchroniseToken regToken) {
		regTokens.add(regToken);
	}
	
	public void removeRegToken(SynchroniseToken regToken) {
		regTokens.remove(regToken);
	}

	public int getTaTokenIndex() {
		return taTokenIndex;
	}

	public void setTaTokenIndex(int taTokenIndex) {
		this.taTokenIndex = taTokenIndex;
	}

}
