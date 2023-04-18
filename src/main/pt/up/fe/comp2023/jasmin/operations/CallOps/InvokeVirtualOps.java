package pt.up.fe.comp2023.jasmin.operations.CallOps;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;
import pt.up.fe.comp2023.jasmin.operations.CallOpsCode;

import java.util.HashMap;

public class InvokeVirtualOps extends InvokeAbstract{
    public InvokeVirtualOps(CallInstruction instruction, HashMap<String, Descriptor> VarTable,
                            int LabelCounter, String ThisClassName, HashMap<String, String> importsMap) {
        super(instruction, VarTable, LabelCounter, ThisClassName, importsMap);
    }

    @Override
    public String toJasmin() {
        CallInstruction instruction = (CallInstruction) this.getInstruction();
        StringBuilder jasminCode = new StringBuilder(loadElement(instruction.getFirstArg()));

        for (Element e : instruction.getListOfOperands())
            jasminCode.append(loadElement(e));

        jasminCode.append("\tinvokevirtual ").append(instruction.getFirstArg().getType().getTypeOfElement() == ElementType.THIS ?
                        ThisClassName
                        :
                        this.importsMap.getOrDefault(
                                ((ClassType) instruction.getFirstArg().getType()).getName(),
                                ((ClassType) instruction.getFirstArg().getType()).getName()
                        )
                )
                .append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                .append("(");

        for (Element e : instruction.getListOfOperands())
            jasminCode.append(Jasmin.getParseType(e.getType()));

        jasminCode.append(")").append(Jasmin.getParseType(instruction.getReturnType())).append("\n");

        return jasminCode.toString();
    }
}
