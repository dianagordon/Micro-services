package bgu.spl.mics.impl;

import com.google.gson.Gson;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import bgu.spl.mics.impl.Store;
import bgu.spl.mics.impl.JsonFileStructure.DiscountScheduleItem;
import bgu.spl.mics.impl.JsonFileStructure.PurchaseScheduleItem;
public class ShoeStoreRunner {

	private static final Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName());
	 
	public static void main(String[] args) {
		if(args.length < 1){
			LOGGER.severe("Error: Missing JSON file");
			return;
		}
		
		Gson gson = new Gson();
		String jsonFileName = args[0];
		
		try
		{
			byte[] encoded = Files.readAllBytes(Paths.get(jsonFileName));
			String jsonString = new String(encoded,  StandardCharsets.UTF_8);
			JsonFileStructure parsedJson = gson.fromJson(jsonString, JsonFileStructure.class);
			ShoeStorageInfo[] newShoes = new ShoeStorageInfo[parsedJson.initialStorage.size()];
			for(int i = 0; i < newShoes.length; i++){
				newShoes[i] = new ShoeStorageInfo(parsedJson.initialStorage.get(i).shoeType, parsedJson.initialStorage.get(i).amount, 0);
				
			}
			Store.getInstance().load(newShoes);
			
			CountDownLatch latchObject = new CountDownLatch (1+parsedJson.services.factories+parsedJson.services.sellers+parsedJson.services.customers.size());
			
			ShoeFactoryService[] factories = new ShoeFactoryService[parsedJson.services.factories];
			for(int i = 0; i < parsedJson.services.factories; i++){
				factories[i] = new ShoeFactoryService(i, latchObject);
			}
			
			SellingService[] sellers = new SellingService[parsedJson.services.sellers];
			for(int i = 0; i < parsedJson.services.sellers; i++){
				sellers[i] = new SellingService(i, latchObject);
			}
			
			List<DiscountSchedule> discountSchedule = new LinkedList<DiscountSchedule>(); 
			for(DiscountScheduleItem discountScheduleItem : parsedJson.services.manager.discountSchedule){
				discountSchedule.add(new DiscountSchedule(discountScheduleItem.shoeType,discountScheduleItem.tick,discountScheduleItem.amount));
			}
			ManagementService manager = new ManagementService(discountSchedule,latchObject);
			
			TimeService time = new TimeService(parsedJson.services.time.speed, parsedJson.services.time.duration,latchObject);
			
			WebsiteClientService[] customers = new WebsiteClientService[parsedJson.services.customers.size()];
			for(int i = 0; i < parsedJson.services.customers.size(); i++){
				List<PurchaseSchedule> purchaseSchedule = new LinkedList<PurchaseSchedule>(); 
				for(PurchaseScheduleItem purchaseScheduleItem : parsedJson.services.customers.get(i).purchaseSchedule){
					purchaseSchedule.add(new PurchaseSchedule(purchaseScheduleItem.shoeType,purchaseScheduleItem.tick));
				}
				customers[i] = new WebsiteClientService(parsedJson.services.customers.get(i).name, purchaseSchedule, new LinkedHashSet<String>(parsedJson.services.customers.get(i).wishList), latchObject);
			}
			
			LOGGER.info("Creating objects from json comleted");
			LOGGER.info("Creating threads");
			
			// Starting all threads
			Thread timeThread = new Thread(time);
			Thread managerThread = new Thread(manager);
			Thread[] factoriesThreads = new Thread[factories.length];
			for(int i = 0; i < factoriesThreads.length; i++){
				factoriesThreads[i] = new Thread(factories[i]);
			}
			Thread[] sellingServiceThread = new Thread[sellers.length];
			for(int i = 0; i < sellers.length; i++){
				sellingServiceThread[i] = new Thread(sellers[i]);
			}
			
			Thread[] customersThread = new Thread[customers.length];
			for(int i = 0; i < customers.length; i++){
				customersThread[i] = new Thread(customers[i]);
			}
			
			LOGGER.info("Starting threads");
			managerThread.start();
			for(int i = 0; i < factoriesThreads.length; i++){
				factoriesThreads[i].start();
			}
			for(int i = 0; i < sellingServiceThread.length; i++){
				sellingServiceThread[i].start();
			}
			for(int i = 0; i < customersThread.length; i++){
				customersThread[i].start();
			}
			
			timeThread.start();
			
			// Starting all threads
			LOGGER.info("Threads started succesfully");
			try{
				managerThread.join();
				for(int i = 0; i < factoriesThreads.length; i++){
					factoriesThreads[i].join();
				}
				for(int i = 0; i < sellingServiceThread.length; i++){
					sellingServiceThread[i].join();
				}
				for(int i = 0; i < customersThread.length; i++){
					customersThread[i].join();
				}
				
				timeThread.join();
	
			}
			catch(InterruptedException e){
				LOGGER.info("Exception in thread " + e);
			}
				
			LOGGER.info("Threads terminated");
			
			Store.getInstance().print();
			
			LOGGER.info("Program terminated successfully");
			
		}
		catch(IOException e)
		{
			  LOGGER.severe("Error parsing JSON file");
			  return;
		}
		
	}

}


