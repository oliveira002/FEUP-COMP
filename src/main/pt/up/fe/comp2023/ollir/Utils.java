package pt.up.fe.comp2023.ollir;

public class Utils {
    public static String toOllirType(String type, boolean is_array){
        if(is_array) return ".array.i32"; //only int[] is supported
        return switch (type){
            case "int" -> ".i32";
            case "boolean" -> ".bool";
            case "String" -> ".String";
            default -> "."+type;
        };
    }
}
