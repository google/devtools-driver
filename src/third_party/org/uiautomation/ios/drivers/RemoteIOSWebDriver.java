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

package org.uiautomation.ios.drivers;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.devtoolsdriver.devtools.DevtoolsCommand;
import com.google.devtoolsdriver.devtools.DevtoolsEvent;
import com.google.devtoolsdriver.devtools.Network;
import com.google.devtoolsdriver.devtools.Page;
import com.google.devtoolsdriver.devtools.Runtime;
import com.google.devtoolsdriver.devtools.Timeline;
import com.google.devtoolsdriver.webdriver.Browser;
import com.google.devtoolsdriver.webdriver.BrowserException;
import com.google.devtoolsdriver.webdriver.BrowserLauncher;
import com.google.devtoolsdriver.webdriver.JsAtoms;
import com.google.devtoolsdriver.webdriver.PageId;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.uiautomation.ios.ServerSideSession;
import org.uiautomation.ios.logging.PerformanceListener;
import org.uiautomation.ios.wkrdp.DOMContext;
import org.uiautomation.ios.wkrdp.WebInspectorHelper;
import org.uiautomation.ios.wkrdp.model.NodeId;
import org.uiautomation.ios.wkrdp.model.RemoteWebElement;

/** Provides an API to drive the browser. */
public final class RemoteIOSWebDriver implements AutoCloseable {
  private static final Logger log = Logger.getLogger(RemoteIOSWebDriver.class.getName());

  private static final DateTimeFormatter HTTP_DATE_TIME =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O").withZone(ZoneOffset.UTC);

  private final ServerSideSession session;
  private final BrowserLauncher launcher;
  private final Optional<PerformanceListener> perfListener;
  private final ExecutorService executor = Executors.newCachedThreadPool();

  private Browser browser;
  private WebInspectorHelper inspector;

  public RemoteIOSWebDriver(ServerSideSession session, BrowserLauncher launcher) {
    this.session = checkNotNull(session);
    this.launcher = checkNotNull(launcher);
    perfListener = session.getLogManager().performanceListener();
  }

  public ImmutableList<PageId> listPages() {
    try {
      return browser.listPages();
    } catch (BrowserException e) {
      throw new WebDriverException(e);
    }
  }

  public void start() {
    try {
      browser = launcher.launch(session.getCapabilities());
    } catch (BrowserException e) {
      throw new WebDriverException(e);
    }
    inspector = new WebInspectorHelper(browser, session);
    addEventListener(inspector);
    if (perfListener.isPresent()) {
      addEventListener(perfListener.get());
    }
    enablePageEvents();
  }

  private void enablePageEvents() {
    inspector.sendCommand(Page.enable());
    if (perfListener.isPresent()) {
      inspector.sendCommand(Network.enable());
      inspector.sendCommand(Timeline.start());
    }
  }

  public ImmutableMap<String, Object> capabilities() {
    // TODO(user): Add the following capabilities:
    // browser version, OS version, whether the device is a simulator, device model
    return ImmutableMap.<String, Object>builder()
        .put("takesScreenshot", true)
        .put("rotatable", true)
        .put("locationContextEnabled", true)
        .put("browserName", browser.browserName())
        .put("platform", browser.platformName())
        .put("platformName", browser.platformName())
        .put("javascriptEnabled", true)
        .put("cssSelectors", true)
        .put("takesElementScreenshot", false)
        .build();
  }

  @Override
  public void close() {
    if (!MoreExecutors.shutdownAndAwaitTermination(executor, 5, SECONDS)) {
      log.severe("Executor did not terminate successfully");
    }
    if (browser != null) {
      try {
        browser.close();
      } catch (BrowserException e) {
        throw new WebDriverException(e);
      }
    }
  }

  public RemoteWebElement createElement(String elementId) {
    String pageId = elementId.split("_")[0];
    int nodeId = Integer.parseInt(elementId.split("_")[1]);

    if (!inspector.getPageIdentifier().asString().equals(pageId)) {
      throw new StaleElementReferenceException(
          "Node "
              + nodeId
              + "is stale.It might still exist, but the "
              + "window with focus has changed.");
    }
    return new RemoteWebElement(new NodeId(nodeId), inspector);
  }

  public void switchTo(PageId pageId) {
    try {
      if (browser.switchTo(pageId)) {
        enablePageEvents();
      }
    } catch (BrowserException e) {
      log.severe(Throwables.getStackTraceAsString(e));
    }
  }

  private void addEventListener(Consumer<DevtoolsEvent> listener) {
    // TODO(user): Figure out why these have to be executed asynchronously.
    browser.addEventListener(
        event -> {
          executor.execute(
              () -> {
                try {
                  listener.accept(event);
                } catch (Exception e) {
                  log.severe(Throwables.getStackTraceAsString(e));
                }
              });
        });
  }

