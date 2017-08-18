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

import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.ServerSideSession;
import org.uiautomation.ios.drivers.RemoteIOSWebDriver;
import org.uiautomation.ios.servlet.CommandConfiguration;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;

/** Handles a WebDriver request. */
public abstract class CommandHandler {
  private final IOSServerManager server;
  private final ServerSideSession session;
  private final WebDriverLikeRequest request;

  CommandHandler(IOSServerManager server, WebDriverLikeRequest request) {
    this.server = server;
    this.request = request;

    if (request.hasVariable(":sessionId")) {
      session = server.getSession(request.getSession());
    } else {
      session = null;
    }
  }

  public abstract Response handle() throws Exception;

  final RemoteIOSWebDriver getWebDriver() {
    return getSession().getWebDriver();
  }

  final ServerSideSession getSession() {
    return session;
  }

  final IOSServerManager getServer() {
    return server;
  }

  final WebDriverLikeRequest getRequest() {
    return request;
  }

  final void waitForPageToLoad() {
    getWebDriver().waitForPageToLoad();
  }

  @SuppressWarnings("unchecked")
  final <T> T getConf(String key, T defaultValue) {
    CommandConfiguration conf = getSession().configure(getRequest().getGenericCommand());
    T res = (T) conf.get(key);
    return res != null ? res : defaultValue;
  }

  final Response createResponse(Object value) {
    Response r = new Response();
    r.setSessionId(getSession().getSessionId());
    r.setStatus(0);
    r.setValue(value);
    return r;
  }
}
