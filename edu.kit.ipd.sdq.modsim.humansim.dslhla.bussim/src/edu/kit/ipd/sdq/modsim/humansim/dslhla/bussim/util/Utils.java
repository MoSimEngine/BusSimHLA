package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.util;

import org.apache.log4j.Logger;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component.BusModel;


public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class);

    public static void log(AbstractSimEntityDelegator entity, String msg) {
        StringBuilder s = new StringBuilder();
        s.append("[" + entity.getName() + "] ");
        s.append("(tFed=" + ((BusModel)entity.getModel()).getComponent().getCurrentFedTime() + ") ");
        s.append("(tSim=" + entity.getModel().getSimulationControl().getCurrentSimulationTime() + ")");
        s.append(msg);
        LOGGER.info(s.toString());
    }
    
    public static void cmdLog(AbstractSimEntityDelegator entity, String msg){
        StringBuilder s = new StringBuilder();
        s.append("[" + entity.getName() + "] ");
        s.append("(tFed=" + ((BusModel)entity.getModel()).getComponent().getCurrentFedTime() + ") ");
        s.append("(tSim=" + entity.getModel().getSimulationControl().getCurrentSimulationTime() + ")");
        s.append(msg);
        System.out.println(s.toString());
    }

}