  public void waitForPageToLoad() {
    //currentInspector.waitForPageToLoad();
  }

  public void get(String url) {
    inspector.get(url);
  }

  public void addCookie(String name,
      String value,
      String path,
      String domain,
      boolean secure,
      boolean httpOnly,
      Instant expiry) {
    //TODO: Use Network.setCookie when it is available.
    String cookie = String.format(
        "document.cookie = \"%s=%s; path=%s; domain=%s; expires=%s%s\";",
        name,
        value,
        path,
        domain,
        HTTP_DATE_TIME.format(expiry),
        secure ? "; secure" : "");
    JsonObject response = inspector.sendCommand(Runtime.evaluate(cookie).withReturnByValue(true));
    inspector.cast(response); /* throw out result, if no errors */
  }

  public List<Cookie> getCookies() {
    List<Cookie> res = new ArrayList<>();
    JsonObject o = inspector.sendCommand(Page.getCookies());
    JsonArray cookies = o.getJsonArray("cookies");
    if (cookies != null) {
      for (int i = 0; i < cookies.size(); i++) {
        JsonObject cookie = cookies.getJsonObject(i);
        String name = cookie.getString("name");
        String value = cookie.getString("value");
        String domain = cookie.getString("domain");
        String path = cookie.getString("path");
        Date expiry = new Date(cookie.getJsonNumber("expires").longValue());
        boolean isSecure = cookie.getBoolean("secure");
        Cookie c = new Cookie(name, value, domain, path, expiry, isSecure);
        res.add(c);
      }
      return res;
    } else {
      // TODO
    }
    return null;
  }

  public void deleteCookie(String name, String url) {
    inspector.sendCommand(Page.deleteCookie(name, url));
  }

  public String getCurrentUrl() {
    return inspector.getCurrentUrl();
  }

  public String getTitle() {
    DevtoolsCommand cmd = Runtime.evaluate("document.title;").withReturnByValue(true);
    JsonObject response = inspector.sendCommand(cmd);
    return inspector.cast(response);
  }

  public PageId getCurrentPageId() {
    return inspector.getPageIdentifier();
  }

  public RemoteWebElement findElementByCssSelector(String cssSelector) throws Exception {
    return inspector.getDocument().findElementByCSSSelector(cssSelector);
  }

  public List<RemoteWebElement> findElementsByCssSelector(String cssSelector) {
    return inspector.getDocument().findElementsByCSSSelector(cssSelector);
  }

  public String getPageSource() {
    DevtoolsCommand cmd =
        Runtime.evaluate("new window.XMLSerializer().serializeToString(document);")
            .withReturnByValue(true);
    JsonObject response = inspector.sendCommand(cmd);
    return inspector.cast(response);
  }

  public Dimension getSize() throws Exception {
    String f =
        "(function(element) { var result = "
            + JsAtoms.getInteractableSize("window.top")
            + ";"
            + "var res = "
            + JsAtoms.stringify("result")
            + ";"
            + "return  res;  })";
    DevtoolsCommand cmd =
        Runtime.callFunctionOn(getDocument().getRemoteObject().getId(), f).withReturnByValue(false);

    JsonObject response = inspector.sendCommand(cmd);
    return new Dimension(response.getInt("width"), response.getInt("height"));
  }

  public void back() {
    String f = "(function() { var f=" + JsAtoms.back() + ";})()";
    JsonObject response = inspector.sendCommand(Runtime.evaluate(f).withReturnByValue(true));
    inspector.cast(response); /* throw out result, if no errors */
  }

  public void forward() {
    try {
      String f = "(function() { var f=" + JsAtoms.forward() + ";})()";
      JsonObject response = inspector.sendCommand(Runtime.evaluate(f).withReturnByValue(true));
      inspector.cast(response);
    } catch (Exception e) {
      log.log(Level.SEVERE, "forward error", e);
    }
  }

  public void refresh() {
    try {
      inspector.sendCommand(Page.reload());
    } catch (Exception e) {
      log.log(Level.SEVERE, "refresh error", e);
    }
  }

  public Object executeScript(String script, JsonArray args) {
    return inspector.executeScript(script, args);
  }

  public Object executeAsyncScript(String script, JsonArray args) {
    return inspector.executeAsyncScript(script, args);
  }

  public RemoteWebElement getDocument() {
    return inspector.getDocument();
  }

  public DOMContext getContext() {
    return inspector.getContext();
  }

  public byte[] takeScreenshot() {
    try {
      return browser.takeScreenshot();
    } catch (BrowserException e) {
      throw new WebDriverException(e);
    }
  }
}
