package brap.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class PageChangeDetector extends Thread {
	/**
	 * Logger
	 */
	private Logger LOGGER = Logger
			.getLogger(PageChangeDetector.class.getName());

	private String previousURL;
	private String currentURL;
	private String currentSource;
	private String previousSource;
	public static int sleepTime = 2;
	public static boolean stop = false;
	private RemoteWebDriver driver;
	UserActionManager myManager;
	private WebElement we;
	public static int pageDetectorId = 1;

	/**
	 * 
	 * @param userActionManager
	 * @param driver
	 * @param timeInterval
	 *            probing rate in seconds
	 */
	PageChangeDetector(UserActionManager userActionManager,
			RemoteWebDriver driver, int timeInterval) {
		previousURL = driver.getCurrentUrl();
		previousSource = driver.getPageSource();
		this.driver = driver;
		sleepTime = timeInterval;
		this.myManager = userActionManager;
	}

	public void stopDetecting() {
		stop = true;
	}

	public void probeRate(int seconds) {

	}

	public WebElement getAWebElement(String tagName) {
		WebElement we = null;
		try {
			we = driver.findElementByTagName(tagName);
		} catch (Exception e) {
		}
		return we;
	}

	public void run() {
		int myId = pageDetectorId;

		LOGGER.info("Page change detector# " + myId + " started its job.");
		stop = false;
		// Get one element in a page so that you can see if it goes stale
		we = getAWebElement("form");
		if (we == null) {
			we = getAWebElement("table");
		}
		if (we == null) {
			we = getAWebElement("div");
		}
		if (we == null) {
			we = getAWebElement("a");
		}
		if (we == null) {
			we = getAWebElement("body");
		}
		boolean pageChanged = false;
		while (!stop) {
			try {
				if (this.driver == null) {
					stop = true;
					pageChanged = false;
				} else {
					Thread.sleep(sleepTime * 1000);
					pageChanged = isPageChanged();
					if (pageChanged) {
						stop = true;
					}
				}
			} catch (InterruptedException e) {
				// e.printStackTrace();
				LOGGER.info("Page change detector# " + myId
						+ " is quitting now!");
				stop = true;
				pageChanged = false;
			}
		}

		if (pageChanged) {
			LOGGER.info("Page change detected.  Now testing time-to-time to know whether the new page is stable. ");
			waitTillPageLoadingIsStable(driver);
			
			LOGGER.info("Page change is stable. The detector# " + myId
					+ " is reporting this to its manager.");
			pageDetectorId++;
			myManager.reportPageChangedInfo();
		}
		LOGGER.info("Page change detector# " + myId + " finished it's job.");

	}

	public boolean isPageChanged() {
		boolean urlChanged = false;
		boolean sourceChanged = false;
		boolean staleElement = false;

		// Method1: URL based page change detection
		try {
			currentURL = this.driver.getCurrentUrl();
			currentSource = this.driver.getPageSource();
			urlChanged = !previousURL.equalsIgnoreCase(this.currentURL);
		} catch (Exception e) {
		}

		// Method2: Element based page change detection
		try {
			if (we != null) {
				// try go see if element is stale.
				we.getLocation();
			}
		} catch (Exception e) {
			staleElement = true;
		}

		// Method3: Content based page change detector
		Set<String> oldTokens = new HashSet<String>();
		Set<String> newTokens = new HashSet<String>();
		oldTokens.addAll(Arrays.asList(previousSource.split(" ")));
		newTokens.addAll(Arrays.asList(currentSource.split(" ")));
		newTokens.retainAll(oldTokens);
		int percentMatch = newTokens.size() * 100 / oldTokens.size();
		if (percentMatch < 90) {
			sourceChanged = true;
		}

		boolean changed = false;
		if (urlChanged) {
			LOGGER.info("Page change detected (Reason: URL changed) !");
			changed = true;
		} else if (sourceChanged) {
			LOGGER.info("Page change detected (Reason: source match= "
					+ percentMatch + "%)!");
			changed = true;
		} else if (staleElement) {
			LOGGER.info("Page Change  detected (Reason: stale element !)");
			changed = true;
		} else {
			// System.out.println("Same page!");
		}

		return changed;
	}

	public void waitTillPageLoadingIsStable(RemoteWebDriver driver) {
		String previousSource = "";// initially empty
		String currentSource = "";
		boolean stable = false;
		while (!stable) {
			try {
				if (driver == null) {
					break;
				} else {
					previousSource = currentSource;
					currentSource = driver.getPageSource(); // get current
															// source
					if (!previousSource.isEmpty()) {
						Set<String> oldTokens = new HashSet<String>();
						Set<String> newTokens = new HashSet<String>();
						oldTokens.addAll(Arrays.asList(previousSource
								.split(" ")));
						newTokens
								.addAll(Arrays.asList(currentSource.split(" ")));
						//LOGGER.info("oldTokens.size() + "\t"
						//		+ newTokens.size());

						newTokens.retainAll(oldTokens);
						int percentMatch = newTokens.size() * 100
								/ oldTokens.size();
						if (percentMatch >= 90) {
							LOGGER.info("Page seems stable now..");
							stable = true;
							break;
						}
					}
					LOGGER.info("Not stable yet, sleeping for a while ...");
					Thread.sleep(sleepTime * 1000);

				}
			} catch (InterruptedException e) {
				LOGGER.info("Code for waiting till page becomes stable got problem, it is quitting now!");
				stable = true;
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://www.expedia.com";
		System.setProperty("webdriver.chrome.driver",
				"C:\\browser\\drivers\\chromedriver");
		RemoteWebDriver driver = new ChromeDriver();
		driver.get(url);
		// PageChangeDetector detector = new PageChangeDetector(null, driver,
		// 2);
		// detector.start();

		//PageChangeDetector.waitTillPageLoadingIsStable(driver);
	}

}




