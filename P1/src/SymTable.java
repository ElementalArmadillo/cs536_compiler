import java.util.*;

/**
 * @author mihirarora
 *
 * Class that models a symbol table object
 */
public class SymTable {
	private List<HashMap<String,Sym>> symTable; // list of HashMaps to store the symbol table
												// each HashMap stores symbols in a different scope
	
	/**
	 * Class constructor. Initializes the symbol table to a LinkedList of HashMaps containing 
	 * 	a single empty HashMap.
	 */
	public SymTable() {
		this.symTable = new LinkedList<HashMap<String,Sym>>();
		this.symTable.add(new HashMap<String,Sym>());
	}
	
	/**
	 * @param name
	 * @param sym
	 * @throws DuplicateSymException when name is already present in the HashMap at the head of the list
	 * @throws EmptySymTableException when the symbol table list is empty
	 * 
	 * Adds the name-symbol pair to the HashMap at the head of the list.
	 */
	public void addDecl(String name, Sym sym) throws DuplicateSymException, EmptySymTableException {
		if (this.symTable.isEmpty()) {
			throw new EmptySymTableException();
		}
		
		if (name == null || sym == null) {
			throw new IllegalArgumentException();
		}
		
		if (this.symTable.get(0).containsKey(name)) {
			throw new DuplicateSymException();
		} else {
			this.symTable.get(0).put(name, sym);
		}
	}
	
	/**
	 * Adds a new HashMap at the head of the list.
	 */
	public void addScope() {
		this.symTable.add(0, new HashMap<String,Sym>());
	}
	
	/**
	 * @param name
	 * @return Sym that matches the name passed, null if name not found
	 * @throws EmptySymTableException when the symbol table list is empty
	 * 
	 * Searches the HashMap at the head of the list for the name passed
	 */
	public Sym lookupLocal(String name) throws EmptySymTableException{
		if (this.symTable.isEmpty()) {
			throw new EmptySymTableException();
		}
		
		return this.symTable.get(0).get(name);
	}
	
	/**
	 * @param name
	 * @return Sym that matches the name passed, null if name not found
	 * @throws EmptySymTableException when the symbol table list is empty
	 * 
	 * Searches all of the HashMaps in the list for the name passed
	 */
	public Sym lookupGlobal(String name) throws EmptySymTableException{
		if (this.symTable.isEmpty()) {
			throw new EmptySymTableException();
		}
		
		for (int i = 0; i < this.symTable.size(); ++i) {
			if (this.symTable.get(i).containsKey(name)) {
				return this.symTable.get(i).get(name);
			}
		}
		
		return null;	
	}
	
	/**
	 * @throws EmptySymTableException when the symbol table list is empty
	 * 
	 * Removes the HashMap at the head of the list
	 */
	public void removeScope() throws EmptySymTableException {
		if (this.symTable.isEmpty()) {
			throw new EmptySymTableException();
		}
		
		this.symTable.remove(0);
	}
	
	/**
	 * Prints the symbol table to the console
	 */
	public void print() {
		System.out.print("\nSym Table\n");
		for (int i = 0; i < this.symTable.size(); ++i) {
			System.out.println(this.symTable.get(i).toString());
		}
		System.out.println();
	}
}
