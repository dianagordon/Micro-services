package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StoreTest {

	
	@Before
	public void setUp() throws Exception {
		Store.getInstance().cleanAll();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetInstance() {
		assertNotNull(Store.getInstance());
		
	}

	@Test
	public void testLoad() {
		ShoeStorageInfo[] shoeStorage = {new ShoeStorageInfo("shoe 1", 5, 0), new ShoeStorageInfo("shoe 2", 4, 0), new ShoeStorageInfo("shoe 3", 8, 0)};
		Store.getInstance().load(shoeStorage);
		ShoeStorageInfo[] shoesAfterLoad = Store.getInstance().getstorage();
		
		assertArrayEquals(shoeStorage, shoesAfterLoad);
	}

	@Test
	public void testTake() {
		ShoeStorageInfo[] shoeStorage = {new ShoeStorageInfo("shoe 1", 1, 0)};
		Store.getInstance().load(shoeStorage);
		Store.getInstance().take("shoe 1", false);
		ShoeStorageInfo[] shoesAfterLoad = Store.getInstance().getstorage();
		assertEquals(shoesAfterLoad[0].getAmountOnStorage(), 0);
	}

	@Test
	public void testAdd() {
		ShoeStorageInfo[] shoeStorage = {new ShoeStorageInfo("shoe 1", 1, 0)};
		Store.getInstance().load(shoeStorage);
		Store.getInstance().add("shoe 1", 1);
		ShoeStorageInfo[] shoesAfterLoad = Store.getInstance().getstorage();
		
		assertEquals(shoesAfterLoad[0].getAmountOnStorage(), 2);
		assertEquals(shoesAfterLoad[0].getShoeType(), "shoe 1");
	}

	@Test
	public void testAddDiscount() {
		ShoeStorageInfo[] shoeStorage = {new ShoeStorageInfo("shoe 1", 0, 1)};
		Store.getInstance().load(shoeStorage);
		Store.getInstance().add("shoe 1", 1);
		ShoeStorageInfo[] shoesAfterLoad = Store.getInstance().getstorage();
		
		assertEquals(shoesAfterLoad[0].getDiscountedAmount(), 1);
		assertEquals(shoesAfterLoad[0].getShoeType(), "shoe 1");
	}

	@Test
	public void testFile() {
		Receipt receipt = new Receipt("Idan", "Diana", "shoe 1", false, 5, 4, 1);
		Store.getInstance().file(receipt);
		Receipt[] savedReceipt = Store.getInstance().getReceipts();
		assertEquals(savedReceipt[0], receipt);
	}
}
