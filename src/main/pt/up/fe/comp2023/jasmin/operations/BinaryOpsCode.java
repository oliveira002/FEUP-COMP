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
        if(op.getOpType() != OperationType.ADD && op.getOpType() != OperationType.SUB
                && op.getOpType() != OperationType.MUL && op.getOpType() != OperationType.DIV)
            throw new RuntimeException("Invalid operation type");

        jasminCode.append(loadElement(instruction.getLeftOperand()))
                .append(loadElement(instruction.getRightOperand()));

        switch (op.getOpType()) {
            case ADD -> jasminCode.append("\tiadd\n");
            case SUB -> jasminCode.append("\tisub\n");
            case MUL -> jasminCode.append("\timul\n");
            case DIV -> jasminCode.append("\tidiv\n");
        }

        return jasminCode.toString();
    }
}
