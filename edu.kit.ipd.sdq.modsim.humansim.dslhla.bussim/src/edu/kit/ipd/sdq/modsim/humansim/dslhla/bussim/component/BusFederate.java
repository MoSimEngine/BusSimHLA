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

public class BusFederate {

	
	private RTIambassador rtiamb;
	private BusFederateAmbassador fedamb; // created when we connect
	private HLAfloat64TimeFactory timeFactory; // set when we join
	protected EncoderFactory encoderFactory; // set when we join
	
	protected InteractionClassHandle registerAtBusStopHandle;
	protected InteractionClassHandle humanEntersBusHandle;
	protected InteractionClassHandle humanExitsBusHandle;
	protected InteractionClassHandle humanReadyHandle;
	protected InteractionClassHandle busSimReadyHandle;
	protected InteractionClassHandle busStopReadyHandle;
	
	protected ParameterHandle humanNameExitBusHandle;
	protected ParameterHandle humanNameEnterBusHandle;
	protected ParameterHandle humanNameRegisterHandle;
	protected ParameterHandle busStopNameRegisterHandle;
	protected ParameterHandle busStopNameExitHandle;
	protected ParameterHandle busStopNameEnterHandle;
	
	protected ObjectClassHandle humanObjectClassHandle;
	protected AttributeHandle collectedHandle;
	protected AttributeHandle humanNameAttributeHandle;
	protected AttributeHandle destinationHandle;
	
	protected ObjectClassHandle busStopObjectClassHandle;
	protected AttributeHandle busStopNameAttributeHandle;
	
	
	protected ObjectInstanceHandle busObjectHandles;

	
	
	protected LinkedList<RegisterCall> regCalls = new LinkedList<RegisterCall>();
	protected LinkedList<DestinationChangeCall> destCalls = new LinkedList<DestinationChangeCall>();
	
	public int readyCounter = 0;
	
	

	private final String fedInfoStr =  "!BusSim Federate-Info!: ";
	private String federateName;
	
	private boolean regulateTime = true;
	private boolean constrainTime = false;
	
	double startTime;
	private int initialisedHumans = 0;
	private BusModel simulation;
	private LinkedList<ObjectInstanceHandle> humanHandles;
	public HLAAdapter adapterService;
	 
	public BusFederate(BusModel simulation){
		this.simulation = simulation;
		this.humanHandles = new LinkedList<ObjectInstanceHandle>();
	}
	
	public void runFederate(String fedName) throws Exception{
		
		
		this.federateName = fedName;
		log(fedInfoStr + "Creating RTIambassador");
		rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

		log(fedInfoStr + "Connecting");
		fedamb = new BusFederateAmbassador(this);
		rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
		
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
		//rtiamb.registerFederationSynchronizationPoint(HumanSimValues.READY_TO_START_SIM, null);
		
		while (fedamb.isAnnounced == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}

		
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
		
		
		
		//waitForUser();
		waitForUser();
		rtiamb.synchronizationPointAchieved(HumanSimValues.READY_TO_RUN);
		log(fedInfoStr + "Achieved sync point: " + HumanSimValues.READY_TO_RUN + ", waiting for federation...");
	
		log(fedInfoStr + "Before Time Policy Enable");
		
		constrainTime = true;
		regulateTime = true;
		runTimePolicyEnabling();
		

		while (fedamb.isReadyToRun == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}
		log(fedInfoStr + "Time Policy Enabled");
		//waitForUser();
		publishAndSubscribe();
		
		log(fedInfoStr + "Published and Subscribed");
	
		//rtiamb.synchronizationPointAchieved(HumanSimValues.READY_TO_INIT);
		//log(fedInfoStr + "Achieved sync point: " + HumanSimValues.READY_TO_INIT + ", waiting for rest of federation...");
		
		//Initialise BusStops
        initialiseBusStops();
		//Utils.log(simulation.getBus "Initialised Bus in Sim");
		
		//Utils.log(simulation.getBusfedInfoStr + "Starting to execute Bus");
		
		
		while(initialisedHumans != HumanSimValues.NUM_HUMANS){
			advanceTime(1.0);
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);

		}
		
		
		for (Human h : simulation.getHumans()) {
			log("Human: " + h.getName());
		}
		log("----------" + simulation.getHumans().size() + "------------");
		

		for (Human hu : simulation.getHumans()) {
			//Utils.log(simulation.getBus "Human in Sim: " + hu.getName());
			sendCollectedAcquireRequest(hu);
		}
		simulation.startSimulation();
		//
		//Start Simulation
		//
		
		//Wait 5 time stepts to get humans;
//		for(int i = 0; i < 5; i++){
//			advanceTime(1);
//		}
//		humans = new LinkedList<HumanBS>(); 
//		if(humans.size() == HumanSimValues.NUM_HUMANS){
//			Utils.log(simulation.getBus fedInfoStr + " All ready and good to go");
//			new BusProcess(bus).lifeCycle();
//		} else {
//			for(int i = 0; i < 5; i++){
//				advanceTime(1);
//			}
//		}
		//Wait to start busSim
		
