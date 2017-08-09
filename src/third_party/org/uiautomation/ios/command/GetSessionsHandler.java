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

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.ServerSideSession;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;

public class GetSessionsHandler extends CommandHandler {
  public GetSessionsHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  @Override
  public Response handle() throws Exception {
    JSONArray res = new JSONArray();
    for (ServerSideSession s : getServer().getSessions()) {
      JSONObject session = new JSONObject();
      session.put("id", s.getSessionId());
      session.put("capabilities", new DesiredCapabilities());
      res.put(session);
    }

    Response resp = new Response();
    resp.setSessionId("dummy one");
    resp.setStatus(0);
    resp.setValue(res.toString());
    return resp;
  }
}
