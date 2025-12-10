# NovelReader

Aplikacja desktopowa umożliwiająca wygodne czytanie książek wformacie EPUB oraz zarządzanie nimi za pomocą biblioteki użytkownika.

### Środowisko

Aplikacja została stworzona, zaimplementowana oraz testowana w środowiskach Windows 11 oraz Linux Ubuntu 25.10

### Uruchomienie aplikacji

W celu uruchomienia aplikacji należy włączyć aplikację **Docker Desktop** <br>
Następnie postawić kontener poleceniem ```docker compose up``` w folderze projektu **NovelReader_server** <br>
Następnie rozpocząć działanie serwera uruchamiając plik **NovelReader_server\src\main\java\org.example.novelreader\NovelReaderApplication.java** <br>

Po wszczęciu pracy serwera należy uruchomić aplikację poprzez plik **NoverReader_client\src\main\java\org.core.novelreader_client\Launcher.java**