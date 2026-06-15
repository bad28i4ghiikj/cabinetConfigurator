# Firebase Setup Guide

## 1. Firebase Console Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: `CabinetConfigurator`
4. Enable Google Analytics (optional)
5. Wait for project creation

## 2. Android App Registration

1. In Firebase Console, click "Add app" → Android
2. Package name: `com.example.cabinetconfigurator`
3. Debug SHA-1: Get it by running:
   ```bash
   ./gradlew signingReport
   ```
   Copy the SHA-1 value from `debug` build variant
4. Download `google-services.json`
5. Place it in `app/` directory (replace the template file)

## 3. Enable Firebase Services

In Firebase Console → Project Settings → Enable these services:

### Authentication
- Go to Authentication → Sign-in method
- Enable: Email/Password

### Firestore Database
- Go to Firestore Database → Create database
- Start in test mode (for development)
- Create collection: `quotes`

### Cloud Storage
- Go to Storage → Create bucket
- Use default settings

### Analytics (Optional)
- Already enabled by default

## 4. Security Rules (for production)

### Firestore Rules
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /quotes/{uid}/{document=**} {
      allow read, write: if request.auth.uid == uid;
    }
  }
}
```

### Storage Rules
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /pdfs/{uid}/{allPaths=**} {
      allow read, write: if request.auth.uid == uid;
    }
  }
}
```

## 5. Integration in Code

### Usage Example

```kotlin
// Initialize Firebase (done automatically in MainActivity)
FirebaseModule.initialize()

// Get managers
val authManager = FirebaseModule.getAuthManager()
val firestoreManager = FirebaseModule.getFirestoreManager()
val storageManager = FirebaseModule.getStorageManager()

// Login
val success = authManager.loginWithEmail("user@example.com", "password")

// Upload quote to Firestore
firestoreManager.uploadQuote(quote)

// Upload PDF to Storage
storageManager.uploadPdf(pdfFile, quoteId)

// Observe cloud quotes
firestoreManager.observeCloudQuotes().collect { quotes ->
    // Handle quotes
}
```

## 6. Available Features

### FirebaseAuthManager
- `registerWithEmail(email, password, displayName)` - Register new user
- `loginWithEmail(email, password)` - Login user
- `logout()` - Logout current user
- `getUid()` - Get current user UID
- `getEmail()` - Get current user email
- `getDisplayName()` - Get current user display name

### FirebaseFirestoreManager
- `uploadQuote(quote)` - Save quote to cloud
- `deleteQuoteFromCloud(quoteId)` - Delete quote from cloud
- `observeCloudQuotes()` - Real-time quote sync

### FirebaseStorageManager
- `uploadPdf(pdfFile, quoteId)` - Upload PDF to cloud
- `downloadPdf(fileName)` - Download PDF
- `listUserPdfs()` - List all user PDFs
- `deletePdf(fileName)` - Delete PDF
- `getPdfDownloadUrl(fileName)` - Get public download URL

## 7. Troubleshooting

**"google-services.json is required"**
- Download the file from Firebase Console and place in `app/` directory

**Authentication fails**
- Enable Email/Password in Firebase Console → Authentication

**Firestore write fails**
- Check security rules are set to test mode or have correct rules

**Storage upload fails**
- Ensure user is authenticated (login first)
- Check Storage bucket exists
