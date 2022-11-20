package com.webtest;
import org.openqa.selenium.By;  
import org.openqa.selenium.WebDriver;  
import org.openqa.selenium.chrome.ChromeDriver;  


public class Testing {

	public static void main(String[] args) {
	    System.setProperty("webdriver.chrome.driver", "C:\\Users\\SONY\\Downloads\\driver\\chromedriver.exe");
	    WebDriver driver=new ChromeDriver();  
	    driver.get("https://d32g0ru97b53ik.cloudfront.net/");
	    driver.findElement(By.name("username")).sendKeys("himanshu.mittal@zversal.com"); 
	    driver.findElement(By.name("password")).sendKeys("Hkm@0101!");
	    driver.findElement(By.xpath("//button[contains(text(),'Sign In')]")).click();
	      




	}

}
