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

import com.google.common.collect.Sets;
import com.google.devtoolsdriver.webdriver.BrowserLauncher;
import java.io.File;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.uiautomation.ios.ServerSideSession.SessionState;

/** Manages the ios-driver server sessions. */
public final class IOSServerManager {
  private static final Logger log = Logger.getLogger(IOSServerManager.class.getName());

  private final Set<ServerSideSession> sessions = Sets.newConcurrentHashSet();
  private final Set<String> stoppedSessionIds = Sets.newConcurrentHashSet();
  private final BrowserLauncher launcher;
  private final Object lock = new Object();
  private State state = State.stopped;

  /** State of the server. */
  public enum State {
    starting,
    running,
    stopping,
    stopped;
  }

  IOSServerManager(BrowserLauncher launcher) {
    // force stop session if running for too long
    setState(State.starting);
    this.launcher = launcher;

    // setup logging
    String loggingConfigFile = System.getProperty("java.util.logging.config.file");
    if (loggingConfigFile != null) {
      if (!new File(loggingConfigFile).exists()) {
        System.err.println(
            "logging file not found: " + new File(loggingConfigFile).getAbsolutePath());
        loggingConfigFile = null; // to revert to builtin one
      }
    }
    if (loggingConfigFile == null) {
      // do not use builtin ios-logging.properties if -Djava.util.logging.config.file set
      // or if skipLoggingConfiguration is set to true
      try {
        LogManager.getLogManager()
            .readConfiguration(
                IOSServerManager.class.getResourceAsStream("/ios-logging.properties"));
      } catch (Exception e) {
        System.err.println("Cannot configure logger.");
      }
    }

    setState(State.running);
  }

  public void stop() {
    for (java.util.logging.Handler h : log.getHandlers()) {
      if (h instanceof FileHandler) {
        ((FileHandler) h).close();
      }
    }
    for (ServerSideSession session : sessions) {
      session.stop();
    }
    sessions.clear();
  }

  public ServerSideSession createSession(DesiredCapabilities cap) {
    if (getState() != State.running) {
      return null;
    }
    ServerSideSession session = new ServerSideSession(this, cap, launcher);
    sessions.add(session);
    return session;
  }

  void registerSessionHasStop(ServerSideSession session) {
    stoppedSessionIds.add(session.getSessionId());
    sessions.remove(session);
  }

  public Set<ServerSideSession> getSessions() {
    return sessions;
  }

  public ServerSideSession getSession(String sessionId) {
    // first, check if the session stopped already
    if (stoppedSessionIds.contains(sessionId)) {
      throw newSessionStoppedException(sessionId);
    }

    // check if the session is in the process of stopping
    for (ServerSideSession session : sessions) {
      if (session.getSessionId().equals(sessionId)) {
        if (session.getSessionState() == SessionState.STOPPED) {
          throw newSessionStoppedException(sessionId);
        } else {
          return session;
        }
      }
    }

    throw new WebDriverException("Cannot find session " + sessionId + " on the server.");
  }

  private static WebDriverException newSessionStoppedException(String sessionId) {
    return new WebDriverException(String.format("Session %s stopped", sessionId));
  }

  public void stopGracefully() throws InterruptedException {
    // refuse further requests
    setState(State.stopping);
    // wait for requests to be processed
    while (getSessions().size() != 0) {
      Thread.sleep(250);
    }
    // stops
    stop();
    setState(State.stopped);
  }

  private void setState(State state) {
    synchronized (lock) {
      this.state = state;
    }
  }

  private State getState() {
    synchronized (lock) {
      return this.state;
    }
  }
}
