package edu.kit.ipd.sdq.modsim.adaption;

import java.util.LinkedList;

public abstract class AdaptationDescription {

	private String name;
	
	private LinkedList<DataMarkerMapping> markers;

	public AdaptationDescription(String name) {
		this.name = name;
		this.markers = new LinkedList<DataMarkerMapping>();
	}

	public String getName() {
		return name;
	}

	public LinkedList<DataMarkerMapping> getMarkers() {
		return markers;
	}

	public void setMarkers(LinkedList<DataMarkerMapping> markers) {
		this.markers = markers;
	}
	
	public void addMarker(DataMarkerMapping marker){
		markers.add(marker);
	}
}
