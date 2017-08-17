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

package org.uiautomation.ios.wkrdp;

import static com.google.devtoolsdriver.devtools.Runtime.callArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.devtoolsdriver.devtools.DOM;
import com.google.devtoolsdriver.devtools.DevtoolsCommand;
import com.google.devtoolsdriver.devtools.DevtoolsEvent;
import com.google.devtoolsdriver.devtools.Page;
import com.google.devtoolsdriver.devtools.Runtime;
import com.google.devtoolsdriver.devtools.Runtime.CallArgument;
import com.google.devtoolsdriver.util.JavaxJson;
import com.google.devtoolsdriver.webdriver.Browser;
import com.google.devtoolsdriver.webdriver.BrowserException;
import com.google.devtoolsdriver.webdriver.PageId;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.uiautomation.ios.ServerSideSession;
import org.uiautomation.ios.command.SetScriptTimeoutHandler;
import org.uiautomation.ios.servlet.WebDriverLikeCommand;
import org.uiautomation.ios.wkrdp.events.ChildIframeInserted;
import org.uiautomation.ios.wkrdp.events.ChildNodeRemoved;
import org.uiautomation.ios.wkrdp.events.Event;
import org.uiautomation.ios.wkrdp.events.EventFactory;
import org.uiautomation.ios.wkrdp.model.NodeId;
import org.uiautomation.ios.wkrdp.model.RemoteObject;
import org.uiautomation.ios.wkrdp.model.RemoteObjectArray;
import org.uiautomation.ios.wkrdp.model.RemoteWebElement;

/** Command interactions with the browser's web inspector. */
public final class WebInspectorHelper implements Consumer<DevtoolsEvent> {
  private static final Logger log = Logger.getLogger(WebInspectorHelper.class.getName());

  private static final Duration DEFAULT_COMMAND_TIMEOUT = Duration.ofSeconds(60);
  private static final Duration DEFAULT_SCRIPT_TIMEOUT = Duration.ofSeconds(30);
  private static final Duration DEFAULT_PAGE_LOAD_TIMEOUT = Duration.ofSeconds(300);
  private static final ImmutableSet<String> PRIMITIVE_TYPES =
      ImmutableSet.of("boolean", "number", "string");

  private final ServerSideSession session;
  private final DOMContext context;
  private final Browser browser;

  public WebInspectorHelper(Browser browser, ServerSideSession session) {
    this.session = session;
    this.context = new DOMContext(this);
    this.browser = browser;
  }

  public JsonObject sendCommand(DevtoolsCommand command) {
    return sendCommand(command, DEFAULT_COMMAND_TIMEOUT);
  }

  public JsonObject sendCommand(DevtoolsCommand command, Duration timeout) {
    try {
      return browser.sendCommand(command, timeout).json();
    } catch (BrowserException e) {
      throw new WebDriverException(e);
    }
  }

  public PageId getPageIdentifier() {
    return browser.activePage();
  }

  public RemoteWebElement getDocument() {
    long deadline = System.currentTimeMillis() + DEFAULT_PAGE_LOAD_TIMEOUT.toMillis();
    return getDocument(deadline);
  }

  private RemoteWebElement getDocument(long deadline) {
    RemoteWebElement result = context.getDocument();
    if (result == null) {
      result = retrieveDocumentAndCheckReady(deadline);
      RemoteWebElement window = getMainWindow();
      context.setCurrentFrame(null, result, window);
    }
    return result;
  }

  private RemoteWebElement getMainWindow() {
    return new RemoteWebElement(new NodeId(0), this);
  }

  private RemoteWebElement retrieveDocumentAndCheckReady(long deadline) {
    RemoteWebElement element = null;
    String readyState = "";
    while (!readyState.equals("complete")) {
      if (deadline > 0 && System.currentTimeMillis() > deadline) {
        throw new TimeoutException("Timeout waiting to get the document.");
      }

      try {
        if (element == null) {
          element = retrieveDocument();
        }
        readyState = element.getRemoteObject().call(".readyState");
      } catch (WebDriverException e) {
        log.info("Caught exception waiting for document to be ready. Retrying...: " + e);
        element = null;
      }
    }
    return element;
  }

  private RemoteWebElement retrieveDocument() {
    JsonObject result = sendCommand(DOM.getDocument());
    JsonObject root = result.getJsonObject("root");
    RemoteWebElement rme = new RemoteWebElement(new NodeId(root.getInt("nodeId")), this);
    return rme;
  }

  public void get(String url) {
    try {
      context.eventsLock().lock();
      sendCommand(Page.navigate(url));
      context.newContext();
      checkForPageLoad();
      context.waitForLoadEvent();
      // wait for everything to be ready by fetching the doc.
      getDocument();
    } finally {
      context.eventsLock().unlock();
    }
  }

