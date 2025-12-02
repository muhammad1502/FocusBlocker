# FocusBlocker 🛡️🔕

**FocusBlocker** is a minimalist Android application designed to help you regain focus by blocking distracting notifications from specific apps. Unlike standard Do Not Disturb modes, FocusBlocker allows you to target specific apps (like WhatsApp, Instagram, Slack) while keeping important notifications active.

## ✨ Features

* **Selective Blocking:** Add specific package names (e.g., `com.whatsapp`, `com.slack`) to a block list.
* **Resume & Restore:** When you turn off "Block Mode", the app automatically restores the notifications you missed, so you never lose important messages.
* **Quick Settings Tile:** Toggle Focus Mode on/off instantly from your phone's notification shade/control center.
* **Persistent Storage:** Your block list is saved automatically.
* **Background Service:** Runs efficiently in the background using Android's native `NotificationListenerService`.
* **Privacy First:** No internet permission required (except for Gradle build). All data stays on your device.

## 🛠️ Technical Details

* **Language:** Kotlin
* **Minimum SDK:** API 24 (Android 7.0)
* **Target SDK:** API 34 (Android 14)
* **Architecture:** MVVM-lite (Service-based)
* **Key Components:**
    * `NotificationListenerService`: To intercept and cancel notifications.
    * `TileService`: To provide the Quick Settings toggle.
    * `SharedPreferences`: For persisting the block list.

## 🚀 How to Install

1.  **Download the APK:** Go to the [Releases](link-to-your-releases) page (once you create one) and download the latest `.apk`.
2.  **Install:** Open the file on your Android device. You may need to allow installation from unknown sources.
3.  **Grant Permissions:**
    * Open the app and click **"1. Grant Permission"**.
    * Find **FocusBlocker** in the list and toggle it **ON**.
    * (Android 13+) Allow the app to send notifications (required for the Restore feature).

## 💻 How to Build (For Developers)

1.  Clone the repository:
    ```bash
    git clone [https://github.com/YourUsername/FocusBlocker.git](https://github.com/YourUsername/FocusBlocker.git)
    ```
2.  Open the project in **Android Studio**.
3.  Sync Gradle files.
4.  Connect your device (Samsung S23 etc.) or use an Emulator.
5.  Click **Run** (Green Play Button).

## 📝 Usage

1.  **Add Apps:** Type the package name of the app you want to block (e.g., `com.instagram.android`) and click **Add**.
    * *Tip: You can find package names by sharing an app from the Play Store to a browser and looking at the URL `id=...` parameter.*
2.  **Start Blocking:** Toggle the switch to **ON** or tap the Quick Settings tile.
3.  **Stop Blocking:** Toggle the switch to **OFF**. All blocked notifications will reappear in your status bar marked as "(Missed)".

## 🤝 Contributing

Contributions are welcome! Please fork the repository and create a pull request.

## 📄 License

[MIT License](LICENSE)
