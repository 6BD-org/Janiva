package com.oracle.truffle.jx.parser;

public abstract class AbstractPosition<T> {
  static enum Type {
    OBJECT,
    LIST
  }

  public abstract Type positionType();

  public abstract T getPosition();

  public static ListPosition listIdx(Integer idx) {
    return new ListPosition(idx);
  }

  public static ObjectPosition objKey(String key) {
    return new ObjectPosition(key);
  }

  public static class ListPosition extends AbstractPosition<Integer> {

    private final Integer idx;

    public ListPosition(Integer idx) {
      this.idx = idx;
    }

    @Override
    public Type positionType() {
      return Type.LIST;
    }

    @Override
    public Integer getPosition() {
      return idx;
    }
  }

  public static class ObjectPosition extends AbstractPosition<String> {
    private final String key;

    public ObjectPosition(String key) {
      this.key = key;
    }

    @Override
    public Type positionType() {
      return Type.OBJECT;
    }

    @Override
    public String getPosition() {
      return key;
    }
  }
}
