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
package org.uiautomation.ios.wkrdp.model;

import static com.google.devtoolsdriver.devtools.Runtime.callArgument;

import com.google.common.collect.ImmutableList;
import com.google.devtoolsdriver.devtools.DOM;
import com.google.devtoolsdriver.devtools.DevtoolsCommand;
import com.google.devtoolsdriver.devtools.Runtime;
import com.google.devtoolsdriver.devtools.Runtime.CallArgument;
import com.google.iosdevicecontrol.util.JavaxJson;
import com.google.devtoolsdriver.webdriver.JsAtoms;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.uiautomation.ios.wkrdp.WebInspectorHelper;

public class RemoteWebElement {
  private final WebInspectorHelper inspector;
  private final NodeId nodeId;
  private RemoteObject remoteObject;

  public RemoteWebElement(NodeId id, WebInspectorHelper inspector) {
    if (inspector == null) {
      throw new WebDriverException("inspector cannot be null.");
    }
    this.inspector = inspector;
    this.nodeId = id;
  }

  public RemoteWebElement(NodeId nodeId, RemoteObject remoteObject, WebInspectorHelper inspector)
      throws Exception {
    this(nodeId, inspector);
    this.remoteObject = remoteObject;
  }

  public String getReference() {
    return inspector.getPageIdentifier().asString() + "_" + getNodeId().getId();
  }

  public void click() {
    clickAtom();
    inspector.checkForPageLoad();
  }

  private void clickAtom() {
    try {
      String f = "(function(arg) { var text = " + JsAtoms.tap("arg") + "; return text;})";
      JsonObject response =
          getInspectorResponse(f, true, callArgument().withObjectId(getRemoteObject().getId()));
      inspector.cast(response);
    } catch (Exception e) {
      throw new WebDriverException(e);
    }
  }

  public void moveTo() {
    String f = "(function(arg) { var text = " + JsAtoms.moveMouse("arg") + "; return text;})";
    JsonObject response =
        getInspectorResponse(f, true, callArgument().withObjectId(getRemoteObject().getId()));
    inspector.cast(response);
  }

  public NodeId getNodeId() {
    return nodeId;
  }

  public boolean exists() {
    try {
      inspector.sendCommand(DOM.resolveNode(nodeId.getId()));
      return true;
    } catch (Exception e) {
      if ("No node with given id found".equals(e.getMessage())) {
        return false;
      }
      throw new RuntimeException("case not implemented" + e.getMessage());
    }
  }

  public RemoteObject getRemoteObject() {
    if (remoteObject == null) {
      JsonObject response = inspector.sendCommand(DOM.resolveNode(nodeId.getId()));
      remoteObject = inspector.cast(response);
    }
    return remoteObject;
  }

  public String getText() throws Exception {
    String f =
        "(function(arg) { "
            + "var text = "
            + JsAtoms.getVisibleText("arg")
            + ";"
            + "return text;})";
    JsonObject response =
        getInspectorResponse(f, true, callArgument().withObjectId(getRemoteObject().getId()));
    return inspector.cast(response);
  }

  public Point getLocation() throws Exception {
    String f =
        "(function(arg) { "
            + "var loc = "
            + JsAtoms.getLocation("arg")
            + ";"
            + "return "
            + JsAtoms.stringify("loc")
            + ";})";
    JsonObject response =
        getInspectorResponse(f, true, callArgument().withObjectId(getRemoteObject().getId()));
    String s = inspector.cast(response);
    JsonObject o = JavaxJson.parseObject(s);
    return new Point(o.getInt("left"), o.getInt("top"));
  }

  public Dimension getSize() throws Exception {
    String f =
        "(function(arg) { "
            + "var size = "
            + JsAtoms.getSize("arg")
            + ";"
            + "return "
            + JsAtoms.stringify("size")
            + ";})";
    JsonObject response =
        getInspectorResponse(f, true, callArgument().withObjectId(getRemoteObject().getId()));
    String s = inspector.cast(response);
    JsonObject o = JavaxJson.parseObject(s);
    return new Dimension(o.getInt("width"), o.getInt("height"));
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String attributeName) {
    T res;
    if (attributeName.indexOf('-') != -1
        || attributeName.indexOf(':') != -1
        || attributeName.indexOf('.') != -1) {
      res = (T) getRemoteObject().call(".getAttribute('" + attributeName + "')");
    } else {
      res = (T) getRemoteObject().call("." + attributeName);
    }
    if (res == null || "class".equals(attributeName)) {
      // textarea.value != testarea.getAttribute("value");
      res = (T) getRemoteObject().call(".getAttribute('" + attributeName + "')");
    }
    if (res == null) {
      return (T) "";
    } else {
      return res;
    }
  }

