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

import com.google.devtoolsdriver.util.JavaxJson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/* A remote message is a message created by the browser, as opposed to the devtools client */
@RunWith(JUnit4.class)
public class MessageFromJsonTest {
  @Test
  public void testEventFromJson() {
    String paramsText =
        "{\"scriptId\":\"69\","
            + "\"url\":\"https://docs.oracle.com/javaee/7/api/script.js\","
            + "\"startLine\":0,\"startColumn\":0,\"endLine\":31,\"endColumn\":0,"
            + "\"executionContextId\":3,"
            + "\"hash\":\"0FDEBE32DE7C1F965D844FA75116EE9B72483064\","
            + "\"isContentScript\":false,\"isInternalScript\":false,\"isLiveEdit\":false,"
            + "\"sourceMapURL\":\"\",\"hasSourceURL\":false,"
            + "\"deprecatedCommentWasUsed\":false}";
    String eventText = "{\"method\":\"Debugger.scriptParsed\", \"params\":" + paramsText + "}";
    DevtoolsEvent event = DevtoolsEvent.fromJson(JavaxJson.parseObject(eventText));
    assertThat(event.method()).isEqualTo("Debugger.scriptParsed");
    assertThat(event.params()).isEqualTo(JavaxJson.parseObject(paramsText));
  }

  @Test
  public void testEventWithNoParamsFromJson() {
    String eventText = "{\"method\":\"Console.messagesCleared\"}";
    DevtoolsEvent event = DevtoolsEvent.fromJson(JavaxJson.parseObject(eventText));
    assertThat(event.method()).isEqualTo("Console.messagesCleared");
    assertThat(event.params()).isEqualTo(JavaxJson.EMPTY_OBJECT);
  }

  @Test
  public void testResultFromJson() {
    String resultText = "{\"value\": \"Hello world!\"}";
    String messageText = "{\"id\": 38, \"result\": " + resultText + "}";
    DevtoolsResult result = DevtoolsResult.fromJson(JavaxJson.parseObject(messageText));
    assertThat(result.json()).isEqualTo(JavaxJson.parseObject(resultText));
  }
}
