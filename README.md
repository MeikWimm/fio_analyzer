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
- mvn exec:exec@run-javafx (alternativ:)
- java --module-path ./lib --add-modules javafx.controls,javafx.fxml -jar ./Atool-1.0-SNAPSHOT.jar
    - diesen Command im /target Verzeichnis ausführen

## Verwendung
- Um mit dem fio-Tool Logs zu erzeugen, wird der Parameter `--write_bw_log=[logname]` verwendet.
- Wähle das Verzeichnis aus, in dem sich die `fio`-Logdateien befinden.
- In der Tabelle können folgende Werte angepasst werden:
  - **Runs** (zwischen `1` und `1000`)
  - **Alpha** (zwischen `0.0001` und `0.9999`)
  - **CV Threshold** (zwischen `0.1` und `0.3`)
    → Einfach auf die jeweilige Zahl klicken und bearbeiten und mit [Enter] bestätigen.
- Sobald das Verzeichnis gesetzt ist, können mit dem **Refresh-Button** neue Logs geladen werden, wenn neue Dateien im Verzeichnis auftauchen.
- Mit **Rechtsklick** auf die Items in der Tabelle können statistische Tests ausgewählt werden.
---

# Anwendungsbeispiel für das Tool
1. **Job mit fio ausführen:**`./fio --rw=write --loop=10 --write_bw_log=mytest --name=test --size=1024m`
2. **Analysetool starten** und den Pfad auswählen, wo sich die Logs befinden.
3. **Run-Anzahl anpassen**, je nachdem wie oft geloopt wurde (im Beispiel: 10).
4. **Optionale Anpassungen**: Alpha-Wert und weitere Einstellungen nach Bedarf ändern.
5. **Test auswählen**: Mit **Rechtsklick** einen Test auswählen.

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

### Use sequential Run (otherwise adjacent)
- **HINWEIS**: für Atool v0.2.1 (sowie die vorherigen Versionen) ist das Labeling vertauscht! ->  "Use adjacent run (otherwise sequential)"
- **Beschreibung**: Aktiviert die Verwendung von benachbarten Durchläufen, anstelle einer sequenziellen Verarbeitung.
- **Standardwert**: Deaktiviert.

### Skip runs
- **Beschreibung**: Anzahl der Durchläufe, die übersprungen werden sollen.
- **Standardwert**: 0.

### Use Bonferroni correction
- **Beschreibung**: Aktiviert die Bonferroni-Korrektur zur statistischen Analyse.
- **Standardwert**: Deaktiviert.

### Required runs for steady state
- **Beschreibung**: Mindestanzahl aufeinanderfolgender Runs, die erforderlich sind, um einen stabilen Zustand festzulegen.
- **Standardwert**: 5.
- **Maximal Wert**: 10.
- **Minimal Wert**: 2.

---

## Statistische Tests

- **Verfügbare Tests**:
  - ANOVA
  - Confidence Interval
  - T-Test
  - U-Test
  - Tukey HSD

---

## ANOVA-Einstellungen

### Number of Runs to compare for ANOVA
- **Beschreibung**: Anzahl der Durchläufe, die für den ANOVA-Test verglichen werden.
- **Standardwert**: 2.
- **Schieberegler**: Werte von 2 bis 5.

---

## Speichern der Einstellungen

- Auf **Save & Exit** klicken, um die Änderungen zu speichern und das Fenster zu schließen.

---

## Hinweise
- Standardwerte sind als **(Default)** markiert.
- Einstellungen können je nach Anwendungskontext angepasst werden.
