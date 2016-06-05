package bgu.spl.mics.impl;

import bgu.spl.mics.Request;

public class ManufacturingOrderRequest implements Request<Receipt> {

	private ShoeStorageInfo fShoe;
	private int fRequestTick;
	
	public ManufacturingOrderRequest(ShoeStorageInfo shoe, int requestTick){
		fShoe = shoe;
		fRequestTick = requestTick;
	}

	public ShoeStorageInfo getShoe(){
		return fShoe;
	}
	
	public int getRequestTick(){
		return fRequestTick;
	}
	
}
