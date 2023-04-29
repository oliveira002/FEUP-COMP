package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class SymbolTableCR implements SymbolTable {
    private String className = "";
    private String classSuper = "";
    private final List<Report> reports;

    private List<String> methods = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private List<Symbol> fields  = new ArrayList<>();
    private Map<String, Type> returnTypes = new HashMap<>();
    private Map<String, List<Symbol>> parameters = new HashMap<>();
    private Map<String, List<Symbol>> localVariables = new HashMap<>();

    public SymbolTableCR() {
        this.reports = new ArrayList<>();
    }
    @Override
    public List<String> getImports() {
        var imports = this.imports;
        return imports != null ? imports : Collections.emptyList();
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
        var fields = this.fields;
        return fields != null ? fields : Collections.emptyList();
    }

    public void addField(Symbol s) {
        if(fieldExists(s.getName())) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Duplicated Field!"));
            return;
        };
        this.fields.add(s);
    }

    public boolean fieldExists(String var) {
        for(Symbol s: fields) {
            if(Objects.equals(s.getName(), var)) {
                return true;
            }
        }
        return false;
    }

    public void addMethod(String methodName, Type returnType, List<Symbol> parameters) {
        if (methodExists(methodName)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Duplicated Method!"));
            return;
        }
        this.methods.add(methodName);
        this.returnTypes.put(methodName,returnType);
        this.parameters.put(methodName,parameters);
    }

    @Override
    public List<String> getMethods() {
        var methods = this.methods;
        return methods != null ? methods : Collections.emptyList();
    }

    public void addLocalVar(String methodName, Symbol var) {
        if (localVarExists(var.getName(), methodName)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Duplicated Local Vars!"));
            return;
        }
        if(localVariables.containsKey(methodName)) {
            List<Symbol> curr_variables = localVariables.get(methodName);
            curr_variables.add(var);
            localVariables.put(methodName,curr_variables);
        }
        else {
            List<Symbol> nova = new ArrayList<>();
            nova.add(var);
            localVariables.put(methodName, nova);
        }
    }

    @Override
    public Type getReturnType(String s) {
        var type = this.returnTypes.get(s);
        return type != null ? type : new Type("void", false) ;
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

    public Type getLocalVariableType(String id, String methodName) {
        List<Symbol> methodVars = this.localVariables.get(methodName);
        for(Symbol s : methodVars) {
            if(Objects.equals(s.getName(), id)) {
                return s.getType();
            }
        }
        return new Type("unknown",false);
    }

    public boolean methodExists(String methodName) {
        for(String x : methods) {
            if(Objects.equals(x, methodName)) {
                return true;
            }
        }
        return false;
    }

    public boolean localVarExists(String var, String methodName) {
        if(localVariables.containsKey(methodName)) {
            List<Symbol> locals = getLocalVariables(methodName);
            for(Symbol s : locals) {
                if(Objects.equals(s.getName(), var)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean paramExists(String param, String methodName) {
        if(parameters.containsKey(methodName)) {
            List<Symbol> locals = getParameters(methodName);
            for(Symbol s : locals) {
                if(Objects.equals(s.getName(), param)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }


    public List<Report> getReports() {
        return reports;
    }

    @Override
    public String toString() {
        return print();
    }


    public List<Object> getLocalVarType(String localVar, String method){

        List<Symbol> localVariablesAux = localVariables.get(method);

        if(localVariablesAux == null){
            return null;
        }

        for(Symbol aux : localVariablesAux){
            if(Objects.equals(aux.getName(), localVar))
                return List.of(aux.getType().getName(), aux.getType().isArray());
        }
        return null;
    }

    public List<Object> getParamType(String param, String method){

        List<Symbol> params_aux = parameters.get(method);

        if(params_aux == null){
            return null;
        }

        for(Symbol aux : params_aux){
            if(Objects.equals(aux.getName(), param))
                return List.of(aux.getType().getName(), aux.getType().isArray());
        }
        return null;
    }

    public List<Object> getFieldType(String field){

        for(Symbol aux : fields){
            if(Objects.equals(aux.getName(), field))
                return List.of(aux.getType().getName(), aux.getType().isArray());
        }
        return null;
    }

    public List<String> getParsedImports() {
        List <String> parsedImports = new ArrayList<>();

        for (String s : imports) {
            int lastDotIndex = s.lastIndexOf(".");
            parsedImports.add(s.substring(lastDotIndex + 1));
        }

        return parsedImports;
    }

    public int getParamIndex(String param, String method){
        int index = 1;
        var methodParams = getParameters(method);
        for(Symbol paramAux : methodParams){
            if (paramAux.getName().equals(param)) return index;
            index++;
        }
        //Is syntatic analysis is correct, this should never happen
        return -1;
    }

    public Type getAnyType(String var, String method){
        List<Object> type;

        if((type = getLocalVarType(var, method)) != null) return new Type((String) type.get(0), (boolean) type.get(1));
        if((type = getParamType(var, method)) != null) return new Type((String) type.get(0), (boolean) type.get(1));
        if((type = getFieldType(var)) != null) return new Type((String) type.get(0), (boolean) type.get(1));

        return null;
    }

}
