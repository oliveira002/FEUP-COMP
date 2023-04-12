package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.io.File;
import java.util.Collections;

public class Jasmin implements JasminBackend {
    private ClassUnit ollirCode;
    public Jasmin(){

    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.ollirCode = ollirResult.getOllirClass();
        File file = new File("./jasmin/" + this.ollirCode.getClassName() + ".j");
        return new JasminResult(ollirResult, "", Collections.emptyList());
    }
}
