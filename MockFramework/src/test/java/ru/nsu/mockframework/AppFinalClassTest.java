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
    test = JMock.mock(TestFinalClass.class);
    assertNull(test.testString());
    JMock.when(test.testString()).thenReturn("FINAL MOCKED");
    assertEquals("FINAL MOCKED", test.testString());
  }

}
