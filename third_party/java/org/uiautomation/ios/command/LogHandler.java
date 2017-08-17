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
import javax.json.JsonObject;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.ServerSideSession;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;

public class LogHandler extends CommandHandler {
  public LogHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  @Override
  public Response handle() throws Exception {
    ServerSideSession session = getSession();
    JsonObject payload = getRequest().getPayload();
    String type = payload.getString("type");
    List<LogEntry> entries = session.getLogManager().getLog(type);
    return createResponse(entries);
  }
}
