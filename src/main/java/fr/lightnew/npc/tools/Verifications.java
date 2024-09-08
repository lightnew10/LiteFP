package fr.lightnew.npc.tools;

public class Verifications {

    public static boolean isInt(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBoolean(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Boolean.parseBoolean(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
