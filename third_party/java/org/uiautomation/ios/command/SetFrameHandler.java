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

import java.util.List;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;
import org.uiautomation.ios.wkrdp.model.RemoteWebElement;

public class SetFrameHandler extends CommandHandler {
  public SetFrameHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  // NoSuchWindow - If the currently selected window has been closed.
  // NoSuchFrame - If the frame specified by id cannot be found.
  @Override
  public Response handle() throws Exception {
    JsonValue p = getRequest().getPayload().get("id");

    if (JsonValue.NULL.equals(p)) {
      getWebDriver().getContext().setCurrentFrame(null, null, null);
    } else {
      RemoteWebElement iframe;
      switch (p.getValueType()) {
        case NUMBER:
          iframe = getIframe(((JsonNumber) p).intValue());
          break;
        case OBJECT:
          String id = ((JsonObject) p).getString("ELEMENT");
          iframe = getWebDriver().createElement(id);
          break;
        case STRING:
          iframe = getIframe(((JsonString) p).getString());
          break;
        default:
          throw new UnsupportedCommandException("cannot select frame by " + p.getClass());
      }
      RemoteWebElement document = iframe.getContentDocument();
      RemoteWebElement window = iframe.getContentWindow();
      getWebDriver().getContext().setCurrentFrame(iframe, document, window);
    }

    Response res = new Response();
    res.setSessionId(getSession().getSessionId());
    res.setStatus(0);
    res.setValue(new JSONObject());
    return res;
  }

  private RemoteWebElement getIframe(Integer index) throws Exception {
    List<RemoteWebElement> iframes = getWebDriver().findElementsByCssSelector(
        "iframe,frame");
    try {
      return iframes.get(index);
    } catch (IndexOutOfBoundsException i) {
      throw new NoSuchFrameException(
          "detected " + iframes.size() + " frames. Cannot get index = " + index);
    }
  }

  private RemoteWebElement getIframe(String id) throws Exception {
    RemoteWebElement currentDocument = getWebDriver().getDocument();

    String
        selector =
        "iframe[name='" + id + "'],iframe[id='" + id + "'],frame[name='" + id + "'],frame[id='" + id
        + "']";
    try {
      RemoteWebElement frame = currentDocument.findElementByCSSSelector(selector);
      return frame;
    } catch (NoSuchElementException e) {
      throw new NoSuchFrameException(e.getMessage(), e);
    }
  }
}
