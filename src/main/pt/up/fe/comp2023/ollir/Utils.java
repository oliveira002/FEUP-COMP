package pt.up.fe.comp2023.ollir;

public class Utils {

    static int currentTemp = 1;

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
        return value ? "1.bool" : "0.bool";
    }

    public static String nextTemp(){
        return "temp" + currentTemp++;
    }
}
