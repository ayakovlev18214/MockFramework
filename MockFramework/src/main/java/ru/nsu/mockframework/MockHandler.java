package ru.nsu.mockframework;

import java.util.List;

public class MockHandler {
  public static void callback(IJMock mock, String funcName, String desc, String rType) {
    JMock.setLastRanMock(new IntermediateMock(mock, funcName, desc, rType));
  }
}
