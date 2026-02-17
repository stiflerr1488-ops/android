# Firebase quick setup (must-do)

1) In Firebase Console, add an Android app with package name:

   com.example.teamcompass

   (or change applicationId/namespace in app/build.gradle.kts to your own package and re-download json)

2) Download `google-services.json` and place it here:

   android/app/google-services.json

3) Enable:
   - Authentication -> Anonymous
   - Realtime Database -> Create database

4) Enable Realtime Database rules (MVP):

```
{
  "rules": {
    "teams": {
      "$code": {
        ".read": "auth != null && root.child('teams').child($code).child('members').child(auth.uid).exists()",
        "meta": {
          ".write": "auth != null && (!data.exists())"
        },
        "members": {
          "$uid": {
            ".write": "auth != null && auth.uid === $uid",
            ".validate": "newData.hasChildren(['callsign','joinedAt'])"
          }
        },
        "state": {
          "$uid": {
            ".write": "auth != null && auth.uid === $uid && root.child('teams').child($code).child('members').child(auth.uid).exists()",
            ".validate": "newData.hasChildren(['lat','lon','acc','ts','callsign'])"
          }
        }
      }
    }
  }
}
```

Then run:

Windows PowerShell:

```
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :app:installDebug
```
