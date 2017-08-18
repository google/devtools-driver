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

import com.google.common.base.Optional;

import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/*
 * Maps and stores WebDriver logs to their log type and registers listeners.
 */
public class IOSLogManager {
  private final Map<String, Log> logs = new HashMap<>();

  public IOSLogManager(LoggingPreferences prefs) {
    Level perfLevel = prefs.getLevel(LogType.PERFORMANCE);
    if (perfLevel.intValue() < Level.OFF.intValue()) {
      logs.put(LogType.PERFORMANCE, new WebDriverLog(LogType.PERFORMANCE, perfLevel));
    }
  }

  public Optional<PerformanceListener> performanceListener() {
    Log log = logs.get(LogType.PERFORMANCE);
    return log == null
        ? Optional.<PerformanceListener>absent()
        : Optional.of(new PerformanceListener(log));
  }

  public Set<String> getTypes() {
    return logs.keySet();
  }

  public List<LogEntry> getLog(String type) {
    WebDriverLog log = (WebDriverLog) logs.get(type);
    return (log == null ? (Collections.<LogEntry>emptyList()) : log.getAndClearEntries());
  }
}
