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

        Descriptor descriptor = VarTable.get(op.getName());

        if (descriptor.getVarType().getTypeOfElement() == ElementType.ARRAYREF && op.getType().getTypeOfElement() != ElementType.ARRAYREF) {
            ArrayOperand arrayOp = (ArrayOperand) op;
            Element i = arrayOp.getIndexOperands().get(0);
            jasminCode.append(getDescriptor(descriptor)).append(loadElement(i));
        }
        else{
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
                            if (literalValue >= -127 && literalValue <= 127) {
                                return "\tiinc " + VarTable.get(operand.getName()).getVirtualReg() + " " + literalValue + "\n";
                            }
                        }
                    }
                }
            }
        }


        String rhsCode = jasmin.routeInstruction(rhs,VarTable,MethodName);

        if (op.getType() instanceof ArrayType t) {
            if (op instanceof ArrayOperand o) {
                jasminCode.append(loadElement(o))
                        .append(loadElement(o.getIndexOperands().get(0)))
                        .append(rhsCode)
                        .append((t.getArrayType() == ElementType.INT32 || t.getArrayType() == ElementType.BOOLEAN) ? "\tiastore\n" : "\taastore\n");

                return jasminCode.toString();
            }
        }

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
