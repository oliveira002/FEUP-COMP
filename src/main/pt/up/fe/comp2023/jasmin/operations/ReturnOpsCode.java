package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class ReturnOpsCode extends InstructionClass{

    public ReturnOpsCode(ReturnInstruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, jasmin);
    }

    @Override
    public String toJasmin() {
        ReturnInstruction instruction = (ReturnInstruction) this.getInstruction();
        if (!instruction.hasReturnValue())
            return "\treturn\n";

        ElementType reType = instruction.getOperand().getType().getTypeOfElement();
        StringBuilder jasminCode = new StringBuilder();
        if (reType == ElementType.INT32 || reType == ElementType.BOOLEAN) {
            jasminCode.append(super.loadElement(instruction.getOperand()))
                    .append("\tireturn\n");
        } else {
            jasminCode.append(super.loadElement(instruction.getOperand()))
                    .append("\tareturn\n");
        }
        return jasminCode.toString();
    }
}
