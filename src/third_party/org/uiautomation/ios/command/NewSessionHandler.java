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
package org.uiautomation.ios.command;

import com.google.iosdevicecontrol.util.FluentLogger;
import com.google.iosdevicecontrol.util.JavaxJson;
import javax.json.JsonObject;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.ServerSideSession;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;

/** Handles new session requests. */
public final class NewSessionHandler extends CommandHandler {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public NewSessionHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  @Override
  public Response handle() throws Exception {
    ServerSideSession session = null;
    try {
      JsonObject capsJson = getRequest().getPayload().getJsonObject("desiredCapabilities");
      session = safeStart(new DesiredCapabilities(JavaxJson.toJavaMap(capsJson)));
      if (session == null) {
        throw new SessionNotCreatedException("Failed to start session.");
      }

      Response r = new Response();
      r.setSessionId(session.getSessionId());
      r.setValue(session.getWebDriver().capabilities());
      r.setStatus(0);
      return r;
    } catch (Exception e) {
      logger.atSevere().withCause(e).log();
      if (session != null) {
        session.stop();
      }
      if (e instanceof WebDriverException) {
        throw e;
      } else {
        throw new SessionNotCreatedException(e.getMessage(), e);
      }
    }
  }

  private ServerSideSession safeStart(DesiredCapabilities cap) {
    ServerSideSession session = null;
    try {
      // init session
      session = getServer().createSession(cap);
      if (session == null) {
        throw new SessionNotCreatedException(
            "The server is currently shutting down and doesn't accept new tests.");
      }

      // start session
      session.start();
      return session;
    } catch (Exception e) {
      // TODO(user): Clean this up to meet logging best practices (should not log and throw).
      logger.atSevere().withCause(e).log("Error starting the session");
      if (session != null) {
        session.stop();
      }
      throw new SessionNotCreatedException(e.getMessage(), e);
    }
  }
}
