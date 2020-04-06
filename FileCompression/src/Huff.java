import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;


public class Huff implements IHuffConstants, ITreeMaker, IHuffEncoder, IHuffHeader, IHuffModel {
	
	private HuffTree htree;
	private HashMap<Integer, String> charEncoding;
	private HashMap<Integer, Integer> charFreq;
	private int headerSize;
	
	public HashMap<Integer, Integer> getCharFreq(){
		return charFreq;
	}
	
	public void setCharFreq(HashMap<Integer, Integer> map) {
		charFreq = map;
	}
	@Override
	/**
     * The number of bits in the header using the implementation, including
     * the magic number presumably stored.
     * @return the number of bits in the header
     */
	public int headerSize() {
		return headerSize;
	}
	
	public void setHeaderSize(int sizeOfHeader) {
		headerSize = sizeOfHeader;
	}
	public HashMap<Integer, String> getCharEncoding(){
		return charEncoding;
	}

	public HuffTree getHuffTree() {
		return htree;
	}
	
	public void setHuffTree(HuffTree h) {
		htree = h;
	}

	public boolean compressedFileSmaller() {
		int bitsRegular = 0;
		int bitsCompressed = 0;
		for(Integer key : getCharFreq().keySet()) {
			bitsRegular += BITS_PER_WORD * getCharFreq().get(key);
		}
		for(Integer key : getCharFreq().keySet()) {
			bitsCompressed += getCharFreq().get(key) * getCharEncoding().get(key).length();
		}
		bitsCompressed += BITS_PER_INT;
		bitsCompressed += htree.size();
		return bitsCompressed < bitsRegular ? true : false;
	}
	
	
	@Override
	/**
     * Write a compressed version of the data read by the InputStream parameter,
     * -- if the stream is not the same as the stream last passed to initialize,
     * then compression won't be optimal, but will still work. If force is
     * false, compression only occurs if it saves space. If force is true
     * compression results even if no bits are saved.
     * 
     * @param inFile is the input stream to be compressed
     * @param outFile   specifies the OutputStream/file to be written with compressed data
     * @param force  indicates if compression forced
     * @return the size of the compressed file or -1 if error ocurred 
     */
	public int write(String inFile, String outFile, boolean force) {

		InputStream is = null;
		BitOutputStream os = null;
		int bitsWritten = 0;
	
		try {
			is = new FileInputStream(inFile);		//create new fileinputstream to pass to makeHuff
			makeHuffTree(is);						//makeHuffTree in order to make table
			makeTable();								//make table of encodings
			showCounts();
			is.close();								//close the input stream
			if(!(compressedFileSmaller() || force)) return 0; //if compressed is bigger and not force 
			int ch;
			is = new FileInputStream(inFile);	//create new one to read in all chars & write their encodings
			os = new BitOutputStream(outFile);		//create file output stream to write to
			bitsWritten += writeHeader(os);						//writes magic number and info to rebuild tree
			while((ch = is.read()) != -1) {					//while still chars to read
				String chEncoding = getCharEncoding().get(ch);	//get encoding for char read in
				int encodingAsInt = Integer.parseInt(chEncoding, 2);	//convert encoding to decimal number 
				os.write(chEncoding.length(), encodingAsInt);			//write out that decimal number bit by bit
				bitsWritten += chEncoding.length();					//increment num of bits written
			}
			//write Psesuo EOF encoding
			String pseudoEncoding = getCharEncoding().get(PSEUDO_EOF);	//get encoding for PSEUDO_EOF
			int pseudoEncodingAsInt = Integer.parseInt(pseudoEncoding, 2);	//convert to decimal num
			os.write(pseudoEncoding.length(), pseudoEncodingAsInt);	//write out PSUEDO_EOF encoding bit by bit
			bitsWritten += pseudoEncoding.length();				//increment num bits written
			os.close();
			is.close();
			return bitsWritten;	
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return -1; 									//in case it didn't go smoothly and didn't catch an exception
	}

	@Override
	/**
     * Write the header, including magic number and all bits needed to
     * reconstruct a tree, e.g., using <code>readHeader</code>.
     * @param out is where the header is written
     * @return the size of the header
     */
	public int writeHeader(BitOutputStream out) {
		int sizeHeader = 0; 
		out.write(BITS_PER_INT, MAGIC_NUMBER);
		sizeHeader += BITS_PER_INT;		
		sizeHeader += writeHeaderHelp(out, htree.root());
		setHeaderSize(sizeHeader);
		return sizeHeader;
	}
	
	private int writeHeaderHelp(BitOutputStream out, IHuffBaseNode node) {
		int sizeHeader = 0;
		if(node.isLeaf()) {
			out.write(1, 1);
			sizeHeader ++;
			out.write(BITS_PER_WORD + 1, ((HuffLeafNode) node).element());
			sizeHeader += BITS_PER_WORD + 1;
			return sizeHeader;
		}
		out.write(1, 0);
		sizeHeader++;
		sizeHeader += writeHeaderHelp(out, ((HuffInternalNode) node).left());
		sizeHeader += writeHeaderHelp(out, ((HuffInternalNode) node).right());
		return sizeHeader;
	} 
	
	@Override
	/**
     * Uncompress a previously compressed file.
     * 
     * @param inFile  is the compressed file to be uncompressed
     * @param outFile is where the uncompressed bits will be written
     * @return the size of the uncompressed file
     */
	public int uncompress(String inFile, String outFile) {
		
		BitInputStream is = null; //bitinputstream
		BitOutputStream os = null; //file output stream
		int sizeUncompressed = 0;
		
		try {
			is = new BitInputStream(inFile);
			os = new BitOutputStream(outFile);
			HuffTree referenceTree = readHeader(is);
			IHuffBaseNode node = referenceTree.root();
			
			while(true) {								//keep going until we reach PSUEDO_EOF
				
				while(!node.isLeaf()) {					//while we havent reached a leaf		
					if(is.read(1) == 0) {				//if next bit is 0, go to left child
						node = ((HuffInternalNode) node).left();	
					}
					else {								//if next bit is 1, go to right child
						node = ((HuffInternalNode) node).right();
					}
				}
				//once we reach leaf, check if it is PSEUDO_EOF
				if(((HuffLeafNode) node).element() == PSEUDO_EOF) { //if so, end the method, return bits written
					is.close();
					os.close();
					return sizeUncompressed;
				}
				os.write(BITS_PER_WORD, ((HuffLeafNode) node).element()); //else, write the char to out File
				sizeUncompressed += BITS_PER_WORD;				//increment size by 8 bits
				node = referenceTree.root();					//reset our starting node to root of tree
			}			
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}		
		return -1;			//if something went wrong and no Exception caught
	}
	
	@Override
	/**
     * Read the header and return an ITreeMaker object corresponding to
     * the information/header read.
     * @param in is source of bits for header
     * @return an ITreeMaker object representing the tree stored in the header
     * @throws IOException if the header is bad, e.g., wrong MAGIC_NUMBER, wrong
     * number of bits, I/O error occurs reading
     */
	public HuffTree readHeader(BitInputStream in) throws IOException {
		if(in.read(BITS_PER_INT) != MAGIC_NUMBER) {
			throw new IOException("Magic Number is wrong!!\n");
		}
		return recreateTree(in);
	}
	
	private HuffTree recreateTree(BitInputStream in) throws IOException{
		if(in.read(1) == 1) {										
			return new HuffTree(in.read(9), 0);
		}
		HuffTree leftChild = recreateTree(in);
		HuffTree rightChild = recreateTree(in);		
		return new HuffTree(leftChild.root(), rightChild.root(), 0);
	}


	@Override
	/**
     * Initialize state from a tree, the tree is obtained
     * from the treeMaker.
     * @return the map of chars/encoding
     */
	public Map<Integer, String> makeTable() {
		 charEncoding = new HashMap<Integer, String>();		 
		 makeTableHelp(htree.root(), "");		 
		 return charEncoding;			
		 
	}
	
	private String makeTableHelp(IHuffBaseNode h, String coding) {
		if(h.isLeaf()) {
			charEncoding.put(((HuffLeafNode) h).element(), coding);
			return "";	
		}
		makeTableHelp(((HuffInternalNode) h).left(), coding + "0");
		makeTableHelp(((HuffInternalNode) h).right(), coding + "1");
		return "";
		
	}

	@Override
	 /**
     * Returns coding, e.g., "010111" for specified chunk/character. It
     * is an error to call this method before makeTable has been
     * called.
     * @param i is the chunk for which the coding is returned
     * @return the huff encoding for the specified chunk
     */
	public String getCode(int i) {		
		return charEncoding.get(i);
	}

	@Override
	/**
     * @return a map of all characters and their frequency
     */
	//traverse our tree and print the element and weight of each leaf node
	public Map<Integer, Integer> showCounts() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		map = showCountsHelp(htree.root(), map);
		setCharFreq(map);
		return map;
		
	}
	
