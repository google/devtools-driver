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

import static com.google.devtoolsdriver.devtools.DevtoolsDomain.DOM;

import java.util.List;
import javax.json.JsonObject;

/**
 * Factory for messages in the devtools DOM domain. For a specification of this domain's methods,
 * see the <a href="https://chromedevtools.github.io/debugger-protocol-viewer/tot/DOM/" >debugging
 * protocol viewer</a>. Note that not all the domain's methods have been implemented yet.
 */
public final class DOM {
  private static DevtoolsCommand command(String methodSuffix) {
    return new DevtoolsCommand.NoOptionals(DOM.methodName(methodSuffix));
  }

  public static DevtoolsCommand enable() {
    return command("enable");
  }

  public static DevtoolsCommand disable() {
    return command("disable");
  }

  public static DevtoolsCommand getDocument() {
    return command("getDocument");
  }

  /** A fluent Devtools command exposing the ability to add optional properties */
  public static final class RequestChildNodesCommand
      extends DevtoolsCommand.WithOptionals<RequestChildNodesCommand> {
    private RequestChildNodesCommand() {
      super(DOM.methodName("requestChildNodes"));
    }

    private RequestChildNodesCommand(JsonObject params) {
      super(DOM.methodName("requestChildNodes"), params);
    }

    public RequestChildNodesCommand withDepth(long depth) {
      return with("depth", depth);
    }

    @Override
    RequestChildNodesCommand create(JsonObject params) {
      return new RequestChildNodesCommand(params);
    }
  }

  public static RequestChildNodesCommand requestChildNodes(int nodeId) {
    return new RequestChildNodesCommand().with("nodeId", nodeId);
  }

  public static DevtoolsCommand querySelector(int nodeId, String selector) {
    return command("querySelector").with("nodeId", nodeId).with("selector", selector);
  }

  public static DevtoolsCommand querySelectorAll(int nodeId, String selector) {
    return command("querySelectorAll").with("nodeId", nodeId).with("selector", selector);
  }

  public static DevtoolsCommand setNodeName(int nodeId, String name) {
    return command("setNodeName").with("nodeId", nodeId).with("name", name);
  }

  public static DevtoolsCommand setNodeValue(int nodeId, String value) {
    return command("setNodeValue").with("nodeId", nodeId).with("value", value);
  }

  public static DevtoolsCommand removeNode(int nodeId) {
    return command("removeNode").with("nodeId", nodeId);
  }

  public static DevtoolsCommand setAttributeValue(int nodeId, String name, String value) {
    return command("setAttributeValue")
        .with("nodeId", nodeId)
        .with("name", name)
        .with("value", value);
  }

  /** A fluent Devtools command exposing the ability to add optional properties */
  public static final class SetAttributesAsTextCommand
      extends DevtoolsCommand.WithOptionals<SetAttributesAsTextCommand> {
    private SetAttributesAsTextCommand() {
      super(DOM.methodName("setAttributesAsText"));
    }

    private SetAttributesAsTextCommand(JsonObject params) {
      super(DOM.methodName("setAttributesAsText"), params);
    }

    public SetAttributesAsTextCommand withDepth(String name) {
      return with("name", name);
    }

    @Override
    SetAttributesAsTextCommand create(JsonObject params) {
      return new SetAttributesAsTextCommand(params);
    }
  }

  public static SetAttributesAsTextCommand setAttributesAsText(int nodeId, String text) {
    return new SetAttributesAsTextCommand().with("nodeId", nodeId).with("text", text);
  }

  public static DevtoolsCommand removeAttribute(int nodeId, String name) {
    return command("removeAttribute").with("nodeId", nodeId).with("name", name);
  }

  public static DevtoolsCommand getOuterHtml(int nodeId) {
    return command("getOuterHtml").with("nodeId", nodeId);
  }

  public static DevtoolsCommand setOuterHTML(int nodeId, String outerHtml) {
    return command("setOuterHTML").with("nodeId", nodeId).with("outerHtml", outerHtml);
  }

  public static DevtoolsCommand getSearchResults(String searchId, int fromIndex, int toIndex) {
    return command("getSearchResults")
        .with("searchId", searchId)
        .with("fromIndex", fromIndex)
        .with("toIndex", toIndex);
  }

  public static DevtoolsCommand discardSearchResults(String searchId) {
    return command("discardSearchResults").with("searchId", searchId);
  }

