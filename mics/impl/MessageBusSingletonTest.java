package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.impl.MessageBusSingleton.RegisteredService;

public class MessageBusSingletonTest {

	@Before
	public void setUp() throws Exception {
		MessageBusSingleton.getInstance().cleanAll();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetInstance() {
		assertNotNull(MessageBusSingleton.getInstance());
	}

	@Test
	public void testSubscribeRequest() {
		CountDownLatch cl = new CountDownLatch(2);
		MicroService m = new SellingService(2, cl);
		MessageBusSingleton.getInstance().register(m);
		MessageBusSingleton.getInstance().subscribeRequest(Request.class, m);
		ConcurrentMap<MicroService,RegisteredService>  registeredServices = MessageBusSingleton.getInstance().getRegisteredServices();
		assertNotNull(registeredServices.get(m).fSubscribedMessageTypes.contains(Request.class));
	}

	@Test
	public void testSubscribeBroadcast() {
		CountDownLatch cl = new CountDownLatch(2);
		MicroService m = new SellingService(2, cl);
		MessageBusSingleton.getInstance().register(m);
		MessageBusSingleton.getInstance().subscribeBroadcast(Broadcast.class, m);
		ConcurrentMap<MicroService,RegisteredService>  registeredServices = MessageBusSingleton.getInstance().getRegisteredServices();
		assertNotNull(registeredServices.get(m).fSubscribedMessageTypes.contains(Broadcast.class));
	}

	@Test
	public void testComplete() {
		CountDownLatch cl = new CountDownLatch(2);
		MicroService m = new SellingService(2, cl);
		MessageBusSingleton.getInstance().register(m);
		ShoeStorageInfo shoeStorage = new ShoeStorageInfo("shoe 1", 1, 0);
		
		MicroService m1 = new SellingService(1, cl);
		MessageBusSingleton.getInstance().register(m1);
		MessageBusSingleton.getInstance().subscribeRequest(PurchaseOrderRequest.class, m1);
		
		PurchaseOrderRequest r = new PurchaseOrderRequest(shoeStorage, false, 1);
		MessageBusSingleton.getInstance().sendRequest(r, m);
		
		MessageBusSingleton.getInstance().complete(r, new Receipt("Idan", "Diana", "shoe 1", false, 5, 4, 1));
		Map<Request<?>, MicroService> map = MessageBusSingleton.getInstance().getRequestsOnProcess();
		assertNull(map.get(r));
		
		
		
	}

	@Test
	public void testSendBroadcast() {
		CountDownLatch cl = new CountDownLatch(2);
		MicroService m = new SellingService(2, cl);
		MessageBusSingleton.getInstance().register(m);
		
		MicroService m1 = new SellingService(1, cl);
		MessageBusSingleton.getInstance().register(m1);
		MessageBusSingleton.getInstance().subscribeBroadcast(TickBroadcast.class, m1);
		
		TickBroadcast r = new TickBroadcast(1);
		MessageBusSingleton.getInstance().sendBroadcast(r);
		
		try{
			Message msg = MessageBusSingleton.getInstance().awaitMessage(m1);
			assertNotNull(msg);
		}catch (InterruptedException e){
			fail("Exception occurred");
		}
	}

	@Test
	public void testSendRequest() {
		CountDownLatch cl = new CountDownLatch(2);
		MicroService m = new SellingService(2, cl);
		MessageBusSingleton.getInstance().register(m);
		ShoeStorageInfo shoeStorage = new ShoeStorageInfo("shoe 1", 1, 0);
		
		MicroService m1 = new SellingService(1, cl);
		MessageBusSingleton.getInstance().register(m1);
		MessageBusSingleton.getInstance().subscribeRequest(PurchaseOrderRequest.class, m1);
		
		PurchaseOrderRequest r = new PurchaseOrderRequest(shoeStorage, false, 1);
		MessageBusSingleton.getInstance().sendRequest(r, m);
		Map<Request<?>, MicroService> map = MessageBusSingleton.getInstance().getRequestsOnProcess();
		assertNotNull(map.get(r));
	}

	@Test
	public void testRegister() {
		CountDownLatch cl = new CountDownLatch(2);
		MicroService m = new SellingService(2, cl);
		MessageBusSingleton.getInstance().register(m);
		ConcurrentMap<MicroService,RegisteredService>  registeredServices = MessageBusSingleton.getInstance().getRegisteredServices();
		assertNotNull(registeredServices.get(m));
	}

	@Test
	public void testUnregister() {
		CountDownLatch cl = new CountDownLatch(2);
		MicroService m = new SellingService(2, cl);
		MessageBusSingleton.getInstance().register(m);
		MessageBusSingleton.getInstance().unregister(m);
		ConcurrentMap<MicroService,RegisteredService>  registeredServices = MessageBusSingleton.getInstance().getRegisteredServices();
		assertNull(registeredServices.get(m));
	}

	@Test
	public void testAwaitMessage() {
		CountDownLatch cl = new CountDownLatch(2);
		MicroService m = new SellingService(2, cl);
		MessageBusSingleton.getInstance().register(m);
		ShoeStorageInfo shoeStorage = new ShoeStorageInfo("shoe 1", 1, 0);
		
		MicroService m1 = new SellingService(1, cl);
		MessageBusSingleton.getInstance().register(m1);
		MessageBusSingleton.getInstance().subscribeRequest(PurchaseOrderRequest.class, m1);
		
		PurchaseOrderRequest r = new PurchaseOrderRequest(shoeStorage, false, 1);
		MessageBusSingleton.getInstance().sendRequest(r, m);
		
		try{
			Message msg = MessageBusSingleton.getInstance().awaitMessage(m1);
			assertNotNull(msg);
		}catch (InterruptedException e){
			fail("Exception occurred");
		}
	}

}
