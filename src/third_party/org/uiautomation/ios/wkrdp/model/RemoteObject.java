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

import com.google.common.collect.ImmutableList;
import com.google.devtoolsdriver.devtools.DOM;
import com.google.devtoolsdriver.devtools.DevtoolsCommand;
import com.google.devtoolsdriver.devtools.Runtime;
import com.google.devtoolsdriver.webdriver.JsAtoms;
import javax.json.JsonObject;
import org.json.JSONException;
import org.uiautomation.ios.wkrdp.WebInspectorHelper;

/** A remote object on a webpage. */
public final class RemoteObject {
  private final String objectId;
  private final WebInspectorHelper inspector;

  public RemoteObject(String objectId, WebInspectorHelper inspector) {
    this.inspector = inspector;
    this.objectId = objectId;
  }

  public String getId() {
    return objectId;
  }

  public RemoteWebElement getWebElement() throws JSONException, Exception {
    JsonObject result = inspector.sendCommand(DOM.requestNode(objectId));
    int id = result.getInt("nodeId");
    NodeId nodeId = new NodeId(id);
    return new RemoteWebElement(nodeId, this, inspector);
  }

  @Override
  public String toString() {
    return objectId;
  }

  public <T> T call(String function) {
    String f = "(function(arg) { var res = this" + function + "; return res;})";
    DevtoolsCommand cmd =
        Runtime.callFunctionOn(getId(), f)
            .withReturnByValue(false)
            .withArguments(ImmutableList.of(Runtime.callArgument().withValue("")));
    JsonObject response = inspector.sendCommand(cmd);
    return inspector.cast(response);
  }

  public String stringify() {
    String f = "(function() { var res = " + JsAtoms.stringify("this") + "; return res;})";
    DevtoolsCommand cmd = Runtime.callFunctionOn(getId(), f).withReturnByValue(false);
    JsonObject response = inspector.sendCommand(cmd);
    return inspector.cast(response);
  }
}
