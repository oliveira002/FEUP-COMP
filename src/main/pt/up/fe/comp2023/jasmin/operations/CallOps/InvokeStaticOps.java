package pt.up.fe.comp2023.jasmin.operations.CallOps;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class InvokeStaticOps extends InvokeAbstract{
    public InvokeStaticOps(CallInstruction instruction, HashMap<String, Descriptor> VarTable,
                           int LabelCounter, String ThisClassName, HashMap<String, String> importsMap, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, ThisClassName, importsMap, jasmin);
    }

    @Override
    public String toJasmin() {
        StringBuilder jasminCode = new StringBuilder();
        CallInstruction instruction = (CallInstruction) this.getInstruction();
        for (Element e : instruction.getListOfOperands())
            jasminCode.append(loadElement(e));

        String temp = ((Operand) instruction.getFirstArg()).getName();
        String className;
        if (temp.equals("this")) {
            className = ThisClassName;
        } else {
            className = temp;
        }

        String literal = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");
        jasminCode.append("\tinvokestatic ")
                .append(className)
                .append("/")
                .append(literal)
                .append("(");

        for (Element e : instruction.getListOfOperands())
            jasminCode.append(new Jasmin().getParseType(e.getType()));

        jasminCode.append(")").append(jasmin.getParseType(instruction.getReturnType())).append("\n");

        return jasminCode.toString();
    }
}