  public String getCssValue(String propertyName) throws Exception {
    String f =
        "(function(element,value) { var result = "
            + JsAtoms.getEffectiveStyle("element", "value")
            + "; return result;})";
    JsonObject response =
        getInspectorResponse(
            f,
            true,
            callArgument().withObjectId(getRemoteObject().getId()),
            callArgument().withValue(propertyName));
    return inspector.cast(response);
  }

  public boolean isSelected() throws Exception {
    String f =
        "(function(arg) { "
            + "var isDisplayed = "
            + JsAtoms.isSelected("arg")
            + ";"
            + "return isDisplayed;})";
    return inspector.cast(
        getInspectorResponse(f, true, callArgument().withObjectId(getRemoteObject().getId())));
  }

  public boolean isEnabled() throws Exception {
    String f =
        "(function(arg) { "
            + "var isEnabled = "
            + JsAtoms.isEnabled("arg")
            + ";"
            + "return isEnabled;})";
    return inspector.cast(
        getInspectorResponse(f, true, callArgument().withObjectId(getRemoteObject().getId())));
  }

  public boolean isDisplayed() throws Exception {
    String f =
        "(function(arg) { "
            + "var isDisplayed = "
            + JsAtoms.isShown("arg")
            + ";"
            + "return isDisplayed;})";
    return inspector.cast(
        getInspectorResponse(f, true, callArgument().withObjectId(getRemoteObject().getId())));
  }

  public RemoteWebElement findElementByLinkText(String text, boolean partialMatch)
      throws Exception {
    String ifStatement;
    if (partialMatch) {
      ifStatement = "if ( elements[i].innerText.indexOf(text) != -1 ){";
    } else {
      ifStatement = "if (text === elements[i].innerText ){";
    }
    String f =
        "(function(text) { "
            + "var elements = this.querySelectorAll('a');"
            + "for ( var i =0;i<elements.length;i++){"
            + ifStatement
            + "  return elements[i];"
            + "}" // end
            // if
            + "}" // end for
            + "return null;"
            + "})"; // end function
    JsonObject response = getInspectorResponse(f, false, callArgument().withValue(text));
    RemoteObject ro = inspector.cast(response);
    if (ro == null) {
      return null;
    } else {
      return ro.getWebElement();
    }
  }

  public List<RemoteWebElement> findElementsByLinkText(String text, boolean partialMatch)
      throws Exception {
    String ifStatement;
    if (partialMatch) {
      ifStatement = "if ( elements[i].innerText.indexOf(text) != -1 ){";
    } else {
      ifStatement = "if (text === elements[i].innerText ){";
    }

    String f =
        "(function(text) { "
            + "var elements = this.querySelectorAll('a');"
            + "var result = new Array();"
            + "for ( var i =0;i<elements.length;i++){"
            + ifStatement
            + "  result.push(elements[i]);"
            + "}" // end
            // if
            + "}" // end for
            + "return result;"
            + "})"; // end function
    JsonObject response = getInspectorResponse(f, false, callArgument().withValue(text));
    List<RemoteObject> ros = inspector.cast(response);
    List<RemoteWebElement> res = new ArrayList<>();
    for (RemoteObject ro : ros) {
      res.add(ro.getWebElement());
    }
    return res;
  }

  public RemoteWebElement findElementByCSSSelector(String selector) {
    JsonObject response = inspector.sendCommand(DOM.querySelector(nodeId.getId(), selector));
    // TODO freynaud
    NodeId id = new NodeId(response.getInt("nodeId"));
    if (!id.exist()) {
      throw new NoSuchElementException("no element matching " + selector);
    }
    RemoteWebElement res = new RemoteWebElement(id, inspector);
    return res;
  }

