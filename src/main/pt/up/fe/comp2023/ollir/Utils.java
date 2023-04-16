package pt.up.fe.comp2023.ollir;

public class Utils {
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
}
