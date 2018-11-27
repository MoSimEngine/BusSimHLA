package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;


public class Token extends AbstractSimEntityDelegator {


	private Queue destination;
	
	private volatile boolean collected;

	private ObjectInstanceHandle oih;

	private ObjectClassHandle och;

	private boolean initialised;
	
	private double drivingTime = 0;
	
	
	


	public Token(ISimulationModel model, String name, ObjectClassHandle och, ObjectInstanceHandle oih) {
		super(model, name);

		this.setOih(oih);
		this.setOch(och);
		
	
	
	}
	
	public Token(ISimulationModel model, String name, ObjectClassHandle och) {
		super(model, name);
	}
	
	

	
	public Queue getDestination(){
		return this.destination;
	}
	
	public void setDestination(Queue destination){
		this.destination = destination;
	}
	


	public boolean isCollected() {
		return collected;
	}

	public void setCollected(boolean collected) {
		this.collected = collected;
	}

		public boolean isInitialised() {
			return initialised;
		}

		public void setInitialised(boolean initialised) {
			this.initialised = initialised;
		}




		public ObjectClassHandle getOch() {
			return och;
		}




		public void setOch(ObjectClassHandle och) {
			this.och = och;
		}




		public ObjectInstanceHandle getOih() {
			return oih;
		}




		public void setOih(ObjectInstanceHandle oih) {
			this.oih = oih;
		}
		
		public void driveOn(double time){
			drivingTime += time;
		}
		
		public double unload(){
			return drivingTime;
		}
		
	
}