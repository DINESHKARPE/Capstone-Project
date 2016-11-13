#Capstone, Stage 2 - Build

 - Completed Task

App conforms to common standards found in the Android Nanodegree General Project Guidelines

App integrates a third-party library.
App validates all input from servers and users. If data does not exist or is in 4 the wrong format, the app logs this fact and does not crash.
App includes support for accessibility. That includes content descriptions, navigation using a D-pad, and, if applicable, non-audio versions of audio cues.
App keeps all strings in a strings.xml file and enables RTL layout switching on all layouts.
App provides a widget to provide relevant information to the user on the home screen.
App integrates two or more Google services. Google service integrations can be a part of Google Play Services or Firebase.
Each service imported in the build.gradle is used in the app.
If Location is used, the app customizes the user’s experience by using the device's location.
If Maps is used, the map provides relevant information to the user. If Maps was not used, student meets specifications.
If Identity is used, the user’s identity influences some portion of the app. If Identity was not used, student meets specifications.
App theme extends AppCompat.
App uses an app bar and associated toolbars.
App uses standard and simple transitions between activities.
App builds from a clean repository checkout with no additional configuration.
App builds and deploys using the installRelease Gradle task.
App is equipped with a signing configuration, and the keystore and passwords are included in the repository. Keystore is referred to by a relative path.
All app dependencies are managed by Gradle.
App implements a ContentProvider to access locally stored data.
Must implement at least one of the three
If it regularly pulls or sends data to/from a web service or API, app updates data in its cache at regular intervals using a SyncAdapter.
OR
If it needs to pull or send data to/from a web service or API only once, or on a per request basis (such as a search application), app uses an IntentService to do so.
OR
It it performs short duration, on-demand requests (such as search), app uses an AsyncTask.
App uses a Loader to move its data to its views.