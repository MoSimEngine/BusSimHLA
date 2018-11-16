package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.entities.Bus;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.timelinesynchronization.SynchroniseToken.SynchronisedActionTypen;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util.Utils;


public class RTITimelineSynchronizer implements TimelineSynchronizer {
	private LinkedList<SynchroniseToken> advanceTimeTokens;
	private LinkedList<SynchroniseToken> rtiActivityTokens;
	private BusModel model;
	private boolean replacedToken = false;

	public RTITimelineSynchronizer(BusModel model) {
		this.advanceTimeTokens = new LinkedList<SynchroniseToken>();
		this.rtiActivityTokens = new LinkedList<SynchroniseToken>();
		this.model = model;
	}

	public boolean putToken(SynchroniseToken token, boolean forcedOverride) {

		Bus h = (Bus) token.getEntity();

		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.RTI_ACTION)) {
			rtiActivityTokens.add(token);
			((Bus) token.getEntity()).addRegToken(token);
			return true;
		}

		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.ADVANCE_TIME)) {
			if (h.getTaToken() != null) {
				if (forcedOverride) {
					for (int i = 0; i < advanceTimeTokens.size(); i++) {
						if (token.getEntity().getName().equals(advanceTimeTokens.get(i).getEntity().getName())) {
							advanceTimeTokens.set(i, token);
							h.setTaToken(token);
							break;
						}
					}
				} else {
					Utils.log(token.getEntity(), "Denied: " + token.getReturnEvent().getName() + ":"
							+ token.getEntity().getName() + "->" + token.getReturnEventTimepoint()
							+ " due to already existing TA Token with returnEvent "
							+ h.getTaToken().getReturnEvent().getName() + "-> "
							+ h.getTaToken().getReturnEventTimepoint() + ":" + h.getTaToken().getEntity().getName());
					return false;
				}
			} else {
				advanceTimeTokens.add(token);
				h.setTaToken(token);
			}

		}

		if (((BusModel) h.getModel()).getComponent().isAdvancingTime()) {
			replacedToken = true;
			return false;
		}

		return checkAndExecute();
	}

	@Override
	public void sortTokens() {

		Collections.sort(advanceTimeTokens);
		Collections.sort(rtiActivityTokens);

	}


	@Override
	public void executeTimeorderedEvents() {


		sortTokens();
//		printTokenOccupationNames();
		
		int counter = 0;
		int j = 0;
		SynchroniseToken actionTok;
		SynchroniseToken timeAdvanceTok = null;

		
		timeAdvanceTok = advanceTimeTokens.pop();
		((Bus) timeAdvanceTok.getEntity()).setTaToken(null);
		for (; j < rtiActivityTokens.size(); j++) {
			actionTok = rtiActivityTokens.get(j);
			if (actionTok.getReturnEventTimepoint() <= timeAdvanceTok.getReturnEventTimepoint()) {
				actionTok.executeAction();
				((Bus) actionTok.getEntity()).removeRegToken(actionTok);
				counter++;
			} else {
				break;
			}
		}

		for (int i = 0; i < counter; i++) {
			if (rtiActivityTokens.size() != 0) {
				rtiActivityTokens.pop();
			}
		}

		
		timeAdvanceTok.executeAction();

		if (replacedToken) {
			sortTokens();
			SynchroniseToken t = advanceTimeTokens.peek();
			if (timeAdvanceTok.getReturnEventTimepoint() > t.getReturnEventTimepoint()) {
				putToken(timeAdvanceTok, false);

				TimeAdvanceSynchronisationEvent e = new TimeAdvanceSynchronisationEvent(model, "TimeAdvanceEvent",
						t.getReturnEvent(), t.getReturnEventTimestep());

				e.schedule(t.getEntity(), 0);
				replacedToken = false;
				return;
			}
		}

		replacedToken = false;
		
		scheduleReturnEvent(timeAdvanceTok);
		
//			Utils.log("!!!!!!!!End Execution!!!!!!!!!!!");
	}

	@Override
	public void scheduleReturnEvent(SynchroniseToken token) {

		
		//Use BigDecimal to avoid rounding errors
		
		BigDecimal returnEventTimepoint = BigDecimal.valueOf(token.getReturnEventTimepoint());
		BigDecimal returnEventTimeStep = BigDecimal.valueOf(token.getReturnEventTimestep());
		BigDecimal currentSimulationTime = BigDecimal.valueOf(token.getEntity().getModel().getSimulationControl().getCurrentSimulationTime());
		BigDecimal correctureStep = returnEventTimepoint.subtract(returnEventTimeStep).subtract(currentSimulationTime);
		BigDecimal resultingTimeStep = returnEventTimeStep.add(correctureStep);
		
		// ignore differences smaller than -+10^(-9) and set to zero 
		if(resultingTimeStep.doubleValue() < 0.0000000001 && resultingTimeStep.doubleValue() > (-0.000000001)) {
			resultingTimeStep = BigDecimal.ZERO;
		}
		
		token.getReturnEvent().schedule(token.getEntity(), resultingTimeStep.doubleValue());

	}

	public boolean checkForExecution() {

		if (advanceTimeTokens.size() == model.getBusses().size()) {
			return true;
		}

		return false;
	}

	public boolean checkAndExecute() {
		if (checkForExecution()) {
			executeTimeorderedEvents();
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void handleEntityLeft(AbstractSimEntityDelegator entity) {
		for (SynchroniseToken synchroniseToken : advanceTimeTokens) {
			if (synchroniseToken.getEntity().equals(entity)) {
				advanceTimeTokens.remove(synchroniseToken);
			}
		}

		for (SynchroniseToken synchroniseToken : rtiActivityTokens) {
			if (synchroniseToken.getEntity().equals(entity)) {
				rtiActivityTokens.remove(synchroniseToken);
			}
		}

		if (checkForExecution()) {
			sortTokens();
			executeTimeorderedEvents();
		}
	}

	public void printTokenOccupationNames() {


		System.out.println("------TokenList" + " FedTime:" + model.getComponent().getCurrentFedTime() + " SimTime:"
				+ model.getSimulationControl().getCurrentSimulationTime() + "--------");
		System.out.print("| TimeAdvances: ");

		for (int m = 0; m < advanceTimeTokens.size(); m++) {
			System.out.print(advanceTimeTokens.get(m).getEntity().getName() + ":"
					+ advanceTimeTokens.get(m).getReturnEvent().getName() + " -> "
					+ advanceTimeTokens.get(m).getReturnEventTimepoint() + ", ");
		}

		System.out.println(" |");

		System.out.print("| Actions: ");

		for (int n = 0; n < rtiActivityTokens.size(); n++) {
			System.out.print(rtiActivityTokens.get(n).getEntity().getName() + ":" + "Register Activity" + " -> "
					+ rtiActivityTokens.get(n).getResultingTimepoint() + ", ");
		}

		System.out.println(" |");
		System.out.println("-----------------------TokenList--------------------");

	}

	@Override
	public boolean revokeToken(SynchroniseToken token) {

		for (SynchroniseToken synchroniseToken : advanceTimeTokens) {
			if (synchroniseToken.equals(token)) {
				advanceTimeTokens.remove(synchroniseToken);
				((Bus) token.getEntity()).setTaToken(null);
				return true;
			}
		}

		for (SynchroniseToken synchroniseToken : rtiActivityTokens) {
			if (synchroniseToken.equals(token)) {
				advanceTimeTokens.remove(synchroniseToken);
				((Bus) token.getEntity()).removeRegToken(token);
				return true;
			}
		}

		return false;
	}
}
