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
package org.uiautomation.ios.servlet;

import java.util.function.BiFunction;
import org.openqa.selenium.UnsupportedCommandException;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.command.AddCookieHandler;
import org.uiautomation.ios.command.AlertHandler;
import org.uiautomation.ios.command.BackHandler;
import org.uiautomation.ios.command.ClearHandler;
import org.uiautomation.ios.command.ClickHandler;
import org.uiautomation.ios.command.CommandHandler;
import org.uiautomation.ios.command.CssPropertyHandler;
import org.uiautomation.ios.command.DeleteAllCookiesHandler;
import org.uiautomation.ios.command.DeleteCookieByNameHandler;
import org.uiautomation.ios.command.ExecuteAsyncScriptHandler;
import org.uiautomation.ios.command.ExecuteScriptHandler;
import org.uiautomation.ios.command.FindElementHandler;
import org.uiautomation.ios.command.FindElementsHandler;
import org.uiautomation.ios.command.ForwardHandler;
import org.uiautomation.ios.command.GetAttributeHandler;
import org.uiautomation.ios.command.GetCapabilitiesHandler;
import org.uiautomation.ios.command.GetConfigurationHandler;
import org.uiautomation.ios.command.GetCookiesHandler;
import org.uiautomation.ios.command.GetCurrentContextHandler;
import org.uiautomation.ios.command.GetElementSizeHandler;
import org.uiautomation.ios.command.GetHandler;
import org.uiautomation.ios.command.GetLocationHandler;
import org.uiautomation.ios.command.GetPageSizeHandler;
import org.uiautomation.ios.command.GetPageSourceHandler;
import org.uiautomation.ios.command.GetSessionsHandler;
import org.uiautomation.ios.command.GetTagNameHandler;
import org.uiautomation.ios.command.GetTextHandler;
import org.uiautomation.ios.command.GetTitleHandler;
import org.uiautomation.ios.command.GetURL;
import org.uiautomation.ios.command.GetWindowHandlesHandler;
import org.uiautomation.ios.command.IsDisplayedHandler;
import org.uiautomation.ios.command.IsEnabledHandler;
import org.uiautomation.ios.command.IsEqualHandler;
import org.uiautomation.ios.command.IsSelectedHandler;
import org.uiautomation.ios.command.LogHandler;
import org.uiautomation.ios.command.LogTypesHandler;
import org.uiautomation.ios.command.MoveToHandler;
import org.uiautomation.ios.command.NewSessionHandler;
import org.uiautomation.ios.command.QuitSessionHandler;
import org.uiautomation.ios.command.RefreshHandler;
import org.uiautomation.ios.command.ServerStatusHandler;
import org.uiautomation.ios.command.SetConfigurationHandler;
import org.uiautomation.ios.command.SetCurrentContextHandler;
import org.uiautomation.ios.command.SetFrameHandler;
import org.uiautomation.ios.command.SetImplicitWaitTimeoutHandler;
import org.uiautomation.ios.command.SetScriptTimeoutHandler;
import org.uiautomation.ios.command.SetTimeoutHandler;
import org.uiautomation.ios.command.SetValueHandler;
import org.uiautomation.ios.command.SubmitHandler;
import org.uiautomation.ios.command.TakeScreenshotHandler;

