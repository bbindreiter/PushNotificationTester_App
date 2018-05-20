# Push Notification Tester App

Source of the Demo App found on Google Play to check whether Push Notifications can be received (http://firebase.google.com/docs/cloud-messaging/)

## How to get this thing running

Reuse the same Firebase project you've used for the server part (https://github.com/berndfinal/PushNotificationTester_Web). In the Firebase Console go to Project Settings, add your Android App and download the generated google-services.json file. Copy it into the [app folder](/app/)

Deploy the server part on Google App Engine and modify the URL's in
[ConnectThread](/app/src/main/java/com/firstrowria/pushnotificationtester/threads/ConnectThread.java) and [TriggerNotificationThread](/app/src/main/java/com/firstrowria/pushnotificationtester/threads/TriggerNotificationThread.java) and you should be able to run this demo App.
