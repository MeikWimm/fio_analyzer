# ATool

Ein Analysewerkzeug für `fio`-Benchmark-Logs mit grafischer Oberfläche und statistischen Auswertungen.

---

## Abhängigkeiten

- **JavaFX Library**  
  [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)  
  → Den Ordner `lib` aus `javafx-sdk-21.0.1/lib` ins `target/`-Verzeichnis kopieren.

- **OpenJDK 21**

---

## Building and Running ATool

# Projekt bauen
mvn package

# Anwendung starten
- mvn exec:exec@run-javafx (oder:)
- java --module-path ./lib --add-modules javafx.controls,javafx.fxml -jar ./Atool-1.0-SNAPSHOT.jar
    - diesen Command im /target Verzeichnis ausführen

## Verwendung

- Wähle das Verzeichnis aus, in dem sich die `fio`-Logdateien befinden.
- In der Tabelle können folgende Werte angepasst werden:
  - **Runs** (zwischen `1` und `1000`)
  - **Alpha** (zwischen `0.0001` und `0.9999`)  
    → Einfach auf die jeweilige Zahl klicken und bearbeiten.
- Sobald das Verzeichnis gesetzt ist, können mit dem **Refresh-Button** neue Logs geladen werden, wenn neue Dateien im Verzeichnis auftauchen.
- Mit **Rechtsklick** auf die Items in der Tabelle können statistische Tests ausgewählt werden.

---

## Einstellungen

- Unter **Settings** kann die Einheit für die I/O-Geschwindigkeit geändert werden (z. B. KiB/s, MiB/s).
- **Average time per milli**:
  - Hiermit wird angegeben, ob die `fio`-Daten z. B. alle `100 ms` gemittelt werden sollen.
  - Bei einem Wert von `1` wird jeder Millisekunde ein I/O-Geschwindigkeitswert zugeordnet.
- Der letzte **Slider für "Runs"** betrifft den **ANOVA-Test**:
  - Gibt an, über wie viele Durchläufe hinweg die statistische Analyse durchgeführt wird.
