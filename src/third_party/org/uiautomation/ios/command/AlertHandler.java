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

import org.openqa.selenium.remote.ErrorCodes;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.IOSServerManager;
import org.uiautomation.ios.servlet.WebDriverLikeRequest;

/**
 * Handles WebDriver alert commands.
 */
public class AlertHandler extends CommandHandler {
  public AlertHandler(IOSServerManager driver, WebDriverLikeRequest request) {
    super(driver, request);
  }

  @Override
  public Response handle() throws Exception {
    // We don't have any way currently of dealing with alert dialogs with pure JS, so we respond to
    // every alert command with the "no alert present" error code.
    Response response = new Response();
    response.setSessionId(getSession().getSessionId());
    response.setStatus(ErrorCodes.NO_ALERT_PRESENT);
    response.setValue(new ErrorCodes().toState(ErrorCodes.NO_ALERT_PRESENT));
    return response;
  }
}
