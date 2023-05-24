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
            } case ANDB -> {
                jasminCode.append("\tiadd\n").append("\ticonst_2\n")
                        .append("\tisub\n").append("\tiflt Then").append(LabelCounter).append('\n')
                        .append("\ticonst_1\n").append("\tgoto EndIf").append(LabelCounter).append("\n")
                        .append("\tThen").append(LabelCounter).append(":\n").append("\ticonst_0\n")
                        .append("\tEndIf").append(LabelCounter++).append(":\n");
            }
            default -> {
                return "";
            }
        }

        return jasminCode.toString();
    }
}
