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

import static com.google.devtoolsdriver.devtools.DevtoolsDomain.CONSOLE;

/**
 * Factory for messages in the devtools Console domain. For a specification of this domain's
 * methods, see the <a
 * href="https://chromedevtools.github.io/debugger-protocol-viewer/tot/Console/">debugging protocol
 * viewer</a>. Note that not all the domain's methods have been implemented yet.
 */
public final class Console {
  private static DevtoolsCommand command(String methodSuffix) {
    return new DevtoolsCommand.NoOptionals(CONSOLE.methodName(methodSuffix));
  }

  public static DevtoolsCommand enable() {
    return command("enable");
  }

  public static DevtoolsCommand disable() {
    return command("disable");
  }

  public static DevtoolsCommand clearMessages() {
    return command("clearMessages");
  }
}
