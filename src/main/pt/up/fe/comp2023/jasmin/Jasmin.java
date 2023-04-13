package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.AccessModifiers;
import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

public class Jasmin implements JasminBackend {
    private ClassUnit OllirCode;
    private HashMap<String, String>  importsMap = new HashMap<>();
    private String defaultSuperClass = "java/lang/Object";
    public Jasmin(){

    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.OllirCode = ollirResult.getOllirClass();

        File file = new File("./jasmin/" + this.OllirCode.getClassName() + ".j");
        StringBuilder jasminCode = new StringBuilder();

        for (String importString : this.OllirCode.getImports()) {
            var splittedImport = importString.split("\\.");
            this.importsMap.put(splittedImport[splittedImport.length - 1], String.join("/", splittedImport));
        }

        jasminCode.append(this.jasminHeader());

        return new JasminResult(ollirResult, jasminCode.toString(), Collections.emptyList());
    }
    public String jasminHeader(){
        StringBuilder code = new StringBuilder();
        String classSpec = ".class ";
        String superSpec = ".super ";

        if (this.OllirCode.getClassAccessModifier() != AccessModifiers.DEFAULT) {
            code.append(classSpec + this.OllirCode.getClassAccessModifier().toString().toLowerCase()).append(" ");
        }
        else{
            code.append(classSpec + "public ");
        }
        code.append(this.OllirCode.getClassName()).append("\n");

        if (this.OllirCode.getSuperClass() != null)
            this.defaultSuperClass = this.importsMap.getOrDefault(this.OllirCode.getSuperClass(), this.OllirCode.getSuperClass());
        code.append(superSpec + defaultSuperClass);

        code.append("\n\n");

        return code.toString();
    }

}
