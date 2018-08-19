package edu.kit.ipd.sdq.modsim.adaption;

public class DataMarkerMapping {

	private DataMarker marker;
	private String type;
	
	public DataMarkerMapping(DataMarker marker, String type){
		this.marker = marker;
		this.type = type;
	}

	public DataMarker getMarker() {
		return marker;
	}

	public String getType() {
		return type;
	}
	
}
