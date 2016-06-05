package bgu.spl.mics.impl;

public class DiscountSchedule {
	private String fShoeType;
	private int fTick;
	private int fAmount;
	
	public DiscountSchedule(String shoeType, int tick, int amount){
		fShoeType = shoeType;
		fTick = tick;
		fAmount = amount;
	}
	
	public String getShoeType(){
		return fShoeType;
	}
	
	public int getTickNum(){
		return fTick;
	}
	
	public int getAmount(){
		return fAmount;
	}
}