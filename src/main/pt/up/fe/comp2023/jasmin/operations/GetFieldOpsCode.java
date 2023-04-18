package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.GetFieldInstruction;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Operand;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class GetFieldOpsCode extends InstructionClass{

    String ThisClassName;
    HashMap<String, String> importsMap;

    public GetFieldOpsCode(Instruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter,
                           Jasmin jasmin, String ThisClassName, HashMap<String, String> importsMap) {
        super(instruction, VarTable, LabelCounter, jasmin);
        this.ThisClassName = ThisClassName;
        this.importsMap = importsMap;
    }

    @Override
    public String toJasmin() {
        GetFieldInstruction instruction = (GetFieldInstruction) this.getInstruction();
        return super.loadElement(instruction.getFirstOperand()) +
                "\tgetfield " + this.importsMap.getOrDefault(ThisClassName,ThisClassName) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + jasmin.getParseType(instruction.getSecondOperand().getType()) + "\n";
    }
}
