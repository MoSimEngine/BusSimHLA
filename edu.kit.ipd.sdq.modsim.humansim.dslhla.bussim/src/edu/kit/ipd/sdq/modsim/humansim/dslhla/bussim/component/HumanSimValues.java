package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;


public class HumanSimValues {
	
	public final static int NUM_BUSSTOPS = 3;
	public final static int NUM_HUMANS = 40;
	public final static Duration MAX_SIM_TIME = Duration.hours(24);
	public final static Duration BUSY_WAITING_TIME_STEP = Duration.seconds(5);
	public final static boolean USE_SPIN_WAIT = true;
	public final static boolean PROCESS_ORIENTED = false;
	public static final String READY_TO_RUN = "ReadyToRun";
	public static final String READY_TO_START_SIM = "ReadyToStart";
	public static final String READY_TO_INIT = "ReadyToInit";
	public static final boolean MESSAGE = false;
	
}
