package pt.up.fe.comp2023.jasmin.operations.CallOps;

import org.specs.comp.ollir.CallInstruction;
import org.specs.comp.ollir.Descriptor;
import pt.up.fe.comp2023.jasmin.Jasmin;
import pt.up.fe.comp2023.jasmin.operations.InstructionClass;

import java.util.HashMap;

abstract public class InvokeAbstract extends InstructionClass {
    String ThisClassName;
    HashMap<String, String> importsMap;
    public InvokeAbstract(CallInstruction instruction, HashMap<String, Descriptor> VarTable,
                          int LabelCounter, String ThisClassName, HashMap<String, String>  importsMap, Jasmin jasmin) {

        super(instruction, VarTable, LabelCounter, jasmin);
        this.ThisClassName = ThisClassName;
        this.importsMap = importsMap;
    }
}
