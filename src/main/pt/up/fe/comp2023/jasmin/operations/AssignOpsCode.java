package pt.up.fe.comp2023.jasmin.operations;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.Jasmin;

import java.util.HashMap;

public class AssignOpsCode extends InstructionClass{
    public String MethodName;
    public AssignOpsCode(AssignInstruction instruction, HashMap<String, Descriptor> VarTable, int LabelCounter, String MethodName, Jasmin jasmin) {
        super(instruction, VarTable, LabelCounter, jasmin);
        this.MethodName = MethodName;
    }

    @Override
    public String toJasmin() {
        StringBuilder jasminCode = new StringBuilder();
        AssignInstruction instruction = (AssignInstruction) this.getInstruction();
        Operand op = (Operand) instruction.getDest();
        Instruction rhs = instruction.getRhs();

        if (rhs.getInstType() == InstructionType.BINARYOPER) {
            BinaryOpInstruction binaryOp = ((BinaryOpInstruction) rhs);
            if (binaryOp.getOperation().getOpType() == OperationType.ADD) {
                boolean leftLiteral = binaryOp.getLeftOperand().isLiteral();
                boolean rightLiteral = binaryOp.getRightOperand().isLiteral();

                LiteralElement literal = null;
                Operand operand = null;

                if (leftLiteral && !rightLiteral) {
                    literal = (LiteralElement) binaryOp.getLeftOperand();
                    operand = (Operand) binaryOp.getRightOperand();
                } else if (!leftLiteral && rightLiteral) {
                    literal = (LiteralElement) binaryOp.getRightOperand();
                    operand = (Operand) binaryOp.getLeftOperand();
                }
                if (literal != null && operand != null) {
                    if (operand.getName().equals(op.getName())) {
                        int literalValue = Integer.parseInt((literal).getLiteral());
                        if (literalValue >= -128 && literalValue <= 127) {
                            return "\tiinc " + VarTable.get(operand.getName()).getVirtualReg() + " " + literalValue + "\n";
                        }
                    }
                }
            }
        }

        String rhsCode = jasmin.routeInstruction(rhs,VarTable,MethodName);
        //as arrays are not yet implemented, we can assume that the destination is a variable

        jasminCode.append(rhsCode);
        if (op.getType().getTypeOfElement() == ElementType.INT32 || op.getType().getTypeOfElement() == ElementType.BOOLEAN)
            jasminCode.append("\tistore");
        else {
            jasminCode.append("\tastore");
        }
        int reg = VarTable.get(op.getName()).getVirtualReg();
        jasminCode.append((reg <= 3) ? "_" : " ").append(reg).append("\n");
        return jasminCode.toString();
    }
}
