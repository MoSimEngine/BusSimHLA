package  edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;


public class BusFederateAmbassador extends NullFederateAmbassador{
	
	private BusFederate federate;
	
	protected double federateTime = 0.0;
	protected double federateLookahead = 0.0;

	protected boolean isRegulating = false;
	protected boolean isConstrained = false;
	protected boolean isAdvancing = false;

	protected boolean isAnnounced = false;
	protected boolean isReadyToRun = false;
	
	


	public BusFederateAmbassador(BusFederate federate){
		this.federate = federate;
	}
	

	@Override
	public void synchronizationPointRegistrationFailed(String label, SynchronizationPointFailureReason reason) {
		federate.log("Failed to register sync point: " + label + ", reason=" + reason);
	}

	@Override
	public void synchronizationPointRegistrationSucceeded(String label) {
		federate.log("Successfully registered sync point: " + label);
	}

	@Override
	public void announceSynchronizationPoint(String label, byte[] tag) {
		federate.log("Synchronization point announced: " + label);
		if (label.equals(HumanSimValues.READY_TO_RUN))
			this.isAnnounced = true;
	}

	@Override
	public void federationSynchronized(String label, FederateHandleSet failed) {
		federate.log("Federation Synchronized: " + label);
		if (label.equals(HumanSimValues.READY_TO_RUN))
			this.isReadyToRun = true;
	}

	@Override
	public void timeRegulationEnabled(LogicalTime time) {
		this.federateTime = ((HLAfloat64Time) time).getValue();
		this.isRegulating = true;
	}

	@Override
	public void timeConstrainedEnabled(LogicalTime time) {
		this.federateTime = ((HLAfloat64Time) time).getValue();
		this.isConstrained = true;
	}

	@Override
	public void timeAdvanceGrant(LogicalTime time) {
		this.federateTime = ((HLAfloat64Time) time).getValue();
		this.isAdvancing = false;
	}

	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
			String objectName) throws FederateInternalError {
		//federate.log("Discoverd Object: handle=" + theObject + ", classHandle=" + theObjectClass + ", name=" + objectName);
		
//		if(theObjectClass.equals(federate.humanObjectClassHandle)){
//			federate.handleDiscoveredHuman(theObject, theObjectClass, objectName);
//		}
		
		
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
			byte[] tag, OrderType sentOrder, TransportationTypeHandle transport, SupplementalReflectInfo reflectInfo)
			throws FederateInternalError {
		reflectAttributeValues(theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo);
		//System.out.println("Got Attributes");
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
			byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time,
			OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError {
		//System.out.println("Got Attributes");
			federate.handleHumanAttributeUpdates(theObject, theAttributes);
		

	}

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters,
			byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport,
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
		this.receiveInteraction(interactionClass, theParameters, tag, sentOrdering, theTransport, null, sentOrdering,
				receiveInfo);
	}

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters,
			byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time,
			OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {

		
		if(interactionClass.equals(federate.registerAtBusStopHandle)){
			decodeRegisterInteraction(theParameters);
		} else if (interactionClass.equals(federate.humanReadyHandle)){
			
		} else {
			federate.log("Interaction not handled");
		}
		
	}

	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] tag, OrderType sentOrdering,
			SupplementalRemoveInfo removeInfo) throws FederateInternalError {
		federate.log("Object Removed: handle=" + theObject);
	}
	
	

	
	private void decodeRegisterInteraction(ParameterHandleValueMap map){
		HLAASCIIstring humanName = federate.encoderFactory.createHLAASCIIstring();
		HLAASCIIstring busStop = federate.encoderFactory.createHLAASCIIstring();
		
		try{
			humanName.decode(map.get(federate.humanNameRegisterHandle));
			busStop.decode(map.get(federate.busStopNameRegisterHandle));
		} catch(DecoderException de){
			de.printStackTrace();
			return;
		}
		
		//federate.log("Received bus stop registration from: " + humanName.getValue() + "at BusStop: " + busStop.getValue());
		
		federate.handleRegistration(federate.service.filter(String.class.getTypeName(), map.get(federate.humanNameRegisterHandle)), 
				federate.service.filter(String.class.getTypeName(), map.get(federate.busStopNameRegisterHandle)));
	}
	
	public String decodeStringValues(byte[] bytes){
	
		HLAASCIIstring value = federate.encoderFactory.createHLAASCIIstring();
		
		
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			de.printStackTrace();
			return "";
		}
		
	}
	
	

}
