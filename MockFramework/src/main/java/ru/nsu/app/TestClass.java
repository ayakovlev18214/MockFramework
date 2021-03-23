package ru.nsu.app;

public class TestClass {
    private int ret;

    public static void foo() {

    }

    public static String staticStr() {
        return "TEST STATIC";
    }

    public int testString(int a) {
       return a;
    }

    public String testString2(String string, long str) {
        return string + " " + str;
    }

    public String testInt1(int integer) {
        return String.valueOf(integer);
    }


    public int testInt() {
      return 228;
    }

    public char testChar() {return 'a'; }

    public void testVoid() {
    }
}
