# Push Notification Tester

## How to get this thing running

Register a new Project on Google Developer Console (https://console.developers.google.com). This project has an Id (which is a text you can choose) and a number which needs to be set in [gcm.xml](/app/src/main/res/values/gcm.xml).

Then deploy the server part on e.g. Google App Engine. Modify the URL's in
[ConnectThread](/app/src/main/java/com/firstrowria/pushnotificationtester/threads/ConnectThread.java) and [TriggerNotificationThread](/app/src/main/java/com/firstrowria/pushnotificationtester/threads/TriggerNotificationThread.java) and you should be able to run this demo App.
