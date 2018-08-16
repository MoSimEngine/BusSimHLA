package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;


public class Human extends AbstractSimEntityDelegator {


	private BusStop destination;
	
	private volatile boolean collected;

	private ObjectInstanceHandle oih;

	private ObjectClassHandle och;

	private boolean initialised;

	
	


	public Human(ISimulationModel model, String name, ObjectClassHandle och, ObjectInstanceHandle oih) {
		super(model, name);

		this.setOih(oih);
		this.setOch(och);
		
	
	}
	
	public Human(ISimulationModel model, String name, ObjectClassHandle och) {
		super(model, name);
	}
	
	

	
	public BusStop getDestination(){
		return this.destination;
	}
	
	public void setDestination(BusStop destination){
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
}