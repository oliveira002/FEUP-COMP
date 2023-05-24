package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.*;
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
        if(instruction instanceof OpCondInstruction){
            OpInstruction  opInstruction = ((OpCondInstruction) instruction).getCondition();
            Element leftOperand = opInstruction.getOperands().get(0);
            Element rightOperand = null;
            if(opInstruction.getOperands().size() > 1){
                rightOperand = opInstruction.getOperands().get(1);
            }
            OperationType type = opInstruction.getOperation().getOpType();
            if(type == OperationType.NOTB){
                jasminCode.append(loadElement(leftOperand))
                        .append("\tifeq ").append(((CondBranchInstruction) instruction).getLabel()).append('\n');
                return jasminCode.toString();
            }
            assert rightOperand != null;
            switch (type){
                case LTH, LTE, GTH, GTE -> {
                    jasminCode.append(loadElement(leftOperand)).append(loadElement(rightOperand)).append("\tisub\n")
                            .append('\t').append(getLabelComp(opInstruction.getOperation())).append(' ')
                            .append(((CondBranchInstruction) instruction).getLabel()).append('\n');
                }
                case EQ -> {
                    jasminCode.append(loadElement(leftOperand)).append(loadElement(rightOperand)).append("\tisub\n")
                            .append("\tifeq ").append(((CondBranchInstruction) instruction).getLabel()).append('\n');
                }
                case NEQ -> {
                    jasminCode.append(loadElement(leftOperand)).append(loadElement(rightOperand)).append("\tisub\n")
                            .append("\tifne ").append(((CondBranchInstruction) instruction).getLabel()).append('\n');
                }
                case ORB -> {
                    jasminCode.append(loadElement(leftOperand))
                            .append("\tifne ").append(((CondBranchInstruction) instruction).getLabel()).append('\n')
                            .append(loadElement(rightOperand))
                            .append("\tifne ").append(((CondBranchInstruction) instruction).getLabel()).append('\n');
                }
                case ANDB -> {
                    jasminCode.append(loadElement(leftOperand))
                            .append("\tifeq Then").append(LabelCounter).append('\n')
                            .append(loadElement(rightOperand))
                            .append("\tifeq Then").append(LabelCounter).append('\n')
                            .append("\tgoto ").append(((CondBranchInstruction) instruction).getLabel()).append('\n')
                            .append("\tThen").append(LabelCounter++).append(":\n");
                }
            }
        }else if (instruction instanceof SingleOpCondInstruction singleOp) {
            jasminCode.append(loadElement(singleOp.getOperands().get(0)))
                    .append("\tifne ").append(((CondBranchInstruction) instruction).getLabel()).append("\n");
        }

        return jasminCode.toString();
    }
}
