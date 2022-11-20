# Alert CPD

This CPD manages all user and system alerts.

## **API Info:**

### URL Scheme:

### Routes:

### **Authorization:**

## Architecture:

## Deployment and Dependencies

### Other process dependencies

- On Tomcat to get Active user details
USER_EMAIL_MAP_URL=http://eqplus.quodd.com/b4utrade/app/QuoddActiveUsers.do

- Fundamental Data process for fundamental alerts

- Email server to deliver email alerts to users


### Database dependencies

- Alert database - to manage active and historical alerts
mysql-quoddsupport-1.quodd.private:3306/alert

- Dowjones News database - to manage news related alerts
mysql-newsedge.quodd.private:3306/djnews

- NewsEdge News Database - to manage news related alerts
mysql-newsedge.quodd.private:3306/newsedge


# Daily Alert

This process is ran once a day to add all historical alerts to active alerts which are set with frequency as "daily alert". 
This ensures that "Daily alert" is active for user at start of day for monitoring.

This process code resides in alert cpd.