  public String getCurrentUrl() {
    RemoteWebElement document = getDocument();
    String f = "(function(arg) { var url=this.URL;return url;})";
    JsonObject response =
        sendCommand(
            Runtime.callFunctionOn(document.getRemoteObject().getId(), f).withReturnByValue(true));
    return cast(response);
  }

  // TODO: fix the element swapping.
  public Object executeScript(String script, JsonArray args) {
    try {
      ImmutableList<CallArgument> arguments = processScriptArguments(args);
      JsonObject response = getScriptResponse(script, arguments);
      return cast(response);
    } catch (Exception e) {
      throw new WebDriverException(e);
    }
  }

  private ImmutableList.Builder<CallArgument> toBuilder(ImmutableList<CallArgument> args) {
    return ImmutableList.<CallArgument>builder().addAll(args);
  }

  private JsonObject getScriptResponse(String script) {
    return getScriptResponse(script, ImmutableList.<CallArgument>of());
  }

  private JsonObject getScriptResponse(String script, ImmutableList<CallArgument> arguments) {
    RemoteWebElement document = getDocument();
    if (!context.isOnMainFrame()) {
      arguments =
          toBuilder(arguments)
              .add(callArgument().withObjectId(document.getRemoteObject().getId()))
              .add(callArgument().withObjectId(context.getWindow().getRemoteObject().getId()))
              .build();
      String contextObject =
          "{'document': arguments["
              + (arguments.size() - 2)
              + "], 'window': arguments["
              + (arguments.size() - 1)
              + "]}";
      script = "with (" + contextObject + ") {" + script + "}";
    }

    String funcDec = "(function() { " + script + "})";
    DevtoolsCommand cmd =
        Runtime.callFunctionOn(document.getRemoteObject().getId(), funcDec)
            .withArguments(arguments)
            .withReturnByValue(false);
    JsonObject response = sendCommand(cmd, getScriptTimeout());
    checkForJSErrors(response);
    return response;
  }

  public void checkForJSErrors(JsonObject response) {
    if (response.getBoolean("wasThrown")) {
      JsonObject details = response.getJsonObject("result");
      String desc = details.getString("description");
      throw new WebDriverException("JS error :" + desc);
    }
  }

  private ImmutableList<CallArgument> processScriptArguments(JsonArray args) {
    ImmutableList.Builder<CallArgument> argsBuilder = ImmutableList.builder();
    for (JsonValue arg : args) {
      if (ValueType.OBJECT.equals(arg.getValueType())) {
        JsonObject jsonArg = (JsonObject) arg;
        if (jsonArg.containsKey("ELEMENT")) {
          NodeId n = new NodeId(Integer.parseInt(jsonArg.getString("ELEMENT").split("_")[1]));
          RemoteWebElement rwep = new RemoteWebElement(n, this);
          argsBuilder.add(callArgument().withObjectId(rwep.getRemoteObject().getId()));
        } else {
          log.info("JsonObject without ELEMENT tag" + jsonArg);
        }
      } else if (ValueType.ARRAY.equals(arg.getValueType())) {
        JsonObject array = getScriptResponse("return " + arg + ";");
        argsBuilder.add(callArgument().withObjectId(getResponseBody(array).getString("objectId")));
      } else if (ValueType.FALSE.equals(arg.getValueType())) {
        argsBuilder.add(callArgument().withValue(false));
      } else if (ValueType.TRUE.equals(arg.getValueType())) {
        argsBuilder.add(callArgument().withValue(true));
      } else if (ValueType.NUMBER.equals(arg.getValueType())) {
        argsBuilder.add(callArgument().withValue(((JsonNumber) arg).longValue()));
      } else if (ValueType.STRING.equals(arg.getValueType())) {
        argsBuilder.add(callArgument().withValue(((JsonString) arg).getString()));
      } else {
        throw new WebDriverException("Unsupported argument type: " + arg.getValueType());
      }
    }
    return argsBuilder.build();
  }

  private JsonObject getResponseBody(JsonObject response) {
    JsonObject body = null;
    try {
      body =
          response.containsKey("result")
              ? response.getJsonObject("result")
              : response.getJsonObject("object");
    } catch (Exception e) {
      throw new WebDriverException(e);
    }

    if (body == null) {
      throw new RuntimeException("Error parsting " + response);
    }
    return body;
  }

