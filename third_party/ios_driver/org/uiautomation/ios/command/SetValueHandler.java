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

import com.google.devtoolsdriver.util.JavaxJson;
import javax.json.JsonArray;
import javax.json.JsonValue;
import org.json.JSONObject;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;
import org.uiautomation.ios.wkrdp.model.RemoteWebElement;

public class SetValueHandler extends CommandHandler {
  public SetValueHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  @Override
  public Response handle() throws Exception {
    String ref = getRequest().getVariableValue(":reference");
    RemoteWebElement element = getWebDriver().createElement(ref);
    JsonArray array = getRequest().getPayload().getJsonArray("value");

    String value = "";
    for (JsonValue jsonValue : array) {
      value += JavaxJson.toJavaObject(jsonValue);
    }
    element.setValueAtoms(value);

    Response res = new Response();
    res.setSessionId(getSession().getSessionId());
    res.setStatus(0);
    res.setValue(new JSONObject());
    return res;
  }
}