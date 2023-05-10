package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.*;

public class ConstantPropagation extends PreorderJmmVisitor<Integer,Integer> {
    private final Map<String,String> varMap = new HashMap<>();

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("VarAssign", this::visitAssignment);
        addVisit("MethodDeclaration", this::visitMethod);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("ConditionStmt", this::visitConditional);

    }

    private Integer defaultVisit(JmmNode jmmNode, Integer dummy) {
        return null;
    }

    private Integer visitAssignment(JmmNode jmmNode, Integer dummy) {
        if(jmmNode.getJmmParent().getKind().equals("ThenStmt")) {
            return 1;
        }
        JmmNode child = jmmNode.getJmmChild(0);
        // meaning assigning a variable to a literal
        if(child.getKind().equals("Integer") || child.getKind().equals("Boolean")) {
            String value = child.get("value");
            varMap.put(jmmNode.get("var"),value);
            return 0;
        }
        if(child.getKind().equals("Identifier")) {
            if(varMap.containsKey(child.get("var"))) {
                varMap.put(jmmNode.get("var"),varMap.get(child.get("var")));
            }
        }
        return 1;
    }

    private Integer visitMethod(JmmNode jmmNode, Integer dummy) {
        varMap.clear();
        return 1;
    }

    private Integer visitIdentifier(JmmNode jmmNode, Integer dummy) {
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
        }
        return 1;
    }

    private Integer visitConditional(JmmNode node, Integer dummy) {
        if(node.get("conditional").equals("if")) {
            JmmNode exp = node.getJmmChild(0);
            JmmNode ifExp = node.getJmmChild(1);
            JmmNode thenExp = node.getJmmChild(2);

            Set<String> varsInIfAndThen = new HashSet<>();
            varsInIfAndThen.addAll(getConditionalAssignments(ifExp));
            varsInIfAndThen.addAll(getConditionalAssignments(thenExp));

            for(String x : varsInIfAndThen) {
                if(varMap.containsKey(x)) {
                    varMap.remove(x);
                }
            }
        }
        else if(node.get("conditional").equals("while")) {
            JmmNode exp = node.getJmmChild(0);
            JmmNode whileExp = node.getJmmChild(1);

            List<String> varsInWhile = getConditionalAssignments(whileExp);
            for(String x : varsInWhile) {
                if(varMap.containsKey(x)) {
                    varMap.remove(x);
                }
            }
        }
        return 1;
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
