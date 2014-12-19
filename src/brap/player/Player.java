package brap.player;

import static brap.form.FormConstants.SELECT;
import static brap.form.FormConstants.TEXT;
import static brap.form.FormConstants.TEXTAREA;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.google.common.base.Function;

import brap.action.InteractionFileParser;
import brap.action.UserActionLogger;
import brap.form.FormElement;

public class Player {
	private Logger LOGGER = Logger.getLogger(Player.class.getName());

	private WebDriver driver;

	public Player(WebDriver driver){
		this.driver = driver;
	}

	public String playActions(List<String> lines, String url) {
		return playActions(lines,url,-1);
	}
	
	/**
	 * 
	 * @param lines
	 * @param url
	 * @param delayInSeconds - if >0, the player pauses for that many seconds before executing another interaciton
	 * @return
	 */
	public String playActions(List<String> lines, String url, int delayInSeconds) {
		LOGGER.info("Starting URL:" + url + ", Total Size: " + lines.size());
		String results = "";
		int i = 1;
		for (String action : lines) {
			System.out.println(i + ". " + action);
			i++;
		}
		results = "<html><head></head><body>Test</body></html>";

		if (!url.contains("://"))
			url = "http://" + url;

		try {
			// Clear all cookies
			driver.manage().deleteAllCookies();
			driver.get(url);

			// Remove header which is the first row
			if (lines.size() > 1) {
				lines.remove(0);
			} else {
				LOGGER.info("No data for playing interactions.");
				return "<html><head></head><body>No data for playing interactions.</body></html>";
			}
			for (String line : lines) {
				if (line.startsWith("#") || line.isEmpty()) {
					// wait for a while
					continue;
				}
				Map<String, String> parsedOp = InteractionFileParser.parseLine(
						line, UserActionLogger.HEADERS);
				String name = parsedOp.get("name").trim();
				String id = parsedOp.get("id").trim();
				String value = parsedOp.get("value").trim();
				String event = parsedOp.get("event").trim();
				String tag = parsedOp.get("tag").trim();
				String type = parsedOp.get("type").trim();


				String width = parsedOp.get("width").trim();
				String height = parsedOp.get("height").trim();
				String top = parsedOp.get("top").trim();
				String left = parsedOp.get("left").trim();

				// System.out.println("Parsing the line:" + line);
				// for (String kk : parsedOp.keySet()) {
				// System.out.println("=> " + kk + ":" + parsedOp.get(kk));
				// }

				WebElement we = null;
				boolean isValid = false;
				boolean foundById = false, foundByName = false, foundByValue = false;

				String foundByInfo = "";
				if (id != null && !id.isEmpty()) {
					try {
						we = driver.findElement(By.id(id));
						foundById = true;
						foundByInfo = "Id";
					} catch (Exception e) {
						LOGGER.info("Id is present in recording but could not be found during play-out");
					}
				}

				// Validate results obtained by id: check if the element found
				// by id has the same name as
				// This is needed as ids are sometimes assigned randomly e.g.
				// ebay
				if (foundById) {
					// if Id finds the element and its name is empty, simply
					// declare finding as valid. Otherwise validate the name
					if (name.isEmpty()) {
						isValid = true;
					} else {
						try {
							String name1 = we.getAttribute("name").trim();
							if (name1.equalsIgnoreCase(name)) {
								isValid = true;
							} else {
								isValid = false;
							}
						} catch (Exception e) {
						}
					}
				}

				if (!isValid && name != null && !name.isEmpty()) {
					try {
						List<WebElement> we1 = driver.findElements(By
								.name(name));
						if (we1.size() > 0) {
							we = we1.get(0);
							isValid = true;
							foundByName = true;
							foundByInfo = "Name";
							try {
								double midX = Integer.parseInt(left)
										+ (Integer.parseInt(width)) / 2.0;
								double midY = Integer.parseInt(top)
										+ (Integer.parseInt(height)) / 2.0;
								double distance = 999999.0;
								for (int k = 0; k < we1.size(); k++) {
									WebElement e = we1.get(k);
									// Get mid point
									double xk = e.getLocation().x
											+ e.getSize().width / 2.0;
									double yk = e.getLocation().y
											+ e.getSize().height / 2.0;
									// Find the distance
									double dk = Math.sqrt(Math
											.pow(midX - xk, 2)
											+ Math.pow(midY - yk, 2));
									if (distance > dk) {
										we = e;
										distance = dk;
									}
								}
							} catch (Exception e) {
							}
						}

					} catch (Exception e) {
						LOGGER.info("Name is present in recording but could not be found during play-out");
					}
				}

				// Try to use value to get the web element if previous attempts
				// fail
				if (!isValid && value != null && !value.isEmpty()) {
					try {
						we = driver
								.findElement(By.xpath("//*[contains(@value,'"
										+ value + "')]"));
						foundByValue = true;
						foundByInfo = "Value";
					} catch (Exception e) {
						LOGGER.info("Tried to search the element using its value, but it failed.");
					}
				}

				if (we == null) {
					// Try finding element by its tag name and location
					// information
					double midX, midY;
					try {
						midX = Integer.parseInt(left) + (Integer.parseInt(width))
								/ 2.0;
						midY = Integer.parseInt(top) + (Integer.parseInt(height))
								/ 2.0;
						List<WebElement> elms = driver.findElements(By
								.tagName(tag));
						double distance = 999999.0;
						boolean hasType = (type!=null) && (type.length()>2);
						for (int k = 0; k < elms.size(); k++) {
							WebElement e = elms.get(k);

							String elmType = "";
							try {
								elmType = FormElement.computeType(e);
							} catch (Exception e1) {
							}
							if (hasType
									&& (elmType == null || elmType.isEmpty() || !type
											.equalsIgnoreCase(elmType))) {
								continue;
							}
							
							// Get mid point
							double xk = e.getLocation().x + e.getSize().width
									/ 2.0;
							double yk = e.getLocation().y + e.getSize().height
									/ 2.0;
							// Find the distance
							double dk = Math.sqrt(Math.pow(midX - xk, 2)
									+ Math.pow(midY - yk, 2));
							if (distance > dk) {
								we = e;
								distance = dk;
							}
						}
						if(we!=null) {
							foundByInfo="tagSearch";
						}

					} catch (Exception e) {

					}

				}

				System.out
						.println("result is:" + foundByInfo + " for :" + line);

				if (we != null) {
					LOGGER.info("Found element by: " + foundByInfo);
					String elmType = "";
					try {
						elmType = FormElement.computeType(we);
						//Irrespective of element type:
						//a) wait till it becomes visible 
						this.fluentWaitTillVisible(we,driver);
						
						//b) move to this element to perform some actions 
						Actions actions = new Actions(driver);
						actions.moveToElement(we);
				
					} catch (Exception e) {

					}
					if (elmType.equalsIgnoreCase(TEXT)
							|| elmType.equalsIgnoreCase(
									TEXTAREA)) {
						we.clear();
						if (foundById) {
							// js.executeScript("document.getElementById('"+id+"').setAttribute('value', '"+value+"')");
						} else {
							// js.executeScript("document.getElementByName('"+name+"').setAttribute('value', '"+value+"')");
						}
						we.sendKeys(value);
						// we.sendKeys(Keys.TAB);
					} else if (elmType.equalsIgnoreCase(
							SELECT)) {
						WebElement select =we;
						List<WebElement> options = select.findElements(By
								.tagName("option"));
						boolean done = false;
						for (WebElement option : options) {
							String vall = option.getText().trim();
							String val2 = value.trim();
							if (vall.equalsIgnoreCase(val2)) {
								option.click();
								done = true;
								break;
							}
						}
						if (!done) {
							select.sendKeys(value + "\t");
						}
					} else {
							try{
								fluentWaitTillClickable(we,driver);
							}catch(Exception e){
								LOGGER.info("Exception while performing FluentWait");
							}
						
						we.click();
					}
					//if DelayInSeconds is specified then pause 
					try {
						if (delayInSeconds > 0) {
							LOGGER.info("Waiting for " + delayInSeconds
									+ " seconds");
							Thread.sleep(delayInSeconds * 1000);
						}
					} catch (Exception e) {
						LOGGER.info("Exception while waiting ");
					}
					
				} else {
					LOGGER.info("Element couldn't be found. So Couldn't execute the command:"
							+ line);
				}

			}

			// close the browser
			// closeBrowser(waitSeconds);
			results = driver.getPageSource();
			// Print the result in the console
			//System.out.println(results);
		} catch (Exception e) {
			results = "<html><head></head><body>" + e.toString()
					+ "</body></html>";
		}

		return results;
	}

	public  WebElement fluentWaitTillFind(final By locator,WebDriver driver) {
	    Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
	            .withTimeout(30, TimeUnit.SECONDS)
	            .pollingEvery(5, TimeUnit.SECONDS)
	            .ignoring(NoSuchElementException.class);
	    WebElement we = wait.until(new Function<WebDriver, WebElement>() {
	        public WebElement apply(WebDriver driver) {
	            return driver.findElement(locator);
	        }
	    });
	    return  we;
	};
	
	public  void fluentWaitTillVisible(WebElement we,WebDriver driver) {
	    Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
	            .withTimeout(30, TimeUnit.SECONDS)
	            .pollingEvery(5, TimeUnit.SECONDS)
	            .ignoring(NoSuchElementException.class);
	    wait.until(ExpectedConditions.visibilityOf(we));
	};
	
	public  void fluentWaitTillClickable(WebElement we,WebDriver driver) {
	    Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
	            .withTimeout(30, TimeUnit.SECONDS)
	            .pollingEvery(5, TimeUnit.SECONDS)
	            .ignoring(NoSuchElementException.class);
	    wait.until(ExpectedConditions.elementToBeClickable(we));
	};
	
}
