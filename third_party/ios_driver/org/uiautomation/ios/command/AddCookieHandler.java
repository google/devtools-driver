// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.uiautomation.ios.command;

import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.json.JsonObject;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;

/**
 * Handles adding cookies.
 */
public class AddCookieHandler extends CommandHandler {
  public AddCookieHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  @Override
  public Response handle() throws Exception {
    JsonObject payload = getRequest().getPayload();
    JsonObject cookie = payload.getJsonObject("cookie");

    String name = cookie.getString("name", "");
    String value = cookie.getString("value", "");
    String path = cookie.getString("path", "/");
    String domain;
    if (cookie.containsKey("domain")) {
      domain = cookie.getString("domain");
    } else {
      URL url = new URL(getWebDriver().getCurrentUrl());
      domain = url.getHost();
    }
    boolean secure = cookie.getBoolean("secure", false);
    boolean httpOnly = cookie.getBoolean("httpOnly", false);
    Instant expiry =
        cookie.containsKey("expiry")
        ? Instant.ofEpochSecond(cookie.getJsonNumber("expiry").longValueExact())
        : OffsetDateTime.now(ZoneOffset.UTC).plusYears(20).toInstant();

    getWebDriver().addCookie(name, value, path, domain, secure, httpOnly, expiry);
    Response res = new Response();
    res.setSessionId(getSession().getSessionId());
    res.setStatus(0);
    return res;
  }
}
