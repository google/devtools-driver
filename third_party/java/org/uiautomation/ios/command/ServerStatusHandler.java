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

import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.ServerSideSession;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;

public class ServerStatusHandler extends CommandHandler {
  public ServerStatusHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  @Override
  public Response handle() throws Exception {
    JSONObject res = generateStatus();

    Set<ServerSideSession> sessions = getServer().getSessions();
    Response resp = new Response();

    resp.setStatus(0);
    resp.setValue(res);
    if (sessions.size() == 0) {
      resp.setSessionId(null);
    } else if (sessions.size() == 1) {
      resp.setSessionId(sessions.iterator().next().getSessionId());
    } else {
      throw new WebDriverException("NI multi sessions per server.");
    }
    return resp;
  }

  public static JSONObject generateStatus() throws JSONException {
    JSONObject res = new JSONObject();
    res.put("state", "success");
    res.put(
        "os",
        new JSONObject()
            .put("name", System.getProperty("os.name"))
            .put("arch", System.getProperty("os.arch"))
            .put("version", System.getProperty("os.version")));

    res.put("java", new JSONObject().put("version", System.getProperty("java.version")));
    return res;
  }
}
