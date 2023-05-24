package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

import java.security.BasicPermission;

public class ConstantFolding extends PostorderJmmVisitor<Integer, Boolean> {

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("BinaryOp", this::visitBinaryOp);
        addVisit("LogicalOp", this::visitLogicalOp);
        addVisit("CompareOp", this::visitCompareOp);
        addVisit("Not", this::visitNot);
    }

    private Boolean defaultVisit(JmmNode jmmNode, Integer dummy) {
        boolean changes = false;
        for(JmmNode node: jmmNode.getChildren()) {
            changes |= visit(node,1);
        }
        return changes;
    }

    private Boolean visitBinaryOp(JmmNode jmmNode, Integer dummy) {
        JmmNode left = jmmNode.getJmmChild(0);
        JmmNode right = jmmNode.getJmmChild(1);

        if(left.getKind().equals("Integer") && right.getKind().equals("Integer")) {
            String op = jmmNode.get("op");
            int leftVal = Integer.parseInt(left.get("value"));
            int rightVal = Integer.parseInt(right.get("value"));
            int result = getBinaryValue(op,leftVal,rightVal);

            JmmNode replacement = new JmmNodeImpl("Integer");
            replacement.put("value", String.valueOf(result));
            replacement.put("lineStart",jmmNode.get("lineStart"));
            replacement.put("colStart",jmmNode.get("colStart"));
            jmmNode.replace(replacement);
            return true;
        }
        return false;
    }

    private Boolean visitLogicalOp(JmmNode jmmNode, Integer dummy) {
        JmmNode left = jmmNode.getJmmChild(0);
        JmmNode right = jmmNode.getJmmChild(1);

        if(left.getKind().equals("Boolean") && right.getKind().equals("Boolean")) {
            String op = jmmNode.get("op");
            boolean leftVal = Boolean.parseBoolean(left.get("value"));
            boolean rightVal = Boolean.parseBoolean(right.get("value"));
            boolean result = getLogicalValue(op,leftVal,rightVal);

            JmmNode replacement = new JmmNodeImpl("Boolean");
            replacement.put("value", String.valueOf(result));
            replacement.put("lineStart",jmmNode.get("lineStart"));
            replacement.put("colStart",jmmNode.get("colStart"));
            jmmNode.replace(replacement);
            return true;
        }
        return false;
    }

    private Boolean visitCompareOp(JmmNode jmmNode, Integer dummy) {
        JmmNode left = jmmNode.getJmmChild(0);
        JmmNode right = jmmNode.getJmmChild(1);

        if(left.getKind().equals("Integer") && right.getKind().equals("Integer")) {
            int leftVal = Integer.parseInt(left.get("value"));
            int rightVal = Integer.parseInt(right.get("value"));
            boolean result = leftVal < rightVal;

            JmmNode replacement = new JmmNodeImpl("Boolean");
            replacement.put("value", String.valueOf(result));
            replacement.put("lineStart",jmmNode.get("lineStart"));
            replacement.put("colStart",jmmNode.get("colStart"));
            jmmNode.replace(replacement);
            return true;
        }
        return false;
    }

    private Boolean visitNot(JmmNode jmmNode, Integer dummy) {
        JmmNode exp = jmmNode.getJmmChild(0);

        if(exp.getKind().equals("Boolean")) {
            boolean expVal = Boolean.parseBoolean(exp.get("value"));
            boolean result = !expVal;

            JmmNode replacement = new JmmNodeImpl("Boolean");
            replacement.put("value", String.valueOf(result));
            replacement.put("lineStart",jmmNode.get("lineStart"));
            replacement.put("colStart",jmmNode.get("colStart"));
            jmmNode.replace(replacement);
            return true;
        }
        return false;
    }

    public Integer getBinaryValue(String op, int leftVal, int rightVal) {
        switch (op) {
            case "+":
                return leftVal + rightVal;
            case "-":
                return leftVal - rightVal;
            case "*":
                return leftVal * rightVal;
            case "/":
                return leftVal / rightVal;
        }

        return 1;
    }

    public Boolean getLogicalValue(String op, boolean leftVal, boolean rightVal) {
        switch (op) {
            case "&&":
                return leftVal && rightVal;
            case "||":
                return leftVal || rightVal;
        }
        return true;
    }
}
