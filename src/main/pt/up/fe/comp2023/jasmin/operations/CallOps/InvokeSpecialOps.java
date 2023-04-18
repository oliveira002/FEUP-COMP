package pt.up.fe.comp2023.jasmin.operations.CallOps;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class InvokeSpecialOps extends InvokeAbstract{
    public InvokeSpecialOps(CallInstruction instruction, HashMap<String, Descriptor> VarTable,
                            int LabelCounter, String ThisClassName, HashMap<String, String> importsMap, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, ThisClassName, importsMap, jasmin);
    }

    @Override
    public String toJasmin() {

        CallInstruction instruction = (CallInstruction) this.getInstruction();

        StringBuilder jasminCode = new StringBuilder(loadElement(instruction.getFirstArg()));

        for (Element e : instruction.getListOfOperands()) {
            jasminCode.append(loadElement(e));
        }

        ElementType typeOfElement = instruction.getFirstArg().getType().getTypeOfElement();
        String className;
        if (typeOfElement == ElementType.THIS) {
            className = importsMap.getOrDefault(ThisClassName, "java/lang/Object");
        } else {
            ClassType classType = (ClassType) instruction.getFirstArg().getType();
            String typeName = classType.getName();
            className = importsMap.getOrDefault(typeName, typeName);
        }

        jasminCode.append("\tinvokespecial ")
                .append(className)
                .append("/<init>(");

        for (Element e : instruction.getListOfOperands())
            jasminCode.append((new Jasmin().getParseType(e.getType())));

        jasminCode.append(")").append((jasmin.getParseType(instruction.getReturnType()))).append("\n");

        return jasminCode.toString();
    }
}
