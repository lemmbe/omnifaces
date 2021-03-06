/*
 * Copyright 2016 OmniFaces
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.omnifaces.test.push.socket;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.junit.Assert.assertEquals;
import static org.omnifaces.test.OmniFacesIT.WebXml.withSocket;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.omnifaces.test.OmniFacesIT;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SocketIT extends OmniFacesIT {

	@FindBy(id="messages")
	private WebElement messages;

	@FindBy(id="newtab")
	private WebElement newtab;

	@FindBy(id="clientOpenedMessages")
	private WebElement clientOpenedMessages;

	@FindBy(id="clientClosedMessages")
	private WebElement clientClosedMessages;

	@FindBy(id="serverEventMessages")
	private WebElement serverEventMessages;

	@FindBy(id="applicationScopedMessage")
	private WebElement applicationScopedMessage;

	@FindBy(id="sessionScopedMessage")
	private WebElement sessionScopedMessage;

	@FindBy(id="viewScopedMessage")
	private WebElement viewScopedMessage;

	@FindBy(id="userTargetedMessage")
	private WebElement userTargetedMessage;

	@FindBy(id="push:applicationScoped")
	private WebElement pushApplicationScoped;

	@FindBy(id="push:sessionScoped")
	private WebElement pushSessionScoped;

	@FindBy(id="push:viewScoped")
	private WebElement pushViewScoped;

	@FindBy(id="push:userTargeted")
	private WebElement pushUserTargeted;

	@FindBy(id="closeAllSockets")
	private WebElement closeAllSockets;

	@Deployment(testable=false)
	public static WebArchive createDeployment() {
		return buildWebArchive(SocketIT.class)
				.withWebXml(withSocket)
				.createDeployment();
	}

	@Test
	public void test() throws Exception {
		testOnopen();

		assertEquals(pushApplicationScoped(), "1," + applicationScopedMessage.getText());
		assertEquals(pushSessionScoped(), "1," + sessionScopedMessage.getText());
		assertEquals(pushViewScoped(), "1," + viewScopedMessage.getText());
		assertEquals(pushUserTargeted(), "1," + userTargetedMessage.getText());

		String firstTab = browser.getWindowHandle();
		openNewTab(newtab);
		testOnopen();

		assertEquals(pushApplicationScoped(), "2," + applicationScopedMessage.getText());
		assertEquals(pushSessionScoped(), "2," + sessionScopedMessage.getText());
		assertEquals(pushViewScoped(), "1," + viewScopedMessage.getText());
		assertEquals(pushUserTargeted(), "2," + userTargetedMessage.getText());

		// Unfortunately Selenium doesn't (seem to?) support starting a new HTTP session within the same IT, so
		// application, session and user sockets can't be tested more extensively. If possible somehow, it's expected
		// that numbers should equal respectively 3, 2, 1, 3 on first session and 3, 1, 1, 3 on second session.

		testOnclose(firstTab);
	}

	private void testOnopen() {
		assertEquals("|serverEvents||applicationScoped||sessionScoped||viewScoped||userTargeted|", clientOpenedMessages.getText());
		waitGui(browser).withTimeout(3, SECONDS).until().element(serverEventMessages).text()
			.equalTo("|opened:applicationScoped||opened:sessionScoped||opened:viewScoped||opened:userTargeted|");
	}

	private void testOnclose(String tabToSwitch) {

/* Outcomment for now.
 *
 * TODO: HtmlUnit (Selenium 2.53.1) bugs on close of websockets after a long time out with below error:
 * WARN: oejut.QueuedThreadPool:main: WebSocketClient@1366581056{STOPPING,8<=8<=200,i=0,q=1} Couldn't stop Thread[WebSocketClient@1366581056-104,5,main]

		guardNoRequest(closeAllSockets).click();
		String closeMessages = clientClosedMessages.getText();
		assertTrue(closeMessages.contains("|serverEvents|")); // Closing doesn't happen synchronously, so ordering may be different.
		assertTrue(closeMessages.contains("|applicationScoped|"));
		assertTrue(closeMessages.contains("|sessionScoped|"));
		assertTrue(closeMessages.contains("|viewScoped|"));
		assertTrue(closeMessages.contains("|userTargeted|"));
		closeCurrentTabAndSwitchTo(tabToSwitch);

*/
	}

	private String pushApplicationScoped() {
		guardAjax(pushApplicationScoped).click();
		String message = messages.getText();
		waitUntilMessages(applicationScopedMessage);
		return message;
	}

	private String pushSessionScoped() {
		guardAjax(pushSessionScoped).click();
		String message = messages.getText();
		waitUntilMessages(sessionScopedMessage);
		return message;
	}

	private String pushViewScoped() {
		guardAjax(pushViewScoped).click();
		String message = messages.getText();
		waitUntilMessages(viewScopedMessage);
		return message;
	}

	private String pushUserTargeted() {
		guardAjax(pushUserTargeted).click();
		String message = messages.getText();
		waitUntilMessages(userTargetedMessage);
		return message;
	}

}