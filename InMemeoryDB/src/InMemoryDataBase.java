import java.io.*;
import java.util.*;

public class InMemoryDataBase {

	/*
	 * Use key, stack(value) for the dataBase base Storage, so it can be easily
	 * unset. Use valCount map to fast retrieve the "NUMEQUALTO" operation.
	 */
	private Map<String, Deque<String>> dataBase = new HashMap<>();
	private Map<String, Integer> valCount = new HashMap<>();

	// dbBlock and countBlock are stacks to snapshot original value if in
	// transaction blocks
	private Deque<Map<String, Deque<String>>> dbBlockStack = new LinkedList<>();
	private Deque<Map<String, Integer>> countBlockStack = new LinkedList<>();

	// Set function, put key-value pair into data base, snapshot modified key and
	// val if in transaction block
	public void set(String key, String val) {

		snapshotDB(key);
		snapshotCount(val);

		if (dataBase.containsKey(key)) {
			String oldVal = dataBase.get(key).peek();
			snapshotCount(oldVal);
			if (valCount.get(oldVal) == 1) {
				valCount.remove(oldVal);
			} else {
				valCount.put(oldVal, valCount.get(oldVal) - 1);
			}
			dataBase.get(key).push(val);
		} else {
			dataBase.put(key, new LinkedList<String>());
			dataBase.get(key).push(val);
		}

		if (valCount.containsKey(val)) {
			valCount.put(val, valCount.get(val) + 1);
		} else {
			valCount.put(val, 1);
		}
	}

	// get function retrieve the value from database
	public String get(String key) {
		if (dataBase.containsKey(key)) {
			return dataBase.get(key).peek();
		} else {
			return null;
		}
	}

	// unset key, snapshot affected key and val if in transaction block
	public void unSet(String key) {

		if (dataBase.containsKey(key)) {

			snapshotDB(key);
			String val = dataBase.get(key).pop();
			snapshotCount(val);

			if (valCount.get(val) == 1) {
				valCount.remove(val);
			} else {
				valCount.put(val, valCount.get(val) - 1);
			}

			if (dataBase.get(key).isEmpty()) {
				dataBase.remove(key);
			} else {
				String currVal = dataBase.get(key).peek();

				snapshotCount(currVal);

				if (valCount.containsKey(currVal)) {
					valCount.put(currVal, valCount.get(currVal) + 1);
				} else {
					valCount.put(currVal, 1);
				}
			}
		}
	}

	// Retrieve the number of values from valCount store
	public int numEqualTo(String val) {
		if (valCount.containsKey(val)) {
			return valCount.get(val);
		}
		return 0;
	}

	private void snapshotDB(String key) {
		if (dbBlockStack.isEmpty())
			return; // not in transaction block
		Map<String, Deque<String>> smallDB = dbBlockStack.peek();
		if (smallDB.containsKey(key))
			return; // snapshot already in previous command

		if (dataBase.containsKey(key)) {
			smallDB.put(key, new LinkedList<>(dataBase.get(key)));
		} else {
			smallDB.put(key, new LinkedList<>()); // put empty, when rollback,
													// empty val need to be
													// delete
		}
	}

	private void snapshotCount(String val) {
		if (countBlockStack.isEmpty())
			return;
		Map<String, Integer> smallCountDB = countBlockStack.peek();
		if (smallCountDB.containsKey(val))
			return;

		if (valCount.containsKey(val)) {
			smallCountDB.put(val, valCount.get(val));
		} else {
			smallCountDB.put(val, 0); // when rollback, 0 count need to be
										// delete
		}
	}

	private void rollBack() {
		if (dbBlockStack.isEmpty()) {
			System.out.println("NO TRANSACTION");
			return;
		}
		// Snapshot at "BEGIN", only recorded modified key and values
		Map<String, Deque<String>> smallDB = dbBlockStack.pop();
		Map<String, Integer> smallCountDB = countBlockStack.pop();

		// rollback modified keys
		for (String key : smallDB.keySet()) {
			if (smallDB.get(key).size() == 0) {
				dataBase.remove(key);
			} else {
				dataBase.put(key, smallDB.get(key));
			}
		}

		// rollback modified value counts
		for (String val : smallCountDB.keySet()) {
			if (smallCountDB.get(val) == 0) {
				valCount.remove(val);
			} else {
				valCount.put(val, smallCountDB.get(val));
			}
		}
	}

	private void execCommand(String command) {
		String[] strs = command.split(" ");
		if (strs.length == 0) {
			System.out.println("Invalid Command!");
			return;
		}
		switch (strs[0].toUpperCase()) {
		case "SET":
			if (strs.length < 3)
				break;
			set(strs[1], strs[2]);
			break;
		case "GET":
			if (strs.length < 2)
				break;
			String s = get(strs[1]);
			if (s == null)
				System.out.println("NULL");
			else
				System.out.println(get(strs[1]));
			break;
		case "UNSET":
			if (strs.length < 2)
				break;
			unSet(strs[1]);
			break;
		case "NUMEQUALTO":
			if (strs.length < 2)
				break;
			System.out.println(numEqualTo(strs[1]));
			break;
		default:
			break;
		}
	}

	public void execLine(String command) {
		switch (command.toUpperCase()) {
		case "BEGIN":
			dbBlockStack.push(new HashMap<>());
			countBlockStack.push(new HashMap<>());
			break;
		case "COMMIT":
			dbBlockStack.clear();
			countBlockStack.clear();
			break;
		case "ROLLBACK":
			rollBack();
			break;
		default:
			execCommand(command);
		}
	}

	public static void main(String args[]) throws Exception {
		/*
		 * Enter your code here. Read input from STDIN. Print output to STDOUT
		 */
		Scanner sc = new Scanner(System.in);
		InMemoryDataBase solution = new InMemoryDataBase();

		while (sc.hasNextLine()) {
			String command = sc.nextLine();
			command = command.trim();
			if (command.toUpperCase().equals("END"))
				break;
			solution.execLine(command);
		}

		sc.close();
	}
}