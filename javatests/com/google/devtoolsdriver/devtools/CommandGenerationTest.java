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

import com.google.common.collect.ImmutableList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CommandGenerationTest {
  @Test
  public void testGeneratedMessageWithNoParams() {
    DevtoolsCommand first = Runtime.enable();
    DevtoolsCommand second = Runtime.disable();
    DevtoolsCommand third = Runtime.run();

    JsonObject firstJson = first.toJson(77);
    JsonObject firstJsonRemade = first.toJson(88);
    JsonObject secondJson = second.toJson(777);
    JsonObject thirdJson = third.toJson(7777);

    JsonObject firstParams = first.params();
    JsonObject secondParams = second.params();
    JsonObject thirdParams = third.params();

    assertThat(first.method()).isEqualTo("Runtime.enable");
    assertThat(firstParams.isEmpty()).isTrue();

    assertThat(second.method()).isEqualTo("Runtime.disable");
    assertThat(secondParams.entrySet().isEmpty()).isTrue();

    assertThat(third.method()).isEqualTo("Runtime.run");
    assertThat(thirdParams.entrySet().isEmpty()).isTrue();

    assertThat(firstJson.getInt("id")).isEqualTo(77);
    assertThat(firstJsonRemade.getInt("id")).isEqualTo(88);
    assertThat(secondJson.getInt("id")).isEqualTo(777);
    assertThat(thirdJson.getInt("id")).isEqualTo(7777);

    assertThat(firstJson.getString("method")).isEqualTo("Runtime.enable");
    assertThat(firstJsonRemade.getString("method")).isEqualTo("Runtime.enable");
    assertThat(secondJson.getString("method")).isEqualTo("Runtime.disable");
    assertThat(thirdJson.getString("method")).isEqualTo("Runtime.run");

    assertThat(firstJson.containsKey("params")).isFalse();
    assertThat(firstJsonRemade).doesNotContainKey("params");
    assertThat(secondJson.containsKey("params")).isFalse();
    assertThat(thirdJson.containsKey("params")).isFalse();
  }

  @Test
  public void testGeneratedMessageWithParams() {
    DevtoolsCommand first = Runtime.compileScript("2 + 2", "www.twoplustwo.com", false, 9000);
    DevtoolsCommand firstEqual = Runtime.compileScript("2 + 2", "www.twoplustwo.com", false, 9000);
    DevtoolsCommand firstNotQuite =
        Runtime.compileScript("2 + 2", "www.twoplustwo.com", false, 9001);
    DevtoolsCommand second =
        Runtime.runScript("2 * 2", 3000)
            .withObjectGroup("helloGroup")
            .withIncludeCommandLineApi(true)
            .withDoNotPauseOnExceptionAndMuteConsole(false);
    DevtoolsCommand third = Runtime.releaseObjectGroup("MyFavoriteGroup");
    DevtoolsCommand fourth =
        Runtime.evaluate("2 + 2")
            .withContextId(5)
            .withGeneratePreview(true)
            .withObjectGroup("SpecialGroup");

    JsonObject firstParams = first.params();
    JsonObject secondParams = second.params();
    JsonObject thirdParams = third.params();
    JsonObject fourthParams = fourth.params();

    JsonObject firstJson = first.toJson(88);
    JsonObject firstJsonRemade = first.toJson(99);
    JsonObject secondJson = second.toJson(889);
    JsonObject thirdJson = third.toJson(7000);
    JsonObject fourthJson = fourth.toJson(9000);

    assertThat(first).isEqualTo(firstEqual);
    assertThat(first.hashCode()).isEqualTo(firstEqual.hashCode());
    assertThat(first).isNotEqualTo(firstNotQuite);
    assertThat(first.hashCode()).isNotEqualTo(firstNotQuite.hashCode());
    assertThat(second).isNotEqualTo(first);
    assertThat(second.hashCode()).isNotEqualTo(first.hashCode());

    assertThat(first.method()).isEqualTo("Runtime.compileScript");
    assertThat(firstParams.getString("expression")).isEqualTo("2 + 2");
    assertThat(firstParams.getString("sourceUrl")).isEqualTo("www.twoplustwo.com");
    assertThat(firstParams.getBoolean("persistScript")).isFalse();
    assertThat(firstParams.getInt("executionContextId")).isEqualTo(9000);

    assertThat(second.method()).isEqualTo("Runtime.runScript");
    assertThat(secondParams.getString("scriptId")).isEqualTo("2 * 2");
    assertThat(secondParams.getInt("executionContextId")).isEqualTo(3000);
    assertThat(secondParams.getString("objectGroup")).isEqualTo("helloGroup");
    assertThat(secondParams.getBoolean("includeCommandLineApi")).isTrue();
    assertThat(secondParams.getBoolean("doNotPauseOnExceptionAndMuteConsole")).isFalse();

    assertThat(third.method()).isEqualTo("Runtime.releaseObjectGroup");
    assertThat(thirdParams.getString("objectGroup")).isEqualTo("MyFavoriteGroup");

    assertThat(fourth.method()).isEqualTo("Runtime.evaluate");
    assertThat(fourthParams.getString("expression")).isEqualTo("2 + 2");
    assertThat(fourthParams.getString("objectGroup")).isEqualTo("SpecialGroup");
    assertThat(fourthParams.getBoolean("generatePreview")).isTrue();
    assertThat(fourthParams.getInt("contextId")).isEqualTo(5);

    assertThat(firstJson.getInt("id")).isEqualTo(88);
    assertThat(firstJsonRemade.getInt("id")).isEqualTo(99);
    assertThat(secondJson.getInt("id")).isEqualTo(889);
    assertThat(thirdJson.getInt("id")).isEqualTo(7000);
    assertThat(fourthJson.getInt("id")).isEqualTo(9000);

    assertThat(firstJson.getString("method")).isEqualTo("Runtime.compileScript");
    assertThat(firstJsonRemade.getString("method")).isEqualTo("Runtime.compileScript");
    assertThat(secondJson.getString("method")).isEqualTo("Runtime.runScript");
    assertThat(thirdJson.getString("method")).isEqualTo("Runtime.releaseObjectGroup");
    assertThat(fourthJson.getString("method")).isEqualTo("Runtime.evaluate");

    assertThat(firstJson.containsKey("params")).isTrue();
    assertThat(firstJsonRemade).containsKey("params");
    assertThat(secondJson.containsKey("params")).isTrue();
    assertThat(thirdJson.containsKey("params")).isTrue();
    assertThat(fourthJson).containsKey("params");

    assertThat(firstJson.get("params")).isEqualTo(firstParams);
    assertThat(firstJsonRemade.get("params")).isEqualTo(firstParams);
    assertThat(secondJson.get("params")).isEqualTo(secondParams);
    assertThat(thirdJson.get("params")).isEqualTo(thirdParams);
    assertThat(fourthJson.get("params")).isEqualTo(fourthParams);
  }

  @Test
  public void testGeneratedMessageWithTypes() {
    JsonArray canonicalArguments =
        Json.createArrayBuilder()
            .add(Runtime.callArgument().withValue("val").withObjectId("567").properties())
            .add(Runtime.callArgument().withValue(3).withObjectId("789").properties())
            .build();
    ImmutableList<Runtime.CallArgument> argumentsList =
        ImmutableList.of(
            Runtime.callArgument().withValue("val").withObjectId("567"),
            Runtime.callArgument().withValue(3).withObjectId("789"));
    DevtoolsCommand first =
        Runtime.callFunctionOn("123", "function funky() { return 2; }")
            .withArguments(argumentsList)
            .withDoNotPauseOnExceptionAndMuteConsole(true)
            .withGeneratePreview(false);

    JsonObject firstParams = first.params();
    assertThat(firstParams.getJsonArray("arguments")).isEqualTo(canonicalArguments);
  }
}
