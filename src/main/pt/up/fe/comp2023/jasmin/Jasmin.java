package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringJoiner;

public class Jasmin implements JasminBackend {
    private ClassUnit OllirCode;
    private HashMap<String, String>  importsMap = new HashMap<>();
    private String defaultSuperClass = "java/lang/Object";
    public Jasmin(){

    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.OllirCode = ollirResult.getOllirClass();

        StringBuilder jasminCode = new StringBuilder();

        for (String importString : this.OllirCode.getImports()) {
            var splittedImport = importString.split("\\.");
            this.importsMap.put(splittedImport[splittedImport.length - 1], String.join("/", splittedImport));
        }

        jasminCode.append(this.jasminHeader());
        jasminCode.append(this.jasminFields());
        boolean hasContructor = false;
        for (Method method : this.OllirCode.getMethods()) {
            if(method.isConstructMethod()){
                if(hasContructor){
                    throw new RuntimeException("2 constructors");
                }
                hasContructor = true;
            }
        }

        if(!hasContructor){
            jasminCode.append(this.defaultConstructor());
        }

        for(Method method: this.OllirCode.getMethods()){
            jasminCode.append(this.jasminMethodParser(method));
        }

        System.out.println(jasminCode);
        return new JasminResult(ollirResult, jasminCode.toString(), Collections.emptyList());
    }
    public String jasminHeader(){
        StringBuilder code = new StringBuilder();
        String classSpec = ".class ";
        String superSpec = ".super ";

        if (this.OllirCode.getClassAccessModifier() != AccessModifiers.DEFAULT) {
            code.append(classSpec + this.OllirCode.getClassAccessModifier().toString().toLowerCase()).append(" ");
        }
        else{
            code.append(classSpec + "public ");
        }
        code.append(this.OllirCode.getClassName()).append("\n");

        if (this.OllirCode.getSuperClass() != null)
            this.defaultSuperClass = this.importsMap.getOrDefault(this.OllirCode.getSuperClass(), this.OllirCode.getSuperClass());
        code.append(superSpec + defaultSuperClass);

        code.append("\n\n");

        return code.toString();
    }

    public String getParseType(Type type) {
        ElementType eType = type.getTypeOfElement();
        if(eType == ElementType.INT32){
            return "I";
        }
        else if(eType == ElementType.BOOLEAN){
            return "Z";
        }
        else if(eType == ElementType.STRING){
            return "Ljava/lang/String;";
        }
        else if(eType == ElementType.VOID){
            return "V";
        }
        else if(eType == ElementType.ARRAYREF){
            return "[" + this.getParseType(new Type(((ArrayType) type).getArrayType()));
        }
        else if(eType == ElementType.OBJECTREF){
            return "L" + this.importsMap.getOrDefault(eType.getClass().getName(), eType.getClass().getName()) + ";";
        }
        else{
            throw new RuntimeException("no include");
        }
    }
    public String jasminFields() {
        StringBuilder code = new StringBuilder();

        for (Field field : this.OllirCode.getFields()) {
            code.append(".field ");

            if (field.getFieldAccessModifier() != AccessModifiers.DEFAULT) {
                code.append(field.getFieldAccessModifier().toString().toLowerCase()).append(" ");
            }
            if (field.isStaticField()) {
                code.append("static ");
            }
            if (field.isFinalField()) {
                code.append("final ");
            }

            code.append(field.getFieldName()).append(" ");
            code.append(this.getParseType(field.getFieldType()));

            code.append('\n');
        }

        return code.toString();
    }
    public String defaultConstructor(){
        StringBuilder code = new StringBuilder(".method public <init>()V\n" +
                "\taload_0\n" +
                "\tinvokespecial " + this.defaultSuperClass + "/<init>()V\n" +
                "\treturn\n" +
                ".end method");
        return code.toString();
    }

    public String jasminMethodParser(Method method){
        String methodSpec = ".method ";
        StringBuilder code = new StringBuilder();

        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT) {
            code.append(method.getMethodAccessModifier().toString().toLowerCase()).append(" ");
        }
        if (method.isStaticMethod()) {
            code.append("static ");
        }
        if (method.isFinalMethod()) {
            code.append("final ");
        }
        if (method.isConstructMethod()) {
            code.append("<init>(");
        } else {
            code.append(method.getMethodName()).append('(');
        }
        code.append(this.getParams(method));
        code.append("\t.limit stack 99\n\t.limit locals 99\n");
        boolean hasReturnInstruction = false;
        for (Instruction instruction : method.getInstructions()) {
            if (instruction instanceof ReturnInstruction) {
                hasReturnInstruction = true;
            }
            code.append(this.routeInstruction(instruction));
        }

        if (!hasReturnInstruction) {
            code.append("\treturn\n");
        }
        code.append(".end method\n\n");
        return code.toString();
    }
    public String getParams(Method method){
        StringBuilder code = new StringBuilder();
        for (Element param : method.getParams()) {
            code.append(this.getParseType(param.getType()));
        }
        code.append(')').append(this.getParseType(method.getReturnType())).append('\n');

        return code.toString();
    }
    public String routeInstruction(Instruction instruction){

        if (instruction instanceof CallInstruction) {
            return routeInstruction((CallInstruction) instruction);
        }

        if (instruction instanceof AssignInstruction) {
            return routeInstruction((AssignInstruction) instruction);
        }

        if (instruction instanceof GotoInstruction) {
            return routeInstruction((GotoInstruction) instruction);
        }

        if (instruction instanceof ReturnInstruction) {
            return routeInstruction((ReturnInstruction) instruction);
        }

        if (instruction instanceof SingleOpInstruction) {
            return routeInstruction((SingleOpInstruction) instruction);
        }

        if (instruction instanceof PutFieldInstruction) {
            return routeInstruction((PutFieldInstruction) instruction);
        }

        if (instruction instanceof GetFieldInstruction) {
            return routeInstruction((GetFieldInstruction) instruction);
        }

        if (instruction instanceof BinaryOpInstruction) {
            return routeInstruction((BinaryOpInstruction) instruction);
        }

        if (instruction instanceof CondBranchInstruction) {
            return routeInstruction((CondBranchInstruction) instruction);
        }

        if (instruction instanceof UnaryOpInstruction) {
            return routeInstruction((UnaryOpInstruction) instruction);
        }

        throw new RuntimeException("no instruction");
    }
}
