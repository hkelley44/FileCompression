import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import org.junit.Test;

public class CharCounterTest {

	@Test
	public void testCountAll() {
		ICharCounter cc = new CharCounter();
		InputStream ins = null;
		int chunks = 0;
		try {
			ins = new ByteArrayInputStream("aaabbc".getBytes("UTF-8"));
			chunks = cc.countAll(ins);
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertEquals(6, chunks); 
	}
	
	@Test
	public void testAdd() {
		CharCounter cc = new CharCounter();
		cc.add(55);
		assertEquals(1, (int) cc.getCharFreq().get(55));
		cc.add(55);
		assertEquals(2, (int) cc.getCharFreq().get(55));
	}
	
	@Test
	public void testGetCount() {
		ICharCounter cc = new CharCounter();
		InputStream ins = null;
		try {
			ins = new ByteArrayInputStream("aaabbc".getBytes("UTF-8"));
			cc.countAll(ins);
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertEquals(3, cc.getCount('a'));
		
		try {
			cc.getCount('d');
		}catch(IllegalArgumentException e) {
			//that's fine
		}
	}
		
	@Test
	public void testSet() {
		CharCounter cc = new CharCounter();
		cc.set(55, 5);
		assertEquals(5, (int) cc.getCharFreq().get(55));
	}
	
	@Test
	public void testClear() {
		CharCounter cc = new CharCounter();
		cc.add(55);
		cc.add(55);
		cc.add(55);
		cc.add(65);
		cc.add(65);
		cc.add(75);
		cc.clear();
		assertEquals(0, (int) cc.getCharFreq().get(55));
		assertEquals(0, (int) cc.getCharFreq().get(65));
		assertEquals(0, (int) cc.getCharFreq().get(75));
	}
	@Test
	public void testGetTable() {
		CharCounter cc = new CharCounter();
		cc.add(55);
		cc.add(55);
		cc.add(65);
		HashMap<Integer, Integer> test = new HashMap<Integer, Integer>();
		test.put(55,  2);
		test.put(65, 1);
		assertEquals(test.keySet(), cc.getCharFreq().keySet());
		for(Integer key : test.keySet()) {
			assertEquals(test.get(key), cc.getCharFreq().get(key));
		}
		
	}

}