  @SuppressWarnings("unchecked")
  public <T> T cast(JsonObject response) {
    JsonObject body = getResponseBody(response);
    try {
      return castResponseBody(body);
    } catch (Exception e) {
      throw new WebDriverException(e + " " + body.toString());
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T castResponseBody(JsonObject body) throws Exception {
    String type = body.getString("type");
    // handle null return
    if ("undefined".equals(type)) {
      return null;
    }

    // handle primitive types: "boolean", "number", "string"
    if (PRIMITIVE_TYPES.contains(type)) { // primitive type.
      if (type.equals("string")) {
        return (T) body.getString("value");
      } else if (type.equals("number")) {
        return (T) (Integer) body.getInt("value");
      } else {
        return (T) (Boolean) body.getBoolean("value");
      }
    }

    // handle objects
    if ("object".equals(type)) {
      if (body.containsKey("value") && body.isNull("value")) {
        return null;
      }

      if ("array".equals(body.getString("subtype"))) {
        RemoteObject array = new RemoteObject(body.getString("objectId"), this);
        RemoteObjectArray a = new RemoteObjectArray(array);
        ArrayList<Object> res = new ArrayList<>();
        for (Object ro : a) {
          res.add(ro);
        }
        return (T) res;
      }

      if (body.containsKey("objectId")) {
        if ("node".equals(body.getString("subtype"))
            || "Window".equals(body.getString("className"))) {
          return (T) new RemoteObject(body.getString("objectId"), this);
        } else {
          RemoteObject ro = new RemoteObject(body.getString("objectId"), this);

          JsonElement o = new JsonParser().parse(ro.stringify());
          return (T) o;
        }
      }
      return (T) new RemoteObject(body.getString("objectId"), this);
    }
    throw new RuntimeException("NI " + body);
  }

  public Object executeAsyncScript(String script, javax.json.JsonArray args) {
    try {
      // These are arrays so they can be passed back and forth as references with objectIds
      // The relevant information is always going to be on index 0
      String resultReadyObjectId =
          getResponseBody(getScriptResponse("return [];")).getString("objectId");
      String resultObjectId =
          getResponseBody(getScriptResponse("return [];")).getString("objectId");

      Boolean realResultFound = false;
      Object realResult = null;
      long whenToTimeout = System.currentTimeMillis() + SetScriptTimeoutHandler.timeout;

      ImmutableList<CallArgument> callbackArgs =
          ImmutableList.of(
              callArgument().withObjectId(resultObjectId),
              callArgument().withObjectId(resultReadyObjectId));
      JsonObject callbackFunction =
          getScriptResponse(
              "var async_results = arguments[0],"
                  + "    async_results_ready = arguments[1];"
                  + "return function(result) {"
                  + " async_results_ready[0] = true;"
                  + " async_results[0] = result;"
                  + "};",
              callbackArgs);

      ImmutableList<CallArgument> arguments =
          toBuilder(processScriptArguments(args))
              .add(
                  callArgument()
                      .withObjectId(getResponseBody(callbackFunction).getString("objectId")))
              .build();
      getScriptResponse(script, arguments);

      while (!realResultFound) {
        Thread.sleep(10);

        ImmutableList<CallArgument> resultReady =
            ImmutableList.of(callArgument().withObjectId(resultReadyObjectId));
        realResultFound =
            (Boolean) cast(getScriptResponse("return !! arguments[0][0];", resultReady));

        if (realResultFound) {
          ImmutableList<CallArgument> result =
              ImmutableList.of(callArgument().withObjectId(resultObjectId));
          realResult = cast(getScriptResponse("return arguments[0][0];", result));
        } else {
          if (System.currentTimeMillis() > whenToTimeout) {
            throw new TimeoutException("Timeout waiting for async script callback.");
          }
        }
      }

      return realResult;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new WebDriverException(e);
    } catch (Exception e) {
      throw new WebDriverException(e);
    }
  }

  public String getDocumentReadyState() {
    String state = null;
    try {
      state =
          (String)
              executeScript("var state = document.readyState; return state", JavaxJson.EMPTY_ARRAY);
    } catch (Exception e) {
      // Arguments should belong to the same JavaScript world as the target object.
      System.err.println("error, reseting because " + e.getMessage());
      context.reset();
      return "unknown";
    }
    return state;
  }

  public void checkForPageLoad() {}

  private long getTimeout() {
    if (session != null) {
      long timeout =
          (Long) session.configure(WebDriverLikeCommand.URL).opt("page load", Long.valueOf(-1));
      if (timeout >= 0) {
        return timeout;
      }
    }
    return DEFAULT_PAGE_LOAD_TIMEOUT.toMillis();
  }

  private Duration getScriptTimeout() {
    if (session != null) {
      long timeout =
          (Long)
              session
              .configure(WebDriverLikeCommand.EXECUTE_SCRIPT)
              .opt("script", Long.valueOf(-1));
      if (timeout >= 0) {
        return Duration.ofMillis(timeout);
      }
    }
    return DEFAULT_SCRIPT_TIMEOUT;
  }

  @Override
  public void accept(DevtoolsEvent event) {
    Event e = EventFactory.createEvent(event);
    if ((e instanceof ChildIframeInserted || e instanceof ChildNodeRemoved)) {
      context.domHasChanged(e);
    }
    if ("Page.frameDetached".equals(event.method())) {
      context.frameDied();
    }
    if ("Page.loadEventFired".equals(event.method())) {
      context.signalNewPageLoadReceived();
    }
  }

  public DOMContext getContext() {
    return context;
  }
}
