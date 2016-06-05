package bgu.spl.mics.impl;

import bgu.spl.mics.Request;

public class RestockRequest implements Request<Boolean> {
	private ShoeStorageInfo fShoe;
	
	public RestockRequest(ShoeStorageInfo shoe){
		fShoe = shoe;
	}
	
	public ShoeStorageInfo getShoe(){
		return fShoe;
	}

}
