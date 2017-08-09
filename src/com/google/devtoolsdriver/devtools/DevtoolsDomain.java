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

/** An enumeration and utility for generating qualified names of methods in the devtools protocol */
enum DevtoolsDomain {
  CONSOLE("Console"),
  DOM("DOM"),
  NETWORK("Network"),
  PAGE("Page"),
  RUNTIME("Runtime"),
  TIMELINE("Timeline");

  private final String name;

  private DevtoolsDomain(String name) {
    this.name = name;
  }

  String methodName(String methodSuffix) {
    return name + "." + methodSuffix;
  }
}
