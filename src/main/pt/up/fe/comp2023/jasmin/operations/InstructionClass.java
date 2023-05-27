package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;
import java.util.HashMap;

abstract public class InstructionClass {
    public Instruction instruction;
    public HashMap<String, Descriptor> VarTable;
    public int LabelCounter;
    public Jasmin jasmin;
    public InstructionClass(Instruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter, Jasmin jasmin) {
        this.instruction = instruction;
        this.VarTable = VarTable;
        this.LabelCounter = LabelCounter;
        this.jasmin = jasmin;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public int getLabelCounter(){
        return LabelCounter;
    }
    public String getDescriptor(Descriptor descriptor) {
        this.jasmin.growStackSize(1);
        ElementType elementType = descriptor.getVarType().getTypeOfElement();
        if (elementType == ElementType.THIS)
            return "\taload_0\n";

        int reg = descriptor.getVirtualReg();
        if (elementType == ElementType.INT32 || elementType == ElementType.BOOLEAN) {
            if (reg <= 3) {
                return "\t" + "iload_" + reg + "\n";
            } else {
                return "\t" + "iload " + reg + "\n";
            }
        } else {
            if (reg <= 3) {
                return "\t" + "aload_" + reg + "\n";
            } else {
                return "\t" + "aload " + reg + "\n";
            }
        }
    }

    public String getLiteral(LiteralElement element) {
        this.jasmin.growStackSize(1);
        String jasminCode = "\t";
        int literal;
        String string = element.getLiteral();

        if (string.matches("\\d+")) {
            literal = Integer.parseInt(element.getLiteral());
        } else {
            return jasminCode + "ldc " + element.getLiteral() + '\n';
        }

        ElementType elementType = element.getType().getTypeOfElement();
        if (elementType == ElementType.INT32 || elementType == ElementType.BOOLEAN) {
            if (literal <= 5 && literal >= -1)
                jasminCode += "iconst_";
            else if (-32768 > literal || literal > 32767 )
                jasminCode += "ldc ";
            else if (-128 > literal || literal > 127)
                jasminCode += "sipush ";
            else
                jasminCode += "bipush ";
        } else {
            jasminCode += "ldc ";
        }

        if(literal == -1 ){
            return jasminCode + "m1\n";
        }
        else{
            return jasminCode + literal + "\n";
        }
    }

    public String loadElement(Element e) {
        if (e.isLiteral())
            return this.getLiteral((LiteralElement) e);

        Descriptor d = this.VarTable.get(((Operand) e).getName());
        if (e.getType().getTypeOfElement() != ElementType.ARRAYREF && d.getVarType().getTypeOfElement() == ElementType.ARRAYREF) {
            ArrayOperand arrayOp = (ArrayOperand) e;
            Element i = arrayOp.getIndexOperands().get(0);
            this.jasmin.growStackSize(1);
            return this.getDescriptor(d) + loadElement(i) + "\tiaload\n";
        }

        return this.getDescriptor(d);
    }
    public String getLabelComp(Operation operation) {
        this.jasmin.growStackSize(1);
        return switch (operation.getOpType()) {
            case GTE -> "ifge";
            case GTH -> "ifgt";
            case LTE -> "ifle";
            case LTH -> "iflt";
            case EQ -> "ifeq";
            case NOTB, NEQ -> "ifne";
            default -> "";
        };
    }

    public String getCompFormula() {
        String code = " Then" + LabelCounter + "\n" +
                "\ticonst_0\n" +
                "\tgoto EndIf" + LabelCounter + '\n' +
                "\tThen" + LabelCounter + ":\n" +
                "\ticonst_1\n" +
                "\tEndIf" + LabelCounter + ":\n";
        LabelCounter = LabelCounter + 1;
        return code;
    }
    public abstract String toJasmin();
}
