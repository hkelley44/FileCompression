
public class main {

	//View test.txt before running this program
	//Once the program has run, there will be 2 new files: testCompressed.txt & testUncompressed.txt
	//The first is the compressed version of test.txt
	//The second is the uncompressed version of the compressed test.txt file

	public static void main(String[] args) {
		Huff h = new Huff();
		h.write("test", "testCompressed", true);
		h.uncompress("testCompressed", "testUncompressed");

	}

}
