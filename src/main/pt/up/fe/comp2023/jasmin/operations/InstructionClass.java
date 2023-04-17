package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.awt.*;
import java.util.HashMap;

abstract public class InstructionClass {
    public Instruction instruction;
    public HashMap<String, Descriptor> VarTable;
    public int LabelCounter;

    public InstructionClass(Instruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter) {
        this.instruction = instruction;
        this.VarTable = VarTable;
        this.LabelCounter = LabelCounter;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public HashMap<String, Descriptor> getVarTable() {
        return VarTable;
    }

    public int getLabelCounter(){
        return LabelCounter;
    }
    public String getDescriptor(Descriptor descriptor) {
        ElementType elementType = descriptor.getVarType().getTypeOfElement();
        if (elementType == ElementType.THIS)
            return "\taload_0\n";

        int reg = descriptor.getVirtualReg();
        if (elementType == ElementType.INT32 || elementType == ElementType.BOOLEAN) {
            if (reg <= 3) {
                return "\t" + "aload_" + reg;
            } else {
                return "\t" + "aload " + reg;
            }
        } else {
            if (reg <= 3) {
                return "\t" + "aload_" + reg;
            } else {
                return "\t" + "aload " + reg;
            }
        }
    }

    public String getLiteral(LiteralElement element) {
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
            Element index = arrayOp.getIndexOperands().get(0);
            return this.getDescriptor(d) + loadElement(index) + "\tiaload\n";
        }

        return this.getDescriptor(d);
    }
    public abstract String toJasmin();
}
