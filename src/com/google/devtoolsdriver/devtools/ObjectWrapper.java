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

import com.google.iosdevicecontrol.util.JavaxJson;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/** A fluent base for all Devtools objects */
abstract class ObjectWrapper<T> {
  final JsonObject object;

  ObjectWrapper(JsonObject object) {
    this.object = object;
  }

  T with(String name, boolean value) {
    return create(toObjectBuilder().add(name, value));
  }

  T with(String name, long value) {
    return create(toObjectBuilder().add(name, value));
  }

  T with(String name, double value) {
    return create(toObjectBuilder().add(name, value));
  }

  T with(String name, String value) {
    return create(toObjectBuilder().add(name, value));
  }

  T with(String name, DevtoolsObject value) {
    return create(toObjectBuilder().add(name, value.object));
  }

  T withNumberArray(String name, List<Long> numList) {
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (long l : numList) {
      arrayBuilder.add(l);
    }
    return create(toObjectBuilder().add(name, arrayBuilder));
  }

  T withStringArray(String name, List<String> stringList) {
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (String s : stringList) {
      arrayBuilder.add(s);
    }
    return create(toObjectBuilder().add(name, arrayBuilder));
  }

  T withObjectArray(String name, List<? extends DevtoolsObject> objectList) {
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (DevtoolsObject t : objectList) {
      arrayBuilder.add(t.object);
    }
    return create(toObjectBuilder().add(name, arrayBuilder));
  }

  /** Create a new instance of the DevtoolsObject subclass, using parameters */
  abstract T create(JsonObject params);

  private T create(JsonObjectBuilder builder) {
    return create(builder.build());
  }

  private JsonObjectBuilder toObjectBuilder() {
    return JavaxJson.toBuilder(object);
  }
}
