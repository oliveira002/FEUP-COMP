package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class SingleOpsCode extends InstructionClass{
    public SingleOpsCode(Instruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, jasmin);
    }

    @Override
    public String toJasmin() {
        Element e;
        if(instruction instanceof SingleOpInstruction){
            SingleOpInstruction instruction = (SingleOpInstruction) this.getInstruction();
            e = instruction.getSingleOperand();
        }
        else{
            CallInstruction instruction = (CallInstruction) this.getInstruction();
            e = instruction.getFirstArg();
        }

        return super.loadElement(e);
    }
}
