package bgu.spl.mics.impl;

import bgu.spl.mics.Request;

public class PurchaseOrderRequest implements Request<Receipt> {

	private ShoeStorageInfo fShoe;
	private boolean fOnlyDiscount;
	private int fRequestTick;
	public PurchaseOrderRequest(ShoeStorageInfo shoe, boolean onlyDiscount, int requestTick){
		fShoe = shoe;
		fOnlyDiscount = onlyDiscount;
		fRequestTick = requestTick;
	}
	
	public ShoeStorageInfo getShoe(){
		return fShoe;
	}
	
	public int getRequestTick(){
		return fRequestTick;
	}
	
	public boolean isOnlyDiscount(){
		return fOnlyDiscount;
	}

}
