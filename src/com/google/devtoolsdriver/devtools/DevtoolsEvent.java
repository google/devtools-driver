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

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.iosdevicecontrol.util.JavaxJson;
import javax.json.JsonObject;

/** An event message in the Devtools protocol. */
@AutoValue
public abstract class DevtoolsEvent implements DevtoolsMessage {
  public static DevtoolsEvent fromJson(JsonObject json) {
    String method = json.getString("method");
    Optional<JsonObject> params = Optional.fromNullable(json.getJsonObject("params"));
    return new AutoValue_DevtoolsEvent(method, params.or(JavaxJson.EMPTY_OBJECT));
  }
}
