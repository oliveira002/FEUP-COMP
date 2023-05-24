package pt.up.fe.comp2023.ollir.optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class LivenessAnalysis {
    private final HashMap<Integer,HashSet<String>> def;

    private final HashMap<Integer,HashSet<String>> use;

    private final HashMap<Integer,HashSet<String>> in;

    private final HashMap<Integer,HashSet<String>> out;

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
            addVarToSet(this.def,instId,definedVar);
        }
    }

    public void addVarToSet(HashMap<Integer, HashSet<String>> map, Integer id, String var) {
        HashSet<String> tempDef = map.get(id);
        if(tempDef != null) {
            tempDef.add(var);
            this.def.put(id,tempDef);
        }
        else {
            HashSet<String> tempiDef = new HashSet<>();
            tempiDef.add(var);
            this.def.put(id,tempiDef);
        }
    }

    public void getReadVars(Instruction inst) {
        switch(inst.getInstType().name()) {
            case "ASSIGN" -> getReadVars(((AssignInstruction) inst).getRhs());
            case "RETURN" -> getReturnVars((ReturnInstruction) inst);
            case "BINARYOPER" -> getBinaryOpVars((BinaryOpInstruction) inst);
            case "UNARYOPER" -> getUnaryOpVar((UnaryOpInstruction) inst);
        };
    }

    public void getReturnVars(ReturnInstruction inst) {
        String varName = getVarName(inst.getOperand());
        this.addVarToSet(this.use,inst.getId(),varName);
    }

    public void getBinaryOpVars(BinaryOpInstruction inst) {
        Element leftOp = inst.getLeftOperand();
        Element rightOp = inst.getRightOperand();

        if(!leftOp.isLiteral()) {
            this.addVarToSet(this.use,inst.getId(),getVarName(leftOp));
        }
        if(!rightOp.isLiteral()) {
            this.addVarToSet(this.use,inst.getId(),getVarName(rightOp));
        }
    }

    public void getUnaryOpVar(UnaryOpInstruction inst) {
        Element rightOp = inst.getOperand();
        if(!rightOp.isLiteral()) {
            this.addVarToSet(this.use,inst.getId(),getVarName(rightOp));
        }
    }

    public String getVarName(Element op) {
        return !op.isLiteral() ? ((Operand) op).getName() : null;
    }

}
