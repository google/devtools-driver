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
import static com.google.common.truth.Truth.assert_;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.devtoolsdriver.util.JavaxJson;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class DevtoolsDebuggerTest {
  @Mock IdGenerator idGen;
  @Mock Consumer<DevtoolsEvent> mockEventHandler;

  private FakeDebugger debugger;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    debugger = new FakeDebugger(idGen);
  }

  @Test
  public void testSendCommand() throws Exception {
    when(idGen.nextId()).thenReturn(77);
    DevtoolsCommand command = Network.setCacheDisabled(false);
    Future<JsonObject> responseFuture = debugger.sendCommandAsync(command);
    JsonObject sentJson = debugger.lastMessageSent().get();
    assertThat(sentJson).isEqualTo(command.toJson(77));

    JsonObject expectedResult = Json.createObjectBuilder().add("key1", "value1").build();
    JsonObject expectedResponse = responseBuilder(77).add("result", expectedResult).build();
    JsonObject unexpectedResult = Json.createObjectBuilder().add("key2", "value2").build();
    JsonObject unexpectedResponse = responseBuilder(777).add("result", unexpectedResult).build();
    JsonObject nonsenseMessage = Json.createObjectBuilder().add("hello", "world").build();

    assertThat(responseFuture.isDone()).isFalse();
    debugger.notifyMessageReceived(unexpectedResponse);
    assertThat(responseFuture.isDone()).isFalse();
    debugger.notifyMessageReceived(nonsenseMessage);
    assertThat(responseFuture.isDone()).isFalse();
    debugger.notifyMessageReceived(expectedResponse);
    assertThat(responseFuture.isDone()).isTrue();

    JsonObject actualResponse = responseFuture.get();
    DevtoolsResult firstResult = DevtoolsDebugger.convertResponseToResult(command, actualResponse);
    assertThat(firstResult.json()).isEqualTo(expectedResult);
  }

  @Test
  public void testSendCommandCausesError() throws Exception {
    when(idGen.nextId()).thenReturn(77);
    DevtoolsCommand command = Network.setCacheDisabled(false);
    Future<JsonObject> responseFuture = debugger.sendCommandAsync(command);

    JsonObject errorResponse = responseBuilder(77).add("error", JavaxJson.EMPTY_OBJECT).build();
    debugger.notifyMessageReceived(errorResponse);
    assertThat(responseFuture.isDone()).isTrue();

    JsonObject actualResponse = responseFuture.get();
    try {
      DevtoolsDebugger.convertResponseToResult(command, actualResponse);
      assert_().fail();
    } catch (DevtoolsErrorException expected) {
      assertThat(expected.command()).isEqualTo(command);
      assertThat(expected.response()).isEqualTo(errorResponse);
    }
  }

  @Test
  public void testSendCommandCausesJsException() throws Exception {
    when(idGen.nextId()).thenReturn(77);
    DevtoolsCommand command = Network.setCacheDisabled(false);
    Future<JsonObject> responseFuture = debugger.sendCommandAsync(command);

    JsonObject errorResponse = responseBuilder(77).add("wasThrown", true).build();
    debugger.notifyMessageReceived(errorResponse);
    assertThat(responseFuture.isDone()).isTrue();

    JsonObject actualResponse = responseFuture.get();
    try {
      DevtoolsDebugger.convertResponseToResult(command, actualResponse);
      assert_().fail();
    } catch (DevtoolsErrorException expected) {
      assertThat(expected.command()).isEqualTo(command);
      assertThat(expected.response()).isEqualTo(errorResponse);
    }
  }

  @Test
  public void testReceiveEvents() {
    final JsonObject firstParams = Json.createObjectBuilder().add("first", "param").build();
    final JsonObject secondParams = Json.createObjectBuilder().add("second", "params").build();
    final JsonObject thirdParams = Json.createObjectBuilder().add("third", "paramz").build();

    JsonObject firstEvent =
        Json.createObjectBuilder().add("method", "Fake.method").add("params", firstParams).build();
    JsonObject secondEvent =
        Json.createObjectBuilder()
            .add("method", "NotReal.invocation")
            .add("params", secondParams)
            .build();
    JsonObject thirdEvent =
        Json.createObjectBuilder()
            .add("method", "Faux.identifier")
            .add("params", thirdParams)
            .build();

    debugger.addEventListener(mockEventHandler);
    debugger.addEventListener(
        event -> {
          if (event.method().equals("Fake.method")) {
            assertThat(event.params()).isEqualTo(firstParams);
          } else if (event.method().equals("NotReal.invocation")) {
            assertThat(event.params()).isEqualTo(secondParams);
          } else if (event.method().equals("Faux.identifier")) {
            assertThat(event.params()).isEqualTo(thirdParams);
          } else {
            fail("Unexpected event method received: " + event.method());
          }
        });

    debugger.notifyMessageReceived(firstEvent);
    debugger.notifyMessageReceived(secondEvent);
    debugger.notifyMessageReceived(thirdEvent);

    verify(mockEventHandler, times(3)).accept(Matchers.<DevtoolsEvent>any());
  }

  private static JsonObjectBuilder responseBuilder(int id) {
    return Json.createObjectBuilder().add("id", id);
  }

  private static final class FakeDebugger extends DevtoolsDebugger {
    private final AtomicReference<JsonObject> lastMessageSent = new AtomicReference<>();

    private FakeDebugger(IdGenerator idGen) {
      super(idGen);
    }

    private Optional<JsonObject> lastMessageSent() {
      return Optional.fromNullable(lastMessageSent.get());
    }

    @Override
    protected void sendMessage(JsonObject message) throws IOException {
      lastMessageSent.set(Preconditions.checkNotNull(message));
    }
  }
}
