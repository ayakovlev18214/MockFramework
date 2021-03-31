package ru.nsu.app;

import java.util.List;

public class TestClass {

    public static String staticReturnStr() {
        return "TEST STATIC";
    }

    public static String staticGetIntAndReturnStr(int i) {
        return String.valueOf(i);
    }

    public List<String> processStringList(List<String> list) {
        return list;
    }

    public int[] processIntArray(int[] arr) {
        return arr;
    }

    public String getTwoPrimitiveArgsAndReturnStrOfThem(int a, char b) {
       return a + " " + b;
    }

    public String getString() {
        return "kek";
    }

    public String getIntAndReturnStrOfGivenInt(int integer) {
        return String.valueOf(integer);
    }


    public int getInt() {
      return 228;
    }

    public char getChar() {return 'a'; }

    public int[] getIntArr() {return new int[]{1, 2, 3};}

}
