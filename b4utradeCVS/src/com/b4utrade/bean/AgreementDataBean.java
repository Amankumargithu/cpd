package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;

public class AgreementDataBean extends DefaultObject{

	private String userName;
	private int userId;
	private String firstName="";
	private String lastName="";	
	private String addressLine1="";
	private String addressLine2="";
	private String city="";
	private String state="";
	private String zip="";
	private String country="";
	private String email="";
	private String number="";
	private String title="";
	private String occupation="";
	private String userType;
	private byte[] creditCardNumber;
	private String ccFirstName = "";
	private String ccLastName ="";
	private String ccEmail = "";
	private String ccExpirationMonth = "";
	private String ccExpirationYear = "";
	private String ccBillingAddress2 = "";
	private String ccBillingAddress = "";
	private String ccCity = "";
	private String ccState ="";
	private String ccZip = "";
	private String ccCountry = "";
	private String ccPhone = "";
	private String ccCardType = "";
	private String subscriberEmployeeNameAddress="N/A";
	private String subscriberTitle="N/A";
	private String subscriberEmploymentFunction="N/A";
	private String signDate ="";
	private String ccSignDate = "";
	private int ccLastDigits;
	private String companyName;
	private String questionA;
	private String questionOther;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getAddressLine1() {
		return addressLine1;
	}
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	public String getAddressLine2() {
		return addressLine2;
	}
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getCountry() {
		return country;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getOccupation() {
		return occupation;
	}
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public byte[] getCreditCardNumber() {
		return creditCardNumber;
	}
	public void setCreditCardNumber(byte[] creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}
	public String getCcFirstName() {
		return ccFirstName;
	}
	public void setCcFirstName(String ccFirstName) {
		this.ccFirstName = ccFirstName;
	}
	public String getCcLastName() {
		return ccLastName;
	}
	public void setCcLastName(String ccLastName) {
		this.ccLastName = ccLastName;
	}
	public String getCcEmail() {
		return ccEmail;
	}
	public void setCcEmail(String ccEmail) {
		this.ccEmail = ccEmail;
	}
//	public String getCcExpirationMonthYear() {
//		return ccExpirationMonthYear;
//	}
//	public void setCcExpirationMonthYear(String ccExpirationMonthYear) {
//		this.ccExpirationMonthYear = ccExpirationMonthYear;
//	}
	public String getCcBillingAddress() {
		return ccBillingAddress;
	}
	public void setCcBillingAddress(String ccBillingAddress) {
		this.ccBillingAddress = ccBillingAddress;
	}
	public String getCcCity() {
		return ccCity;
	}
	public void setCcCity(String ccCity) {
		this.ccCity = ccCity;
	}
	public String getCcState() {
		return ccState;
	}
	public void setCcState(String ccState) {
		this.ccState = ccState;
	}
	public String getCcZip() {
		return ccZip;
	}
	public void setCcZip(String ccZip) {
		this.ccZip = ccZip;
	}
	public String getCcCountry() {
		return ccCountry;
	}
	public void setCcCountry(String ccCountry) {
		this.ccCountry = ccCountry;
	}
	public String getCcPhone() {
		return ccPhone;
	}
	public void setCcPhone(String ccPhone) {
		this.ccPhone = ccPhone;
	}
	public String getCcCardType() {
		return ccCardType;
	}
	public void setCcCardType(String ccCardType) {
		this.ccCardType = ccCardType;
	}
//	public String getEventDisputeText() {
//		return eventDisputeText;
//	}
//	public void setEventDisputeText(String eventDisputeText) {
//		this.eventDisputeText = eventDisputeText;
//	}
//	public String getFax() {
//		return fax;
//	}
//	public void setFax(String fax) {
//		this.fax = fax;
//	}
	public String getSubscriberEmployeeNameAddress() {
		return subscriberEmployeeNameAddress;
	}
	public void setSubscriberEmployeeNameAddress(
			String subscriberEmployeeNameAddress) {
		this.subscriberEmployeeNameAddress = subscriberEmployeeNameAddress;
	}
	public String getSubscriberTitle() {
		return subscriberTitle;
	}
	public void setSubscriberTitle(String subscriberTitle) {
		this.subscriberTitle = subscriberTitle;
	}
	public String getSubscriberEmploymentFunction() {
		return subscriberEmploymentFunction;
	}
	public void setSubscriberEmploymentFunction(String subscriberEmploymentFunction) {
		this.subscriberEmploymentFunction = subscriberEmploymentFunction;
	}
	public String getSignDate() {
		return signDate;
	}
	public void setSignDate(String signDate) {
		this.signDate = signDate;
	}
	public String getCcSignDate() {
		return ccSignDate;
	}
	public void setCcSignDate(String ccSignDate) {
		this.ccSignDate = ccSignDate;
	}
	public String getCcExpirationMonth() {
		return ccExpirationMonth;
	}
	public void setCcExpirationMonth(String ccExpirationMonth) {
		this.ccExpirationMonth = ccExpirationMonth;
	}
	public String getCcExpirationYear() {
		return ccExpirationYear;
	}
	public void setCcExpirationYear(String ccExpirationYear) {
		this.ccExpirationYear = ccExpirationYear;
	}
	public String getCcBillingAddress2() {
		return ccBillingAddress2;
	}
	public void setCcBillingAddress2(String ccBillingAddress2) {
		this.ccBillingAddress2 = ccBillingAddress2;
	}
	public int getCcLastDigits() {
		return ccLastDigits;
	}
	public void setCcLastDigits(int ccLastDigits) {
		this.ccLastDigits = ccLastDigits;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getQuestionA() {
		return questionA;
	}
	public void setQuestionA(String questionA) {
		this.questionA = questionA;
	}
	public String getQuestionOther() {
		return questionOther;
	}
	public void setQuestionOther(String questionOther) {
		this.questionOther = questionOther;
	}
	
}
