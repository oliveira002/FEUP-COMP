package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.UnaryOpInstruction;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class UnaryOpsCode extends InstructionClass{
    public UnaryOpsCode(UnaryOpInstruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, jasmin);
    }

    @Override
    public String toJasmin() {
        StringBuilder jasminCode = new StringBuilder();
        UnaryOpInstruction instruction = (UnaryOpInstruction) this.getInstruction();
        if (instruction.getOperation().getOpType() == OperationType.NOTB) {
            jasminCode.append(super.loadElement(instruction.getOperand()))
                    .append("\tifne ").append("Then").append(this.LabelCounter).append('\n')
                    .append("\ticonst_1\n").append("\tgoto End").append(this.LabelCounter).append('\n')
                    .append("\tThen").append(this.LabelCounter).append(":\n")
                    .append("\ticonst_0\n")
                    .append("\tEnd").append(this.LabelCounter).append(":\n");
        }

        return jasminCode.toString();
    }
}
