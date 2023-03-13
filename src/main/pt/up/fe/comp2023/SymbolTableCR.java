package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;

import java.util.*;

public class SymbolTableCR implements SymbolTable {
    private String className = "";
    private String classSuper = "";
    private List<Report> reports;

    private List<String> methods = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private List<Symbol> fields  = new ArrayList<>();
    private Map<String, Type> returnTypes = new HashMap<>();
    private Map<String, List<Symbol>> parameters = new HashMap<>();
    private Map<String, List<Symbol>> localVariables = new HashMap<>();

    @Override
    public List<String> getImports() {
        return this.imports;
    }

    public void addImport(String s) {
        this.imports.add(s);
    }


    @Override
    public String getClassName() {
        return this.className;
    }

    public void setClassName(String s) {
        this.className = s;
    }

    @Override
    public String getSuper() {
        return this.classSuper;
    }

    public void setSuper(String s) {
        this.classSuper = s;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields;
    }

    public void addField(Symbol s) {
        this.fields.add(s);
    }

    @Override
    public List<String> getMethods() {
        return this.methods;
    }

    public void addMethod(String methodName, Type returnType, List<Symbol> parameters) {
        this.methods.add(methodName);
        this.returnTypes.put(methodName,returnType);
        this.parameters.put(methodName,parameters);
    }

    public void addLocalVar(String methodName, Symbol var) {
        if(localVariables.containsKey(methodName)) {
            List<Symbol> curr_variables = localVariables.get(methodName);
            curr_variables.add(var);
            localVariables.put(methodName,curr_variables);
        }
        else {
            List<Symbol> nova = new ArrayList();
            nova.add(var);
            localVariables.put(methodName, nova);
        }
    }

    @Override
    public Type getReturnType(String s) {
        return this.returnTypes.get(s);
    }

    @Override
    public List<Symbol> getParameters(String s) {
        var params = this.parameters.get(s);
        return params != null ? params : Collections.emptyList();
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        var vars = this.localVariables.get(s);
        return vars != null ? vars : Collections.emptyList();
    }

    @Override
    public String toString() {
        return print();
    }
}