  public static DevtoolsCommand requestNode(String objectId) {
    return command("requestNode").with("objectId", objectId);
  }

  /** A fluent Devtools command exposing the ability to add optional properties */
  public static final class HighlightNodeCommand
      extends DevtoolsCommand.WithOptionals<HighlightNodeCommand> {
    private HighlightNodeCommand() {
      super(DOM.methodName("highlightNode"));
    }

    private HighlightNodeCommand(JsonObject params) {
      super(DOM.methodName("highlightNode"), params);
    }

    public HighlightNodeCommand withNodeId(int nodeId) {
      return with("nodeId", nodeId);
    }

    public HighlightNodeCommand backendNodeId(int backendNodeId) {
      return with("backendNodeId", backendNodeId);
    }

    public HighlightNodeCommand objectId(String objectId) {
      return with("objectId", objectId);
    }

    @Override
    HighlightNodeCommand create(JsonObject params) {
      return new HighlightNodeCommand(params);
    }
  }

  public static HighlightNodeCommand highlightNode(HighlightConfig highlightConfig) {
    return new HighlightNodeCommand().with("highlightConfig", highlightConfig);
  }

  public static DevtoolsCommand hideHighlight() {
    return command("hideHighlight");
  }

  public static DevtoolsCommand pushNodeByPathToFrontend(String path) {
    return command("pushNodeByPathToFrontend").with("path", path);
  }

  public static DevtoolsCommand pushNodesByBackendIdsToFrontend(List<Long> backendNodeIds) {
    return command("pushNodesByBackendIdsToFrontend")
        .withNumberArray("backendNodeIds", backendNodeIds);
  }

  /** A fluent Devtools command exposing the ability to add optional properties */
  public static final class ResolveNodeCommand
      extends DevtoolsCommand.WithOptionals<ResolveNodeCommand> {
    private ResolveNodeCommand() {
      super(DOM.methodName("resolveNode"));
    }

    private ResolveNodeCommand(JsonObject params) {
      super(DOM.methodName("resolveNode"), params);
    }

    public ResolveNodeCommand withObjectGroup(String objectGroup) {
      return with("objectGroup", objectGroup);
    }

    @Override
    ResolveNodeCommand create(JsonObject params) {
      return new ResolveNodeCommand(params);
    }
  }

  public static ResolveNodeCommand resolveNode(int nodeId) {
    return new ResolveNodeCommand().with("nodeId", nodeId);
  }

  public static DevtoolsCommand getAttributes(int nodeId) {
    return command("getAttributes").with("nodeId", nodeId);
  }

  /*================== Devtools Type Objects ===========================*/

  /** A fluent Devtools type that exposes the ability to add optional properties */
  public static final class RGBA extends DevtoolsObject.WithOptionals<RGBA> {
    private RGBA() {
      super();
    }

    private RGBA(JsonObject params) {
      super(params);
    }

    public RGBA witha(double a) {
      return with("a", a);
    }

    @Override
    RGBA create(JsonObject params) {
      return new RGBA(params);
    }
  }

  public static RGBA rgba(int r, int g, int b) {
    return new RGBA().with("a", r).with("g", g).with("b", b);
  }

  /** A fluent Devtools type that exposes the ability to add optional properties */
  public static final class HighlightConfig extends DevtoolsObject.WithOptionals<HighlightConfig> {
    private HighlightConfig() {
      super();
    }

    private HighlightConfig(JsonObject params) {
      super(params);
    }

    public HighlightConfig withShowInfo(boolean showInfo) {
      return with("showInfo", showInfo);
    }

    public HighlightConfig withContentColor(RGBA contentColor) {
      return with("contentColor", contentColor);
    }

    public HighlightConfig withPaddingColor(RGBA paddingColor) {
      return with("paddingColor", paddingColor);
    }

    public HighlightConfig withBorderColor(RGBA borderColor) {
      return with("borderColor", borderColor);
    }

    public HighlightConfig withMarginColor(RGBA marginColor) {
      return with("marginColor", marginColor);
    }

    public HighlightConfig withEventTargetColor(RGBA eventTargetColor) {
      return with("eventTargetColor", eventTargetColor);
    }

    @Override
    HighlightConfig create(JsonObject params) {
      return new HighlightConfig(params);
    }
  }

  public static HighlightConfig highlightConfig() {
    return new HighlightConfig();
  }
}
