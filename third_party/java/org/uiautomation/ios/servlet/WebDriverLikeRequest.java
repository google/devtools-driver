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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.ByteStreams;
import com.google.devtoolsdriver.util.JavaxJson;
import java.io.IOException;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

public class WebDriverLikeRequest {
  private final String method;
  private final String path;
  private final JsonObject payload;

  WebDriverLikeRequest(HttpServletRequest request) throws IOException {
    method = request.getMethod();
    path = request.getPathInfo();
    payload = readPayloadFromRequest(request);
  }

  private static JsonObject readPayloadFromRequest(HttpServletRequest request) throws IOException {
    byte[] payloadBytes = ByteStreams.toByteArray(request.getInputStream());
    return payloadBytes.length == 0
        ? JavaxJson.EMPTY_OBJECT
        : JavaxJson.parseObject(new String(payloadBytes, UTF_8));
  }

  @Override
  public String toString() {
    String res = method + ":" + path;
    if (!payload.isEmpty()) {
      res += "\n\tbody:" + payload;
    }
    return res;
  }

  public JsonObject getPayload() {
    return payload;
  }

  public WebDriverLikeCommand getGenericCommand() {
    return WebDriverLikeCommand.getCommand(method, path);
  }

  public String getVariableValue(String variable) {
    WebDriverLikeCommand genericCommand = getGenericCommand();
    int i = genericCommand.getIndex(variable);
    String[] pieces = path.split("/");
    return pieces[i];
  }

  public boolean hasVariable(String variable) {
    WebDriverLikeCommand genericCommand = getGenericCommand();
    boolean ok = genericCommand.path().contains(variable);
    return ok;
  }

  public String getSession() {
    return getVariableValue(":sessionId");
  }

  public boolean hasSession() {
    return hasVariable(":sessionId");
  }
}
