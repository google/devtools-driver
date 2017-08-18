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

import java.util.ArrayList;
import java.util.List;
import javax.json.JsonObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;
import org.uiautomation.ios.wkrdp.model.RemoteWebElement;

public class FindElementsHandler extends CommandHandler {
  public FindElementsHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  @Override
  public Response handle() throws Exception {
    waitForPageToLoad();

    int implicitWait = getConf("implicit_wait", 0);
    long deadline = System.currentTimeMillis() + implicitWait;
    List<RemoteWebElement> elements = null;
    do {
      try {
        elements = findElements();
        if (elements.size() != 0) {
          break;
        }
      } catch (NoSuchElementException e) {
        // Ignore and try again.
      }
    } while (System.currentTimeMillis() < deadline);

    List<com.google.gson.JsonObject> list = new ArrayList<>();
    for (RemoteWebElement el : elements) {
      com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
      jsonObject.addProperty("ELEMENT", el.getReference());
      list.add(jsonObject);
    }

    Response resp = new Response();
    resp.setSessionId(getSession().getSessionId());
    resp.setStatus(0);
    resp.setValue(list);
    return resp;
  }

  private List<RemoteWebElement> findElements() throws Exception {
    JsonObject payload = getRequest().getPayload();
    String type = payload.getString("using");
    String value = payload.getString("value");

    RemoteWebElement element = null;

    if (getRequest().hasVariable(":reference")) {
      String ref = getRequest().getVariableValue(":reference");
      element = getWebDriver().createElement(ref);
    } else {
      element = getWebDriver().getDocument();
    }

    List<RemoteWebElement> res;
    if ("link text".equals(type)) {
      res = element.findElementsByLinkText(value, false);
    } else if ("partial link text".equals(type)) {
      res = element.findElementsByLinkText(value, true);
    } else if ("xpath".equals(type)) {
      res = element.findElementsByXpath(value);
    } else {
      String cssSelector = ToCSSSelectorConverter.convertToCSSSelector(type, value);
      res = element.findElementsByCSSSelector(cssSelector);
    }
    return res;
  }
}
