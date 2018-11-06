package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util;

import org.apache.log4j.Logger;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;

import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;



public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class);

    public static void log(AbstractSimEntityDelegator entity, String msg, boolean cmd) {
    	
        StringBuilder s = new StringBuilder();
        s.append("[" + entity.getName() + "] ");
        s.append("(tFed=" + ((BusModel)entity.getModel()).getComponent().getCurrentFedTime() + ") ");
        s.append("(tSim=" + entity.getModel().getSimulationControl().getCurrentSimulationTime() + ")");
        s.append(msg);
        if(cmd)
        	System.out.println(s.toString());
        else 
        	LOGGER.info(s.toString());
    }
    
    public static void log(AbstractSimEntityDelegator entity, String msg) {
    	Utils.log(entity, msg, true);
    }
    
    public static void log(String msg, boolean cmd) {
   	 if(cmd)
        	System.out.println(msg);
        else 
        	LOGGER.info(msg);
   }
   

   public static void log(String msg) {
   	log(msg, true);
   }
}
