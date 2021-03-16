package ru.nsu.mockframework;

class IntermediateRunningMock {
  private final Object mock;
  private final String methodName;

  IntermediateRunningMock(Object mock, String methodName) {
    this.mock = mock;
    this.methodName = methodName;
  }

  Object getMock() {
    return mock;
  }

  String getMethodName() {
    return methodName;
  }
}
