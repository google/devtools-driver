/*
 * Copyright 2012-2013 eBay Software Foundation and ios-driver committers
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
package org.uiautomation.ios;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openqa.selenium.remote.CapabilityType.LOGGING_PREFS;

import com.google.devtoolsdriver.webdriver.BrowserLauncher;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.uiautomation.ios.command.configuration.DriverConfigurationStore;
import org.uiautomation.ios.drivers.RemoteIOSWebDriver;
import org.uiautomation.ios.logging.IOSLogManager;
import org.uiautomation.ios.servlet.CommandConfiguration;
import org.uiautomation.ios.servlet.DriverConfiguration;
import org.uiautomation.ios.servlet.WebDriverLikeCommand;

/** A WebDriver session. */
public final class ServerSideSession {
  /** State of a session. */
  enum SessionState {
    CREATED,
    RUNNING,
    STOPPED
  }

  private static final Logger log = Logger.getLogger(ServerSideSession.class.getName());

  private final String sessionId;
  private final IOSServerManager server;
  private final DesiredCapabilities capabilities;
  private final RemoteIOSWebDriver driver;
  private final DriverConfiguration configuration;
  private final IOSLogManager logManager;

  @GuardedBy("this")
  private SessionState state = SessionState.CREATED;

  ServerSideSession(
      IOSServerManager server, DesiredCapabilities desiredCapabilities, BrowserLauncher launcher) {
    sessionId = UUID.randomUUID().toString();
    this.server = checkNotNull(server);
    this.capabilities = checkNotNull(desiredCapabilities);

    logManager = createLogManager(desiredCapabilities);
    driver = new RemoteIOSWebDriver(this, launcher);
    configuration = new DriverConfigurationStore();
  }

  private static IOSLogManager createLogManager(DesiredCapabilities caps) {
    LoggingPreferences loggingPrefs = (LoggingPreferences) caps.getCapability(LOGGING_PREFS);
    if (loggingPrefs == null) {
      loggingPrefs = new LoggingPreferences();
    }
    try {
      return new IOSLogManager(loggingPrefs);
    } catch (Exception ex) {
      log.log(Level.SEVERE, "log manager error", ex);
      throw new SessionNotCreatedException("Cannot create logManager", ex);
    }
  }

  public String getSessionId() {
    return sessionId;
  }

  public synchronized SessionState getSessionState() {
    return state;
  }

  public DesiredCapabilities getCapabilities() {
    return capabilities;
  }

  public CommandConfiguration configure(WebDriverLikeCommand command) {
    return configuration.configure(command);
  }

  public RemoteIOSWebDriver getWebDriver() {
    return driver;
  }

  public synchronized void start() {
    driver.start();
    state = SessionState.RUNNING;
  }

  public synchronized void stop() {
    if (state != SessionState.STOPPED) {
      state = SessionState.STOPPED;
      server.registerSessionHasStop(this);
      driver.close();
    }
  }

  public IOSLogManager getLogManager() {
    return logManager;
  }

  @Override
  public String toString() {
    return sessionId;
  }
}
