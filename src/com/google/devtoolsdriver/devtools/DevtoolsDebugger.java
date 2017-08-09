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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ForwardingFuture.SimpleForwardingFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.json.JsonObject;

/** A template for communication with a developer tools remote debugger. */
public abstract class DevtoolsDebugger {
  private static final IdGenerator ID_GENERATOR =
      new IdGenerator() {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public int nextId() {
          return counter.getAndIncrement();
        }
      };

  private final IdGenerator idGenerator;
  private final Map<Integer, CommandFuture> idToFuture = new ConcurrentHashMap<>();
  private final Set<Consumer<DevtoolsEvent>> eventListeners = new CopyOnWriteArraySet<>();

  protected DevtoolsDebugger() {
    this(ID_GENERATOR);
  }

  @VisibleForTesting
  DevtoolsDebugger(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  /**
   * Sends a command to the debugger and returns the result.
   *
   * @throws DevtoolsErrorException - if the command caused an error in the debugger.
   * @throws IOException - if an I/O error occurred.
   */
  public final DevtoolsResult sendCommand(DevtoolsCommand command, Duration timeout)
      throws IOException, DevtoolsErrorException {
    CommandFuture future = sendCommandAsync(command);
    try {
      JsonObject response =
          Futures.getChecked(future, IOException.class, timeout.toNanos(), NANOSECONDS);
      return convertResponseToResult(command, response);
    } catch (IOException e) {
      idToFuture.remove(future.id());
      throw e;
    }
  }

  @VisibleForTesting
  final CommandFuture sendCommandAsync(DevtoolsCommand command) throws IOException {
    int commandId = idGenerator.nextId();
    CommandFuture future = new CommandFuture(commandId);
    idToFuture.put(commandId, future);
    try {
      sendMessage(command.toJson(commandId));
      return future;
    } catch (IOException e) {
      idToFuture.remove(commandId);
      throw e;
    }
  }

  @VisibleForTesting
  static final DevtoolsResult convertResponseToResult(DevtoolsCommand command, JsonObject response)
      throws DevtoolsErrorException {
    if (response.containsKey("error") || response.getBoolean("wasThrown", false)) {
      throw new DevtoolsErrorException(command, response);
    }
    return DevtoolsResult.fromJson(response);
  }

  /** Adds a listener to be notified to devtools events. */
  public final void addEventListener(Consumer<DevtoolsEvent> listener) {
    eventListeners.add(listener);
  }

  /**
   * Sends a JSON message to the socket.
   *
   * @throws IOException - if an I/O error occurred.
   */
  protected abstract void sendMessage(JsonObject message) throws IOException;

  /** Notify listener that a message has been received */
  protected final void notifyMessageReceived(JsonObject message) {
    // If there's no id, it's an event message.
    if (message.containsKey("id")) {
      int commandId = message.getInt("id");
      CommandFuture future = idToFuture.remove(commandId);
      if (future != null) {
        future.set(message);
      }
      // Or, it's a result object if it has a method.
    } else if (message.containsKey("method")) {
      DevtoolsEvent event = DevtoolsEvent.fromJson(message);
      for (Consumer<DevtoolsEvent> listener : eventListeners) {
        listener.accept(event);
      }
    } // Drop the message if we cannot identify it.
  }

  @Override
  public final boolean equals(Object other) {
    return super.equals(other);
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  static final class CommandFuture extends SimpleForwardingFuture<JsonObject> {
    private final int id;

    private CommandFuture(int id) {
      super(SettableFuture.create());
      this.id = id;
    }

    private int id() {
      return id;
    }

    private void set(JsonObject value) {
      ((SettableFuture<JsonObject>) delegate()).set(value);
    }
  }
}
