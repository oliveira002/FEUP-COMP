package pt.up.fe.comp2023.ollir;


import java.util.List;

public class Utils {

    static int currentTemp = 0;
    static int ifCounter = 0;

    public static String toOllirType(String type, boolean is_array){

        StringBuilder ollirType = new StringBuilder();

        if(is_array)
            ollirType.append(".array");
        return switch (type){
            case "int" -> ollirType.append(".i32").toString();
            case "boolean" -> ollirType.append(".bool").toString();
            case "String" -> ollirType.append(".String").toString();
            case "void" -> ollirType.append(".V").toString();
            default -> ollirType.append(".").append(type).toString();
        };
    }

    public static String boolToOllir(boolean value){
        return value ? "1" : "0";
    }

    public static String nextTemp(){
        return "temp" + currentTemp++;
    }

    public static List<String> nextThenEndIf(){
        return List.of("THEN"+ifCounter++, "ENDIF"+ifCounter++);
    }
}
