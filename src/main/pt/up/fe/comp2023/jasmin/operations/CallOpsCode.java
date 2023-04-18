package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.CallInstruction;
import org.specs.comp.ollir.CallType;
import org.specs.comp.ollir.Descriptor;
import pt.up.fe.comp2023.jasmin.operations.CallOps.InvokeVirtualOps;

import java.util.HashMap;

public class CallOpsCode extends InstructionClass{
    public CallOpsCode(CallInstruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter) {
        super(instruction, VarTable, LabelCounter);
    }

    @Override
    public String toJasmin() {
        CallInstruction instruction = (CallInstruction) this.getInstruction();
        StringBuilder jasminCode = new StringBuilder();

        if(instruction.getInvocationType() == CallType.invokevirtual){
            InvokeVirtualOps code = new InvokeVirtualOps(instruction, this.VarTable, this.LabelCounter, this.ThisClassName, this.importsMap);
            jasminCode.append(code.toJasmin());
        }

        return jasminCode.toString();
    }
}
