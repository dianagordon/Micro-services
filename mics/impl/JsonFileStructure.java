package bgu.spl.mics.impl;

import java.util.List;
import java.util.Set;
public class JsonFileStructure {
	
	List<ShoeTypeGeneral> initialStorage;
	Services services;	
	
	public class TimeInfo {
		int speed;
		int duration;
	}

	
	public class ShoeTypeGeneral {
		String shoeType;
		int amount;
	}
	
	public class DiscountScheduleItem {
		String shoeType;
		int amount;
		int tick;
	}
	
	public class PurchaseScheduleItem {
		String shoeType;
		int tick;
	}
	
	public class ManagerInfo {
		List<DiscountScheduleItem> discountSchedule;
	}
	
	public class CustomerInfo {
		String name;
		Set<String> wishList;
		List<PurchaseScheduleItem> purchaseSchedule;
	}
	
	public class Services {
		TimeInfo time;
		ManagerInfo manager;
		int factories;
		int sellers;
		List<CustomerInfo> customers;
	}

}
