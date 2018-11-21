package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class BusStop extends AbstractSimEntityDelegator {


    
    private LinkedList<Human> passengers;
    
    private ObjectInstanceHandle oih;
    private ObjectClassHandle och;
    
    
    public BusStop(ISimulationModel model, String name) {
        super(model, name);
        
       passengers = new LinkedList<Human>();
   
    }
    
    public synchronized void setPassenger(Human human){
    	passengers.add(human);
    }
    
    public void placePassengerInFront(Human human) {
    	passengers.addFirst(human);
    }
    
    public synchronized void removePassenger(Human human){
    	passengers.remove(human);
    }
    
    public synchronized Human getPassenger(){
    	return passengers.remove();
    }
    
    public int getPassengersInQueue(){
    	if(passengers.isEmpty()){
    		return 0;
    	} else {
    		return passengers.size();
    	}	
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    public ObjectInstanceHandle getOih(){
    	return oih;
    }
    
    public void setOih(ObjectInstanceHandle oih){
    	this.oih = oih;
    }
    
    
    public ObjectClassHandle getOch(){
    	return och;
    }
    
    public void setOch(ObjectClassHandle och){
    	this.och = och;
    }

}
