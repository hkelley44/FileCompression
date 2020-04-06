import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.junit.Test;

public class HuffTest {

	@Test
	public void testWrite() {
		Huff test = new Huff();
		assertEquals(88, test.write("InFile", "OutFile", true));
		assertEquals(0, test.write("InFile", "OutFile", false));
	}
	
	@Test
	public void testWriteHeader() {
		Huff test = new Huff();
		InputStream is = null;
		BitOutputStream os = null;
		
		try {
			is = new FileInputStream("InFile");		//create new fileinputstream to pass to makeHuff
			test.makeHuffTree(is);						//makeHuffTree in order to make table
			test.makeTable();								//make table of encodings
			test.showCounts();
			is.close();								//close the input stream 
			is = new FileInputStream("InFile");	//create new one to read in all chars & write their encodings
			os = new BitOutputStream("OutFile");		//create file output stream to write to
		}catch(Exception e) {
			e.printStackTrace();
		}
			assertEquals(75, test.writeHeader(os));	
	}
	
	@Test
	public void testUncompress() {
		Huff test = new Huff();
		test.write("InFile", "OutFile", true);
		assertEquals(48, test.uncompress("OutFile", "OutFileUnzip"));
	}
	
	@Test
	public void testReadHeader() {
		HuffLeafNode a = new HuffLeafNode(97, 3);
		HuffLeafNode b = new HuffLeafNode(98, 2);
		HuffLeafNode c = new HuffLeafNode(99, 1);
		HuffLeafNode p = new HuffLeafNode(256, 1);
		HuffInternalNode z = new HuffInternalNode(c, p, 4);
		HuffInternalNode y = new HuffInternalNode(b, z, 5);
		HuffTree myTree = new HuffTree(a, y, 10);
		Huff test = new Huff();
		test.write("InFile", "OutFile", true);
		BitInputStream is = null; //bitinputstream
		HuffTree referenceTree = null;
		try {
			is = new BitInputStream("OutFile");
			referenceTree = test.readHeader(is);
		}catch(Exception e) {
			e.printStackTrace();
		}		
		int counts = testReadHeaderHelp(myTree.root(), referenceTree.root());
		System.out.println("Counts is " + counts);
		assertEquals(4, counts);
		
		try {
			is = new BitInputStream("BadCompress");
			referenceTree = test.readHeader(is);
		}catch(IOException e) {
			System.out.println("IOException caught as expected");
		}		
		
	}
	private int testReadHeaderHelp(IHuffBaseNode expected, IHuffBaseNode actual) {
		int counts = 0;
		if(expected.isLeaf() && actual.isLeaf()) {
			assertEquals(((HuffLeafNode) expected).element(), ((HuffLeafNode) actual).element());
			counts++;
			return counts;
		}
		return testReadHeaderHelp(((HuffInternalNode) expected).left(), ((HuffInternalNode) actual).left()) +
		testReadHeaderHelp(((HuffInternalNode) expected).right(), ((HuffInternalNode) actual).right());
	}

	@Test
	public void testHeaderSize() {
		Huff test = new Huff();
		InputStream is = null;
		BitOutputStream os = null;
		
		try {
			is = new FileInputStream("InFile");		//create new fileinputstream to pass to makeHuff
			test.makeHuffTree(is);						//makeHuffTree in order to make table
			test.makeTable();								//make table of encodings
			test.showCounts();
			is.close();								//close the input stream 
			is = new FileInputStream("InFile");	//create new one to read in all chars & write their encodings
			os = new BitOutputStream("OutFile");		//create file output stream to write to
		}catch(Exception e) {
			e.printStackTrace();
		}
		test.writeHeader(os);
		assertEquals(75, test.headerSize());
	}
	
	@Test
	public void testMakeTable() {
		Huff h = new Huff();
		InputStream is = null;
		try {
			is = new ByteArrayInputStream("aaabbc".getBytes("UTF-8"));
			h.makeHuffTree(is);						//makeHuffTree in order to make table
			h.makeTable();	
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		assertEquals("0", h.getCharEncoding().get(97));
		assertEquals("10", h.getCharEncoding().get(98));
		assertEquals("110", h.getCharEncoding().get(99));
		assertEquals("111", h.getCharEncoding().get(256));
		assertTrue(h.getCharEncoding().keySet().contains(97));
		assertTrue(h.getCharEncoding().keySet().contains(98));
		assertTrue(h.getCharEncoding().keySet().contains(99));
		assertTrue(h.getCharEncoding().keySet().contains(256));
	}
	
	@Test
	public void testGetCode() {
		Huff h = new Huff();
		InputStream is = null;
		try {
			is = new ByteArrayInputStream("aaabbc".getBytes("UTF-8"));
			h.makeHuffTree(is);						//makeHuffTree in order to make table
			h.makeTable();	
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		assertEquals("0", h.getCode(97));
		assertEquals("10", h.getCode(98));
		assertEquals("110", h.getCode(99));
		assertEquals("111", h.getCode(256));		
		assertEquals(h.getCode(100), null);
		
	}

	@Test
	public void testShowCounts() {
		HashMap<Integer, Integer> hmap = new HashMap<Integer, Integer>();
		hmap.put(97, 3);
		hmap.put(98, 2);
		hmap.put(99, 1);
		hmap.put(256, 1);
		Huff h = new Huff();
		InputStream is = null;
		try {
			is = new ByteArrayInputStream("aaabbc".getBytes("UTF-8"));
			h.makeHuffTree(is);						//makeHuffTree in order to make table
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		h.showCounts();
		assertEquals(hmap.keySet(), h.getCharFreq().keySet());
		for(Integer key : hmap.keySet()) {
			assertEquals(hmap.get(key), h.getCharFreq().get(key));
		}
		
	}
	
}
