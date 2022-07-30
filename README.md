# LockscreenEar
Android lockscreen for people with perfect pitch. Available from Android 7 (API 24) (Not yet tested on android 12).

<div align="center">
	<img src="https://github.com/EmanueleDeSantis/LockscreenEar/blob/main/img/Screenshot_MainActivity.png" alt="Screenshot_MainActivity" width="250" height="500"/> 
	<img src="https://github.com/EmanueleDeSantis/LockscreenEar/blob/main/img/Screenshot_LockscreenEarService_locked.png" alt="Screenshot_LockscreenEarService_locked" width="250" height="500"/>
	<img src="https://github.com/EmanueleDeSantis/LockscreenEar/blob/main/img/Screenshot_LockscreenEarService_unlocked.png" alt="Screenshot_LockscreenEarService_unlocked" width="250" height="500"/>
</div>

Although this app is suppposed to be a lockscreen, it is also an excellent tool to improve your musical ear,
in fact, in the training mode, once you set the number of notes to be guessed at the same time, you can listen the output as mutch as you want and you will also get the solution from the lowest note to the highest:

<div align="center">
	<img src="https://github.com/EmanueleDeSantis/LockscreenEar/blob/main/img/Screenshot_EarTrainingFragment.png" alt="Screenshot_EarTrainingFragment" width="250" height="500"/>
</div>

-) red color means you guessed wrong;<br />
-) yellow color means you missed that note;<br />
-) green color means you guessed right.<br />

Also, you can click the "DIAPASON" button to listen to the A at 440Hz.

## Settings
In the setting activity you can customize as much as you want the dynamics of the lockscreen service:

<div align="center">
	<img src="https://github.com/EmanueleDeSantis/LockscreenEar/blob/main/img/Screenshot_SettingsActivity.png" alt="Screenshot_SettingsActivity" width="250" height="500"/> 
	<img src="https://github.com/EmanueleDeSantis/LockscreenEar/blob/main/img/Screenshot_VolumeAdapterSettingFragment.png" alt="Screenshot_VolumeAdapterSettingFragment" width="250" height="500"/>
</div>

## Warnings
If enabled, the main service undertakes, after the device wakes up (e.g after the keyguard is gone), to start the lockscreen offered by LockscreenEar.

**If during this activity any action is taken to bypass it, the screen will be locked again.**

Please do not start it if you do not have perfect pitch, you may not be able to unlock your screen in emergency situations, although there is a test to be passed in order to be able to start it.

## Download

Download the app from the [Releases section](https://github.com/EmanueleDeSantis/LockscreenEar/releases/latest)

or get it via the IzzyOnDroid F-Droid compatible repo :

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
     alt="Get it on IzzyOnDroid"
     height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.taffo.lockscreenear)
