package pt.up.fe.comp2023.ollir.optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class LivenessAnalysis {
    private final HashMap<Instruction,HashSet<String>> def;

    private final HashMap<Instruction,HashSet<String>> use;

    private final HashMap<Instruction,HashSet<String>> in;

    private final HashMap<Instruction,HashSet<String>> out;

    private final Method method;

    private final List<Instruction> instructionList;

    public LivenessAnalysis(Method method) {
        this.def = new HashMap<>();
        this.use = new HashMap<>();
        this.in = new HashMap<>();
        this.out = new HashMap<>();
        this.method = method;
        this.instructionList = method.getInstructions();
    }

    public void analyse() {
        int i = 1;
        parseDefUse();
        return;
    }

    public void parseDefUse() {
        for(Instruction inst: instructionList) {
            getWrittenVars(inst);
            getReadVars(inst);
        }
    }

    public void getWrittenVars(Instruction inst) {
        if(Objects.equals(inst.getInstType().name(),"ASSIGN")) {
            AssignInstruction assign = (AssignInstruction) inst;
            String definedVar = ((Operand) assign.getDest()).getName();
            int instId = inst.getId();
            addVarToSet(this.def,inst,definedVar);
        }
    }

    public void addVarToSet(HashMap<Instruction, HashSet<String>> map, Instruction inst, String var) {
        HashSet<String> tempDef = map.get(inst);
        if(tempDef != null) {
            tempDef.add(var);
            map.put(inst,tempDef);
        }
        else {
            HashSet<String> tempiDef = new HashSet<>();
            tempiDef.add(var);
            map.put(inst,tempiDef);
        }
    }

    public void getReadVars(Instruction inst) {
        switch(inst.getInstType().name()) {
            case "ASSIGN" -> getReadVars(((AssignInstruction) inst).getRhs());
            case "RETURN" -> getReturnVars((ReturnInstruction) inst);
            case "BINARYOPER" -> getBinaryOpVars((BinaryOpInstruction) inst);
            case "UNARYOPER" -> getUnaryOpVar((UnaryOpInstruction) inst);
            case "PUTFIELD" -> getPutField((PutFieldInstruction) inst);
            case "GETFIELD" -> getField((GetFieldInstruction) inst);
            case "NOPER" -> getNoper((SingleOpInstruction) inst);
            case "CALL" -> getMethodCall((CallInstruction) inst);
            case "BRANCH" -> getBranch((CondBranchInstruction) inst);
        };
    }

    public void getReturnVars(ReturnInstruction inst) {
        String varName = getVarName(inst.getOperand());
        if(varName != null) {
            this.addVarToSet(this.use,inst,varName);
        }
    }

    public void getNoper(SingleOpInstruction inst) {
        Element firstOp = inst.getSingleOperand();
        if(!firstOp.isLiteral()) {
            this.addVarToSet(this.use,inst,getVarName(firstOp));
        }
    }

    public void getBranch(CondBranchInstruction inst) {
        SingleOpCondInstruction sInst = (SingleOpCondInstruction) inst;
        getReadVars(sInst.getCondition());
    }

    public void getMethodCall(CallInstruction inst) {
        int a;
        List<Element> params = inst.getListOfOperands();
        for(Element param: params) {
            if(!param.isLiteral()) {
                this.addVarToSet(this.use,inst,getVarName(param));
            }
        }
    }

    public void getBinaryOpVars(BinaryOpInstruction inst) {
        Element leftOp = inst.getLeftOperand();
        Element rightOp = inst.getRightOperand();

        if(!leftOp.isLiteral()) {
            this.addVarToSet(this.use,inst,getVarName(leftOp));
        }
        if(!rightOp.isLiteral()) {
            this.addVarToSet(this.use,inst,getVarName(rightOp));
        }
    }

    public void getUnaryOpVar(UnaryOpInstruction inst) {
        Element rightOp = inst.getOperand();
        if(!rightOp.isLiteral()) {
            this.addVarToSet(this.use,inst,getVarName(rightOp));
        }
    }

    public void getField(GetFieldInstruction inst) {
        Element secondOp = inst.getSecondOperand();
        if(!secondOp.isLiteral()) {
            this.addVarToSet(this.use,inst,getVarName(secondOp));
        }
    }

    public void getPutField(PutFieldInstruction inst) {
        Element secondOp = inst.getSecondOperand();
        Element thirdOp = inst.getThirdOperand();

        if(!secondOp.isLiteral()) {
            this.addVarToSet(this.use,inst,getVarName(secondOp));
        }
        if(!thirdOp.isLiteral()) {
            this.addVarToSet(this.use,inst,getVarName(thirdOp));
        }
    }

    public String getVarName(Element op) {
        return !op.isLiteral() ? ((Operand) op).getName() : null;
    }

}
