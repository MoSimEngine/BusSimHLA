package edu.kit.ipd.sdq.modsim.humansim.dslhla.bussim.component;


public class HumanSimValues {
	
	public final static int NUM_BUSSTOPS = 3;
	public final static int NUM_HUMANS = 50;
	public final static Duration MAX_SIM_TIME = Duration.hours(24);
	public final static Duration BUSY_WAITING_TIME_STEP = Duration.seconds(30);
	public final static boolean USE_SPIN_WAIT = false;
	public static final String READY_TO_RUN = "ReadyToRun";
	public static final boolean MESSAGE = false;
	public static final boolean EVOKE = true;
	public static final boolean FULL_SYNC = false;
	
}
