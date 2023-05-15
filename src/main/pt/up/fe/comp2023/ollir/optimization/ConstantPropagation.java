package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.*;

public class ConstantPropagation extends PreorderJmmVisitor<Integer,Boolean> {
    private final Map<String,String> varMap = new HashMap<>();

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("VarAssign", this::visitAssignment);
        addVisit("MethodDeclaration", this::visitMethod);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("ConditionStmt", this::visitConditional);

    }

    private Boolean defaultVisit(JmmNode jmmNode, Integer dummy) {
        return null;
    }

    private Boolean visitAssignment(JmmNode jmmNode, Integer dummy) {
        if(jmmNode.getJmmParent().getKind().equals("ThenStmt")) {
            return false;
        }
        JmmNode child = jmmNode.getJmmChild(0);
        // meaning assigning a variable to a literal
        if(child.getKind().equals("Integer") || child.getKind().equals("Boolean")) {
            String value = child.get("value");
            varMap.put(jmmNode.get("var"),value);
            return false;
        }
        if(child.getKind().equals("Identifier")) {
            if(varMap.containsKey(child.get("var"))) {
                varMap.put(jmmNode.get("var"),varMap.get(child.get("var")));
            }
        }
        return false;
    }

    private Boolean visitMethod(JmmNode jmmNode, Integer dummy) {
        varMap.clear();
        return false;
    }

    private Boolean visitIdentifier(JmmNode jmmNode, Integer dummy) {
        String varName = jmmNode.get("var");
        if(varMap.containsKey(varName)) {
            String value = varMap.get(varName);
            String type = "";
            type = value.matches("-?\\d+") ? "Integer" : "Boolean";
            JmmNode replacement = new JmmNodeImpl(type);
            replacement.put("value", String.valueOf(value));
            replacement.put("lineStart",jmmNode.get("lineStart"));
            replacement.put("colStart",jmmNode.get("colStart"));
            jmmNode.replace(replacement);
            return true;
        }
        return false;
    }

    private Boolean visitConditional(JmmNode node, Integer dummy) {
        if(node.get("conditional").equals("if")) {
            JmmNode exp = node.getJmmChild(0);
            visit(exp, null);
            JmmNode ifExp = node.getJmmChild(1);
            visit(ifExp, null);
            JmmNode thenExp = node.getJmmChild(2);
            visit(thenExp, null);

            Set<String> varsInIfAndThen = new HashSet<>();
            varsInIfAndThen.addAll(getConditionalAssignments(ifExp));
            varsInIfAndThen.addAll(getConditionalAssignments(thenExp));

            for(String x : varsInIfAndThen) {
                varMap.remove(x);
            }
        }
        else if(node.get("conditional").equals("while")) {
            JmmNode exp = node.getJmmChild(0);
            visit(exp, null);
            JmmNode whileExp = node.getJmmChild(1);

            List<String> varsInWhile = getConditionalAssignments(whileExp);
            for(String x : varsInWhile) {
                varMap.remove(x);
            }
        }
        return false;
    }


    public List<String> getConditionalAssignments(JmmNode node) {
        List<String> vars = new ArrayList<>();
        for(JmmNode child : node.getChildren()) {
            if(child.getKind().equals("VarAssign")) {
                vars.add(child.get("var"));
            }
        }

        return vars;
    }
}