public enum WebDriverLikeCommand {
  ACCEPT_ALERT("POST", "/session/:sessionId/accept_alert", AlertHandler::new),
  ADD_COOKIE("POST", "/session/:sessionId/cookie", AddCookieHandler::new),
  ATTRIBUTE(
      "GET", "/session/:sessionId/element/:reference/attribute/:name", GetAttributeHandler::new),
  BACK("POST", "/session/:sessionId/back", BackHandler::new),
  CLEAR("POST", "/session/:sessionId/element/:reference/clear", ClearHandler::new),
  CLICK("POST", "/session/:sessionId/element/:reference/click", ClickHandler::new),
  CONFIGURE(
      "POST",
      "/session/:sessionId/ios-driver/configure/command/:command",
      SetConfigurationHandler::new),
  CSS("GET", "/session/:sessionId/element/:reference/css/:name", CssPropertyHandler::new),
  CURRENT_URL("GET", "/session/:sessionId/url", GetURL::new),
  DELETE_COOKIE("DELETE", "/session/:sessionId/cookie", DeleteAllCookiesHandler::new),
  DELETE_COOKIE_BY_NAME(
      "DELETE", "/session/:sessionId/cookie/:name", DeleteCookieByNameHandler::new),
  DELETE_SESSION("DELETE", "/session/:sessionId", QuitSessionHandler::new),
  DISMISS_ALERT("POST", "/session/:sessionId/dismiss_alert", AlertHandler::new),
  DISPLAYED("GET", "/session/:sessionId/element/:reference/displayed", IsDisplayedHandler::new),
  ELEMENT("POST", "/session/:sessionId/element/:reference/element", FindElementHandler::new),
  ELEMENT_ROOT("POST", "/session/:sessionId/element", FindElementHandler::new),
  ELEMENT_SIZE("GET", "/session/:sessionId/element/:reference/size", GetElementSizeHandler::new),
  ELEMENTS("POST", "/session/:sessionId/element/:reference/elements", FindElementsHandler::new),
  ELEMENTS_ROOT("POST", "/session/:sessionId/elements", FindElementsHandler::new),
  ENABLED("GET", "/session/:sessionId/element/:reference/enabled", IsEnabledHandler::new),
  EQUAL("GET", "/session/:sessionId/element/:reference/equals/:other", IsEqualHandler::new),
  EXECUTE_ASYNC_SCRIPT("POST", "/session/:sessionId/execute_async", ExecuteAsyncScriptHandler::new),
  EXECUTE_SCRIPT("POST", "/session/:sessionId/execute", ExecuteScriptHandler::new),
  FORWARD("POST", "/session/:sessionId/forward", ForwardHandler::new),
  FRAME("POST", "/session/:sessionId/frame", SetFrameHandler::new),
  GET_ALERT_TEXT("GET", "/session/:sessionId/alert_text", AlertHandler::new),
  GET_CONFIGURATION(
      "GET",
      "/session/:sessionId/ios-driver/configure/command/:command",
      GetConfigurationHandler::new),
  GET_COOKIE("GET", "/session/:sessionId/cookie", GetCookiesHandler::new),
  GET_SESSION("GET", "/session/:sessionId", GetCapabilitiesHandler::new),
  GET_WINDOW_HANDLE("GET", "/session/:sessionId/window_handle", GetCurrentContextHandler::new),
  IMPLICIT_WAIT(
      "POST", "/session/:sessionId/timeouts/implicit_wait", SetImplicitWaitTimeoutHandler::new),
  LOCATION("GET", "/session/:sessionId/element/:reference/location", GetLocationHandler::new),
  LOG("POST", "/session/:sessionId/log", LogHandler::new),
  LOG_TYPES("GET", "/session/:sessionId/log/types", LogTypesHandler::new),
  MOVE_TO("POST", "/session/:sessionId/moveto", MoveToHandler::new),
  NEW_SESSION("POST", "/session", NewSessionHandler::new),
  REFRESH("POST", "/session/:sessionId/refresh", RefreshHandler::new),
  SCREENSHOT("GET", "/session/:sessionId/screenshot", TakeScreenshotHandler::new),
  SELECTED("GET", "/session/:sessionId/element/:reference/selected", IsSelectedHandler::new),
  SESSIONS("GET", "/sessions", GetSessionsHandler::new),
  SET_ALERT_TEXT("POST", "/session/:sessionId/alert_text", AlertHandler::new),
  SET_SCRIPT_TIMEOUT(
      "POST", "/session/:sessionId/timeouts/async_script", SetScriptTimeoutHandler::new),
  SET_TIMEOUT("POST", "/session/:sessionId/timeouts", SetTimeoutHandler::new),
  SET_VALUE("POST", "/session/:sessionId/element/:reference/value", SetValueHandler::new),
  SOURCE("GET", "/session/:sessionId/source", GetPageSourceHandler::new),
  STATUS("GET", "/status", ServerStatusHandler::new),
  SUBMIT("POST", "/session/:sessionId/element/:reference/submit", SubmitHandler::new),
  TAG_NAME("GET", "/session/:sessionId/element/:reference/name", GetTagNameHandler::new),
  TEXT("GET", "/session/:sessionId/element/:reference/text", GetTextHandler::new),
  TITLE("GET", "/session/:sessionId/title", GetTitleHandler::new),
  URL("POST", "/session/:sessionId/url", GetHandler::new),
  WINDOW("POST", "/session/:sessionId/window", SetCurrentContextHandler::new),
  WINDOW_HANDLES("GET", "/session/:sessionId/window_handles", GetWindowHandlesHandler::new),
  WINDOW_SIZE("GET", "/session/:sessionId/window/:windowHandle/size", GetPageSizeHandler::new);

  public static WebDriverLikeCommand getCommand(String method, String path) {
    for (WebDriverLikeCommand command : values()) {
      if (command.isGenericFormOf(method, path)) {
        return command;
      }
    }
    throw new UnsupportedCommandException("cannot find command for " + method + ", " + path);
  }

  private final String method;
  private final String path;

  @SuppressWarnings("ImmutableEnumChecker")
  private final BiFunction<IOSServerManager, WebDriverLikeRequest, CommandHandler> handlerFactory;

  private WebDriverLikeCommand(
      String method,
      String path,
      BiFunction<IOSServerManager, WebDriverLikeRequest, CommandHandler> handlerFactory) {
    this.method = method;
    this.path = path;
    this.handlerFactory = handlerFactory;
  }

  String path() {
    return path;
  }

  public CommandHandler createHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    return handlerFactory.apply(driver, request);
  }

  private boolean isGenericFormOf(String method, String path) {
    String genericPath = this.path;
    String genericMethod = this.method;
    if (!genericMethod.equals(method)) {
      return false;
    }
    String[] genericPieces = genericPath.split("/");
    String[] pieces = path.split("/");
    if (genericPieces.length != pieces.length) {
      return false;
    } else {
      for (int i = 0; i < pieces.length; i++) {
        String genericPiece = genericPieces[i];
        if (genericPiece.startsWith(":")) {
          continue;
        } else {
          if (!genericPiece.equals(pieces[i])) {
            return false;
          }
        }
      }
      return true;
    }
  }

  int getIndex(String variable) {
    String[] pieces = path.split("/");
    for (int i = 0; i < pieces.length; i++) {
      String piece = pieces[i];
      if (piece.startsWith(":") && piece.equals(variable)) {
        return i;
      }
    }
    throw new UnsupportedCommandException("cannot find the variable " + variable + " in " + path);
  }

  public boolean isSessionLess() {
    return !path.contains(":sessionId");
  }
}
