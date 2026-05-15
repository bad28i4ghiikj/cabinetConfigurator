# Cabinet Configurator — Compile-ready starter

To jest **compile-ready starter** Android app w Kotlin + Jetpack Compose + Room + WorkManager + Retrofit.

## Założenia
- `minSdk = 26`
- Kotlin `1.9.24`
- AGP `8.5.2`
- Compose BOM `2024.06.00`
- Room `2.6.1`

## Co działa już teraz
- buduje się jako projekt Android
- tworzy lokalną bazę Room
- ma aktywny profil cenowy inicjalizowany przy starcie
- pozwala wpisać kilka parametrów cenowych
- ma prosty konfigurator wyceny
- liczy wycenę lokalnie
- zapisuje wycenę wraz ze snapshotem parametrów do Room
- pokazuje historię wycen

## Czego jeszcze nie robi
- nie ma pełnego backend auth
- sync worker jest szkieletem
- UI jest celowo proste, ale działające

## Jak uruchomić
1. Otwórz katalog w Android Studio.
2. Poczekaj aż Gradle pobierze zależności.
3. Uruchom app na emulatorze / urządzeniu z Android 8.0+.
