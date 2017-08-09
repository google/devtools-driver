/*
 * Copyright 2013 ios-driver committers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.uiautomation.ios.logging;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.devtoolsdriver.devtools.DevtoolsEvent;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;

/**
 * A listener class that enables and logs Network and Timeline messages from DevTools.
 */
public class PerformanceListener implements Consumer<DevtoolsEvent> {
  private static final String[] DOMAINS = new String[] {"Network.", "Page.", "Timeline."};

  private final Log log;

  public PerformanceListener(Log log) {
    this.log = checkNotNull(log);
  }

  private boolean shouldLog(DevtoolsEvent event) {
    for (String domain : DOMAINS) {
      if (event.method().startsWith(domain)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void accept(DevtoolsEvent event) {
    if (!shouldLog(event)) {
      return;
    }
    // The remote performance log clients expect the events to be formatted in
    // the following specific JSON format.
    // See: https://sites.google.com/a/chromium.org/chromedriver/logging/performance-log
    JsonObject messageJson =
        Json.createObjectBuilder()
            .add("method", event.method())
            .add("params", event.params())
            .build();
    JsonObject eventJson =
        Json.createObjectBuilder()
            .add("message", messageJson.toString())
            .build();
    log.addEntry(Level.INFO, eventJson.toString());
  }
}
