package codetoscan;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class SampleSeleniumTest {

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");

        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://example.com/login");

            // Hardcoded username and password (risky)
            String username = "admin";
            String password = "password123";

            driver.findElement(By.id("username123")).sendKeys(username);  // brittle locator (digits in id)
            driver.findElement(By.id("password")).sendKeys(password);

            Thread.sleep(5000);  // bad: explicit sleep (flaky test risk)

            driver.findElement(By.id("loginButton")).click();

        } catch (Exception e) {
            // empty catch block (bad practice)
        } finally {
            driver.quit();
        }
    }
}
