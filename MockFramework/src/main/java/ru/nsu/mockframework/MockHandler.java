package ru.nsu.mockframework;

public class MockHandler {
  public static void callback(IJMock mock, String funcName, String desc, String rType) {
    JMock.setLastRanMock(new IntermediateRunningMock(mock, funcName, desc, rType));
  }
}
