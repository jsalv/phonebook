package phonebook.hashes;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**
 * <p>{@link QuadraticProbingHashTable} is an Openly Addressed {@link HashTable} which uses <b>Quadratic
 * Probing</b> as its collision resolution strategy. Quadratic Probing differs from <b>Linear</b> Probing
 * in that collisions are resolved by taking &quot; jumps &quot; on the hash table, the length of which
 * determined by an increasing polynomial factor. For example, during a key insertion which generates
 * several collisions, the first collision will be resolved by moving 1^2 + 1 = 2 positions over from
 * the originally hashed address (like Linear Probing), the second one will be resolved by moving
 * 2^2 + 2= 6 positions over from our hashed address, the third one by moving 3^2 + 3 = 12 positions over, etc.
 * </p>
 *
 * <p>By using this collision resolution technique, {@link QuadraticProbingHashTable} aims to get rid of the
 * &quot;key clustering &quot; problem that {@link LinearProbingHashTable} suffers from. Leaving more
 * space in between memory probes allows other keys to be inserted without many collisions. The tradeoff
 * is that, in doing so, {@link QuadraticProbingHashTable} sacrifices <em>cache locality</em>.</p>
 *
 * @author YOUR NAME HERE!
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see LinearProbingHashTable
 * @see CollisionResolver
 */
public class QuadraticProbingHashTable extends OpenAddressingHashTable {

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
	public QuadraticProbingHashTable(boolean soft) {
		primeGenerator = new PrimeGenerator();
		table = new KVPair[this.primeGenerator.getCurrPrime()];
		count = 0;
		softFlag = soft;
	}

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
		int index = 1;
		int bucketDex = (hash(key) + (index - 1) + (int)Math.pow((index-1), 2)) % table.length;
		// Case 1: Array is empty
		if (table[bucketDex] == null || table[bucketDex].equals(TOMBSTONE)) {
			table[bucketDex] = new KVPair(key, value);
			// Case 2: bucketDex is occupied
		} else {
			index++;
			bucketDex = (hash(key) + (index - 1) + (int)Math.pow((index-1), 2)) % table.length;
			while (bucketDex < table.length) {
				probeCount++;
				if (table[bucketDex] == null || table[bucketDex].equals(TOMBSTONE)) {
					table[bucketDex] = new KVPair(key, value);
					break;
				}
				index++;
				bucketDex = (hash(key) + (index - 1) + (int)Math.pow((index-1), 2)) % table.length;
			}  		

		}
		count++;
		return new Probes(value, probeCount);
	}


	@Override
	public Probes get(String key) {
		if (key != null) {
			int index = 1;
			int x = (hash(key) + (index - 1) + (int)Math.pow((index-1), 2)) % table.length;
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
				while (i < table.length) {
					// probeCount needs to be in the beginning to signify cell visitation
					probeCount++;
					// Key doesn't exist
					if (table[i] == null)
						break;
					// Key exists
					else if (table[i] != null && table[i].getKey() == key) {
						val = table[i].getValue();
						break;
					}
					index++;				
					i = (hash(key) + (index - 1) + (int)Math.pow((index-1), 2)) % table.length;
				}
				return new Probes(val, probeCount); 
			}
		}
		return new Probes(null,0);
	}

	@Override
	public Probes remove(String key) {
		if (key != null) {
			int index = 1;
			int x = (hash(key) + (index - 1) + (int)Math.pow((index-1), 2)) % table.length;
			int probeCount = 1;
			String oldVal = null;
			/* ALGORITHM
			 * 1. Search for deleted element counting probes while doing so
			 * 2. If found, set deleted element to null (no probe count needed)
			 * 3. Reinsert subsequent elements until a null element is reached*/
			while (table[x % table.length] != null) {
				int i = x % table.length;
				// Check if table[x] equals target key
				if (table[i].getKey() == key) {
					oldVal = table[i].getValue();
					
					// Soft deletion
					if (softFlag) {
						table[i] = TOMBSTONE;
						break;
					} else {
						// Hard deletion
						table[i] = null;
						KVPair[] prev = table;
						table = new KVPair[primeGenerator.getCurrPrime()];
						count = 0;
						// Re-insert ALL elements just like resizing except same capacity
						int j = 0;
						while (j < table.length) {					
							probeCount++;
							if (prev[j] != null && prev[j].equals(TOMBSTONE)==false)
								probeCount += put(prev[j].getKey(), prev[j].getValue()).getProbes();
							j++;
						}
						break;
					}
				}
				// Otherwise, increment probeCount to next entry and iterate through array 
				probeCount++;
				index++;				
				x = (hash(key) + (index - 1) + (int)Math.pow((index-1), 2)) % table.length;
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
	public int size(){
		return count;
	}

	@Override
	public int capacity() {
		return table.length;
	}

}