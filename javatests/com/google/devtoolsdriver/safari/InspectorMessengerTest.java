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

package com.google.devtoolsdriver.safari;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.devtoolsdriver.safari.InspectorMessenger.AppListing;
import com.google.devtoolsdriver.util.JavaxJson;
import com.google.iosdevicecontrol.testing.FakeInspectorSocket;
import com.google.iosdevicecontrol.webinspector.ApplicationDisconnectedMessage;
import com.google.iosdevicecontrol.webinspector.ApplicationSentListingMessage;
import com.google.iosdevicecontrol.webinspector.ForwardGetListingMessage;
import com.google.iosdevicecontrol.webinspector.ForwardSocketDataMessage;
import com.google.iosdevicecontrol.webinspector.ForwardSocketSetupMessage;
import com.google.iosdevicecontrol.webinspector.InspectorApplication;
import com.google.iosdevicecontrol.webinspector.InspectorMessage;
import com.google.iosdevicecontrol.webinspector.InspectorPage;
import com.google.iosdevicecontrol.webinspector.ReportConnectedApplicationListMessage;
import com.google.iosdevicecontrol.webinspector.ReportIdentifierMessage;
import com.google.iosdevicecontrol.webinspector.WebInspector;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InspectorMessengerTest {
  private static final String CONNECTION_ID = "CONNECTION_ID";
  private static final String APP_BUNDLE_ID = "com.apple.mobilesafari";
  private static final String CONTENT_APP_ID = "CONTENT_APP_ID";
  private static final int CONTENT_PAGE_ID = 1;
  private static final InspectorApplication MAIN_APP =
      InspectorApplication.builder()
          .applicationBundleId(APP_BUNDLE_ID)
          .applicationId("MAIN_APP_ID")
          .applicationName("Mobile Safari")
          .isApplicationActive(true)
          .isApplicationProxy(false)
          .build();
  private static final InspectorApplication CONTENT_APP =
      InspectorApplication.builder()
          .applicationBundleId("CONTENT_BUNDLE_ID")
          .applicationId(CONTENT_APP_ID)
          .applicationName("Web Content")
          .hostApplicationId(MAIN_APP.applicationId())
          .isApplicationActive(true)
          .isApplicationProxy(true)
          .build();
  private static final InspectorApplication OTHER_APP =
      InspectorApplication.builder()
          .applicationBundleId("OTHER_BUNDLE_ID")
          .applicationId("OTHER_APP_ID")
          .applicationName("other app")
          .isApplicationActive(true)
          .isApplicationProxy(true)
          .build();
  private static final ImmutableList<InspectorPage> CONTENT_PAGES =
      ImmutableList.of(
          InspectorPage.builder()
              .connectionId(CONNECTION_ID)
              .pageId(CONTENT_PAGE_ID)
              .title("Page 1")
              .type("WIRTypeWeb")
              .url("http://www.google.com/1")
              .build(),
          InspectorPage.builder()
              .connectionId(CONNECTION_ID)
              .pageId(2)
              .title("Page 2")
              .type("WIRTypeWeb")
              .url("http://www.google.com/2")
              .build());
  private static final ImmutableList<InspectorPage> OTHER_PAGES =
      ImmutableList.of(
          InspectorPage.builder()
              .connectionId(CONNECTION_ID)
              .pageId(3)
              .title("Page 3")
              .type("WIRTypeWeb")
              .url("http://www.wrongapp.com")
              .build());

  private FakeInspectorSocket fakeInspectorSocket;
  private FakeScheduledExecutorService fakeExecutor;
  private InspectorMessenger messenger;

  @Before
  public void setup() {
    fakeInspectorSocket = new FakeInspectorSocket();
    fakeExecutor = new FakeScheduledExecutorService();
    messenger = new InspectorMessenger(new WebInspector(fakeInspectorSocket), fakeExecutor);
  }

  @Test
  public void testSendConnect() throws IOException {
    messenger.sendConnect();
    assertThat(onlyMessageSent()).isInstanceOf(ReportIdentifierMessage.class);
  }

  @Test
  public void testSendSwitchTo() throws IOException {
    boolean switched = messenger.sendSwitchTo(CONTENT_APP_ID, CONTENT_PAGE_ID);
    assertThat(switched).isTrue();
    assertThat(onlyMessageSent()).isInstanceOf(ForwardSocketSetupMessage.class);
    assertThat(messenger.activePageId().getAsInt()).isEqualTo(CONTENT_PAGE_ID);

    switched = messenger.sendSwitchTo(CONTENT_APP_ID, CONTENT_PAGE_ID);
    assertThat(switched).isFalse();
    assertThat(fakeInspectorSocket.dequeueMessagesSent()).isEmpty();
    assertThat(messenger.activePageId().getAsInt()).isEqualTo(CONTENT_PAGE_ID);

    switched = messenger.sendSwitchTo(CONTENT_APP_ID, CONTENT_PAGE_ID + 1);
    assertThat(switched).isTrue();
    assertThat(onlyMessageSent()).isInstanceOf(ForwardSocketSetupMessage.class);
    assertThat(messenger.activePageId().getAsInt()).isEqualTo(CONTENT_PAGE_ID + 1);
  }

  @Test
  public void testSendListPages() throws IOException {
    messenger.sendSwitchTo(CONTENT_APP_ID, CONTENT_PAGE_ID);
    fakeInspectorSocket.dequeueMessagesSent();
    messenger.sendListPages();
    assertThat(onlyMessageSent()).isInstanceOf(ForwardGetListingMessage.class);
  }

  @Test
  public void testSendCommand() throws IOException {
    messenger.setEventListener(event -> {});
    messenger.sendSwitchTo(CONTENT_APP_ID, CONTENT_PAGE_ID);
    fakeInspectorSocket.dequeueMessagesSent();
    messenger.sendCommand(JavaxJson.EMPTY_OBJECT);
    assertThat(onlyMessageSent()).isInstanceOf(ForwardSocketDataMessage.class);
  }

  @Test
  public void testDeviceDisconnected() throws IOException {
    messenger.sendSwitchTo(CONTENT_APP_ID, CONTENT_PAGE_ID);
    assertThat(messenger.activePageId().getAsInt()).isEqualTo(CONTENT_PAGE_ID);
    receiveMessage(
        ApplicationDisconnectedMessage.builder()
            .applicationBundleId(CONTENT_APP.applicationBundleId())
            .applicationId(CONTENT_APP.applicationId())
            .applicationName(CONTENT_APP.applicationName())
            .hostApplicationId(CONTENT_APP.optionalHostApplicationId().get())
            .isApplicationActive(CONTENT_APP.isApplicationActive())
            .isApplicationProxy(CONTENT_APP.isApplicationProxy())
            .build());
    assertThat(messenger.activePageId().isPresent()).isFalse();
  }

  @Test
  public void testAwaitAllApplistings() throws IOException {
    // The consumer is notified of the main and content apps, but no matching apps yet.
    receiveMessage(
        ReportConnectedApplicationListMessage.builder()
            .applicationDictionary(ImmutableList.of(MAIN_APP, CONTENT_APP, OTHER_APP))
            .build());
    assertThat(messenger.getAllAppListings(APP_BUNDLE_ID).isPresent()).isFalse();

    // Pages arrive for a different app id so app and pages still not "ready".
    receiveMessage(
        ApplicationSentListingMessage.builder()
            .applicationId(OTHER_APP.applicationId())
            .listing(OTHER_PAGES)
            .build());
    assertThat(messenger.getAllAppListings(APP_BUNDLE_ID).isPresent()).isFalse();

    // Now pages arrive for the right app id.
    receiveMessage(
        ApplicationSentListingMessage.builder()
            .applicationId(CONTENT_APP_ID)
            .listing(CONTENT_PAGES)
            .build());
    assertThat(messenger.getAllAppListings(APP_BUNDLE_ID).isPresent()).isTrue();

    ImmutableSet<AppListing> appListings = messenger.awaitAllAppListings(APP_BUNDLE_ID);
    ImmutableList<InspectorPage> pages = Iterables.getOnlyElement(appListings).listing.get();
    assertThat(pages).isEqualTo(CONTENT_PAGES);
  }

  @Test
  public void testAwaitPages() throws IOException {
    messenger.sendSwitchTo(CONTENT_APP_ID, CONTENT_PAGE_ID);
    receiveMessage(
        ReportConnectedApplicationListMessage.builder()
            .applicationDictionary(ImmutableList.of(MAIN_APP, CONTENT_APP))
            .build());
    receiveMessage(
        ApplicationSentListingMessage.builder()
            .applicationId(CONTENT_APP_ID)
            .listing(CONTENT_PAGES)
            .build());
    assertThat(messenger.getPages().isPresent()).isTrue();
    assertThat(messenger.awaitPages()).isEqualTo(CONTENT_PAGES);

    // Clearing the pages leaves the app still ready but no pages.
    messenger.sendListPages();
    assertThat(messenger.getPages().isPresent()).isFalse();

    // Now the new pages arrive.
    ImmutableList<InspectorPage> newPages =
        ImmutableList.<InspectorPage>builder()
            .addAll(CONTENT_PAGES)
            .add(
                InspectorPage.builder()
                    .connectionId(CONNECTION_ID)
                    .pageId(999)
                    .title("Page 999")
                    .type("WIRTypeWeb")
                    .url("http://www.google.com/999")
                    .build())
            .build();
    receiveMessage(
        ApplicationSentListingMessage.builder()
            .applicationId(CONTENT_APP.applicationId())
            .listing(newPages)
            .build());
    assertThat(messenger.getPages().isPresent()).isTrue();
    assertThat(messenger.awaitPages()).isEqualTo(newPages);
  }

  @Test
  public void testClose() throws IOException {
    assertThat(fakeExecutor.isShutdown()).isFalse();
    assertThat(fakeInspectorSocket.isClosed()).isFalse();

    messenger.close();

    assertThat(fakeExecutor.isShutdown()).isTrue();
    assertThat(fakeInspectorSocket.isClosed()).isTrue();
  }

  private void receiveMessage(InspectorMessage message) {
    fakeInspectorSocket.enqueueMessageToReceive(message);
    fakeExecutor.simulateSleepExecutingAtMostOneTask();
  }

  private InspectorMessage onlyMessageSent() {
    return Iterables.getOnlyElement(fakeInspectorSocket.dequeueMessagesSent());
  }
}
