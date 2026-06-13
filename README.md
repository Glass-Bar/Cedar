# Glass App Launcher – GDK XE24

A Glass Glassware that places a **LiveCard** on the timeline (the "widget").  
Tapping the card opens a **scrollable CardScrollView** listing every installed  
launchable app. Tapping an entry launches that app.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Android Studio | 2.x – 3.x recommended for API 19 projects |
| Android SDK Platform | **4.4.2 (API 19)** |
| Glass Development Kit Preview | From SDK Manager → "Android 4.4.2 (API 19)" |
| JDK | 7 or 8 |
| Google Glass (Explorer Edition) | Running **XE20.1 – XE24** |

---

## 1 – Install the GDK add-on

1. Open **Android Studio → SDK Manager** (or `sdkmanager` on the CLI).  
2. Under **Android 4.4.2 (API 19)** check:
   - `SDK Platform`
   - `Glass Development Kit Preview`
3. Click **Install packages** and accept the licence.

The GDK is a *provided* dependency — it ships with Glass and must **not** be
bundled in the APK.  The `app/build.gradle` already sets:

```groovy
compileSdkVersion 19
provided fileTree(dir: 'libs', include: ['*.jar'])
```

The GDK classes (`com.google.android.glass.*`) are resolved at compile time
against the add-on stub jar that the SDK Manager places inside the Android SDK
folder (`extras/google/glass_sdk/...`).  Android Studio picks this up
automatically once you select *"Glass Development Kit Developer Preview"* as
the compile target for the module.

---

## 2 – Open in Android Studio

```
File → Open → select the AppLauncher/ folder
```

When prompted, choose:

- **Compile with:** `Glass Development Kit Developer Preview`  
- **Theme:** `None` (or remove `android:theme` from the manifest root `<application>`)

---

## 3 – Enable USB debugging on Glass

```
Settings → Device Info → Turn on debug
```

Then connect Glass via USB and accept the RSA key prompt.

---

## 4 – Build & install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or simply hit **Run ▶** inside Android Studio.

---

## 5 – How to use

1. Wake Glass: tap the touchpad or say **"OK Glass"**.  
2. Say **"start launcher"** (the custom voice trigger).  
3. The **LiveCard** appears in the timeline with the label *"App Launcher"*.  
4. **Tap** the card — a menu appears:
   - **Open Launcher** → opens the full-screen immersion
   - **Stop** → removes the card and stops the service
5. Inside the immersion **swipe forward/backward** to scroll through apps.  
6. **Tap** on an app card to launch it.  
7. **Swipe down** at any time to go back to the timeline.

---

## Project structure

```
AppLauncher/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/glass/applauncher/
│   │   ├── LauncherService.java   ← publishes the LiveCard
│   │   ├── MenuActivity.java      ← transparent menu trampoline
│   │   ├── AppListActivity.java   ← CardScrollView immersion
│   │   ├── AppScrollAdapter.java  ← GDK CardScrollAdapter
│   │   └── AppInfo.java           ← simple data class
│   └── res/
│       ├── layout/
│       │   ├── live_card.xml       ← card shown in the timeline
│       │   └── card_app_item.xml   ← one card per app in the list
│       ├── menu/
│       │   └── live_card_menu.xml  ← tap menu (Open / Stop)
│       ├── values/strings.xml
│       └── xml/voice_trigger.xml   ← "start launcher" trigger
└── app/build.gradle
```

---

## Key GDK APIs used

| Class | Purpose |
|---|---|
| `LiveCard` | The timeline "widget" |
| `LiveCard.PublishMode.REVEAL` | Navigates to the card on publish |
| `RemoteViews` | Renders content inside the LiveCard |
| `CardScrollView` | Horizontally scrollable full-screen card list |
| `CardScrollAdapter` | Adapter backing the `CardScrollView` |
| `Sounds.TAP` | Glass audible tap feedback |
| `VoiceTrigger` XML | "OK Glass, start launcher" hook |

---

## Notes & caveats

- The GDK is **deprecated** (Glass Explorer Edition was discontinued).  
  This code compiles and runs on XE20.1–XE24 firmware.
- `GET_TASKS` permission is used on API 19; it was restricted in later Android
  versions but is unrestricted on Glass.
- For production submission (MyGlass), a voice trigger keyword must be approved
  by Google.  For sideloaded developer builds any keyword string works.
- Glass has no traditional "launcher"; every app that declares a
  `CATEGORY_LAUNCHER` Activity shows up in the list, including system apps.
  You can filter them by checking `ApplicationInfo.FLAG_SYSTEM` if desired.
