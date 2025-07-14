# ATool

Ein Analysewerkzeug für `fio`-Benchmark-Logs mit grafischer Oberfläche und statistischen Auswertungen.

---

## Abhängigkeiten

- **JavaFX Library**  
  [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)  - verwendete version 21.0.7 (LTS) SDK
  → Den Inhalt (außer die .zip Datei) aus `/javafx-sdk-21.0.7` ins `target/`-Verzeichnis kopieren nicht den `javafx-sdk-21.0.7` Ordner selbst.
  JavaFX wird nur für das Ausführen und nicht zum Bauen des Source Codes benötigt.
- **OpenJDK 21**

---

## Building and Running ATool
- Die Commands  werden unter /Atool ausgeführt
- wenn noch nicht gebaut wurde sollte der /Atool Ordner den /src Ordner beinhalten sowie die beiden Dateien pom.xml und nbactions.xml

# Projekt bauen
- mvn package

# Anwendung starten
- `java --module-path ./lib --add-modules javafx.controls,javafx.fxml -jar ./Atool-1.0-SNAPSHOT.jar`
    - diesen Command im /target Verzeichnis ausführen

## Verwendung
- Um mit dem fio-Tool Logs zu erzeugen, wird der Parameter `--write_bw_log=[logname]` verwendet.
  - **DABEI SOLLTE EIN FIO RUN MINDESTENS EINE MINUTE LAUFEN.**
- Wähle das Verzeichnis aus, in dem sich die `fio`-Logdateien befinden.
- In der Tabelle können folgende Werte angepasst werden:
  - **Alpha** (Auswählbar zwischen `0.1`, `0.05` und `0.01`)
  - **CV Threshold** (zwischen `0.05` und `0.5`)
    → Einfach auf die jeweilige Zahl klicken und bearbeiten und mit [Enter] bestätigen.
- Sobald das Verzeichnis gesetzt ist, ist es möglich mit dem **Refresh-MenuItem** in der Menüleiste unter `File` neue Logs zu laden, wenn neue Dateien im Verzeichnis auftauchen.
- Mit **Rechtsklick** auf die Items in der Tabelle können statistische Tests ausgewählt werden.
---

# Anwendungsbeispiel für das Tool
1. **Job mit fio ausführen mittels loop:**  
   `./fio --rw=write --loop=10 --write_bw_log=mytest --name=test --size=1024m`  
   **Alternativ (via runtime):**  
   `./fio --rw=write --runtime=300 --loop=1000 --write_bw_log=mytest --name=test --size=1024m`

2. **Analysetool starten** und den Pfad auswählen, wo sich die Logs befinden.

3. **Optionale Anpassungen**: Alpha-Wert und weitere Einstellungen nach Bedarf ändern.

4. **Evaluate steady state klicken**: Mit diesem Button werden alle Tests ausgeführt und in einer Tabelle dargestellt.

5. **Speichern**: Die Evaluierung aller Tests speichern mit **Save evaluation**.


- Das Beispiel wird genauer unter dem Verzeichnis /ExampleLog erklärt


# Einstellungen für die Anwendung

Diese Dokumentation beschreibt die verfügbaren Einstellungen und deren Bedeutung in der Anwendung.

---

## Datenkonvertierung
- **Optionen**:
  - **MebiByte**
  - **KibiByte**
  - **KiloByte**
- **Standardwert**: KibiByte.
- **Schieberegler**: Werte von 1 bis 500 für die Fenstergröße (Window size) für CoV und CUSUM.

---

## Run-Einstellungen

### Skip seconds sections
- **Beschreibung**: Wie viele Sekunden des start sollen ignoriert werden. Eine Sektion entspricht einer Sekunde der Daten.
- **Standardwert**: 0 Sekunden.

### Use Bonferroni correction
- **Beschreibung**: Aktiviert die Bonferroni-Korrektur zur statistischen Analyse.
- **Standardwert**: Deaktiviert.
---
### Required time for steady state
- **Beschreibung**: Mindestanzahl aufeinanderfolgender Sektionen/Sekunden, die erforderlich sind, um einen stabilen Zustand festzulegen.
- **Standardwert**: 60 Sekunden.
- **Maximal Wert**: 60 Sekunden.
- **Minimal Wert**: 30 Sekunden.

---

## Statistische Tests

- **Verfügbare Tests**:
  - ANOVA
  - Confidence Interval
  - T-Test
  - U-Test
  - Tukey HSD

---

## Speichern der Einstellungen

- Auf **Save & Exit** klicken, um die Änderungen zu speichern und das Fenster zu schließen.

---

## Hinweise
- Standardwerte sind als **(Default)** markiert.
- Einstellungen können je nach Anwendungskontext angepasst werden.
