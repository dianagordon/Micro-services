package bgu.spl.mics.impl;

public class ShoeStorageInfo {
	private String fShoeType;
	private int fAmountOnStorage;
	private int fDiscountedAmount;
	
	public ShoeStorageInfo(String shoeType, int amountOnStorage, int discountedAmount){
		fShoeType = shoeType;
		fAmountOnStorage = amountOnStorage;
		fDiscountedAmount = discountedAmount;
	}
	
	public String getShoeType(){
		return fShoeType;
	}
	
	public int getAmountOnStorage(){
		return fAmountOnStorage;
	}
	
	public int getDiscountedAmount(){
		return fDiscountedAmount;
	}
	
	public void addAmountOnStorage(int amountToAdd){
		fAmountOnStorage += amountToAdd;
	}
	
	public void addDiscount(int discountAmount){
		fDiscountedAmount += discountAmount;
	}
}