  public List<RemoteWebElement> findElementsByCSSSelector(String selector) {
    JsonObject response = inspector.sendCommand(DOM.querySelectorAll(nodeId.getId(), selector));
    JsonArray nodeIds = response.getJsonArray("nodeIds");
    List<RemoteWebElement> res = new ArrayList<>();
    for (int i = 0; i < nodeIds.size(); i++) {
      NodeId id = new NodeId(nodeIds.getInt(i));
      res.add(new RemoteWebElement(id, inspector));
    }
    return res;
  }

  @Override
  public String toString() {
    try {
      String remoteElement = remoteObject == null ? "not loaded" : remoteObject.getId();
      return "nodeId=" + nodeId + " , remoteElement " + remoteElement; // +
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  public RemoteWebElement getContentDocument() throws Exception {
    JsonObject response =
        getInspectorResponse(
            "(function(arg) { var document = this.contentDocument; return document;})", false);
    RemoteObject ro = inspector.cast(response);
    if (ro == null) {
      throw new NoSuchFrameException("Cannot find the document associated with the frame.");
    } else {
      return ro.getWebElement();
    }
  }

  public void submit() throws Exception {
    String f = "(function(arg) { " + "var text = " + JsAtoms.submit("arg") + ";" + "return text;})";
    JsonObject response =
        getInspectorResponse(f, false, callArgument().withObjectId(getRemoteObject().getId()));
    inspector.cast(response);
    inspector.checkForPageLoad();
  }

  public RemoteWebElement findElementByXpath(String xpath) throws Exception {
    String f =
        "(function(xpath, element) { var result = "
            + JsAtoms.xpath("xpath", "element")
            + ";"
            + "return result;})";
    JsonObject response =
        getInspectorResponse(
            f,
            false,
            callArgument().withValue(xpath),
            callArgument().withObjectId(getRemoteObject().getId()));
    RemoteObject ro = inspector.cast(response);
    if (ro == null) {
      throw new NoSuchElementException("cannot find element by Xpath " + xpath);
    } else {
      return ro.getWebElement();
    }
  }

  public List<RemoteWebElement> findElementsByXpath(String xpath) throws Exception {
    String f =
        "(function(xpath, element) { var results = "
            + JsAtoms.xpaths("xpath", "element")
            + ";"
            + "return results;})";
    JsonObject response =
        getInspectorResponse(
            f,
            false,
            callArgument().withValue(xpath),
            callArgument().withObjectId(getRemoteObject().getId()));
    List<RemoteObject> ros = inspector.cast(response);
    List<RemoteWebElement> res = new ArrayList<>();
    for (RemoteObject ro : ros) {
      res.add(ro.getWebElement());
    }
    return res;
  }

  public void setValueAtoms(String value) throws Exception {
    value = replaceSpecialKeys(value);
    String f =
        "(function(element,value) { var result = "
            + JsAtoms.type("element", "value")
            + ";"
            + "return result;})";
    getInspectorResponse(
        f,
        false,
        callArgument().withObjectId(getRemoteObject().getId()),
        callArgument().withValue(value));
  }

  private String replaceSpecialKeys(String value) {
    value = value.replace(Keys.RETURN.toString().charAt(0), '\r');
    value = value.replace(Keys.ENTER.toString().charAt(0), '\r');
    // TODO: more keys to replace?
    return value;
  }

  public void clear() throws Exception {
    String f =
        "(function(element) { " + "var text = " + JsAtoms.clear("element") + ";" + "return text;})";
    JsonObject response =
        getInspectorResponse(
            f, true, Runtime.callArgument().withObjectId(getRemoteObject().getId()));
    inspector.cast(response);
  }

  public RemoteWebElement getContentWindow() throws Exception {
    JsonObject response =
        getInspectorResponse(
            "(function(arg) { var window = this.contentWindow; return window;})", false);
    RemoteObject ro = inspector.cast(response);
    if (ro == null) {
      throw new NoSuchFrameException("Cannot find the window associated with the frame.");
    } else {
      return ro.getWebElement();
    }
  }

  public String getTagName() {
    String tag = getAttribute("tagName");
    return tag.toLowerCase();
  }

  private JsonObject getInspectorResponse(
      String javascript, boolean returnByValue, CallArgument... args) {
    DevtoolsCommand cmd =
        Runtime.callFunctionOn(getRemoteObject().getId(), javascript)
            .withReturnByValue(returnByValue)
            .withArguments(ImmutableList.copyOf(args));
    JsonObject response = inspector.sendCommand(cmd);
    inspector.checkForJSErrors(response);
    return response;
  }
}
