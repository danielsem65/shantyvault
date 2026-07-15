# Shanty Vault

**Secure Personal Cloud Vault** — An Android application for private, encrypted cloud storage with Supabase.

## Features

### Security
- AES-256 encryption for all files before upload
- Android Keystore for secure token storage
- SQLCipher encrypted Room database
- TLS/HTTPS for all network communication
- Biometric authentication (Fingerprint/Face Unlock)
- Session timeout and auto-logout
- Rate limiting and account lockout protection

### Authentication
- Email and password registration/login via Supabase Auth
- Email verification
- Password reset via email
- Trusted device recognition

### File Management
- Upload, download, rename, delete, move files
- Encrypted upload with automatic client-side encryption
- Grid/List view toggle
- Favorites
- Folder organization (create, rename, delete, move)

### Media Viewer
- Images: Pinch-to-zoom, swipe, fullscreen
- Videos: Play/Pause, seek, fullscreen (ExoPlayer)
- Audio: Player with seek and time display

### Notes
- Encrypted rich text notes
- Pin/unpin notes
- Color-coded notes
- Auto-save (2 second debounce)

### Search
- Instant search with debounce
- Filter by file type

### Settings
- Theme: Light, Dark, System
- Security: Change password, Biometrics, Session management
- Privacy: Storage usage, Notification preferences

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Manual (AppContainer + ViewModelFactory) |
| Database | Room + SQLCipher |
| Local Storage | DataStore |
| Auth | Supabase Auth |
| Cloud Storage | Supabase Storage |
| Backend API | Supabase PostgREST |
| HTTP Client | Ktor |
| Image Loading | Coil |
| Video Player | Media3 ExoPlayer |
| Background Work | WorkManager |
| Security | Android Keystore, AES-256 |
| Biometrics | Android Biometric API |
| Serialization | Kotlinx Serialization |

## Project Structure

```
com.shanty.vault/
├── ShantyVaultApp.kt          # Application class (DI container host)
├── MainActivity.kt            # Entry point
├── di/                        # Dependency Injection
│   ├── AppContainer.kt        # Manual DI container (singletons)
│   └── ViewModelFactory.kt    # ViewModel provider
├── data/
│   ├── local/                 # Room DB, DAOs, DataStore
│   ├── repository/            # Repository implementations
│   ├── model/                 # Room entities
│   └── worker/                # WorkManager workers
├── domain/
│   ├── model/                 # Domain models
│   └── repository/            # Repository interfaces
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

## Setup

### Prerequisites
- Android Studio or IntelliJ IDEA
- JDK 17+
- Android SDK 35

### Supabase Setup
1. Create a Supabase project at [supabase.com](https://supabase.com)
2. Enable Email/Password authentication
3. Create a `vault-files` storage bucket
4. Copy your project URL and anon key
5. Add them to `app/src/main/java/com/shanty/vault/util/Constants.kt`

### Build & Run
```bash
./gradlew assembleDebug
```

### Build Variants
- `debug`: Development build with additional logging
- `release`: Production build with ProGuard, logs stripped
