
/**
 * @author mihirarora
 *
 * Class to model a symbol object
 */
public class Sym {
	private String type; // stores the type of the symbol
	
	/**
	 * @param type
	 * 
	 * Class constructor. Assigns type of the symbol to the given type.
	 */
	public Sym(String type) {
		this.type = type;
	}
	
	/**
	 * @return the type of the symbol
	 */
	public String getType() {
		return this.type;
	}
	
	/**
	 * @return the type of the symbol (will be modified later)
	 */
	public String toString() {
		return this.type;
	}
}
