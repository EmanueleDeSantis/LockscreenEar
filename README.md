# LockScreen
Android lockscreen for people with perfect pitch. Available from Android 7 (API 24).
Altough I built this app for Android 12 (API 31), I only tested it for Android 7 and Android 10 (API 29).

If enabled, the main service undertakes, after the device wakes up (e.g after the keyguard is gone), to start the lockscreen offered by LockScreen.

**If during this activity any action is taken to bypass it, the screen will be locked again.**

This service can also be managed via quick setting, which, **if specified in settings, also provides to lock the screen.**

Please do not start it if you do not have perfect pitch, you may not be able to unlock your screen in emergency situations!