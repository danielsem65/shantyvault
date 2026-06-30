# Shanty Vault

**Secure Personal Cloud Vault** — A premium Android application for private, encrypted cloud storage.

## Features

### Security
- AES-256 encryption for all files before upload
- Android Keystore for secure token storage
- SQLCipher encrypted Room database
- TLS/HTTPS for all network communication
- Biometric authentication (Fingerprint/Face Unlock)
- Session timeout and auto-logout
- Rate limiting and account lockout protection
- Screenshot prevention on sensitive screens
- Input sanitization and response validation

### Authentication
- Email and password registration/login
- Strong password requirements (12+ chars, uppercase, lowercase, digit, special)
- Email verification required before first login
- Password reset via email
- Multi-factor authentication (MFA) support
- Trusted device recognition

### Dashboard
- Welcome message with personalized greeting
- Storage usage overview with progress bar
- Recent uploads carousel
- Favorite files shortcut
- Recent activity timeline
- Quick upload button

### File Management
- Support for: Images, Videos, PDFs, Word, Excel, ZIP, Audio, Text
- Operations: Upload, Download, Rename, Delete, Move, Copy, Search
- Encrypted upload with automatic encryption
- Grid/List view toggle
- Favorite files
- File type icons and previews

### Folder Management
- Create, rename, delete, move nested folders
- Favorite folders
- Path-based navigation

### Search
- Instant search with debounce (300ms)
- Filter by type (Images, Videos, Documents, Audio, Archives)
- Results with file details

### Notes
- Encrypted rich text notes
- Pin/unpin notes
- Color-coded notes
- Auto-save (2 second debounce)
- Search notes

### Media Viewer
- Images: Pinch-to-zoom, swipe, fullscreen
- Videos: Play/Pause, seek, fullscreen (ExoPlayer)
- Documents: Text preview, PDF viewer
- Audio: Player with seek and time display

### Settings
- Theme: Light, Dark, System
- Security: Change password, Biometrics, MFA, Session management
- Privacy: Storage usage, Notification preferences
- About: App version

### Performance
- Lazy loading with pagination
- Image caching with Coil
- Background upload/download with WorkManager
- Retry failed uploads (exponential backoff)
- Efficient memory usage

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room + SQLCipher |
| Local Storage | DataStore |
| Auth | Firebase Authentication |
| Cloud Storage | Firebase Cloud Storage |
| Networking | Retrofit + OkHttp |
| Image Loading | Coil |
| Video Player | Media3 ExoPlayer |
| Background Work | WorkManager |
| Security | Android Keystore, AES-256 |
| Biometrics | Android Biometric API |

## Project Structure

```
com.shanty.vault/
├── ShantyVaultApp.kt          # Application class
├── MainActivity.kt            # Entry point
├── di/                        # Dependency Injection modules
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── data/
│   ├── local/                 # Room DB, DAOs, DataStore
│   ├── remote/                # Retrofit API service
│   ├── repository/            # Repository implementations
│   ├── model/                 # Room entities
│   └── worker/                # WorkManager workers
├── domain/
│   ├── model/                 # Domain models
│   ├── repository/            # Repository interfaces
│   └── usecase/               # Use cases
├── presentation/
│   ├── auth/                  # Login, Register, Biometric screens
│   ├── dashboard/             # Dashboard screen
│   ├── files/                 # File management screens
│   ├── folders/               # Folder detail screen
│   ├── notes/                 # Notes screens
│   ├── search/                # Search screen
│   ├── settings/              # Settings screen
│   ├── viewer/                # Media viewer screen
│   ├── navigation/            # Navigation graph
│   ├── components/            # Shared UI components
│   └── theme/                 # Material 3 theme
├── security/                  # Encryption, Token management
└── util/                      # Constants, Extensions
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- A Firebase project

### Firebase Setup
1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Register your Android app with package name `com.shanty.vault`
3. Download `google-services.json` and place it in `app/`
4. Enable Email/Password authentication in Firebase Console
5. Enable Firebase Storage and set up security rules
6. Enable Firebase Cloud Messaging for notifications

### Build & Run
1. Open the project in Android Studio
2. Sync Gradle (File → Sync Project with Gradle Files)
3. Select a device/emulator (API 26+)
4. Click Run or use `./gradlew assembleDebug`

### Build Variants
- `debug`: Development build with logging enabled
- `release`: Production build with ProGuard, no debug logs

## Security Considerations

- All files are encrypted with AES-256 before uploading to the cloud
- The Room database is encrypted using SQLCipher with a device-derived key
- Authentication tokens are stored in Android Keystore
- Network traffic uses TLS/HTTPS exclusively
- Debug logs are stripped from release builds
- Screenshots are blocked on authentication screens
- Input is validated and sanitized on all entry points
- API responses are validated before processing

## License

Private. All rights reserved.
