package  edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;
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
		Utils.log("Failed to register sync point: " + label + ", reason=" + reason);
	}

	@Override
	public void synchronizationPointRegistrationSucceeded(String label) {
		Utils.log("Successfully registered sync point: " + label);
	}

	@Override
	public void announceSynchronizationPoint(String label, byte[] tag) {
		Utils.log("Synchronization point announced: " + label);
		if (label.equals(HumanSimValues.READY_TO_RUN))
			this.isAnnounced = true;
	}

	@Override
	public void federationSynchronized(String label, FederateHandleSet failed) {
		Utils.log("Federation Synchronized: " + label);
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
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
			byte[] tag, OrderType sentOrder, TransportationTypeHandle transport, SupplementalReflectInfo reflectInfo)
			throws FederateInternalError {
		reflectAttributeValues(theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo);
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
			byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time,
			OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError {
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
			federate.handleRegistration(federate.adapterService.filter(String.class.getTypeName(), theParameters.get(federate.humanNameRegisterHandle)), 
					federate.adapterService.filter(String.class.getTypeName(), theParameters.get(federate.busStopNameRegisterHandle)),
					federate.adapterService.filter(String.class.getTypeName(), theParameters.get(federate.destinationNameRegisterHandle)));
		} else if (interactionClass.equals(federate.unregisterAtBusStopHandle))  {
			
			federate.handleUnregistration(federate.adapterService.filter(String.class.getTypeName(), theParameters.get(federate.humanNameUnregisterHandle)), 
					federate.adapterService.filter(String.class.getTypeName(), theParameters.get(federate.busStopNameUnregisterHandle)));
		} else {
			Utils.log("Interaction not handled");
		}
		
	}

	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] tag, OrderType sentOrdering,
			SupplementalRemoveInfo removeInfo) throws FederateInternalError {
		Utils.log("Object Removed: handle=" + theObject);
	}
}
