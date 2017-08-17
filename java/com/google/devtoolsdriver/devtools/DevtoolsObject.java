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

/** A Devtool-defined object (JSON object with defined schema) to be used in Devtools commands. */
public abstract class DevtoolsObject extends ObjectWrapper<DevtoolsObject> {
  private DevtoolsObject(JsonObject object) {
    super(object);
  }

  /** Get the json properties of this object. */
  public final JsonObject properties() {
    return object;
  }

  @Override
  public final boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof DevtoolsObject) {
      DevtoolsObject that = (DevtoolsObject) other;
      return object.equals(that.object);
    }
    return false;
  }

  @Override
  public final int hashCode() {
    return Objects.hashCode(object);
  }

  @Override
  public final String toString() {
    return MoreObjects.toStringHelper(DevtoolsObject.class)
        .add("properties", properties())
        .toString();
  }

  /** A Devtools type with no optional field */
  static final class NoOptionals extends DevtoolsObject {
    NoOptionals() {
      super(Json.createObjectBuilder().build());
    }

    NoOptionals(JsonObject properties) {
      super(properties);
    }

    @Override
    NoOptionals create(JsonObject object) {
      return new NoOptionals(object);
    }
  }

  /** Abstract type for a Devtools type with optional fields */
  abstract static class WithOptionals<T extends DevtoolsObject> extends DevtoolsObject {
    WithOptionals() {
      super(Json.createObjectBuilder().build());
    }

    WithOptionals(JsonObject properties) {
      super(properties);
    }

    /*
     * The with methods will always call the create method, which will be overrided to
     * return a new instance of an object of type T. This makes the below suppressions safe
     * to add.
     */
    @SuppressWarnings("unchecked")
    @Override
    final T with(String name, boolean value) {
      return (T) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final T with(String name, long value) {
      return (T) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final T with(String name, String value) {
      return (T) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final T with(String name, DevtoolsObject value) {
      return (T) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final T with(String name, double value) {
      return (T) super.with(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    final T withNumberArray(String name, List<Long> numberList) {
      return (T) super.withNumberArray(name, numberList);
    }

    @SuppressWarnings("unchecked")
    @Override
    final T withStringArray(String name, List<String> stringList) {
      return (T) super.withStringArray(name, stringList);
    }

    @SuppressWarnings("unchecked")
    @Override
    final T withObjectArray(String name, List<? extends DevtoolsObject> objectList) {
      return (T) super.withObjectArray(name, objectList);
    }

    @Override
    abstract T create(JsonObject object);
  }
}
