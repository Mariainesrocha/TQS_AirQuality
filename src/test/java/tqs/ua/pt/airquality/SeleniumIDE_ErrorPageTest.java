package tqs.ua.pt.airquality;// Generated by Selenium IDE
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.JavascriptExecutor;
import java.util.*;

public class SeleniumIDE_ErrorPageTest {
  private WebDriver driver;
  JavascriptExecutor js;

  @BeforeEach
  public void setUp() {
    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver();
    js = (JavascriptExecutor) driver;
  }

  @AfterEach
  public void tearDown() {
    driver.quit();
  }

  @Test
  public void seleniumIDE_ErrorPage() {

    driver.get("http://localhost:8080/error");

    driver.manage().window().setSize(new Dimension(1848, 1053));

    driver.findElement(By.cssSelector("p")).click();

    driver.findElement(By.cssSelector("p")).click();

    {
      WebElement element = driver.findElement(By.cssSelector("p"));
      Actions builder = new Actions(driver);
      builder.doubleClick(element).perform();
    }

    driver.findElement(By.cssSelector("p")).click();

    assertThat(driver.findElement(By.cssSelector("p")).getText(), is("The page you are looking for might have been removed had its name changed or is temporarily unavailable."));

    driver.findElement(By.id("notfound")).click();

    driver.findElement(By.cssSelector("a")).click();

    driver.findElement(By.cssSelector(".text")).click();

    driver.findElement(By.cssSelector(".text")).click();

    {
      WebElement element = driver.findElement(By.cssSelector(".text"));
      Actions builder = new Actions(driver);
      builder.doubleClick(element).perform();
    }

    driver.findElement(By.cssSelector(".text")).click();

    assertThat(driver.findElement(By.cssSelector(".text")).getText(), is("Healthy Breeze"));

    driver.close();
  }
}