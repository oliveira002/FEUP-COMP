package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.OpInstruction;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class ConditionalBranchOpsCode extends InstructionClass{
    public ConditionalBranchOpsCode(Instruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, jasmin);
    }

    @Override
    public String toJasmin() {
        StringBuilder jasminCode = new StringBuilder();

        Instruction instruction = this.getInstruction();
        if(instruction instanceof OpInstruction){

        }

        return jasminCode.toString();
    }
}
