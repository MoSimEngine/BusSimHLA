package edu.kit.ipd.sdq.modsim.adaption;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfloat64BE;

public class ByteArrayToDoubleConversion extends AdaptationConversion {
	EncoderFactory factory;
	
	public ByteArrayToDoubleConversion(EncoderFactory factory){
		this.factory = factory;
	}
	
	public double convert(byte[] arr){
		
		HLAfloat64BE d = factory.createHLAfloat64BE();
		
		try {
			d.decode(arr);
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return d.getValue();
	}
	
	public byte[] convert(double d){
		HLAfloat64BE b = factory.createHLAfloat64BE(d);
		return b.toByteArray();
	}
}
