package bgu.spl.mics.impl;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
public class Store {

	private Collection<ShoeStorageInfo> fShoes;
	private Collection<Receipt> fReceipts;
	private final Logger LOGGER = Logger.getLogger(Store.class.getName());
	public enum BuyResult {
		NOT_IN_STOCK(0), NOT_ON_DISCOUNT(1), REGULAR_PRICE(2), DISCOUNTED_PRICE(3);
		
		private int resultNum;
		private BuyResult(int num){
			resultNum = num;
		}
		
		public int getResult(){
			return resultNum;
		}
	};
	private static class StoreHolder {
        private static Store instance = new Store();
    }
	
	private Store()
	{
		fShoes = new ConcurrentLinkedQueue<ShoeStorageInfo>();
		fReceipts = new ConcurrentLinkedQueue<Receipt>();
	}
	
	public static Store getInstance(){
		return StoreHolder.instance;
	}
	
	public void load(ShoeStorageInfo[] storage){
		for(ShoeStorageInfo shoe : storage){
			fShoes.add(shoe);
		}
		LOGGER.info("Store loaded. initial storage is " + fShoes);
	}

	public BuyResult take(String shoeType, boolean onlyDiscount){
		BuyResult result = BuyResult.NOT_IN_STOCK;
		for(ShoeStorageInfo shoe : fShoes){
			if(shoe.getShoeType().equals(shoeType)){
				if(shoe.getShoeType().equals("work-shoes")){
					LOGGER.info("before take: work-shoes amount " + shoe.getAmountOnStorage());
				}
				if(onlyDiscount && shoe.getDiscountedAmount() <= 0){
					result = BuyResult.NOT_ON_DISCOUNT;
					break;
				}
				if(shoe.getDiscountedAmount() > 0){
					result = BuyResult.DISCOUNTED_PRICE;
					shoe.addDiscount(-1);
					shoe.addAmountOnStorage(-1);
					break;
				}
				if(!onlyDiscount && shoe.getAmountOnStorage() > 0){
					result = BuyResult.REGULAR_PRICE;
					shoe.addAmountOnStorage(-1);
					break;
				} 
			}
				
		}
		
		return result;
			
	}
	
	public void add(String shoeType, int amount){
		for(ShoeStorageInfo shoe : fShoes){
			if(shoe.getShoeType().equals(shoeType)){
				shoe.addAmountOnStorage(amount);
				break;
			}	
		}
	}
	
	public void addDiscount(String shoeType, int amount){
		boolean bShoeExist = false;
		for(ShoeStorageInfo shoe : fShoes){
			if(shoe.getShoeType().equals(shoeType)){
				if(shoe.getDiscountedAmount() < shoe.getAmountOnStorage()){
					shoe.addDiscount(amount);
				}
				
				bShoeExist = true;
				break;
			}	
		}
		
		if(!bShoeExist){
			fShoes.add(new ShoeStorageInfo(shoeType, 0, 0));
		}
	}
	
	public void file(Receipt receipt){
		fReceipts.add(receipt);
	}
	
	public void print(){
		int i = 0;
		LOGGER.info("Printing receipts");
		for(Receipt receipt : fReceipts){
			LOGGER.info("Receipt number " + i + ": " + receipt.toString());
			i++;
		}
		
		LOGGER.info("Printing shoes on stock");
		for(ShoeStorageInfo shoe : fShoes){
			LOGGER.info("Shoe type: " + shoe.getShoeType() + " amount " + shoe.getAmountOnStorage() + " discount " + shoe.getDiscountedAmount());
		}
	}
	
	public void cleanAll(){
		fShoes.clear();
		fReceipts.clear();
	}
	
	public ShoeStorageInfo[] getstorage(){
		return fShoes.toArray(new ShoeStorageInfo[fShoes.size()]);
	}
	
	public Receipt[] getReceipts(){
		return fReceipts.toArray(new Receipt[fReceipts.size()]);
	}
}
