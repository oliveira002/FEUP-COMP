package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.jasmin.operations.*;
import pt.up.fe.comp2023.jasmin.operations.CallOps.InvokeSpecialOps;
import pt.up.fe.comp2023.jasmin.operations.CallOps.InvokeStaticOps;
import pt.up.fe.comp2023.jasmin.operations.CallOps.InvokeVirtualOps;
import pt.up.fe.comp2023.jasmin.operations.CallOps.NewOps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Jasmin implements JasminBackend {
    private ClassUnit OllirCode;
    private final HashMap<String, String>  importsMap = new HashMap<>();
    private String defaultSuperClass = "java/lang/Object";
    private int numLabel = 0;

    private int stackSize = 0;
    private int maxStackSize = 0;
    private Boolean Flag = false;

    public Jasmin(){

    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.OllirCode = ollirResult.getOllirClass();

        StringBuilder jasminCode = new StringBuilder();

        for (String importString : this.OllirCode.getImports()) {
            var splittedImport = importString.split("\\.");
            this.importsMap.put(splittedImport.length == 0 ? importString : splittedImport[splittedImport.length - 1], String.join("/", splittedImport));
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
            resetStack();
        }

        return new JasminResult(ollirResult, jasminCode.toString(), Collections.emptyList());
    }
    public String jasminHeader(){
        StringBuilder code = new StringBuilder();
        String classSpec = ".class ";
        String superSpec = ".super ";

        if (this.OllirCode.getClassAccessModifier() != AccessModifiers.DEFAULT) {
            code.append(classSpec).append(this.OllirCode.getClassAccessModifier().toString().toLowerCase()).append(" ");
        }
        else{
            code.append(classSpec).append("public ");
        }
        code.append(this.OllirCode.getClassName()).append("\n");

        if (this.OllirCode.getSuperClass() != null)
            this.defaultSuperClass = this.importsMap.getOrDefault(this.OllirCode.getSuperClass(), this.OllirCode.getSuperClass());
        code.append(superSpec).append(defaultSuperClass);

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
            return "L" + this.importsMap.getOrDefault(((ClassType) type).getName(), ((ClassType) type).getName()) + ";";
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
        return ".method public <init>()V\n" +
                "\taload_0\n" +
                "\tinvokespecial " + this.defaultSuperClass + "/<init>()V\n" +
                "\treturn\n" +
                ".end method";
    }

    public String jasminMethodParser(Method method){
        String methodSpec = ".method ";
        StringBuilder code = new StringBuilder();
        code.append(methodSpec);

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
            code.append("public <init>(");
            Flag = true;
        } else {
            code.append(method.getMethodName()).append('(');
        }
        code.append(this.getParams(method));
        if(!method.isConstructMethod()){
            code.append("\t.limit stack €STACK-LIMIT€\n");
            code.append(getLocalLimit(method.getVarTable()));
        }

        boolean hasReturnInstruction = false;
        for (Instruction instruction : method.getInstructions()) {
            for (Map.Entry<String, Instruction> label : method.getLabels().entrySet()) {
                if (label.getValue().equals(instruction)) {
                    code.append(label.getKey()).append(":\n");
                }
            }
            if (instruction instanceof ReturnInstruction) {
                hasReturnInstruction = true;
            }
            code.append(this.routeInstruction(instruction, method.getVarTable(), method.getMethodName()));
            if(!Flag && instruction instanceof CallInstruction i){
                if(i.getReturnType().getTypeOfElement() != ElementType.VOID
                        &&(i.getInvocationType() == CallType.invokestatic ||
                        i.getInvocationType() == CallType.invokespecial ||
                        i.getInvocationType() == CallType.invokevirtual)){
                    code.append("\tpop\n");
                }
            }
        }

        if (!hasReturnInstruction) {
            code.append("\treturn\n");
        }
        code.append(".end method\n\n");
        Flag = false;
        return code.toString().replaceFirst("€STACK-LIMIT€",Integer.toString(this.maxStackSize));
    }
    public String getParams(Method method){
        StringBuilder code = new StringBuilder();
        for (Element param : method.getParams()) {
            code.append(this.getParseType(param.getType()));
        }
        code.append(')').append(this.getParseType(method.getReturnType())).append('\n');

        return code.toString();
    }
    public String CallRouter(CallInstruction instruction, HashMap<String, Descriptor> varTable){
        StringBuilder jasminCode = new StringBuilder();

        int lowerStack = 0;

        switch (instruction.getInvocationType()) {
            case invokevirtual -> {
                InvokeVirtualOps code = new InvokeVirtualOps(instruction, varTable, this.numLabel, this.OllirCode.getClassName(), this.importsMap, this);
                jasminCode.append(code.toJasmin());

            }
            case invokespecial -> {
                InvokeSpecialOps code = new InvokeSpecialOps(instruction, varTable, this.numLabel, this.OllirCode.getSuperClass(), this.importsMap, this);
                String code2 = code.toJasmin();
                if (Flag) {
                    Flag = false;
                    code2 = code2.replaceFirst("invokespecial", "invokenonvirtual");
                }
                jasminCode.append(code2);

            }
            case invokestatic -> {
                InvokeStaticOps code = new InvokeStaticOps(instruction, varTable, this.numLabel, this.OllirCode.getClassName(), this.importsMap, this);
                jasminCode.append(code.toJasmin());

                lowerStack -= 1;
            }
            case NEW -> {
                NewOps code = new NewOps(instruction, varTable, this.numLabel, this.OllirCode.getSuperClass(), this.importsMap, this);
                jasminCode.append(code.toJasmin());
            }
            case ldc -> {
                SingleOpsCode code = new SingleOpsCode(instruction, varTable, this.numLabel, this);
                jasminCode.append(code.toJasmin());
            }
            case arraylength -> {
                SingleOpsCode code = new SingleOpsCode(instruction, varTable, this.numLabel, this);
                jasminCode.append(code.toJasmin()).append("\tarraylength\n");
            }
        }
        if(instruction.getInvocationType() == CallType.invokestatic ||
                instruction.getInvocationType() == CallType.invokevirtual ||
                instruction.getInvocationType() == CallType.invokespecial){
            boolean hasOps = instruction.getListOfOperands().size() != 0;
            boolean returnNonVoid = instruction.getReturnType().getTypeOfElement() != ElementType.VOID;
            if (hasOps && returnNonVoid) lowerStack = instruction.getListOfOperands().size();
            else if (hasOps) lowerStack = instruction.getListOfOperands().size() + 1;
            else if (!returnNonVoid) lowerStack = 1;
        }
        for(int a = 0; a < lowerStack;a++){
            lowerStackSize();
        }
        return jasminCode.toString();
    }
    public String routeInstruction(Instruction instruction, HashMap<String, Descriptor> varTable, String MethodName){

        if (instruction instanceof CallInstruction) {
            return CallRouter((CallInstruction) instruction, varTable);
        }

        else if (instruction instanceof GotoInstruction) {
            return "\tgoto " + ((GotoInstruction)instruction).getLabel() + "\n";
        }

        else if (instruction instanceof AssignInstruction) {
            AssignOpsCode code = new AssignOpsCode((AssignInstruction) instruction, varTable, this.numLabel, MethodName,this);
            String result = code.toJasmin();
            this.numLabel = code.getLabelCounter() + this.numLabel;
            return result;
        }

        else if (instruction instanceof ReturnInstruction) {
            ReturnOpsCode code = new ReturnOpsCode((ReturnInstruction) instruction, varTable, this.numLabel, this);
            String result = code.toJasmin();
            this.numLabel = code.getLabelCounter() + this.numLabel;
            return result;
        }

        else if (instruction instanceof SingleOpInstruction) {
            SingleOpsCode code = new SingleOpsCode(instruction, varTable, this.numLabel,this);
            String result = code.toJasmin();
            this.numLabel = code.getLabelCounter() + this.numLabel;
            return result;
        }


        else if (instruction instanceof GetFieldInstruction) {
            GetFieldOpsCode code = new GetFieldOpsCode(instruction, varTable,
                    this.numLabel,this,this.OllirCode.getClassName(),this.importsMap);
            String result = code.toJasmin();
            this.numLabel = code.getLabelCounter() + this.numLabel;
            return result;
        }


        else if (instruction instanceof PutFieldInstruction) {
            PutFieldOpsCode code = new PutFieldOpsCode(instruction, varTable,
                    this.numLabel,this,this.OllirCode.getClassName(),this.importsMap);
            String result = code.toJasmin();
            this.numLabel = code.getLabelCounter() + this.numLabel;
            return result;
        }

        else if (instruction instanceof BinaryOpInstruction) {
            BinaryOpsCode code = new BinaryOpsCode(instruction, varTable, this.numLabel,this);
            String result = code.toJasmin();
            this.numLabel = code.getLabelCounter() + this.numLabel;
            return result;
        }

        else if (instruction instanceof UnaryOpInstruction) {
            UnaryOpsCode code = new UnaryOpsCode((UnaryOpInstruction) instruction, varTable, this.numLabel,this);
            String result = code.toJasmin();
            this.numLabel = code.getLabelCounter() + this.numLabel;
            return result;
        }

        else if (instruction instanceof CondBranchInstruction) {
            ConditionalBranchOpsCode code = new ConditionalBranchOpsCode(instruction, varTable, this.numLabel,this);
            String result = code.toJasmin();
            this.numLabel = code.getLabelCounter() + this.numLabel;
            return result;
        }

        throw new RuntimeException("no instruction");
    }

    public String getLocalLimit(HashMap<String, Descriptor> VarTable) {
        int limit = 0;
        for (Descriptor descriptor : VarTable.values()) {
            if (descriptor.getVirtualReg() > limit) {
                limit = descriptor.getVirtualReg();
            }
        }
        return "\t.limit locals " + (limit + 1) + "\n";
    }
    private void resetStack(){
        this.stackSize = 0;
        this.maxStackSize = 0;
    }
    public void growStackSize(int size){
        this.stackSize = this.stackSize + size;
        if(this.stackSize > this.maxStackSize){
            this.maxStackSize = this.stackSize;
        }
    }
    public void lowerStackSize(){
        this.stackSize = this.stackSize - 1;
    }
}

