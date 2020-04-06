import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.*;


public class CharCounter implements ICharCounter, IHuffConstants{
	
	private HashMap<Integer, Integer> charFreq = new HashMap<Integer, Integer>();
	private int chunks;
	
	public int  getChunks() {
		return chunks;
	}
	
	public void setChunks(int countChunks) {
		chunks = countChunks;
	}
	
	public HashMap<Integer, Integer> getCharFreq(){
		return charFreq;
	}
	
	/**
	 * Returns the count associated with specified character.
	 * @param ch is the chunk/character for which count is requested
	 * @return count of specified chunk
	 * @throws the appropriate exception if ch isn't a valid chunk/character
	 */
	//check if mapping returns null, if not, then return count
	public int getCount(int ch) {		
		if(getCharFreq().get(ch) == null) {
			throw new IllegalArgumentException("Not a valid character passed to get count!\n");
		}
		return getCharFreq().get(ch);
	}

	@Override
	/**
	 * Initialize state by counting bits/chunks in a stream
	 * @param stream is source of data
	 * @return count of all chunks/read
	 * @throws IOException if reading fails
	 */
	//can make compatible here for chars of different length BITS_PER_WORD
	public int countAll(InputStream stream) throws IOException  {
		int countChunks = 0;									//keep track of all chunks read
		try {	
			int ch;											//stores byte read each iteration
			while((ch = stream.read()) != -1) {				//while input stream not at end
				add(ch);
				countChunks++;								//increment tracker
			}
			//set(PSEUDO_EOF, 1);
		}
		catch(IOException e) {								//in case read() throws exception
			System.out.println("Error reading in method countALL!");
			e.printStackTrace();
		}	
		setChunks(countChunks);
		return countChunks;
	}

	@Override
	/**
	 * Update state to record one occurrence of specified chunk/character.
	 * @param i is the chunk being recorded
	 */
	public void add(int i) {
		if(getCharFreq().get(i) == null) {			//if mapping doesn't exist
			getCharFreq().put(i,  1);					//create one with value 1
		}
		else {
			getCharFreq().put(i, getCharFreq().get(i)+1);	//else, increment value in mapping
		}
	}

	@Override
	/**
	 * Set the value/count associated with a specific character/chunk.
	 * @param i is the chunk/character whose count is specified
	 * @param value is # occurrences of specified chunk
	 */
	public void set(int i, int value) {
		getCharFreq().put(i, value);			//simply create a mapping with this (key, value)
	}									//any old mapping will be updated

	@Override
	/**
	 * All counts cleared to zero.
	 */
	public void clear() {				//loop through all keys and set their values to 0
		for(int key : getCharFreq().keySet()) {
			getCharFreq().put(key,  0);
		}
	}

	@Override
	/*
	 * @return a map of all characters and their frequency
	 */
	public Map<Integer, Integer> getTable() {
		return getCharFreq();						//return mapping created by countAll()
	}

}


