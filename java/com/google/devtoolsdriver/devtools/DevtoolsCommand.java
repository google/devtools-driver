// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtoolsdriver.devtools;

import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/** A command message in the Devtools protocol. */
public abstract class DevtoolsCommand extends ObjectWrapper<DevtoolsCommand>
    implements DevtoolsMessage {
  private final String method;

  private DevtoolsCommand(String method, JsonObject object) {
    super(object);
    this.method = method;
  }

  @Override
  public final String method() {
    return method;
  }

  @Override
  public final JsonObject params() {
    return object;
  }

  @Override
  public final boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof DevtoolsCommand) {
      DevtoolsCommand that = (DevtoolsCommand) other;
      return method().equals(that.method()) && object.equals(that.object);
    }
    return false;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(method(), object);
  }

  @Override
  public final String toString() {
    return MoreObjects.toStringHelper(DevtoolsCommand.class)
        .add("method", method())
        .add("params", params())
        .toString();
  }

  /**
   * Convert this JSON command to a JsonObject message that can be sent to a remote debugger. The
   * returned messages will have the specified id in its 'id' field.
   */
  final JsonObject toJson(int id) {
    JsonObjectBuilder builder = Json.createObjectBuilder().add("method", method());
    if (!object.isEmpty()) {
      builder.add("params", object);
    }
    builder.add("id", id);
    return builder.build();
  }

  /** A devtools command with no optional parameters */
  static final class NoOptionals extends DevtoolsCommand {
    NoOptionals(String method) {
      super(method, Json.createObjectBuilder().build());
    }

    NoOptionals(String method, JsonObject params) {
      super(method, params);
    }

    @Override
    NoOptionals create(JsonObject params) {
      return new NoOptionals(method(), params);
    }
  }

  /** A devtools command with publicly exposed functions for adding optional parameters */
  abstract static class WithOptionals<C extends DevtoolsCommand> extends DevtoolsCommand {
    WithOptionals(String method) {
      super(method, Json.createObjectBuilder().build());
    }

    WithOptionals(String method, JsonObject params) {
      super(method, params);
    }

    /*
     * The with methods will always call the create method, which will be overrided to
     * return a new instance of an object of type T. This makes the below suppressions safe
     * to add.
     */
    @SuppressWarnings("unchecked")
    @Override
    final C with(String name, boolean value) {
      return (C) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final C with(String name, long value) {
      return (C) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final C with(String name, double value) {
      return (C) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final C with(String name, String value) {
      return (C) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final C with(String name, DevtoolsObject value) {
      return (C) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final C withNumberArray(String name, List<Long> numberList) {
      return (C) super.withNumberArray(name, numberList);
    }

    @SuppressWarnings("unchecked")
    @Override
    final C withStringArray(String name, List<String> stringList) {
      return (C) super.withStringArray(name, stringList);
    }

    @SuppressWarnings("unchecked")
    @Override
    final C withObjectArray(String name, List<? extends DevtoolsObject> objectList) {
      return (C) super.withObjectArray(name, objectList);
    }

    @Override
    abstract C create(JsonObject object);
  }
}
