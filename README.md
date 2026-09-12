# RANSOMSMART

RANSOMSMART is a Java 21 desktop prototype for detecting ransomware-like file
activity in a configured local folder. It is intended for defensive research
and controlled demonstrations, not as a replacement for endpoint protection.

## Current capabilities

- Watches `monitor_folder` for new and modified files.
- Detects selected encryption-style extensions and ransom-note keywords.
- Uses a decoy file and a burst-of-modifications signal.
- Preserves flagged files in `quarantine` using unique evidence names.
- Records alerts in `alerts.log` and `alerts.db`.
- Observes established Windows TCP connections and reports new connections on
  selected high-risk ports.
- Provides a Swing dashboard, manual scan, decoy simulation, and optional
  Google Drive integration.

## Project layout

```text
src/ransomware/       Application source
resources/            Fonts, icon, and local-only OAuth credential location
monitor_folder/       Controlled monitoring samples
quarantine/           Preserved sample evidence and future quarantined files
alerts.log, alerts.db Sample alert history
docs/                 Engineering notes and next-stage plan
```

## Run locally

Install JDK 21 and Maven, then compile:

```powershell
mvn compile
```

Launch `ransomware.Main` from Eclipse (or another IDE) with Maven dependencies
enabled.

The Google Drive feature is optional. Its local OAuth client JSON belongs in
`resources/` and is intentionally ignored by Git. Do not commit credentials.

## Safety note

Use only benign simulations in a disposable test environment. Do not test with
live ransomware or use automatic containment without a reviewed recovery plan.
