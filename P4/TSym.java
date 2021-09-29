import java.util.*;
import java.util.stream.Collectors;

public class TSym {
    String type;

    protected TSym() {
    }

    public TSym(String type) {
        this.type = type;
    }

    public String getKind() {
        return "VAR";
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return type;
    }
}

class StructDeclTSym extends TSym {
    private SymTable symTable;

    public StructDeclTSym(String type) {
        super(type);
    }

    public void setSymTable(SymTable symTable) {
        this.symTable = symTable;
    }

    public SymTable getSymTable() {
        return this.symTable;
    }

    public String getKind() {
        return "STRUCT_DECL";
    }
}

class StructVarTSym extends TSym {
    public StructVarTSym(String type) {
        super(type);
    }

    public String getKind() {
        return "STRUCT_VAR";
    }
}

class FunctionTSym extends TSym {
    private List<TSym> formalList;

    public void setFormalList(List<TSym> formalList) {
        this.formalList = formalList;
    }

    public FunctionTSym(String type) {
        super(type);
    }

    public String getKind() {
        return "FUNCTION";
    }

    @Override
    public String toString() {
        return formalList.stream().map(TSym::toString).collect(Collectors.joining(",")) + "->" + this.type;
    }
}

class FormalTSym extends TSym {
    public FormalTSym(String type) {
        super(type);
    }

    public String getKind() {
        return "FORMAL";
    }
}

class UndefinedTSym extends TSym {
    public String getKind() {
        return "";
    }

    @Override
    public String toString() {
        return "";
    }
}