		//advanceTime(1);
		
		
		
		
		
		//rtiamb.synchronizationPointAchieved(HumanSimValues.READY_TO_START_SIM);
		//advanceTime(1);
		
		//new BusProcess(bus).lifeCycle();
	
	}

	
	public void cleanFederate() throws Exception{
		rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
		Utils.log(simulation.getBus(), "Resigned from Federationn");
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
		
		humanObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Human");
		humanNameAttributeHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "HumanName");
		collectedHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "HumanCollected");
		destinationHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "Destination");
		
		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");
		
		
		AttributeHandleSet humanSubscribeAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanSubscribeAttributes.add(humanNameAttributeHandle);

		humanSubscribeAttributes.add(destinationHandle);
	
		
		AttributeHandleSet humanPublishAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanPublishAttributes.add(collectedHandle);
		
		rtiamb.publishObjectClassAttributes(humanObjectClassHandle, humanPublishAttributes);
		rtiamb.subscribeObjectClassAttributes(humanObjectClassHandle, humanSubscribeAttributes);
	
		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");
		
		AttributeHandleSet busStopAttributes = rtiamb.getAttributeHandleSetFactory().create();
		busStopAttributes.add(busStopNameAttributeHandle);
		rtiamb.publishObjectClassAttributes(busStopObjectClassHandle, busStopAttributes);
		
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
			HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + 1.0);
			rtiamb.updateAttributeValues(tmp.getOih(), attributes, generateTag(), time);
			Utils.log(simulation.getBus(), fedInfoStr + "Registered BusStop ObjectInstance with name :"+ tmp.getName() + " got handle" + tmp.getOih().toString() );
			
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
//			System.out.println("FedTime: " + fedamb.federateTime);
//			System.out.println("Timestelp: " + timestep);
			advancingTo = fedamb.federateTime + timestep;
//			System.out.println("AdvancingTo: " + advancingTo);
		} else {
			Utils.log(simulation.getBus(), "Sim overtime - wants to advance to: " + fedamb.federateTime + timestep + " current time: " + fedamb.federateTime);
			advancingTo =  HumanSimValues.MAX_SIM_TIME.toSeconds().value() + miniStep;
			return false;
			
		}
		
		// request the advance
		fedamb.isAdvancing = true;
		HLAfloat64Time time = timeFactory.makeTime( advancingTo );
		boolean success = false;
		
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
//		System.out.println("AbstractSimEngine Time:" + simulation.getSimulationControl().getCurrentSimulationTime());
//		System.out.println("Federate Time:" + fedamb.federateTime);
//		System.out.println("TimeStep:" + timestep);
		

