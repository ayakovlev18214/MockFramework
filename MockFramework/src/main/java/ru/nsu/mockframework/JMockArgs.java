package ru.nsu.mockframework;

class JMockArgs {
  private EJMock type;
  private Object val;

  JMockArgs(EJMock type, Object val) {
    this.type = type;
    this.val = val;
  }

  EJMock getType() {
    return type;
  }

  Object getVal() {
    return val;
  }
}
