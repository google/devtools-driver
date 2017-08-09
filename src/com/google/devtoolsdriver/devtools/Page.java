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

package com.google.devtoolsdriver.devtools;

import static com.google.devtoolsdriver.devtools.DevtoolsDomain.PAGE;

import javax.json.JsonObject;

/**
 * Factory for messages in the devtools Page domain. For a specification of this domain's methods,
 * see the <a href="https://chromedevtools.github.io/debugger-protocol-viewer/tot/Page/">debugging
 * protocol viewer</a>. Note that not all the domain's methods have been implemented yet.
 */
public final class Page {
  private static DevtoolsCommand command(String methodSuffix) {
    return new DevtoolsCommand.NoOptionals(PAGE.methodName(methodSuffix));
  }

  public static DevtoolsCommand enable() {
    return command("enable");
  }

  public static DevtoolsCommand disable() {
    return command("disable");
  }

  public static DevtoolsCommand addScriptToEvaluateOnLoad(String scriptSource) {
    return command("addScriptToEvaluateOnLoad").with("scriptSource", scriptSource);
  }

  public static DevtoolsCommand removeScriptToEvaluateOnLoad(String identifier) {
    return command("removeScriptToEvaluateOnLoad").with("identifier", identifier);
  }

  public static DevtoolsCommand setAutoAttachToCreatedPages(boolean autoAttach) {
    return command("setAutoAttachToCreatedPages").with("autoAttach", autoAttach);
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class ReloadCommand extends DevtoolsCommand.WithOptionals<ReloadCommand> {
    private ReloadCommand() {
      super(PAGE.methodName("reload"));
    }

    private ReloadCommand(JsonObject command) {
      super(PAGE.methodName("reload"), command);
    }

    public ReloadCommand withIgnoreCache(boolean ignoreCache) {
      return with("ignoreCache", ignoreCache);
    }

    public ReloadCommand withScriptToEvaluateOnLoad(String scriptToEvaluateOnLoad) {
      return with("scriptToEvaluateOnLoad", scriptToEvaluateOnLoad);
    }

    @Override
    ReloadCommand create(JsonObject params) {
      return new ReloadCommand(params);
    }
  }

  public static ReloadCommand reload() {
    return new ReloadCommand();
  }

  public static DevtoolsCommand navigate(String url) {
    return command("navigate").with("url", url);
  }

  public static DevtoolsCommand getNavigationHistory() {
    return command("getNavigationHistory");
  }

  public static DevtoolsCommand navigateToHistoryEntry(int entryId) {
    return command("navigateToHistoryEntry").with("entryId", entryId);
  }

  /** Moved to Network in the latest tip-of-tree */
  public static DevtoolsCommand getCookies() {
    return command("getCookies");
  }

  /** Moved to network in the latest tip-of-tree */
  public static DevtoolsCommand deleteCookie(String cookieName, String url) {
    return command("deleteCookie").with("cookieName", cookieName).with("url", url);
  }

  public static DevtoolsCommand getResourceTree() {
    return command("getResourceTree");
  }

  public static DevtoolsCommand getResourceContent(String frameId, String url) {
    return command("getResourceContent").with("url", url).with("frameId", frameId);
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class SearchInResourceCommand
      extends DevtoolsCommand.WithOptionals<SearchInResourceCommand> {
    private SearchInResourceCommand() {
      super(PAGE.methodName("searchInResource"));
    }

    private SearchInResourceCommand(JsonObject params) {
      super(PAGE.methodName("searchInResource"), params);
    }

    public SearchInResourceCommand withCaseSensitive(boolean caseSensitive) {
      return with("caseSensitive", caseSensitive);
    }

    public SearchInResourceCommand withIsRegex(boolean isRegex) {
      return with("isRegex", isRegex);
    }

    @Override
    SearchInResourceCommand create(JsonObject params) {
      return new SearchInResourceCommand(params);
    }
  }

  public static SearchInResourceCommand searchInResource(String frameId, String url, String query) {
    return new SearchInResourceCommand()
        .with("frameId", frameId)
        .with("url", url)
        .with("query", query);
  }

  public static DevtoolsCommand setDocumentContent(String frameId, String html) {
    return command("setDocumentContent").with("frameId", frameId).with("html", html);
  }

  public static DevtoolsCommand captureScreenshot() {
    return command("captureScreenshot");
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class StartScreencastCommand
      extends DevtoolsCommand.WithOptionals<StartScreencastCommand> {
    private StartScreencastCommand() {
      super(PAGE.methodName("startScreencast"));
    }

    private StartScreencastCommand(JsonObject command) {
      super(PAGE.methodName("startScreencast"), command);
    }

    public StartScreencastCommand withFormat(String format) {
      return with("format", format);
    }

    public StartScreencastCommand withQuality(long quality) {
      return with("quality", quality);
    }

    public StartScreencastCommand withMaxHeight(long maxHeight) {
      return with("maxHeight", maxHeight);
    }

    public StartScreencastCommand withEveryNthFrame(long everyNthFrame) {
      return with("everyNthFrame", everyNthFrame);
    }

    @Override
    StartScreencastCommand create(JsonObject params) {
      return new StartScreencastCommand(params);
    }
  }

  public static StartScreencastCommand startScreencast() {
    return new StartScreencastCommand();
  }

  public static DevtoolsCommand stopScreencast() {
    return command("stopScreencast");
  }

  public static DevtoolsCommand screencastFrameAck(int sessionId) {
    return command("screencastFrameAck").with("sessionId", sessionId);
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class HandleJavaScriptDialogCommand
      extends DevtoolsCommand.WithOptionals<HandleJavaScriptDialogCommand> {
    private HandleJavaScriptDialogCommand() {
      super(PAGE.methodName("handleJavaScriptDialog"));
    }

    private HandleJavaScriptDialogCommand(JsonObject params) {
      super(PAGE.methodName("handleJavaScriptDialog"), params);
    }

    public HandleJavaScriptDialogCommand withPromptText(String promptText) {
      return with("promptText", promptText);
    }

    @Override
    HandleJavaScriptDialogCommand create(JsonObject params) {
      return new HandleJavaScriptDialogCommand(params);
    }
  }

  public static HandleJavaScriptDialogCommand handleJavaScriptDialog(boolean accept) {
    return new HandleJavaScriptDialogCommand().with("accept", accept);
  }

  public static DevtoolsCommand setColorPickerEnabled(boolean enabled) {
    return command("setColorPickerEnabled").with("enabled", enabled);
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class SetOverlayMessageCommand
      extends DevtoolsCommand.WithOptionals<SetOverlayMessageCommand> {
    private SetOverlayMessageCommand() {
      super(PAGE.methodName("setOverlayMessage"));
    }

    private SetOverlayMessageCommand(JsonObject command) {
      super(PAGE.methodName("setOverlayMessage"), command);
    }

    public SetOverlayMessageCommand withMessage(String message) {
      return with("message", message);
    }

    @Override
    SetOverlayMessageCommand create(JsonObject command) {
      return new SetOverlayMessageCommand(command);
    }
  }

  public static SetOverlayMessageCommand setOverlayMessage() {
    return new SetOverlayMessageCommand();
  }

  public static DevtoolsCommand getAppManifest() {
    return command("getAppManifest");
  }

  public static DevtoolsCommand setBlockedEventsWarningThreshold(long threshold) {
    return command("setBlockedEventsWarningThreshold").with("threshold", threshold);
  }

  private Page() {}
}