//		try {
//			advanceTime(timestep);
//		} catch (RTIexception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(timestep != 0.0){
//			while(simEngineNextTime > getCurrentFedTime()){
//				realTimeStep = simEngineNextTime - getCurrentFedTime();
//				try {
//					if(!advanceTime(realTimeStep)){
//						
//						System.out.println("Not Advancing Time");
//						//simulation.getSimulationControl().stop();
//						return;
//					}
//				} catch (RTIexception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//		}
		
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
		//System.out.println("New Fed Time: " + fedamb.federateTime);
	}
	
	public void sendHumanEnterInteraction(Human human, BusStop busStop, double loadingTime) throws RTIexception{
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		HLAASCIIstring humanName = encoderFactory.createHLAASCIIstring(human.getName());
		HLAASCIIstring busStopName = encoderFactory.createHLAASCIIstring(busStop.getName());
		parameters.put(humanNameEnterBusHandle, humanName.toByteArray());
		parameters.put(busStopNameEnterHandle, busStopName.toByteArray());
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + loadingTime);
		
		//Utils.log(simulation.getBus(), "Sending Enter  Interaction for Human"+ human.getName() + " at busstop " + busStop.getName() + " with handle:" + humanExitsBusHandle);
		rtiamb.sendInteraction( humanEntersBusHandle, parameters, generateTag(), time);
	}
	

	public void sendHumanExitsInteraction(Human human, BusStop busStop, double unloadingTime) throws RTIexception{
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		HLAASCIIstring humanName = encoderFactory.createHLAASCIIstring(human.getName());
		HLAASCIIstring busStopName = encoderFactory.createHLAASCIIstring(busStop.getName());
		parameters.put(humanNameExitBusHandle, humanName.toByteArray());
		parameters.put(busStopNameExitHandle, busStopName.toByteArray());
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + unloadingTime);
		Utils.log(simulation.getBus(), "Sending Enter  Interaction for Human"+ human.getName() + " at busstop " + busStop.getName() + " with handle:" + humanExitsBusHandle);
		rtiamb.sendInteraction( humanExitsBusHandle, parameters, generateTag(), time);
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
//		Utils.log(simulation.getBus(), "Modify human collect to " + collected);
		//System.out.println("Sending Collected " + "TimeStep: " + additionalTime + "Resulting Time:" + time.getValue() + "Current time:" + fedamb.federateTime);
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
		//log("Current FedTime:" + fedamb.federateTime);
		//log("Max sim time:" + HumanSimValues.maxSimTime.toSeconds().value());
		
		
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
	
	public void handleRegistration(String humanName, String busStop){
		
		
		//log("Register Action  with HumanName:" + humanName + " for BusStop " + busStop);
		boolean humanFound = false;
		
		
		if(!simulation.registerHumanAtBusStop(humanName, busStop)){
			//this.log(this.fedInfoStr + "Human Not found");
			regCalls.add(new RegisterCall(humanName, busStop, fedamb.federateTime));
			return;
		}	
	}
	
	
	public void handleDiscoveredHuman(ObjectInstanceHandle oih, ObjectClassHandle och,String name){
		
		
		for (Human human : simulation.getHumans()) {
			if(human.getOih().equals(oih)){
				return;
			}
		}

		//log("Adding Human by discovery");
		//addHumanHandle(oih);
	}
	
	
	public void handleHumanAttributeUpdates(ObjectInstanceHandle oih, AttributeHandleValueMap attributes){
		boolean found = false;
		boolean handleAvailable = false;
		String humanName = "";
		String destination = "";
		
		
		
		
		
		for(AttributeHandle handle : attributes.keySet()){
			if(handle.equals(humanNameAttributeHandle)){
				//log("found Human Name Attribute Handle handle");
				humanName = (String)adapterService.filter(String.class.getTypeName(), attributes.get(humanNameAttributeHandle));
			} else if (handle.equals(destinationHandle)) {
				destination = (String)adapterService.filter(String.class.getTypeName(),attributes.get(destinationHandle));
			} else {
				//log("Got more than expected");
			}
		}
//		
//		log("HumanName:" + humanName);
//		log("Destination:" + destination);
		
		if(humanName.equals("")){
			//log(fedInfoStr + " ERROR: got empty name - is destination change");
			handleHumanDestinationChange(oih, destination);
			return;
		}
		
		
		//log(fedInfoStr + " Got Human Attribute Update");
	
		
		for (Human human : simulation.getHumans()) {
			if(human.getOih().equals(oih)){
				//log("Found human in list while updating attributes");
				for(int i = 0; i < simulation.getStops().length; i++){
					if(simulation.getStops()[i].getName().equals(destination)){
						human.setDestination(simulation.getStops()[i]);
						found = true;
						if(!human.isInitialised()){
							initialisedHumans++;
							human.isInitialised();
							searchRegisterCalls(human);
							searchDestinationChangeCalls(human);
						}
					}
				}
			}
		} 
		
		if(!found){
		//log("Human not in list to update attributes - creating");
		
		Human hu = new Human(simulation, humanName, humanObjectClassHandle, oih);
		
		simulation.addHuman(hu);
		
		hu.setInitialised(true);
		initialisedHumans++;
		
		for(int i = 0; i < simulation.getStops().length; i++){
			if(simulation.getStops()[i].getName().equals(destination)){
				hu.setDestination(simulation.getStops()[i]);
			}
		}
		
		searchRegisterCalls(hu);
		searchDestinationChangeCalls(hu);
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
		
		destCalls.add(new DestinationChangeCall(oih, destination, fedamb.federateTime));
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
	
	private void searchRegisterCalls(Human hu){
		
		//log("Searching remaining calls");
		for (RegisterCall call : regCalls) {
			if(call.getName().equals(hu.getName())){
				for(int i = 0; i < simulation.getStops().length; i++){
					if(simulation.getStops()[i].getName().equals(call.bs)){
						simulation.getStops()[i].setPassenger(hu);
					}
				}
			}
		}
	}
	
	private void searchDestinationChangeCalls(Human hu){
		
	
		
		//log("Searching remaining calls");
		for (DestinationChangeCall call : destCalls) {
			if(call.getInstance().equals(hu.getOih())){
				for(int i = 0; i < simulation.getStops().length; i++){
					if(simulation.getStops()[i].getName().equals(call.getDestination())){
						simulation.getStops()[i].setPassenger(hu);
					}
				}
			}
		}
	}
	
	public class RegisterCall{
		String name;
		String bs;
		Double timeStamp;
		
		public RegisterCall(String name, String bs, Double timestamp){
			this.name = name;
			this.bs = bs;
			this.timeStamp = timestamp;
		}
		
		public String getName(){
			return name;
		}
		
		public String getBusStop(){
			return bs;
		}
		
		public Double getTimeStamp(){
			return timeStamp;
		}
	}
	
	public class DestinationChangeCall{
		ObjectInstanceHandle instance;
		String destination;
		Double timeStamp;
		
		public DestinationChangeCall(ObjectInstanceHandle instance, String destination, Double timestamp){
			this.instance = instance;
			this.destination = destination;
			this.timeStamp = timestamp;
		}
		
		public ObjectInstanceHandle getInstance(){
			return instance;
		}
		
		public String getDestination(){
			return destination;
		}
		
		public Double getTimeStamp(){
			return timeStamp;
		}
			
	}
	
	public void addHumanHandle(ObjectInstanceHandle handle){
		humanHandles.add(handle);
	}

}
