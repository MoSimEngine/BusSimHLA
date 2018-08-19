package edu.kit.ipd.sdq.modsim.adaption;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger32BE;


public class ByteArrayToInteger32BEConversion extends AdaptationConversion{

	EncoderFactory factory;
	
	public ByteArrayToInteger32BEConversion(EncoderFactory factory){
		this.factory = factory;
	}
	
	public Integer convert(byte[] arr){
		HLAinteger32BE i = factory.createHLAinteger32BE();
		try {
			i.decode(arr);
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return i.getValue();
	}
	
	public byte[] convert(Integer i){
		HLAinteger32BE s = factory.createHLAinteger32BE(i);
		return s.toByteArray();
	}
}
