package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.SingleOpInstruction;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class SingleOpsCode extends InstructionClass{
    public SingleOpsCode(SingleOpInstruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, jasmin);
    }

    @Override
    public String toJasmin() {
        SingleOpInstruction instruction = (SingleOpInstruction) this.getInstruction();
        return super.loadElement(instruction.getSingleOperand());
    }
}
