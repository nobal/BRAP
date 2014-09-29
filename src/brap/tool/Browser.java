package brap.tool;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;


public class Browser {
	private RemoteWebDriver webDriver;
	String type;
	String browserBin;
	String profile;

	public Browser(String url,String type,String browserBin,String profile){
		 this.type = type;
		 this.browserBin=browserBin;
		 this.profile=profile;
		 openBrowser(url,this.type,this.browserBin,this.profile);
	}
	public Browser(String type,String browserBin){
		 this.type = type;
		 this.browserBin=browserBin;
	}
	
	public void openUrl(String url) {
			openBrowser(url,this.type,this.browserBin,this.profile);
	}
	
	public void openBrowser(String url,String type,String browserBin,String profile) {
		try {
			if(type.toLowerCase().contains("chrome")){
				ChromeOptions options = new ChromeOptions();
				options.addArguments("test-type");
				if(profile!=null){
					options.addArguments("user-data-dir="
							+ profile);
				}
				System.setProperty("webdriver.chrome.driver",browserBin );
				webDriver = (RemoteWebDriver) new ChromeDriver(options);
			}else if(type.toLowerCase().contains("firefox")){
				webDriver = new FirefoxDriver();
			}
			webDriver.get(url);
		} catch (Exception e) {
		}

	}
	/**
	 * Closes all browser windows and stops all browser interactions.
	 */
	public void close() {
		//webDriver.close();
		webDriver.quit();
	}
	
	
	public RemoteWebDriver getWebDriver(){
		return this.webDriver;
	}
	
	
}
