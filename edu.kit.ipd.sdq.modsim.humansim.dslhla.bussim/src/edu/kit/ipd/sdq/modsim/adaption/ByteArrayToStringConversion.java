package edu.kit.ipd.sdq.modsim.adaption;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIstring;

public class ByteArrayToStringConversion extends AdaptationConversion {

	EncoderFactory factory;
	
	public ByteArrayToStringConversion(EncoderFactory factory){
		this.factory = factory;
	}
	
	
	public String convert(byte[] arr){
		HLAASCIIstring s = factory.createHLAASCIIstring();
		try {
			s.decode(arr);
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s.getValue();
	}
	
	public byte[] convert(String str){
		HLAASCIIstring s = factory.createHLAASCIIstring(str);
		return s.toByteArray();
	}
}
