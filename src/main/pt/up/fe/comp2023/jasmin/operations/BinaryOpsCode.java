package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class BinaryOpsCode extends InstructionClass{
    public BinaryOpsCode(Instruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, jasmin);
    }

    @Override
    public String toJasmin() {

        StringBuilder jasminCode = new StringBuilder();
        BinaryOpInstruction instruction = (BinaryOpInstruction) this.getInstruction();

        Operation op = instruction.getOperation();

        if(op.getOpType() == OperationType.LTH || op.getOpType() ==OperationType.LTE ||
        op.getOpType() ==OperationType.GTH || op.getOpType() ==OperationType.GTE || op.getOpType() ==OperationType.EQ || op.getOpType() ==OperationType.NEQ){
            boolean rightIsZero = instruction.getRightOperand().isLiteral() && ((LiteralElement) instruction.getRightOperand()).getLiteral().equals("0");
            if(rightIsZero){
                jasminCode.append(loadElement(instruction.getLeftOperand())).append("\t")
                        .append(getLabelComp(op))
                        .append(getCompFormula());
                super.jasmin.lowerStackSize();
                return jasminCode.toString();
            }
        }
        jasminCode.append(loadElement(instruction.getLeftOperand())).append(loadElement(instruction.getRightOperand()));

        switch (op.getOpType()) {
            case ADD -> jasminCode.append("\tiadd\n");
            case SUB -> jasminCode.append("\tisub\n");
            case MUL -> jasminCode.append("\timul\n");
            case DIV -> jasminCode.append("\tidiv\n");
            case LTH, LTE, GTH, GTE, EQ, NEQ -> {
                jasminCode.append("\tisub\n\t")
                        .append(getLabelComp(op))
                        .append(getCompFormula());
                super.jasmin.lowerStackSize();
            }
            case AND,ANDB -> {
                jasminCode.append("\tiand\n");
            }
            default -> {
                return "";
            }
        }
        super.jasmin.lowerStackSize();

        return jasminCode.toString();
    }
}
