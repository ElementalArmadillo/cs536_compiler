import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a C-- program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Children
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, or
// internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  RepeatStmtNode,
//        CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void addIndent(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++)
            p.print(" ");
    }
}

// **********************************************************************
// ProgramNode, DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void analyze() throws EmptySymTableException, IllegalArgumentException {
        SymTable symTable = new SymTable();
        myDeclList.analyze(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public List<TSym> analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        List<TSym> symList = new LinkedList<TSym>();
        for (DeclNode myDecl : myDecls) {
            symList.add(myDecl.analyze(symTable));
        }
        return symList;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<DeclNode> it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode) it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    public List<TSym> analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        List<TSym> symList = new LinkedList<TSym>();
        for (FormalDeclNode fdNode : myFormals) {
            symList.add(fdNode.analyze(symTable));
        }
        return symList;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myDeclList.analyze(symTable);
        myStmtList.analyze(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        for (StmtNode stmt : myStmts) {
            stmt.analyze(symTable);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public void analyze(SymTable symTable) throws EmptySymTableException {
        for (ExpNode exp : myExps) {
            exp.analyze_exp(symTable);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    abstract public TSym analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException;
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public TSym analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        if (mySize == NOT_STRUCT)
            return myId.analyze(symTable, myType.toString(), "VAR");
        else
            return myId.analyze(symTable, myType.toString(), "STRUCT_VAR");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(";");
    }
    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NOT_STRUCT if this is not a struct type
    private TSym sym;
    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type, IdNode id, FormalsListNode formalList, FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public TSym analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        TSym sym = myId.analyze(symTable, myType.toString(), "FUNCTION");

        if (sym.getKind().equals(""))
            return sym;

        FunctionTSym myIdSym = (FunctionTSym) sym;

        symTable.addScope();
        myIdSym.setFormalList(myFormalsList.analyze(symTable));
        myBody.analyze(symTable);
        symTable.removeScope();


        return myIdSym;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent + 4);
        p.println("}\n");
    }

    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public TSym analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        return myId.analyze(symTable, myType.toString(), "FORMAL");
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public TSym analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        TSym sym = myId.analyze(symTable, "struct", "STRUCT_DECL");

        if (sym.getKind().equals(""))
            return sym;

        SymTable structSymTable = new SymTable();
        myDeclList.analyze(structSymTable);
        StructDeclTSym structVarSym = (StructDeclTSym) sym;
        structVarSym.setSymTable(structSymTable);
        return sym;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("struct ");
        myId.unparse(p, 0);
        p.println("{");
        myDeclList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("};\n");
    }

    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }

    @Override
    public String toString() {
        return "int";
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }

    @Override
    public String toString() {
        return "bool";
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }

    @Override
    public String toString() {
        return "void";
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        myId.unparse(p, 0);
    }

    @Override
    public String toString() {
        return myId.toString();
    }

    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException;
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myAssign.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myExp.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myExp.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myExp.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myExp.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myExp.analyze_exp(symTable);
        symTable.addScope();
        myDeclList.analyze(symTable);
        myStmtList.analyze(symTable);
        symTable.removeScope();
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1, StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myExp.analyze_exp(symTable);
        symTable.addScope();
        myThenDeclList.analyze(symTable);
        myThenStmtList.analyze(symTable);
        symTable.removeScope();
        symTable.addScope();
        myElseDeclList.analyze(symTable);
        myElseStmtList.analyze(symTable);
        symTable.removeScope();
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent + 4);
        myThenStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
        addIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent + 4);
        myElseStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
    }

    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myExp.analyze_exp(symTable);
        symTable.addScope();
        myDeclList.analyze(symTable);
        myStmtList.analyze(symTable);
        symTable.removeScope();
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myExp.analyze_exp(symTable);
        symTable.addScope();
        myDeclList.analyze(symTable);
        myStmtList.analyze(symTable);
        symTable.removeScope();
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("repeat (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        myCall.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }


    public void analyze(SymTable symTable) throws EmptySymTableException, IllegalArgumentException {
        if (myExp != null)
            myExp.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    protected int myLineNum;
    protected int myCharNum;
    abstract public TSym analyze_exp(SymTable symTable) throws EmptySymTableException;
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        return new UndefinedTSym();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        return new UndefinedTSym();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        return new UndefinedTSym();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        return new UndefinedTSym();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public TSym analyze(SymTable symTable, String type, String kind)
            throws EmptySymTableException, IllegalArgumentException {
        declFlag = true;
        boolean pass = true;

        if (!(kind.equals("FUNCTION")) && type.equals("void")) {
            ErrMsg.fatal(myLineNum, myCharNum, "Non-function declared void");
            pass = false;
        }

        if (kind.equals("STRUCT_VAR") && symTable.lookupGlobal(type) == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Invalid name of struct type");
            pass = false;
        }

        switch (kind) {
            case "STRUCT_VAR":
                sym = new StructVarTSym(type);
                break;
            case "STRUCT_DECL":
                sym = new StructDeclTSym(type);
                break;
            case "VAR":
                sym = new TSym(type);
                break;
            case "FUNCTION":
                sym = new FunctionTSym(type);
                break;
            case "FORMAL":
                sym = new FormalTSym(type);
                break;
            default:
                sym = new UndefinedTSym();
                break;
        }

        try {
            symTable.addDecl(myStrVal, sym);
        } catch (DuplicateSymException e) {
            ErrMsg.fatal(myLineNum, myCharNum, "Multiply declared identifier");
            pass = false;
        }

        if (pass == true) {
          return sym;
        } else {
          return new UndefinedTSym();
        }
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        declFlag = false;
        sym = symTable.lookupGlobal(myStrVal);
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
            sym = new UndefinedTSym();
        }
        return sym;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (sym != null && declFlag == false)
          p.print("(" + sym + ")");
    }

    @Override
    public String toString() {
        return myStrVal;
    }

    private String myStrVal;
    private TSym sym;
    private boolean declFlag;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
        myCharNum = loc.myCharNum;
        myLineNum = loc.myLineNum;
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        myLocSym = myLoc.analyze_exp(symTable);
        if (!(myLocSym.getKind().equals("STRUCT_VAR"))) {
            ErrMsg.fatal(myLoc.myLineNum, myLoc.myCharNum, "Dot-access of non-struct type");
            return new UndefinedTSym();
        }

        StructDeclTSym structDeclSym = (StructDeclTSym) symTable.lookupGlobal(myLocSym.getType());

        myIdSym = structDeclSym.getSymTable().lookupGlobal(myId.toString());
        if (myIdSym == null) {
            ErrMsg.fatal(myId.myLineNum, myId.myCharNum, "Invalid struct field name");
            myIdSym = new UndefinedTSym();
        }
        return myIdSym;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print(").");
        myId.unparse(p, 0);
        p.print("(" + myIdSym + ")");
    }

    private ExpNode myLoc;
    private IdNode myId;
    private TSym myLocSym;
    private TSym myIdSym;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
        myCharNum = lhs.myCharNum;
        myLineNum = lhs.myLineNum;
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        myExp.analyze_exp(symTable);
        return myLhs.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)
            p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)
            p.print(")");
    }

    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
        myCharNum = name.myCharNum;
        myLineNum = name.myLineNum;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        myExpList.analyze(symTable);
        return myId.analyze_exp(symTable);
    }

    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    protected ExpNode myExp;

    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
        myCharNum = exp.myCharNum;
        myLineNum = exp.myLineNum;
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        return myExp.analyze_exp(symTable);
    }
}

abstract class BinaryExpNode extends ExpNode {
    protected ExpNode myExp1;
    protected ExpNode myExp2;

    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
        myCharNum = exp1.myCharNum;
        myLineNum = exp1.myLineNum;
    }


    public TSym analyze_exp(SymTable symTable) throws EmptySymTableException {
        myExp2.analyze_exp(symTable);
        return myExp1.analyze_exp(symTable);
    }
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
