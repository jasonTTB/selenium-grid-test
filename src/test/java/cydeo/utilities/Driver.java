package cydeo.utilities;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

public class Driver {

    private Driver(){}

    private static InheritableThreadLocal<WebDriver> driverPool = new InheritableThreadLocal<>();

    public static WebDriver getDriver(){

        if(driverPool.get() == null){

            String browserType = System.getProperty("BROWSER") != null ? System.getProperty("BROWSER") : ConfigurationReader.getProperty("browser");
            System.out.println("Browser: " + browserType);

            switch (browserType){
                case "remote-chrome":
                    try {
                        String gridAddress = "34.239.154.115";
                        URL url = new URL("http://"+ gridAddress + ":4444/wd/hub");
                        ChromeOptions chromeOptions = new ChromeOptions();
                        chromeOptions.addArguments("--start-maximized");
                        driverPool.set(new RemoteWebDriver(url, chromeOptions));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "remote-firefox":
                    try {
                        String gridAddress = "34.239.154.115";
                        URL url = new URL("http://"+ gridAddress + ":4444/wd/hub");
                        FirefoxOptions firefoxOptions = new FirefoxOptions();
                        firefoxOptions.addArguments("--start-maximized");
                        driverPool.set(new RemoteWebDriver(url, firefoxOptions));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "chrome":
                    WebDriverManager.chromedriver().setup();
                    driverPool.set(new ChromeDriver());
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions options = new FirefoxOptions();
                    options.addArguments("--start-maximized");
                    // Uncomment the next line if you need to run in headless mode
                    // options.addArguments("--headless");
                    System.out.println("Setting up Firefox driver...");
                    System.out.println("GeckoDriver path: " + System.getProperty("webdriver.gecko.driver"));
                    driverPool.set(new FirefoxDriver(options));
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;
                case "headless-chrome":
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions option = new ChromeOptions();
                    option.addArguments("--headless=new");
                    driverPool.set(new ChromeDriver(option));
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;
            }
        }

        return driverPool.get();
    }

    public static void closeDriver(){
        if (driverPool.get()!=null){
            driverPool.get().quit();
            driverPool.remove();
        }
    }
}
