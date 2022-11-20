package com.gillco.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Gillcotest {

	public static void main(String[] args) {
		 System.setProperty("webdriver.chrome.driver", "C:\\Users\\SONY\\Downloads\\driver\\chromedriver.exe");
		    WebDriver driver=new ChromeDriver();  
		    driver.get("https://gillcogroup.com/");
		   // driver.findElement(By.xpath("//body/div[1]/div[1]/aside[1]/div[1]/a[1]/span[2]")).click();
		 //   driver.findElement(By.xpath("//body/div[1]/div[1]/aside[1]/div[1]/a[1]/span[2]/span[1]")).click();
         driver.findElement(By.linkText("Know More")).click();
         driver.findElement(By.xpath("//body/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[2]/a[1]/span[1]")).click();

	}

}
