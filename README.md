# Push Notification Tester

## How to get this thing running

Reuse the same project you've used for the server part (https://github.com/berndfinal/PushNotificationTester_Web). The project has an Id (which was the text you choose) and a number which needs to be set in [gcm.xml](/app/src/main/res/values/gcm.xml).

Deploy the server part on Google App Engine and modify the URL's in
[ConnectThread](/app/src/main/java/com/firstrowria/pushnotificationtester/threads/ConnectThread.java) and [TriggerNotificationThread](/app/src/main/java/com/firstrowria/pushnotificationtester/threads/TriggerNotificationThread.java) and you should be able to run this demo App.
