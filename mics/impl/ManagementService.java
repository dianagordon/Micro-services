package bgu.spl.mics.impl;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
public class ManagementService extends MicroService {
	
	private int currentTick;
	private List<DiscountSchedule> fDiscountSchedule;
	private HashMap<String, HashMap<ManufacturingOrderRequest, LinkedList<RestockRequest>>> fShoesOnOrder;
	private CountDownLatch fLatchObject;
	private final Logger LOGGER = Logger.getLogger(ManagementService.class.getName());
	public ManagementService(List<DiscountSchedule> discountSchedule, CountDownLatch latchObject) {
		super("manager");
		fDiscountSchedule = discountSchedule;
		currentTick = 0;
		fShoesOnOrder = new HashMap<String, HashMap<ManufacturingOrderRequest, LinkedList<RestockRequest>>>();
		fLatchObject = latchObject;
    }

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>(){
			@Override
			public void call(TickBroadcast newTick){
				currentTick = newTick.getTick();
				LOGGER.info(getName() + ": Received TickBroadcast. currentTick is " + currentTick);
				Iterator<DiscountSchedule> it = fDiscountSchedule.iterator();
				while(it.hasNext())
				{
					DiscountSchedule discountSchedule = it.next();
					if(currentTick >= discountSchedule.getTickNum()){
						 LOGGER.info(getName() + ": Sending NewDiscountBroadcast");
						 ShoeStorageInfo shoe = new ShoeStorageInfo(discountSchedule.getShoeType(), discountSchedule.getAmount(), discountSchedule.getAmount());
						 Store.getInstance().addDiscount(discountSchedule.getShoeType(), discountSchedule.getAmount());
						 sendBroadcast(new NewDiscountBroadcast(shoe));
						 it.remove();
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
		
		subscribeRequest(RestockRequest.class, new Callback<RestockRequest>(){
			@Override
			public void call(RestockRequest restockRequest){
				LOGGER.info(getName() + ": RestockRequest received for " + restockRequest.getShoe().getShoeType());
				
				// Check if already on order
				boolean isAlreadyOrdered = false;
				
				if(fShoesOnOrder.containsKey(restockRequest.getShoe().getShoeType())){
					HashMap<ManufacturingOrderRequest, LinkedList<RestockRequest>> currentRequestShoe = fShoesOnOrder.get(restockRequest.getShoe().getShoeType());
					for(Map.Entry<ManufacturingOrderRequest,LinkedList<RestockRequest>> entry : currentRequestShoe.entrySet()){
						LinkedList<RestockRequest> requestsOfCurrentMR = entry.getValue();
						if(entry.getKey().getShoe().getAmountOnStorage() > requestsOfCurrentMR.size())
						{
							requestsOfCurrentMR.add(restockRequest);
							isAlreadyOrdered = true;
							break;
						}
					}
				}
				
				if(!isAlreadyOrdered){					
					ManufacturingOrderRequest manufacturingOrderRequest = new ManufacturingOrderRequest(new ShoeStorageInfo(restockRequest.getShoe().getShoeType(), currentTick%5+1, 0), currentTick);
					
					if(fShoesOnOrder.containsKey(restockRequest.getShoe().getShoeType())){
						LinkedList<RestockRequest> rrList = new LinkedList<RestockRequest>();
						rrList.add(restockRequest);
						fShoesOnOrder.get(restockRequest.getShoe().getShoeType()).put(manufacturingOrderRequest, rrList);
					} else {
						LinkedList<RestockRequest> rrList = new LinkedList<RestockRequest>();
						rrList.add(restockRequest);
						HashMap<ManufacturingOrderRequest, LinkedList<RestockRequest>> manufactureMap = new HashMap<ManufacturingOrderRequest, LinkedList<RestockRequest>>();
						manufactureMap.put(manufacturingOrderRequest, rrList);
						fShoesOnOrder.put(restockRequest.getShoe().getShoeType(),manufactureMap);
					}
					
					LOGGER.info(getName() + ": Sending ManufacturingOrderRequest for " + (currentTick%5+1) + " of type " + restockRequest.getShoe().getShoeType());
					if(!sendRequest(manufacturingOrderRequest, new Callback<Receipt>(){
						@Override
						public void call(Receipt receipt){
							LOGGER.info(getName() + ": ManufacturingOrderRequest completed");
							
							LinkedList<RestockRequest> shoeOnOrder = fShoesOnOrder.get(restockRequest.getShoe().getShoeType()).get(manufacturingOrderRequest);
							if(shoeOnOrder.size() < manufacturingOrderRequest.getShoe().getAmountOnStorage()){
								Store.getInstance().add(restockRequest.getShoe().getShoeType(),  manufacturingOrderRequest.getShoe().getAmountOnStorage() - shoeOnOrder.size());
							}
							for(RestockRequest rr : shoeOnOrder){
								complete(rr, true);
							}
							
							Store.getInstance().file(receipt);
							fShoesOnOrder.remove(manufacturingOrderRequest);
						}
					}));
				}
			}					
		});	
		
		fLatchObject.countDown();
		LOGGER.info(getName() + " finished initialization");
	}
}
