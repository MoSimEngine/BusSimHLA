package edu.kit.ipd.sdq.modsim.adaption;

import java.util.LinkedList;

public class HLAByteArrayAdaption extends AdaptationDescription{

	DataMarkerMapping baseMarker;
	private LinkedList<HLAByteArrayDerivedElement> derived;
	
	public HLAByteArrayAdaption(DataMarkerMapping baseMarker){
		super("BaseConnectedHLAByteArray");
		this.baseMarker = baseMarker;
		derived = new LinkedList<HLAByteArrayDerivedElement>();
	}
	
	public void addDerivedElement(HLAByteArrayDerivedElement derivedElement){
		derived.add(derivedElement);
		addMarker(derivedElement.marker);
	}
	
	public LinkedList<HLAByteArrayDerivedElement> getDerived(){
		return derived;
	}
	
	
}