	private HashMap<Integer, Integer> showCountsHelp(IHuffBaseNode n, HashMap<Integer, Integer> map) {
		if(n.isLeaf()) {
			char nAsChar = (char) ((HuffLeafNode) n).element();
			System.out.println("Char: " + nAsChar + " Frequency: " + ((HuffLeafNode) n).weight());
			map.put(((HuffLeafNode) n).element(), ((HuffLeafNode) n).weight());
			return map;
		}
		showCountsHelp(((HuffInternalNode) n).left(), map);
		showCountsHelp(((HuffInternalNode) n).right(), map);
		return map;
	}

	@Override
	/**
     * Return the  Huffman/coding tree.
     * @return the Huffman tree
     */
	
	public HuffTree makeHuffTree(InputStream stream) throws IOException {
		CharCounter cc = new CharCounter();
		try {
		cc.countAll(stream);												//makes map of char:freq
		}catch(IOException e) {
			throw new IOException("Error reading in stream in makeHuff, i.e. from cc.countAll\n");
		}
		
		//create priority queue with initial capacity = number of chars found in stream
		PriorityQueue<HuffTree> pq = new PriorityQueue<HuffTree>(cc.getCharFreq().keySet().size());
		for(int key : cc.getCharFreq().keySet()) {
			HuffTree charFreqNode = new HuffTree(key, cc.getCount(key));	//create huff tree for each char"freq pair
			pq.add(charFreqNode);
		}
		pq.add(new HuffTree(PSEUDO_EOF, 1)); 							//add pseudo EOF node to priority queue
		return makeHuffTreeHelp(pq);
	}	
	
	//create HuffTree from priority queue
	private HuffTree makeHuffTreeHelp(PriorityQueue<HuffTree> pq) {																	
		while(pq.size() > 1) {											//while at least two elements left
			HuffTree tmp1 = pq.poll();
			HuffTree tmp2 = pq.poll();
			if(tmp1.root().isLeaf()) {				//ensures all leaves on left and all internal nodes on right...to visualize
				HuffTree tmp3 = new HuffTree(tmp1.root(), tmp2.root(), tmp1.weight() + tmp2.weight()); 
				pq.add(tmp3);
			}
			else {
				HuffTree tmp3 = new HuffTree(tmp2.root(), tmp1.root(), tmp1.weight() + tmp2.weight());
				pq.add(tmp3);
			}
		}
		HuffTree finalHuffTree = pq.poll(); 						//set huff tree to the tree we just built
		setHuffTree(finalHuffTree);
		return finalHuffTree; 									//polls the only remaining elements which should have max weight
	}
		

}
