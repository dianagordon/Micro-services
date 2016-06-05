package bgu.spl.mics.impl;
public class PurchaseSchedule {
	private String fShoeType;
	private int fTick;
	
	public PurchaseSchedule(String shoeType, int tick){
		fShoeType = shoeType;
		fTick = tick;
	}
	
	public String getShoeType(){
		return fShoeType;
	}
	
	public int getTickNum(){
		return fTick;
	}
}
