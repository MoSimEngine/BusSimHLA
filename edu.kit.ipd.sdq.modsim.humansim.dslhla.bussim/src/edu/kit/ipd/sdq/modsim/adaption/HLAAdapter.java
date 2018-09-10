package edu.kit.ipd.sdq.modsim.adaption;

import java.util.LinkedList;


import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.time.HLAfloat64TimeFactory;

public class HLAAdapter {

	protected EncoderFactory encoderFactory; // set when we join
	LinkedList<AdaptationDescription> attachments;
	HLAByteArrayAdaption byteArrayDescription;

	
	
	public HLAAdapter(){

	
		attachments = new LinkedList<AdaptationDescription>();
	}
	

	
	
	@SuppressWarnings("unchecked")
	public <E> E filter(String reqType, byte[] array){
		for (AdaptationDescription description : attachments) {
			if(description instanceof HLAByteArrayAdaption){
				HLAByteArrayAdaption tmp = (HLAByteArrayAdaption )description;
				if(tmp.baseMarker.getType().equals(array.getClass().getTypeName())){
					for (HLAByteArrayDerivedElement element : tmp.getDerived()) {
						if(reqType.equals(String.class.getTypeName()) && reqType.equals(element.marker.getType())){
							return (E) ((ByteArrayToStringConversion)element.conversion).convert(array);
						} else if(reqType.equals(int.class.getTypeName()) && reqType.equals(element.marker.getType())){
							return (E) ((ByteArrayToInteger32BEConversion)element.conversion).convert(array);
						}
					}
				}
			} 
		}
		throw new IllegalArgumentException("No Marker correspondance found");
	}
	
	@SuppressWarnings("unchecked")
	public <E> byte[] filter(E convert){
		for (AdaptationDescription description : attachments) {
			if(description instanceof HLAByteArrayAdaption){
				HLAByteArrayAdaption tmp = (HLAByteArrayAdaption )description;
				if(tmp.baseMarker.getType().equals(byte[].class.getTypeName())){
					for (HLAByteArrayDerivedElement element : tmp.getDerived()) {
						if(element.marker.getType().equals(convert.getClass().getTypeName()) && convert.getClass().getTypeName().equals(String.class.getTypeName())){
							return ((ByteArrayToStringConversion)element.conversion).convert((String)convert);
						} else if(element.marker.getType().equals(convert.getClass().getTypeName()) && convert.getClass().getTypeName().equals(Integer.class.getTypeName())){
							return ((ByteArrayToInteger32BEConversion)element.conversion).convert((Integer)convert);
						}
					}
				}
			} 
		}
		throw new IllegalArgumentException("No Marker correspondance found");
		
	}
	
	public void addDescription(AdaptationDescription description){
		attachments.add(description);
		
	}	
}
