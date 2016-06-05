package bgu.spl.mics.impl;

import bgu.spl.mics.Broadcast;

public class NewDiscountBroadcast implements Broadcast {
	private ShoeStorageInfo fShoe;
	
	public NewDiscountBroadcast(ShoeStorageInfo shoe){
		fShoe = shoe;
	}
	
	public ShoeStorageInfo getShoe(){
		return fShoe;
	}
}
