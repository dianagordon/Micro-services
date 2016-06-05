package bgu.spl.mics.impl;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;

public class ShoeFactoryService extends MicroService {

	private int currentTick;
	private int currentMRTickStart;
	private CountDownLatch fLatchObject;
	private Queue<ManufacturingOrderRequest> fOrderRequests;
	private final Logger LOGGER = Logger.getLogger(ShoeFactoryService.class.getName());
	public ShoeFactoryService(int i, CountDownLatch latchObject) {
		super("factory " + i);
		
		currentTick = 0;
		currentMRTickStart = 0;
		fLatchObject = latchObject;
		fOrderRequests = new LinkedList<ManufacturingOrderRequest>();
    }
	
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>(){
			@Override
			public void call(TickBroadcast newTick){
				currentTick = newTick.getTick();
				LOGGER.info(getName() + ": Received TickBroadcast. currentTick is " + currentTick);
				if(!fOrderRequests.isEmpty()){
					if(currentTick - currentMRTickStart > fOrderRequests.peek().getShoe().getAmountOnStorage()){
						LOGGER.info(getName() + ": Finished ManufacturingOrderRequest in currentTick " + currentTick);
						complete(fOrderRequests.peek(), new Receipt(getName(), "store", fOrderRequests.peek().getShoe().getShoeType(), false, currentTick, fOrderRequests.peek().getRequestTick(), fOrderRequests.peek().getShoe().getAmountOnStorage()));
						fOrderRequests.poll();
						if(!fOrderRequests.isEmpty()){
							currentMRTickStart = currentTick -1;
						}
					}
				}
				
			}
			
		});
		
		subscribeRequest(ManufacturingOrderRequest.class, new Callback<ManufacturingOrderRequest>(){
			@Override
			public void call(ManufacturingOrderRequest req){
				LOGGER.info(getName() + ": Received ManufacturingOrderRequest in currentTick " + currentTick);
				if(fOrderRequests.isEmpty()){
					currentMRTickStart = currentTick;
				}
				fOrderRequests.add(req);
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
