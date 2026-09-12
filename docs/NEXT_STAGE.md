# Next-stage engineering baseline

## Current boundaries

The application detects activity in one relative folder and performs local,
alert-only network observation. It does not attribute file changes to a process
or block processes and connections.

## First implementation milestone

Introduce a typed alert model and an event pipeline before adding more rules:

1. Define `Alert`, `AlertSeverity`, `AlertEvidence`, and `ResponseAction`.
2. Route all detectors through one alert service instead of writing directly to
   the GUI, log, database, and quarantine service.
3. Add a configuration file for monitored directories, allowlists, detector
   thresholds, and network rules.
4. Add JUnit tests for every detector, quarantine naming, and alert persistence.

## Second milestone: process attribution

Collect the process ID, executable path, command line, parent process, and
connection ownership for suspicious activity. On Windows, use a reviewed
telemetry source such as Sysmon event logs, ETW, or WMI. Associate the evidence
with the alert before considering containment actions.

## Third milestone: safe response

Build an incident timeline and a restore workflow. Any process termination or
network isolation must require a configurable confidence threshold, retain
evidence, and provide an operator override.

## Definition of done for the next stage

- A reproducible Maven build and test command.
- No local secrets, build output, or workspace cache tracked by Git.
- Deterministic tests for benign and suspicious sample files.
- One alert record contains detector reason, severity, affected path, file hash,
  process evidence when available, and response outcome.
