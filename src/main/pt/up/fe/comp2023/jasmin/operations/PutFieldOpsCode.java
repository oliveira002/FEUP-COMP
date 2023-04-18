package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.PutFieldInstruction;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class PutFieldOpsCode extends InstructionClass{
    String ThisClassName;
    HashMap<String, String>  importsMap;
    public PutFieldOpsCode(Instruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter,
                           Jasmin jasmin, String ThisClassName,HashMap<String, String>  importsMap) {
        super(instruction, VarTable, LabelCounter, jasmin);
        this.ThisClassName = ThisClassName;
        this.importsMap = importsMap;
    }

    @Override
    public String toJasmin() {
        PutFieldInstruction instruction = (PutFieldInstruction) this.getInstruction();
        return super.loadElement(instruction.getFirstOperand()) +
                this.loadElement(instruction.getThirdOperand()) +
                "\tputfield " +
                ((((Operand) instruction.getFirstOperand()).getName().equals("this")) ?
                        this.importsMap.getOrDefault(ThisClassName,ThisClassName) : (((Operand) instruction.getFirstOperand()).getName())) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + jasmin.getParseType(instruction.getSecondOperand().getType()) + "\n";
    }
}
