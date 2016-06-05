package bgu.spl.mics.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import java.util.Comparator;
import java.util.Collections;
public class WebsiteClientService extends MicroService {

	private int currentTick;
	private List<PurchaseSchedule> fPurchaseSchedule;
	private Set<String> fWishList;
	private CountDownLatch fLatchObject;
	private final Logger LOGGER = Logger.getLogger(WebsiteClientService.class.getName());
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList, CountDownLatch latchObject) {
		super(name);
		currentTick = 0;
		fPurchaseSchedule = purchaseSchedule;
		fWishList = wishList;
		fLatchObject = latchObject;
		
		Comparator<PurchaseSchedule> cmpPurchaseSchedule = new Comparator<PurchaseSchedule>(){
			public int compare(PurchaseSchedule o1, PurchaseSchedule o2){
				return o1.getTickNum() - o2.getTickNum();
			}
		};
		Collections.sort(fPurchaseSchedule, cmpPurchaseSchedule);
    }
	
	@Override
	protected void initialize() {
		currentTick = 1;
		subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>(){
			@Override
			public void call(TickBroadcast newTick){
				currentTick = newTick.getTick();
				LOGGER.info(getName() + ": Received TickBroadcast. currentTick is " + currentTick);
				
				for(PurchaseSchedule currentPurchase : fPurchaseSchedule){
					if(currentPurchase.getTickNum() == currentTick){
						LOGGER.info(getName() + " sending PurchaseOrderRequest");
						ShoeStorageInfo shoe = new ShoeStorageInfo(currentPurchase.getShoeType(), 1, 0); 
						PurchaseOrderRequest request = new PurchaseOrderRequest(shoe, false, currentTick);
						sendRequest(request, new Callback<Receipt>(){
							@Override
							public void call(Receipt receipt){
								LOGGER.info(getName() + " received Receipt for PurchaseOrderRequest");
								fPurchaseSchedule.remove(currentPurchase);
							}
						});
				}
				}
				 
			}
		});
		
		subscribeBroadcast(NewDiscountBroadcast.class, new Callback<NewDiscountBroadcast>(){
			@Override
			public void call(NewDiscountBroadcast newDiscount){
				LOGGER.info(getName() + " received NewDiscountBroadcast" );
				for(String wishItem : fWishList) {
					if(wishItem.equals(newDiscount.getShoe().getShoeType())){
						fWishList.remove(wishItem);
						LOGGER.info(getName() + " sending PurchaseOrderRequest");
						PurchaseOrderRequest request = new PurchaseOrderRequest(newDiscount.getShoe(), true, currentTick);
						sendRequest(request, new Callback<Receipt>(){
							@Override
							public void call(Receipt receipt){
								if(receipt == null){
									fWishList.add(wishItem);
								}
								else {
									LOGGER.info(getName() + " received Receipt for PurchaseOrderRequest");
								}
								
							}
						});
					}
				}
			}
		});

		
		subscribeBroadcast(TerminationBroadcast.class, new Callback<TerminationBroadcast>(){
			@Override
			public void call(TerminationBroadcast newTerminationMessage){
				LOGGER.info(getName() + ": Received TerminationBroadcast. currentTick is " + currentTick + ". Terminating...");
				terminate();
			}
		});
		
		fLatchObject.countDown();
		LOGGER.info(getName() + " finished initialization");
	}

}
