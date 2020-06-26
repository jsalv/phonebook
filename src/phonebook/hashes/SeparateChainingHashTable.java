package phonebook.hashes;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.KVPairList;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**<p>{@link SeparateChainingHashTable} is a {@link HashTable} that implements <b>Separate Chaining</b>
 * as its collision resolution strategy, i.e the collision chains are implemented as actual
 * Linked Lists. These Linked Lists are <b>not assumed ordered</b>. It is the easiest and most &quot; natural &quot; way to
 * implement a hash table and is useful for estimating hash function quality. In practice, it would
 * <b>not</b> be the best way to implement a hash table, because of the wasted space for the heads of the lists.
 * Open Addressing methods, like those implemented in {@link LinearProbingHashTable} and {@link QuadraticProbingHashTable}
 * are more desirable in practice, since they use the original space of the table for the collision chains themselves.</p>
 *
 * @author Jemimah E.P. Salvacion
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see LinearProbingHashTable
 * @see OrderedLinearProbingHashTable
 * @see CollisionResolver
 */
public class SeparateChainingHashTable implements HashTable{

    /* ****************************************************************** */
    /* ***** PRIVATE FIELDS / METHODS PROVIDED TO YOU: DO NOT EDIT! ***** */
    /* ****************************************************************** */

    private KVPairList[] table;
    private KVPair[][] table2d;
    private int count;
    private PrimeGenerator primeGenerator;

    // We mask the top bit of the default hashCode() to filter away negative values.
    // Have to copy over the implementation from OpenAddressingHashTable; no biggie.
    private int hash(String key){
        return (key.hashCode() & 0x7fffffff) % table.length;
    }

    /* **************************************** */
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  */
    /* **************************************** */
    /**
     *  Default constructor. Initializes the internal storage with a size equal to the default of {@link PrimeGenerator}.
     */
    public SeparateChainingHashTable(){
    	count = 0;  	
    	primeGenerator = new PrimeGenerator();
    	table = new KVPairList[primeGenerator.getCurrPrime()];
    	table2d = new KVPair[primeGenerator.getCurrPrime()][new PrimeGenerator().getNextPrime()];
    }

    @Override
    public Probes put(String key, String value) {
    	if (key == null || value == null)
    		throw new IllegalArgumentException("key or value input cannot be null!"); 
    	/* Create a hash table of hash tables where second level of hash is greater
		 * in length and still prime because prime numbers enable a higher chance of an even 
		 * distribution of elements and a larger array size would avoid an entry 
		 * having multiple elements
		 * */
    	int probeCount = 0; 
    	int bucketDex = hash(key);

    	// Array is empty or bucketDex is unoccupied 	
    	if (table[bucketDex] == null) {   		
    		probeCount++;
    		table[bucketDex] = new KVPairList(key, value);
    		
    	// bucketDex is occupied
    	} else {
    		// Basic chaining
    		table[bucketDex].addBack(key, value);
    		probeCount++;
    		  		
    	}
    	table2d[bucketDex][(key.hashCode() & 0x7fffffff) % table2d[bucketDex].length] = new KVPair(key, value);
    	count++; 
    	Probes p = new Probes(value,probeCount);
    	return p;
    }

    @Override
    public Probes get(String key) {  
    	if (key != null) {
    		int x = hash(key);
        	int y = (key.hashCode() & 0x7fffffff) % table2d[x].length;
        	return table[x].getValue(key);
    	}
    	return new Probes(null,0);
    }

    @Override
    public Probes remove(String key) {
    	if (key != null) {
    		int x = hash(key);
        	int y = (key.hashCode() & 0x7fffffff) % table2d[x].length;
        	Probes prevProbe = table[x].getValue(key);
	    	// Case 1: If element exists
	    	if (table2d[x][y] != null && table2d[x][y].getKey() == key) {
	    		table2d[x][y] = null;
	    		KVPairList newTable = new KVPairList();
	    		for (KVPair kv : table[x]) {
	    			if (kv.getKey().equals(key) == false)
	    				newTable.addBack(kv.getKey(), kv.getValue());
	    		}
	    		table[x] = newTable;
	    	}    	
	    	return prevProbe;
    	}
    	return new Probes(null,0);
    }

    @Override
    public boolean containsKey(String key) {
    	int x = hash(key);
    	int y = (key.hashCode() & 0x7fffffff) % table2d[x].length;
    	return !(table2d[x][y] == null);
    }

    @Override
    public boolean containsValue(String value) {
    	for (int i = 0; i < table.length; i++) {
    		if (table[i].containsValue(value) == true)
    			return true;
    	}
    	return false;  	
    }

    @Override
    public int size() {
    	return count;
    }

    @Override
    public int capacity() {
        return table.length; // Or the value of the current prime.
    }

    /**
     * Enlarges this hash table. At the very minimum, this method should increase the <b>capacity</b> of the hash table and ensure
     * that the new size is prime. The class {@link PrimeGenerator} implements the enlargement heuristic that
     * we have talked about in class and can be used as a black box if you wish.
     * @see PrimeGenerator#getNextPrime()
     */
    public void enlarge() {
    	count = 0;
        KVPairList[] prev = table;;        
        table = new KVPairList[primeGenerator.getNextPrime()];        
        table2d  = new KVPair[table.length][primeGenerator.getNextPrime()];
        for (int i = 0; i < prev.length; i++) {
        	if (prev[i] != null) {
        		for (KVPair kv : prev[i]) {
            		put(kv.getKey(), kv.getValue());
            	}  	
        	}
        	
        }
        
     }

    /**
     * Shrinks this hash table. At the very minimum, this method should decrease the size of the hash table and ensure
     * that the new size is prime. The class {@link PrimeGenerator} implements the shrinking heuristic that
     * we have talked about in class and can be used as a black box if you wish.
     *
     * @see PrimeGenerator#getPreviousPrime()
     */
    public void shrink(){
    	count = 0;
        KVPairList[] prev = table;;        
        table = new KVPairList[primeGenerator.getPreviousPrime()];        
        table2d  = new KVPair[table.length][prev.length];
        for (int i = 0; i < prev.length; i++) {
        	if (prev[i] != null) {
        		for (KVPair kv : prev[i]) {
            		put(kv.getKey(), kv.getValue());
            	}  	
        	}
        	
        }
    }
}
