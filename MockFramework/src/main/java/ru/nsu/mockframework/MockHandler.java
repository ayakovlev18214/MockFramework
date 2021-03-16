package ru.nsu.mockframework;

public class MockHandler {
  public static void callback(Object mock, String funcName) {
    JMock.setLastRunnedMock(new IntermediateRunningMock(mock, funcName));
  }
}
