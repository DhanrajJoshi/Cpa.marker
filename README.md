# Text to Image — Android App

Modern Android application rewritten from Dhanraj Joshi's Text-to-Image & CPA Marker web app using Kotlin and Jetpack Compose.

## Features

- **AI Text → Image Generator**:
  - Creative prompt input with prompt inspiration chips
  - Aspect ratio & dimension controls (1:1 256×256, 1:1 512×512, 4:3 640×480, 2:1 1024×512)
  - Quality and model enhancement options (Standard, High)
  - Real-time generative preview with image zoom/scaling
  - Actions: Copy URL, share artwork, download/recreate
- **History & Gallery**:
  - Local persistence of all generated artworks
  - Detailed modal view with metadata (timestamp, resolution, prompt, filename)
  - Quick action to reuse previous prompts in the generator
- **Portfolio & Projects Showcase**:
  - Dhanraj Joshi's profile, bio, and technical skills overview
  - Experience timeline in web development, AI tooling, and CPA marketing
  - Featured projects with quick-launch prompts into the AI generator
- **Exclusive Rewards & Contact**:
  - CPA offer unlocks and reward codes
  - Contact form with input validation and local offline message queueing

## Architecture & Tech Stack

- **UI**: 100% Jetpack Compose with Material Design 3 (M3)
- **Language**: Kotlin 2.2 with Coroutines & StateFlow
- **Image Loading**: Coil 2.7 with crossfade and async states
- **Networking**: OkHttp 4.12
- **Persistence**: SharedPreferences / JSON offline storage
- **Toolchain**: AGP 9.1, Gradle 9.3.1, Android SDK 36 (Java 21)
