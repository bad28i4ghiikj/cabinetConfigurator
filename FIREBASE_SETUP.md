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
5. Place it in `app/` directory

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

## 5. Usage in Code

Firebase is initialized automatically when the app starts. Use Firebase classes directly:

### Authentication
```kotlin
import com.google.firebase.auth.FirebaseAuth

val auth = FirebaseAuth.getInstance()

// Register
auth.createUserWithEmailAndPassword(email, password)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val user = auth.currentUser
        }
    }

// Login
auth.signInWithEmailAndPassword(email, password)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val user = auth.currentUser
        }
    }

// Logout
auth.signOut()

// Get current user
val user = FirebaseAuth.getInstance().currentUser
val uid = user?.uid
val email = user?.email
val displayName = user?.displayName
```

### Firestore
```kotlin
import com.google.firebase.firestore.FirebaseFirestore

val db = FirebaseFirestore.getInstance()

// Write document
db.collection("quotes").document(quoteId).set(quoteData)
    .addOnSuccessListener { }
    .addOnFailureListener { }

// Read document
db.collection("quotes").document(quoteId).get()
    .addOnSuccessListener { document ->
        val quote = document.toObject(Quote::class.java)
    }

// Delete document
db.collection("quotes").document(quoteId).delete()

// Real-time listener
db.collection("quotes").whereEqualTo("uid", uid)
    .addSnapshotListener { snapshot, error ->
        if (error == null && snapshot != null) {
            val quotes = snapshot.toObjects(Quote::class.java)
        }
    }
```

### Cloud Storage
```kotlin
import com.google.firebase.storage.FirebaseStorage

val storage = FirebaseStorage.getInstance()

// Upload file
val ref = storage.reference.child("pdfs").child(uid).child(fileName)
ref.putFile(pdfUri)
    .addOnSuccessListener { }
    .addOnFailureListener { }

// Download file
ref.getBytes(Long.MAX_VALUE)
    .addOnSuccessListener { bytes ->
        // Use bytes
    }

// List files
storage.reference.child("pdfs").child(uid).listAll()
    .addOnSuccessListener { result ->
        val items = result.items
    }

// Delete file
ref.delete()

// Get download URL
ref.downloadUrl
    .addOnSuccessListener { uri ->
        val url = uri.toString()
    }
```

## Troubleshooting

**"google-services.json is required"**
- Download the file from Firebase Console and place in `app/` directory

**Authentication fails**
- Enable Email/Password in Firebase Console → Authentication

**Firestore write fails**
- Check security rules are set to test mode or have correct rules

**Storage upload fails**
- Ensure user is authenticated (login first)
- Check Storage bucket exists
