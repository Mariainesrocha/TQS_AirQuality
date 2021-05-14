package tqs.ua.pt.airquality;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SeleniumCucumberSteps {
    private WebDriver driver;

    @Given("Open Browser")
    public void OpenBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @When("I navigate to {string}")
    public void i_navigate_to(String url) {
        driver.get(url);
    }

    @When("I type {string}")
    public void i_type(String string) {
        driver.findElement(By.name("q")).sendKeys(string);
    }

    @When("I scroll to {string} section")
    public void i_scroll(String text) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement Element = driver.findElement(By.xpath("//h2[contains(.,'"+text+"')]"));
        js.executeScript("arguments[0].scrollIntoView();", Element);
    }

    @When("I click on {string}")
    public void clickTextButton(String button_text) {
        driver.findElement(By.xpath("//*[text()[contains(.,'"+button_text+"')]]")).click(); //using Selenium click button method
    }

    @When("I write {string} in the {string}")
    public void searchInputFill(String text,String field_name) {
        driver.findElement(By.name(field_name)).sendKeys(text);
    }

    @Then("I should be shown an error message with body like {string}")
    public void showErrorModal(String result) throws InterruptedException {
        Thread.sleep(3000);
        assertThat(driver.findElement(By.className("modal-body")).getText(), is(result));;
        driver.quit();
    }

    @Then("I should be shown results including {string}")
    public void i_should_be_shown_results_including(String result) {
        try {
            driver.findElement(
                    By.xpath("//*[contains(text(), '" + result + "')]"));
        } catch (NoSuchElementException e) {
            throw new AssertionError("\"" + result + "\" not available in results");
        } finally {
            driver.quit();
        }
    }
    @Then("I should be shown a table row with results including {string} in city name")
    public void table_results(String city) {
            assertThat(driver.findElement( By.cssSelector("td:nth-child(1)")).getText(), containsStringIgnoringCase(city));
            driver.quit();
    }
}