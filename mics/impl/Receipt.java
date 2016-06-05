package bgu.spl.mics.impl;

public class Receipt {
	private String fSeller;
	private String fCustomer;
	private String fShoeType;
	private Boolean fDiscount;
	private int fIssuedTick;
	private int fRequestTick;
	private int fAmountSold;
	
	public Receipt(String seller, String customer, String shoeType, Boolean discount, int issuedTick, int requestTick, int amountSold){  
		fSeller = seller;
		fCustomer = customer;
		fShoeType = shoeType;
		fDiscount = discount;
		fIssuedTick = issuedTick;
		fRequestTick = requestTick;
		fAmountSold = amountSold;
	}
	
	public String getSeller(){
		return fSeller;
	}
	public String getCustomer(){
		return fCustomer;
	}
	public String getShoeType(){
		return fShoeType;
	}
	public Boolean getDiscount(){
		return fDiscount;
	}
	public int getRequestTick(){
		return fRequestTick;
	}
	public int getIssuedTick(){
		return fIssuedTick;
	}
	public int getAmountSold(){
		return fAmountSold;
	}
	public void setCustomer(String customer){
		fCustomer = customer;
	}
	public String toString(){
		return new String(getSeller() + "," + getCustomer() + ", bought " + getShoeType() + " with discount " + getDiscount() + " request tick " + getRequestTick() + " issued tick " + getIssuedTick() + " amount " + getAmountSold());
	}
}
