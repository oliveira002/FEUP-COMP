package pt.up.fe.comp2023.jasmin.operations.CallOps;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class InvokeVirtualOps extends InvokeAbstract{
    public InvokeVirtualOps(CallInstruction instruction, HashMap<String, Descriptor> VarTable,
                            int LabelCounter, String ThisClassName, HashMap<String, String> importsMap, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, ThisClassName, importsMap, jasmin);
    }

    @Override
    public String toJasmin() {
        CallInstruction instruction = (CallInstruction) this.getInstruction();
        StringBuilder jasminCode = new StringBuilder(loadElement(instruction.getFirstArg()));

        for (Element e : instruction.getListOfOperands())
            jasminCode.append(loadElement(e));

        ElementType firstArgType = instruction.getFirstArg().getType().getTypeOfElement();
        String invokedClass;

        if (firstArgType == ElementType.THIS) {
            invokedClass = ThisClassName;
        } else {
            ClassType classType = (ClassType) instruction.getFirstArg().getType();
            String className = classType.getName();
            invokedClass = this.importsMap.getOrDefault(className, className);
        }

        String jasminCallCode = "\tinvokevirtual " +
                invokedClass +
                "/" +
                ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "") +
                "(";
        jasminCode.append(jasminCallCode);

        for (Element e : instruction.getListOfOperands())
            jasminCode.append(new Jasmin().getParseType(e.getType()));

        jasminCode.append(")").append(jasmin.getParseType(instruction.getReturnType())).append("\n");

        return jasminCode.toString();
    }
}
