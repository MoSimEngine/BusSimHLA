package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import edu.kit.ipd.sdq.modsim.adaption.HLAByteArrayAdaption;
import edu.kit.ipd.sdq.modsim.adaption.ByteArrayToInteger32BEConversion;
import edu.kit.ipd.sdq.modsim.adaption.ByteArrayToStringConversion;
import edu.kit.ipd.sdq.modsim.adaption.HLAAdapter;
import edu.kit.ipd.sdq.modsim.adaption.DataMarker;
import edu.kit.ipd.sdq.modsim.adaption.DataMarkerMapping;
import edu.kit.ipd.sdq.modsim.adaption.HLAByteArrayDerivedElement;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import hla.rti1516e.time.HLAinteger64Time;

public class BusFederate {

	
	private RTIambassador rtiamb;
	private BusFederateAmbassador fedamb; // created when we connect
	private HLAfloat64TimeFactory timeFactory; // set when we join
	protected EncoderFactory encoderFactory; // set when we join
	
	protected InteractionClassHandle registerAtBusStopHandle;
	protected ParameterHandle humanNameRegisterHandle;
	protected ParameterHandle busStopNameRegisterHandle;
	protected ParameterHandle destinationNameRegisterHandle;
	
	protected InteractionClassHandle unregisterAtBusStopHandle;
	protected ParameterHandle humanNameUnregisterHandle;
	protected ParameterHandle busStopNameUnregisterHandle;
	
	protected InteractionClassHandle humanEntersBusHandle;
	protected ParameterHandle humanNameEnterBusHandle;
	protected ParameterHandle busStopNameEnterHandle;

	protected InteractionClassHandle humanExitsBusHandle;
	protected ParameterHandle humanNameExitBusHandle;
	protected ParameterHandle busStopNameExitHandle;
	protected ParameterHandle humanExitsPassedTimeHandle;
	
	protected ObjectClassHandle humanObjectClassHandle;
	protected AttributeHandle collectedHandle;
	protected AttributeHandle humanNameAttributeHandle;
	protected AttributeHandle destinationHandle;
	
	protected ObjectClassHandle busStopObjectClassHandle;
	protected AttributeHandle busStopNameAttributeHandle;
	
	protected ObjectInstanceHandle busObjectHandles;

	public int readyCounter = 0;
	
	

	private final String fedInfoStr =  "!BusSim Federate-Info!: ";
	private String federateName;
	
	private boolean regulateTime = true;
	private boolean constrainTime = false;
	
	double startTime;
	private BusModel simulation;

	
	public HLAAdapter adapterService;
	 
	public BusFederate(BusModel simulation){
		this.simulation = simulation;
	}
	
	public void runFederate(String fedName) throws Exception{
		
		
		this.federateName = fedName;
		log(fedInfoStr + "Creating RTIambassador");
		rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

		log(fedInfoStr + "Connecting");
		fedamb = new BusFederateAmbassador(this);
		if(HumanSimValues.EVOKE){
			rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
			} else {
				rtiamb.connect(fedamb, CallbackModel.HLA_IMMEDIATE);
			}
		log(fedInfoStr + "Creating Federation");
		
		try {			
			URL[] modules = new URL[] { (new File("FOMS/HumanSimFOM.xml")).toURI().toURL() };

			rtiamb.createFederationExecution("HumanSim1", modules);
			log(fedInfoStr + "Created Federation");
		} catch (FederationExecutionAlreadyExists exists) {
			log(fedInfoStr + "Didn't create federation, it already existed");
		} catch (MalformedURLException urle) {
			log(fedInfoStr + "Exception loading one of the FOM modules from disk: " + urle.getMessage());
			urle.printStackTrace();
			return;
		}
		
		URL[] joinModules = new URL[] { (new File("FOMS/HumanSimFOM.xml")).toURI().toURL() };
		rtiamb.joinFederationExecution(federateName, "HumanSim1", "HumanSim1", joinModules);
		log(fedInfoStr + "Joined fedration as " + federateName);
		

		this.timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();
		rtiamb.registerFederationSynchronizationPoint(HumanSimValues.READY_TO_RUN, null);
		
		
		while (fedamb.isAnnounced == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}

		setUpAdaptation();
		
		waitForUser();
		rtiamb.synchronizationPointAchieved(HumanSimValues.READY_TO_RUN);
		log(fedInfoStr + "Achieved sync point: " + HumanSimValues.READY_TO_RUN + ", waiting for federation...");

		//Define if federation time is constrained by others or regulates time for others
		constrainTime = true;
		regulateTime = true;
		runTimePolicyEnabling();
		
		while (fedamb.isReadyToRun == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}
		
		//Publish and Subscribe all Data
		publishAndSubscribe();
		
		//Initialise BusStops
        initialiseBusStops();
	
      
       	

        
		while(simulation.getHumans().size() != HumanSimValues.NUM_HUMANS){
			if(HumanSimValues.EVOKE){
				advanceTime(1.0);
				rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			} else {
			System.out.print("");
			}
		}
		
		System.out.println("");
		
		
		for (Human h : simulation.getHumans()) {
			log("Human: " + h.getName());
		}
		System.out.println("Total # Humans:" + simulation.getHumans().size());
		

		for (Human hu : simulation.getHumans()) {
			sendCollectedAcquireRequest(hu);
		}
		
		simulation.startSimulation();
	}

	
	public void cleanFederate() throws Exception{
		rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
		Utils.cmdLog(simulation.getBus(), "Resigned from Federationn");
//		
//		try{
//			rtiamb.destroyFederationExecution("HumanSim1");
//			Utils.log(simulation.getBus(), "Destroyed HumanSim federation");
//		} catch (FederationExecutionDoesNotExist fedne){
//			Utils.log(simulation.getBus(), " Federation does not exist");
//		} catch (FederatesCurrentlyJoined fcj) {
//			Utils.log(simulation.getBus(), "Federates still joined at HumanSim");
//		}
	}
	private void enableTimePolicy() throws Exception {
		
		if(regulateTime){
		HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);
		this.rtiamb.enableTimeRegulation(lookahead);

		while (fedamb.isRegulating == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			}
		//log(fedInfoStr + "activated time regulation");
		}
		
