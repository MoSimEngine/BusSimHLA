package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class Queue extends AbstractSimEntityDelegator {


    
    private LinkedList<Token> enqueuedTokens;
    
    private int numberServers = 0;
    
    private ObjectInstanceHandle oih;
    private ObjectClassHandle och;
    
    
    public Queue(ISimulationModel model, String name) {
        super(model, name);
        enqueuedTokens = new LinkedList<Token>();
    }
    
    public synchronized void setToken(Token human){
    	enqueuedTokens.add(human);
    }
    
    public void placeTokensInFront(Token token) {
    	enqueuedTokens.addFirst(token);
    }
    
    public synchronized void removeToken(Token human){
    	enqueuedTokens.remove(human);
    }
    
    public synchronized Token getToken(){
    	return enqueuedTokens.remove();
    }
    
    public int getTokensInQueue(){
    	
    	if(enqueuedTokens == null) {
    		Utils.log(this, "Queue is Null");
    	}
    	
    	
    	if(enqueuedTokens.isEmpty()){
    		return 0;
    	} else {
    		return enqueuedTokens.size();
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
    
	public int getNumberOfAcceptingServers() {
		return numberServers;
	}


	public void serverAcceptingTokens() {
		this.numberServers++;
	}
	
	public void serverStopsAccepting() {
		this.numberServers--;
	}

    
    
    public ObjectClassHandle getOch(){
    	return och;
    }
    
    public void setOch(ObjectClassHandle och){
    	this.och = och;
    }

}
