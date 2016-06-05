package bgu.spl.mics.impl;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.Store;
import bgu.spl.mics.impl.Store.BuyResult;

public class SellingService extends MicroService {

	private int currentTick;
	private CountDownLatch fLatchObject;
	private final Logger LOGGER = Logger.getLogger(SellingService.class.getName());
	public SellingService(int i, CountDownLatch latchObject) {
		super("seller " + i);
		currentTick = 1;
		fLatchObject = latchObject;
    }

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>(){
			@Override
			public void call(TickBroadcast newTick){
				currentTick = newTick.getTick();
				LOGGER.info(getName() + " received TickBroadcast. currentTick is " + currentTick);
			}
		});
		
		subscribeBroadcast(TerminationBroadcast.class, new Callback<TerminationBroadcast>(){
			@Override
			public void call(TerminationBroadcast newTerminationMessage){
				LOGGER.info(getName() + ": Received TerminationBroadcast. currentTick is " + currentTick + ". Terminating...");
				terminate();
			}
		});
		
		subscribeRequest(PurchaseOrderRequest.class, new Callback<PurchaseOrderRequest>(){
			@Override
			public void call(PurchaseOrderRequest req){
				
				LOGGER.info(getName() + " received PurchaseOrderRequest");
				BuyResult res = Store.getInstance().take(req.getShoe().getShoeType(), req.isOnlyDiscount()); 
				switch(res)
				{
				case NOT_IN_STOCK:
				{
					LOGGER.info(getName() + " PurchaseOrderRequest not in stock. Sending RestockRequest");
					if(!sendRequest(new RestockRequest(req.getShoe()), new Callback<Boolean>(){
						@Override
						public void call(Boolean isSucceeded){
							if(isSucceeded){
								LOGGER.info(getName() + " RestockRequest succeeded");
								Receipt receipt = new Receipt(getName(), null, req.getShoe().getShoeType(), req.isOnlyDiscount(), currentTick , req.getRequestTick(), req.getShoe().getAmountOnStorage());
								Store.getInstance().file(receipt);
								complete(req, receipt);
							}
							else {
								LOGGER.warning(getName() + " RestockRequest NOT succeeded");
								complete(req, null);
							}
						}
					}
					)){
						LOGGER.warning(getName() + ": Could not send RestockRequest");
						complete(req, null);
					}
					
					break;
				}
				case NOT_ON_DISCOUNT:
				{
					LOGGER.info(getName() + " found no discount on item");
					complete(req, null);
					break;
				}
				
				case REGULAR_PRICE:
				{
					LOGGER.info(getName() + " sold regular price shoe");
					Receipt receipt = new Receipt(getName(), null, req.getShoe().getShoeType(), req.isOnlyDiscount(), currentTick , req.getRequestTick(), req.getShoe().getAmountOnStorage());
					Store.getInstance().file(receipt);
					complete(req, receipt);
					break;
				}
				case DISCOUNTED_PRICE:
				{
					LOGGER.info(getName() + " sold discounted price shoe");
					Receipt receipt = new Receipt(getName(), null, req.getShoe().getShoeType(), req.isOnlyDiscount(), currentTick , req.getRequestTick(), req.getShoe().getAmountOnStorage());
					Store.getInstance().file(receipt);
					complete(req, receipt);
					break;
				}
				}
			}
			
		});
		
		fLatchObject.countDown();
		LOGGER.info(getName() + " finished initialization");
	}

}
