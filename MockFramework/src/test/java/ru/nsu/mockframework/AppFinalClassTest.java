package ru.nsu.mockframework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.nsu.app.TestFinalClass;

@RemoveFinals({TestFinalClass.class})
@RunWith(JMockFinalRemoverClassRunner.class)
public class AppFinalClassTest {

  @Test
  public void testNonStaticFinal() {
    TestFinalClass test = new TestFinalClass();
    assertEquals("kek", test.testString());
    assertEquals(5, test.getIntAndReturnIt(5));

    test = JMock.mock(TestFinalClass.class);
    assertNull(test.testString());
    assertEquals(0, test.getIntAndReturnIt(5));

    JMock.when(test.testString()).thenReturn("FINAL MOCKED");
    assertEquals("FINAL MOCKED", test.testString());

    JMock.when(test.getIntAndReturnIt(JMock.eq(225))).thenReturn(555);
    assertEquals(555, test.getIntAndReturnIt(225));
    assertEquals(0, test.getIntAndReturnIt(5));

    JMock.when(test.getIntAndReturnIt(JMock.anyNumerical())).thenReturn(228);
    assertEquals(228, test.getIntAndReturnIt(225));
    assertEquals(228, test.getIntAndReturnIt(555));
  }

}
