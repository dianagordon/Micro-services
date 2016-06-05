package bgu.spl.mics.impl;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
public class MessageBusSingleton implements MessageBus {
	
	public class RegisteredService
	{
		public LinkedBlockingQueue<Message> fQueue;
		public LinkedList<Class<? extends Message>> fSubscribedMessageTypes;
		
		public RegisteredService()
		{
			fQueue = new LinkedBlockingQueue<Message>();
			fSubscribedMessageTypes = new LinkedList<Class<? extends Message>>();
		}
	}
	
	private ConcurrentMap<MicroService,RegisteredService> fRegisteredServices;
	private int fLastServiceIndexChecked;
	private Map<Request<?>, MicroService> fMapRequestsOnProcess;
	private final Logger LOGGER = Logger.getLogger(MessageBusSingleton.class.getName());
	private static class MessageBusSingletonHolder {
        private static MessageBusSingleton instance = new MessageBusSingleton();
    }
	
	private MessageBusSingleton()
	{
		fRegisteredServices =  new ConcurrentHashMap<MicroService, RegisteredService>();
		fLastServiceIndexChecked = 0;
		fMapRequestsOnProcess = new HashMap<Request<?>, MicroService>();
	}
	
	public static MessageBusSingleton getInstance()
	{
		return MessageBusSingletonHolder.instance;
	}
	
	@Override
	public <R> void subscribeRequest(Class<? extends Request> type, MicroService m) {
		subscribeMessage(type, m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribeMessage(type, m);
	}
	
	@Override
	public <T> void complete(Request<T> r, T result) {
		RequestCompleted<T> requestCompleted = new RequestCompleted<T>(r, result);
		MicroService requester = fMapRequestsOnProcess.get(r);
		if(requester != null)
		{
			fMapRequestsOnProcess.remove(r);	
		}
		
		if(requester != null && result != null)
		{
			if(result.getClass() == Receipt.class)
			{
				((Receipt)result).setCustomer(requester.getName());
			}
			
			RegisteredService registeredService = fRegisteredServices.get(requester);
			if(registeredService != null){
				registeredService.fQueue.add(requestCompleted);
			}
		}
		
		if(requester == null)
		{
			LOGGER.severe("Error retrieving requester for requestComplete message in MessageBus");
		}
		
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		Iterator<Entry<MicroService, RegisteredService>> it = fRegisteredServices.entrySet().iterator();
		while(it.hasNext()){
			RegisteredService registeredService = it.next().getValue();
				registeredService.fQueue.add(b);
			
		}
	}

	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		
		List<RegisteredService> registeredServicesList = new ArrayList<RegisteredService>(fRegisteredServices.values());
		
		int currentStartPosition = fLastServiceIndexChecked;
		if(requester == null)
		{
			LOGGER.severe("Error: send requester without requester!");
		}
		while(fLastServiceIndexChecked < registeredServicesList.size())
		{
			
			RegisteredService registeredService = registeredServicesList.get(fLastServiceIndexChecked);
			if(registeredService.fSubscribedMessageTypes.contains(r.getClass()))
			{
				registeredService.fQueue.add(r);
				if(fMapRequestsOnProcess.containsKey(r)){
					LOGGER.severe("Key already exists");
				}
				fMapRequestsOnProcess.put(r, requester);
				fLastServiceIndexChecked++;
				return true;
			}
			fLastServiceIndexChecked++;
		}
		
		// If we got to the end, start from the beginning
		fLastServiceIndexChecked = 0;
		while(fLastServiceIndexChecked < currentStartPosition)
		{
			try{
				RegisteredService registeredService = registeredServicesList.get(fLastServiceIndexChecked);
				if(registeredService.fSubscribedMessageTypes.contains(r.getClass()))
				{
					registeredService.fQueue.add(r);
					if(fMapRequestsOnProcess.containsKey(r)){
						LOGGER.severe("Key already exists");
					}
					fMapRequestsOnProcess.put(r, requester);
					fLastServiceIndexChecked++;
					return true;
				}	
				fLastServiceIndexChecked++;
			}catch(IndexOutOfBoundsException e){
				LOGGER.severe("Error in send request: Someone unregistered during send request attempt.");
				return false;
			}
		}
		
		LOGGER.severe("request send failed. requester is " + requester.getName() + ". request details: " + r);
		return false;
	}

	@Override
	public void register(MicroService m) {
		RegisteredService registeredService = fRegisteredServices.get(m);
		if(registeredService == null){
			RegisteredService newRegisteredService = new RegisteredService();
			fRegisteredServices.put(m, newRegisteredService);
			LOGGER.info(m.getName() + " registered in MessageBus");
		}
	}

	@Override
	public void unregister(MicroService m) {
		RegisteredService registeredService = fRegisteredServices.get(m);
		if(registeredService != null){
			fRegisteredServices.remove(m);
			LOGGER.info(m.getName() + " unregistered from MessageBus");
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		
		RegisteredService registeredService = fRegisteredServices.get(m);
		if(registeredService != null){
			Message msg = registeredService.fQueue.take();
			return msg;
		}
		return null;
	}

	private void subscribeMessage(Class<? extends Message> type, MicroService m)
	{
		RegisteredService registeredService = fRegisteredServices.get(m);
		if(registeredService != null){
			if(!registeredService.fSubscribedMessageTypes.contains(type)){
				registeredService.fSubscribedMessageTypes.add(type);
			}
		}
	}
	
	public void cleanAll()
	{
		fRegisteredServices.clear();
		fLastServiceIndexChecked = 0;
		fMapRequestsOnProcess.clear();
	}
	
	public ConcurrentMap<MicroService,RegisteredService> getRegisteredServices(){
		return fRegisteredServices;
	}
	
	public Map<Request<?>, MicroService> getRequestsOnProcess(){
		return fMapRequestsOnProcess;
	}
}
