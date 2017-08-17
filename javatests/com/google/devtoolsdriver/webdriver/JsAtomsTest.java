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

package com.google.devtoolsdriver.webdriver;

import static com.google.common.truth.Truth.assertThat;

import com.google.devtoolsdriver.webdriver.JsAtoms.JsFunction;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link JsAtoms}. */
@RunWith(JUnit4.class)
public class JsAtomsTest {
  @Test
  public void testAllAtomFunctions() {
    for (JsFunction function : JsFunction.values()) {
      assertThat(function.call(fakeArgs(function))).isNotNull();
    }
  }

  private static String[] fakeArgs(JsFunction function) {
    String[] fakeArgs = new String[function.numArgs()];
    Arrays.fill(fakeArgs, "arg");
    return fakeArgs;
  }
}