		if(constrainTime){
		this.rtiamb.enableTimeConstrained();
			
		while (fedamb.isConstrained == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			}
		//log(fedInfoStr + "activated time contrained");
		}
		
		log(fedInfoStr + "Time Policy Enabled");
	}
	
	private void runTimePolicyEnabling() throws Exception{
		
		if(regulateTime && constrainTime){
			while(!fedamb.isConstrained && !fedamb.isRegulating){
				try{
					enableTimePolicy();
					} catch (Exception e){
						log(e.getMessage());
					}
				}
		} else if (regulateTime){
			while(!fedamb.isRegulating){
				try{
					enableTimePolicy();
					} catch (Exception e){
						log(e.getMessage());
					}
				}
		} else if (constrainTime){
			while(!fedamb.isConstrained){
				try{
					enableTimePolicy();
					} catch (Exception e){
						log(e.getMessage());
					}
				}
		} else {
			log("No time policy to enable");
		}
		
		
	}

	
	private void publishAndSubscribe() throws RTIexception {
		registerAtBusStopHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanRegistersAtBusStop");
		rtiamb.subscribeInteractionClass(registerAtBusStopHandle);
		humanNameRegisterHandle = rtiamb.getParameterHandle(registerAtBusStopHandle, "HumanName");
		busStopNameRegisterHandle = rtiamb.getParameterHandle(registerAtBusStopHandle, "BusStopName");
		destinationNameRegisterHandle = rtiamb.getParameterHandle(registerAtBusStopHandle, "DestinationName");
		
		unregisterAtBusStopHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanUnRegistersAtBusStop");
		rtiamb.subscribeInteractionClass(unregisterAtBusStopHandle);
		humanNameUnregisterHandle = rtiamb.getParameterHandle(unregisterAtBusStopHandle, "HumanName");
		busStopNameUnregisterHandle = rtiamb.getParameterHandle(unregisterAtBusStopHandle, "BusStopName");

		humanEntersBusHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanEntersBus");
		rtiamb.publishInteractionClass(humanEntersBusHandle);
		humanNameEnterBusHandle = rtiamb.getParameterHandle(humanEntersBusHandle, "HumanName");
		busStopNameEnterHandle = rtiamb.getParameterHandle(humanEntersBusHandle, "BusStopName");
		
		humanExitsBusHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanExitsBus");
		rtiamb.publishInteractionClass(humanExitsBusHandle);
		humanNameExitBusHandle = rtiamb.getParameterHandle(humanExitsBusHandle, "HumanName");
		busStopNameExitHandle = rtiamb.getParameterHandle(humanExitsBusHandle, "BusStopName");
		humanExitsPassedTimeHandle = rtiamb.getParameterHandle(humanExitsBusHandle, "PassedTime");
		
		humanObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Human");
		humanNameAttributeHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "HumanName");
		collectedHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "HumanCollected");
		destinationHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "Destination");
		
		AttributeHandleSet humanSubscribeAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanSubscribeAttributes.add(humanNameAttributeHandle);
		humanSubscribeAttributes.add(destinationHandle);
		rtiamb.subscribeObjectClassAttributes(humanObjectClassHandle, humanSubscribeAttributes);
	
		AttributeHandleSet humanPublishAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanPublishAttributes.add(collectedHandle);
		rtiamb.publishObjectClassAttributes(humanObjectClassHandle, humanPublishAttributes);
		
		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");
		
		AttributeHandleSet busStopAttributes = rtiamb.getAttributeHandleSetFactory().create();
		busStopAttributes.add(busStopNameAttributeHandle);
		rtiamb.publishObjectClassAttributes(busStopObjectClassHandle, busStopAttributes);
		
		log(fedInfoStr + "Published and Subscribed");
	}
	
	private ObjectInstanceHandle registerBusStopObject() throws RTIexception{
		return rtiamb.registerObjectInstance(busStopObjectClassHandle);
	}
	
	private byte[] generateTag() {
		return ("(timestamp) " + System.currentTimeMillis()).getBytes();
	}
	
	private void initialiseBusStops() throws Exception{
		
		for(int i = 0; i < simulation.getStops().length; i++){
			
			
			BusStop tmp = simulation.getStops()[i];
			
			ObjectInstanceHandle oih = registerBusStopObject();
			
			
			simulation.getStops()[i].setOih(oih);
			simulation.getStops()[i].setOch(busStopObjectClassHandle);
			
			AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
			HLAASCIIstring busStopName = encoderFactory.createHLAASCIIstring(tmp.getName());
			attributes.put(busStopNameAttributeHandle, busStopName.toByteArray());
			HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 1.0);
			rtiamb.updateAttributeValues(tmp.getOih(), attributes, generateTag(), time);

		}
	}
	

	/**
	 * This method will request a time advance to the current time, plus the given
	 * timestep. It will then wait until a notification of the time advance grant
	 * has been received.
	 */
	public synchronized boolean advanceTime( double timestep ) throws RTIexception
	{
		double advancingTo = 0;
		double miniStep = 0.000000001;
		if(fedamb.federateTime + timestep <= HumanSimValues.MAX_SIM_TIME.toSeconds().value()){
			advancingTo = fedamb.federateTime + timestep;
		} else {
			advancingTo =  HumanSimValues.MAX_SIM_TIME.toSeconds().value() + miniStep;
			return false;
		}
		
		// request the advance
		fedamb.isAdvancing = true;
		HLAfloat64Time time = timeFactory.makeTime( advancingTo );
		
		if(HumanSimValues.MESSAGE){
			try{
				rtiamb.nextMessageRequest( time );
			} catch (Exception e){
				log(e.getMessage());
				return false;
			}
		} else {
			try{
				rtiamb.timeAdvanceRequest( time );
				} catch (Exception e){
					log(e.getMessage());
					return false;
				}
		}
		
		
		
		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while( fedamb.isAdvancing )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}

		return true;
	}
	
	public synchronized void synchronisedAdvancedTime(double timestep, AbstractSimEventDelegator simevent, AbstractSimEntityDelegator simentity ){

		if(timestep != 0.0){
			try {
				if(!advanceTime(timestep)){
							
					System.out.println("Not Advancing Time");
					simulation.getSimulationControl().stop();
					return;
				}
			} catch (RTIexception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		simevent.schedule(simentity, timestep);
	}
	
	public void sendHumanEnterInteraction(Human human, BusStop busStop, double loadingTime) throws RTIexception{
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		HLAASCIIstring humanName = encoderFactory.createHLAASCIIstring(human.getName());
		HLAASCIIstring busStopName = encoderFactory.createHLAASCIIstring(busStop.getName());
		parameters.put(humanNameEnterBusHandle, humanName.toByteArray());
		parameters.put(busStopNameEnterHandle, busStopName.toByteArray());
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + loadingTime);
		
		
		rtiamb.sendInteraction( humanEntersBusHandle, parameters, generateTag(), time);
		Utils.log(simulation.getBus(), "Send Enters Interaction for Human"+ human.getName() + " at busstop " + busStop.getName() + " with handle:" + humanEntersBusHandle);
	}
	

	public void sendHumanExitsInteraction(Human human, BusStop busStop, double unloadingTime) throws RTIexception{
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		HLAASCIIstring humanName = encoderFactory.createHLAASCIIstring(human.getName());
		HLAASCIIstring busStopName = encoderFactory.createHLAASCIIstring(busStop.getName());
		parameters.put(humanNameExitBusHandle, humanName.toByteArray());
		parameters.put(busStopNameExitHandle, busStopName.toByteArray());
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + unloadingTime);
		
		rtiamb.sendInteraction( humanExitsBusHandle, parameters, generateTag(), time);
		Utils.log(simulation.getBus(), "Send Exit Interaction for Human"+ human.getName() + " at busstop " + busStop.getName() + " with handle:" + humanExitsBusHandle);
	}
	
	
	public void modifyHumanCollected(Human human, boolean collected, double additionalTime) throws RTIexception{
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
		
		
		int boolValue;
		
		if(collected)
			boolValue = 101;
		else 
			boolValue = 102;
		
		HLAinteger32BE collectedValue = encoderFactory.createHLAinteger32BE(boolValue);
		attributes.put(collectedHandle, collectedValue.toByteArray());
		
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + additionalTime);


		try{
		rtiamb.updateAttributeValues(human.getOih(), attributes, generateTag(), time);
		} catch (Exception e){
			System.out.println("In Collected Exception");
		}
	}
	
	
	public void sendCollectedAcquireRequest(Human human) throws Exception{
		AttributeHandleSet handles = rtiamb.getAttributeHandleSetFactory().create();
		handles.add(collectedHandle);
		rtiamb.attributeOwnershipAcquisition(human.getOih(), handles, generateTag());
	}
	public double getCurrentFedTime(){
		return fedamb.federateTime;
	}
	
	public boolean timeOver(){
		if(fedamb.federateTime > HumanSimValues.MAX_SIM_TIME.toSeconds().value())
			return true;
		else
			return false;
	}
	
	public void log(String msg){
		 StringBuilder s = new StringBuilder();
    	 s.append(msg);
    	 System.out.println(s);
	}
	
	public void handleRegistration(String humanName, String busStop, String destination){
		simulation.registerHumanAtBusStop(humanName, busStop, destination);
	}
	
	public void handleUnregistration(String humanName, String busStop){
		simulation.unregisterHumanAtBusStop(humanName, busStop);
	}

	public void handleHumanAttributeUpdates(ObjectInstanceHandle oih, AttributeHandleValueMap attributes){
		
		boolean found = false;

		String humanName = "";
		String destination = "";
		
		for(AttributeHandle handle : attributes.keySet()){
			if(handle.equals(humanNameAttributeHandle)){
				humanName = (String)adapterService.filter(String.class.getTypeName(), attributes.get(humanNameAttributeHandle));
			} else if (handle.equals(destinationHandle)) {
				destination = (String)adapterService.filter(String.class.getTypeName(),attributes.get(destinationHandle));
			} else {
				log("Got more Attributes than expected");
			}
		}

		
		if(humanName.equals("")){
			handleHumanDestinationChange(oih, destination);
			return;
		}
		
		for (Human human : simulation.getHumans()) {
			if(human.getOih().equals(oih)){
				for(int i = 0; i < simulation.getStops().length; i++){
					if(simulation.getStops()[i].getName().equals(destination)){
						human.setDestination(simulation.getStops()[i]);
						found = true;
						if(!human.isInitialised()){
							human.isInitialised();
						}
					}
				}
			}
		} 
		
		if(!found){
		
		Human hu = new Human(simulation, humanName, humanObjectClassHandle, oih);
		
		simulation.addHuman(hu);
		
		hu.setInitialised(true);
		
		for(int i = 0; i < simulation.getStops().length; i++){
			if(simulation.getStops()[i].getName().equals(destination)){
				hu.setDestination(simulation.getStops()[i]);
			}
		}
		
		}
	
	}
	
	private void handleHumanDestinationChange(ObjectInstanceHandle oih, String destination){
		for (Human human : simulation.getHumans()) {
			if(human.getOih().equals(oih)){
				//log("Found Human to update destination");
				for(int i = 0; i < simulation.getStops().length; i++){
					if(simulation.getStops()[i].getName().equals(destination)){
						
						//log("Found BusStop to update");
						human.setDestination(simulation.getStops()[i]);
						return;
						}
					}
				}
			}
	
		} 
	
	

	public void waitForUser() {
		log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			reader.readLine();
		} catch (Exception e) {
			log("Error while waiting for user input: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void setUpAdaptation(){
		adapterService = new HLAAdapter();
		
		DataMarker byteArray = new DataMarker("byteArray");
		DataMarker stringMarker = new DataMarker("string");
		DataMarker intMarker = new DataMarker("int");
		
		DataMarkerMapping mappingByteArray = new DataMarkerMapping(byteArray, byte[].class.getTypeName());
		DataMarkerMapping mappingHLAString = new DataMarkerMapping(stringMarker, String.class.getTypeName());
		DataMarkerMapping mappingHLAInt32 = new DataMarkerMapping(intMarker, Integer.class.getTypeName());
		
		HLAByteArrayAdaption byteArrayDesription = new HLAByteArrayAdaption(mappingByteArray);
		
		HLAByteArrayDerivedElement HLAStringElement = new HLAByteArrayDerivedElement(mappingHLAString, new ByteArrayToStringConversion(encoderFactory));
		HLAByteArrayDerivedElement HLAInt32Element = new HLAByteArrayDerivedElement(mappingHLAInt32, new ByteArrayToInteger32BEConversion(encoderFactory));
		byteArrayDesription.addDerivedElement(HLAStringElement);
		byteArrayDesription.addDerivedElement(HLAInt32Element);
		
		adapterService.addDescription(byteArrayDesription);
	}

}
