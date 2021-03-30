package ru.nsu.app;

public class TestClass {

    public static String staticGetStr() {
        return "TEST STATIC";
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
