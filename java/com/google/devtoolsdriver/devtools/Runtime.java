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

import static com.google.devtoolsdriver.devtools.DevtoolsDomain.RUNTIME;

import java.util.List;
import javax.json.JsonObject;

/**
 * Factory for messages in the devtools Runtime domain. For a specification of this domain's
 * methods, see the <a href="https://chromedevtools.github.io/debugger-protocol-viewer/tot/Runtime/"
 * >debugging protocol viewer</a>. Note that not all the domain's methods have been implemented yet.
 */
public final class Runtime {
  private static DevtoolsCommand command(String methodSuffix) {
    return new DevtoolsCommand.NoOptionals(RUNTIME.methodName(methodSuffix));
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class EvaluateCommand extends DevtoolsCommand.WithOptionals<EvaluateCommand> {
    private EvaluateCommand() {
      super(RUNTIME.methodName("evaluate"));
    }

    private EvaluateCommand(JsonObject params) {
      super(RUNTIME.methodName("evaluate"), params);
    }

    public EvaluateCommand withObjectGroup(String objectGroup) {
      return with("objectGroup", objectGroup);
    }

    public EvaluateCommand withIncludeCommandLineApi(boolean includeCommandLineApi) {
      return with("includeCommandLineApi", includeCommandLineApi);
    }

    public EvaluateCommand withDoNotPauseOnExceptionAndMuteConsole(
        boolean doNotPauseOnExceptionAndMuteConsole) {
      return with("doNotPauseOnExceptionAndMuteConsole", doNotPauseOnExceptionAndMuteConsole);
    }

    public EvaluateCommand withContextId(long contextId) {
      return with("contextId", contextId);
    }

    public EvaluateCommand withReturnByValue(boolean returnByValue) {
      return with("returnByValue", returnByValue);
    }

    public EvaluateCommand withGeneratePreview(boolean generatePreview) {
      return with("generatePreview", generatePreview);
    }

    public EvaluateCommand withUserGesture(boolean userGesture) {
      return with("userGesture", userGesture);
    }

    @Override
    EvaluateCommand create(JsonObject params) {
      // TODO(user): Auto-generated method stub
      return new EvaluateCommand(params);
    }
  }

  public static EvaluateCommand evaluate(String expression) {
    return new EvaluateCommand().with("expression", expression);
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class CallFunctionOnCommand
      extends DevtoolsCommand.WithOptionals<CallFunctionOnCommand> {
    private CallFunctionOnCommand() {
      super(RUNTIME.methodName("callFunctionOn"));
    }

    private CallFunctionOnCommand(JsonObject params) {
      super(RUNTIME.methodName("callFunctionOn"), params);
    }

    public CallFunctionOnCommand withArguments(List<CallArgument> arguments) {
      return withObjectArray("arguments", arguments);
    }

    public CallFunctionOnCommand withDoNotPauseOnExceptionAndMuteConsole(
        boolean doNotPauseOnExceptionAndMuteConsole) {
      return with("doNotPauseOnExceptionAndMuteConsole", doNotPauseOnExceptionAndMuteConsole);
    }

    public CallFunctionOnCommand withReturnByValue(boolean returnByValue) {
      return with("returnByValue", returnByValue);
    }

    public CallFunctionOnCommand withGeneratePreview(boolean generatePreview) {
      return with("generatePreview", generatePreview);
    }

    @Override
    CallFunctionOnCommand create(JsonObject params) {
      return new CallFunctionOnCommand(params);
    }
  }

  public static CallFunctionOnCommand callFunctionOn(String objectId, String functionDeclaration) {
    return new CallFunctionOnCommand()
        .with("objectId", objectId)
        .with("functionDeclaration", functionDeclaration);
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class GetPropertiesCommand
      extends DevtoolsCommand.WithOptionals<GetPropertiesCommand> {
    private GetPropertiesCommand() {
      super(RUNTIME.methodName("GetProperties"));
    }

    private GetPropertiesCommand(JsonObject params) {
      super(RUNTIME.methodName("GetProperties"), params);
    }

    public GetPropertiesCommand withOwnProperties(boolean ownProperties) {
      return with("ownProperties", ownProperties);
    }

    public GetPropertiesCommand withAccessorPropertiesOnly(boolean accessorPropertiesOnly) {
      return with("accessorPropertiesOnly", accessorPropertiesOnly);
    }

    public GetPropertiesCommand withGeneratePreview(boolean generatePreview) {
      return with("generatePreview", generatePreview);
    }

    @Override
    GetPropertiesCommand create(JsonObject params) {
      return new GetPropertiesCommand(params);
    }
  }

  public static GetPropertiesCommand getProperties(String objectId) {
    return new GetPropertiesCommand().with("objectId", objectId);
  }

  public static DevtoolsCommand releaseObject(String objectId) {
    return command("releaseObject").with("objectId", objectId);
  }

  public static DevtoolsCommand releaseObjectGroup(String objectGroup) {
    return command("releaseObjectGroup").with("objectGroup", objectGroup);
  }

  public static DevtoolsCommand run() {
    return command("run");
  }

  public static DevtoolsCommand compileScript(
      String expression, String sourceUrl, boolean persistScript, int executionContextId) {
    return command("compileScript")
        .with("expression", expression)
        .with("sourceUrl", sourceUrl)
        .with("persistScript", persistScript)
        .with("executionContextId", executionContextId);
  }

  /** A fluent Devtools command that exposes the ability to add optional properties */
  public static final class RunScriptCommand
      extends DevtoolsCommand.WithOptionals<RunScriptCommand> {
    private RunScriptCommand() {
      super(RUNTIME.methodName("runScript"));
    }

    private RunScriptCommand(JsonObject params) {
      super(RUNTIME.methodName("runScript"), params);
    }

    public RunScriptCommand withObjectGroup(String objectGroup) {
      return with("objectGroup", objectGroup);
    }

    public RunScriptCommand withDoNotPauseOnExceptionAndMuteConsole(
        boolean doNotPauseOnExceptionAndMuteConsole) {
      return with("doNotPauseOnExceptionAndMuteConsole", doNotPauseOnExceptionAndMuteConsole);
    }

    public RunScriptCommand withIncludeCommandLineApi(boolean includeCommandLineApi) {
      return with("includeCommandLineApi", includeCommandLineApi);
    }

    @Override
    RunScriptCommand create(JsonObject params) {
      return new RunScriptCommand(params);
    }
  }

  public static RunScriptCommand runScript(String scriptId, int executionContextId) {
    return new RunScriptCommand()
        .with("scriptId", scriptId)
        .with("executionContextId", executionContextId);
  }

  public static DevtoolsCommand enable() {
    return command("enable");
  }

  public static DevtoolsCommand disable() {
    return command("disable");
  }

  /*================== Devtools Type Objects ===========================*/

  /** A fluent Devtools type that exposes the ability to add optional properties */
  public static final class CallArgument extends DevtoolsObject.WithOptionals<CallArgument> {
    private CallArgument() {
      super();
    }

    private CallArgument(JsonObject params) {
      super(params);
    }

    public CallArgument withValue(long value) {
      return with("value", value);
    }

    public CallArgument withValue(String value) {
      return with("value", value);
    }

    public CallArgument withValue(boolean value) {
      return with("value", value);
    }

    public CallArgument withValue(char value) {
      return with("value", value);
    }

    public CallArgument withObjectId(String objectId) {
      return with("objectId", objectId);
    }

    @Override
    CallArgument create(JsonObject params) {
      return new CallArgument(params);
    }
  }

  public static CallArgument callArgument() {
    return new CallArgument();
  }

  private Runtime() {}
}
