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

import static com.google.devtoolsdriver.devtools.DevtoolsDomain.TIMELINE;

/**
 * Factory for messages in the formerly used devtools Timeline domain. Since stable version 1.1,
 * this domain has been deprecated. This class still exists to maintain support for legacy clients.
 */
public final class Timeline {
  private static DevtoolsCommand command(String methodSuffix) {
    return new DevtoolsCommand.NoOptionals(TIMELINE.methodName(methodSuffix));
  }

  public static DevtoolsCommand start() {
    return command("start");
  }

  public static DevtoolsCommand stop() {
    return command("stop");
  }
}
