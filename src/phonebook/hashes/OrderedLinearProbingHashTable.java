package phonebook.hashes;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**
 * <p>{@link OrderedLinearProbingHashTable} is an Openly Addressed {@link HashTable} implemented with
 * <b>Ordered Linear Probing</b> as its collision resolution strategy: every key collision is resolved by moving
 * one address over, and the keys in the chain is in order. It suffer from the &quot; clustering &quot; problem:
 * collision resolutions tend to cluster collision chains locally, making it hard for new keys to be
 * inserted without collisions. {@link QuadraticProbingHashTable} is a {@link HashTable} that
 * tries to avoid this problem, albeit sacrificing cache locality.</p>
 *
 * @author YOUR NAME HERE!
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see LinearProbingHashTable
 * @see QuadraticProbingHashTable
 * @see CollisionResolver
 */
public class OrderedLinearProbingHashTable extends OpenAddressingHashTable {

    /* ********************************************************************/
    /* ** INSERT ANY PRIVATE METHODS OR FIELDS YOU WANT TO USE HERE: ******/
    /* ********************************************************************/

    /* ******************************************/
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS: */
    /* **************************************** */

    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *               we want soft deletion, {@code false} otherwise.
     */
    public OrderedLinearProbingHashTable(boolean soft){
    	primeGenerator = new PrimeGenerator();
		table = new KVPair[this.primeGenerator.getCurrPrime()];
		count = 0;
		softFlag = soft;
    }



    /**
     * Inserts the pair &lt;key, value&gt; into this. The container should <b>not</b> allow for {@code null}
     * keys and values, and we <b>will</b> test if you are throwing a {@link IllegalArgumentException} from your code
     * if this method is given {@code null} arguments! It is important that we establish that no {@code null} entries
     * can exist in our database because the semantics of {@link #get(String)} and {@link #remove(String)} are that they
     * return {@code null} if, and only if, their key parameter is {@code null}. This method is expected to run in <em>amortized
     * constant time</em>.
     *
     * Different from {@link LinearProbingHashTable}, the keys in the chain are <b>in order</b>. As a result, we might increase
     * the cost of insertion and reduce the cost on search miss. One thing to notice is that, in soft deletion, we ignore
     * the tombstone during the reordering of the keys in the chain. We will have some example in the writeup.
     *
     * Instances of {@link OrderedLinearProbingHashTable} will follow the writeup's guidelines about how to internally resize
     * the hash table when the capacity exceeds 50&#37;
     * @param key The record's key.
     * @param value The record's value.
     * @throws IllegalArgumentException if either argument is {@code null}.
     * @return The {@link phonebook.utils.Probes} with the value added and the number of probes it makes.
     */
    @Override
    public Probes put(String key, String value) {
    	if (key == null || value == null)
    		throw new IllegalArgumentException("key or value input cannot be null!");  	
    	int probeCount = 1;
    	/* * * CHECK FOR RESIZING * * */
    	if (count > table.length/2) {
    		KVPair[] prev = table;
    		count = 0;
    		table = new KVPair[primeGenerator.getNextPrime()];
    		for (int x = 0; x < prev.length; x++) {
    			probeCount++;
    			if (prev[x] != null && prev[x].equals(TOMBSTONE) == false) 
    				probeCount += put(prev[x].getKey(), prev[x].getValue()).getProbes();
    		}
    	}
    	int bucketDex = hash(key);
    	// Case 1: Array is empty
    	if (table[bucketDex] == null || table[bucketDex].equals(TOMBSTONE)) {
    		table[bucketDex] = new KVPair(key, value);
    		// Case 2: bucketDex is occupied
    	} else {
    		int i = bucketDex + 1;
    		String temp = key;
    		while (i < table.length) {
    			probeCount++;
    			// Check order of elements
    			if (table[i-1].getKey().compareTo(temp) > 0) {
    				temp = table[i-1].getKey();
    				table[i-1].setKey(key);
    				table[i-1].setValue(value);
    				if (table[i] == null || table[bucketDex].equals(TOMBSTONE)) {
        				table[i] = new KVPair(key, value);
        				break;
        			}
    			} else {  			
    				if (table[i] == null || table[bucketDex].equals(TOMBSTONE)) {
        				table[i] = new KVPair(key, value);
        				break;
        			}
    			}
    			i++;
    		}
    	}
    	count++;
    	return new Probes(value, probeCount);
    }

    @Override
    public Probes get(String key) {
    	if (key != null) {
			int x = hash(key);
			// Case 1: Key is found at hashed index
			if (table[x] != null && table[x].getKey() == key) {
				return new Probes(table[x].getValue(), 1);
				// Case 2: Cell is empty
			} else if (table[x] == null) {
				return new Probes(null,1);
				// Case 3: Key was possibly probed to the next non-empty cell or does not exist
			} else {
				int i = x;
				int probeCount = 0;
				String val = null;
				while (i%table.length < table.length) {				
					// Key doesn't exist
					if (table[i] == null)
						break;
					// Key exists
					else if (table[i] != null && table[i].getKey() == key) {
						val = table[i].getValue();
						break;
					}
					probeCount++;
					i++;
				}
				return new Probes(val, probeCount); 
			}
		}
		return new Probes(null,0);
    }


    /**
     * <b>Return</b> and <b>remove</b> the value associated with key in the {@link HashTable}. If key does not exist in the database
     * or if key = {@code null}, this method returns {@code null}. This method is expected to run in <em>amortized constant time</em>.
     *
     * @param key The key to search for.
     * @return The {@link phonebook.utils.Probes} with associated value and the number of probe used. If the key is {@code null}, return value {@code null}
     * and 0 as number of probes; if the key dones't exists in the database, return {@code null} and the number of probes used.
     */
    @Override
    public Probes remove(String key) {
    	if (key != null) {
			int x = hash(key);
			int probeCount = 1;
			String oldVal = null;
			/* ALGORITHM
			 * 1. Search for deleted element counting probes while doing so
			 * 2. If found, set deleted element to null (no probe count needed)
			 * 3. Reinsert subsequent elements until a null element is reached*/
			while (table[x % table.length] != null) {
				int index = x % table.length;
				// Check if table[x] equals target key
				if (table[index].getKey() == key) {
					oldVal = table[index].getValue();

					// Soft deletion
					if (softFlag) {
						table[index] = TOMBSTONE;
						break;
					} else {
						// Hard deletion
						table[index] = null;
						count--;
						// Index of next element (successor of deleted element)
						int j = index + 1;
						// Since while loop will break for null elements, we have to
						// take into account entries that are null firsthand, and
						// still need a probeCount
						if (table[j % table.length] == null) {
							probeCount++;
							break;
						}
						while (table[j % table.length] != null) {
							int k = j % table.length;
							KVPair toReInsert = table[k];
							table[k] = null;
							// Increment probeCount to reflect visitation of current element
							probeCount++;
							// Increment probeCount according to insertions
							probeCount += put(toReInsert.getKey(), toReInsert.getValue()).getProbes();
							j++;
						}
						probeCount++;
						// Get out of loop after re-insertion
						break;
					}
				}
				// Otherwise, increment probeCount to next entry and iterate through array linearly
				probeCount++;
				x++;			
			}
			return new Probes(oldVal, probeCount);
		}
		return null;
    }

    @Override
    public boolean containsKey(String key) {
    	return get(key).getValue() != null;
    }

    @Override
    public boolean containsValue(String value) {
    	for (int i = 0; i < table.length; i++) {
			if (table[i].getValue() == value)
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
        return table.length;
    }

}
