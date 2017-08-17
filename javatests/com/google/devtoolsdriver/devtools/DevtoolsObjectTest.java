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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DevtoolsObjectTest {
  @Test
  public void testEqualsAndHashCode() {
    DevtoolsObject first = Runtime.callArgument().withValue("val").withObjectId("567");
    DevtoolsObject firstEqual = Runtime.callArgument().withValue("val").withObjectId("567");
    DevtoolsObject firstNotEqual = Runtime.callArgument().withValue("val").withObjectId("568");

    assertThat(first).isEqualTo(firstEqual);
    assertThat(first).isNotEqualTo(firstNotEqual);

    assertThat(first.hashCode()).isEqualTo(firstEqual.hashCode());
    assertThat(first.hashCode()).isNotEqualTo(firstNotEqual.hashCode());
  }
}
