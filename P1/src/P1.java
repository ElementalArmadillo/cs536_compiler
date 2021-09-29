
/**
 * @author mihirarora
 * 
 * Tests Sym.java and SymTable.java
 */
public class P1 {
	
	/**
	 * @param args
	 * 
	 * main method that contains all the tests
	 */
	public static void main(String[] args) {
		//1. Test Sym getType and toString
		Sym sym1 = new Sym("int");
		Sym sym2 = new Sym("String");
		
		// checks if getType() and toString return type correctly
		if (!sym1.getType().equals("int")) {
			System.out.println("Test 1 Failed: expected int but instead got " + sym1.getType());
		}
		
		if (!sym1.toString().equals("int")) {
			System.out.println("Test 1 Failed: expected int but instead got " + sym1.toString());
		}
		
		if (!sym2.getType().equals("String")) {
			System.out.println("Test 1 Failed: expected String but instead got " + sym2.getType());
		}
		
		if (!sym2.toString().equals("String")) {
			System.out.println("Test 1 Failed: expected String but instead got " + sym2.toString());
		}
		
		
		// Test SymTable
		SymTable symTable1 = new SymTable();
		
		//2. Test addScope and removeScope
		symTable1.addScope();
		symTable1.addScope();
		
		// checks if addScope correctly added two more HashMaps
		try {
			symTable1.removeScope();
			symTable1.removeScope();
			symTable1.removeScope();
		} catch (EmptySymTableException e) {
			System.out.println("Test 2 Failed: expected no EmptySymTableException");
		}
		
		
		//3. Test EmptySymTableException
		
		// checks if EmptySymTableException is thrown correctly for different methods in SymTable
		try {
			symTable1.removeScope();
			System.out.println("Test 3 Failed: expected EmptySymTableException");
		} catch (EmptySymTableException e) {
			
		}
		
		try {
			symTable1.lookupLocal("Hello");
			System.out.println("Test 3 Failed: expected EmptySymTableException");
		} catch (EmptySymTableException e) {
			
		}
		
		try {
			symTable1.lookupGlobal("Hello");
			System.out.println("Test 3 Failed: expected EmptySymTableException");
		} catch (EmptySymTableException e) {
			
		}
		
		try {
			symTable1.addDecl("1", sym1);
			System.out.println("Test 3 Failed: expected EmptySymTableException");
		} catch (IllegalArgumentException e) {
			System.out.println("Test 3 Failed: expected EmptySymTableException");
		} catch (DuplicateSymException e) {
			System.out.println("Test 3 Failed: expected EmptySymTableException");
		} catch (EmptySymTableException e) {
			
		}
		
		//4. Test addDecl
		symTable1.addScope();
		
		// checks if IllegalArgumentException is thrown correctly
		try {
			symTable1.addDecl("1", null);
			System.out.println("Test 4 Failed: expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			
		} catch (DuplicateSymException e) {
			System.out.println("Test 4 Failed: expected IllegalArgumentException");
		} catch (EmptySymTableException e) {
			System.out.println("Test 4 Failed: expected IllegalArgumentException");
		}
		
		// checks if valid calls of addDecl throw no exceptions
		try {
			symTable1.addDecl("1", sym1);
			symTable1.addDecl("Hello", sym2);
		} catch (IllegalArgumentException e) {
			System.out.println("Test 4 Failed: expected no Exception");
		} catch (DuplicateSymException e) {
			System.out.println("Test 4 Failed: expected no Exception");
		} catch (EmptySymTableException e) {
			System.out.println("Test 4 Failed: expected no Exception");
		}
		
		// checks if DuplicateSymException is thrown correctly
		try {
			symTable1.addDecl("1", sym2);
			System.out.println("Test 4 Failed: expected DuplicateSymException");
		} catch (IllegalArgumentException e) {
			System.out.println("Test 4 Failed: expected DuplicateSymException");
		} catch (DuplicateSymException e) {
			
		} catch (EmptySymTableException e) {
			System.out.println("Test 4 Failed: expected DuplicateSymException");
		}
		
		
		//5. Test lookupLocal
		
		// checks if null is returned for a missing key
		try {
			if (symTable1.lookupLocal("2") != null) {
				System.out.println("Test 5 Failed: expected return value null");
			}
		} catch (EmptySymTableException e) {
			System.out.println("Test 5 Failed: expected no Exception");
		}
		
		// checks if the right Sym is returned for a valid key
		try {
			if (!symTable1.lookupLocal("1").getType().equals("int")) {
				System.out.println("Test 5 Failed: expected return value 'int'");
			}
		} catch (EmptySymTableException e) {
			System.out.println("Test 5 Failed: expected no Exception");
		}
		
		// checks if lookupLocal searches only the first HashMap
		symTable1.addScope();
		try {
			if (symTable1.lookupLocal("1") != null) {
				System.out.println("Test 5 Failed: expected return value 'null'");
			}
		} catch (EmptySymTableException e) {
			System.out.println("Test 5 Failed: expected no Exception");
		}
		
		//6. Test lookupGlobal
		try {
			symTable1.addDecl("3", new Sym("int"));
			symTable1.addScope();
			symTable1.addDecl("2.54", new Sym("double"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (DuplicateSymException e) {
			e.printStackTrace();
		} catch (EmptySymTableException e) {
			e.printStackTrace();
		}
		
		// checks if lookupGlobal returns the correct Sym for keys in various scopes
		try {
			if (!symTable1.lookupGlobal("Hello").getType().equals("String")) {
				System.out.println("Test 6 Failed: expected return value 'String'");
			}
			
			if (!symTable1.lookupGlobal("2.54").getType().equals("double")) {
				System.out.println("Test 6 Failed: expected return value 'double'");
			}
			
			if (!symTable1.lookupGlobal("3").getType().equals("int")) {
				System.out.println("Test 6 Failed: expected return value 'int'");
			}
		} catch (EmptySymTableException e) {
			System.out.println("Test 6 Failed: expected no Exception");
		}
		
		// checks if lookupGlobal returns null for an invalid key
		try {
			if (symTable1.lookupGlobal("72") != null) {
				System.out.println("Test 6 Failed: expected return value 'null'");
			}
		} catch (EmptySymTableException e) {
			System.out.println("Test 6 Failed: expected no Exception");
		}
		
		//7. Test print
		SymTable symTable2 = new SymTable();
		
		try {
			symTable2.addDecl("5", new Sym("int"));
			symTable2.addScope();
			symTable2.addDecl("3.1415", new Sym("pi"));
			symTable2.addDecl("7.84", new Sym("double"));
			symTable2.addScope();
			symTable2.addDecl("Computer Science", new Sym("String"));
			symTable2.addScope();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (DuplicateSymException e) {
			e.printStackTrace();
		} catch (EmptySymTableException e) {
			e.printStackTrace();
		}
		
		String expected = "\nSym Table\n{}\n{Computer Science=String}\n{3.1415=pi, 7.84=double}\n{5=int}\n\n";
		System.out.println("Expected: ");
		System.out.print(expected);
		
		System.out.println("Result: ");
		// outputs symTable2 to console, compare with expected output
		symTable2.print();
		
	}

}
