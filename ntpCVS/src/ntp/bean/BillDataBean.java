package ntp.bean;

public class BillDataBean {
	private String issue;
	private double discount;
	private double yield;

	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public double getPrice() {
		return discount;
	}
	public void setPrice(double discount) {
		this.discount = discount;
	}
	public double getYield() {
		return yield;
	}
	public void setYield(double yield) {
		this.yield = yield;
	}
}
