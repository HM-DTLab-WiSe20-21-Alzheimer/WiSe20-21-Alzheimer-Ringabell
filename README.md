# sweng1-20-team3-RingABell
***

## Inhaltsverzeichnis
1. [Allgemeine Information](#allgemeine-information)
2. [Technologie](#technologie)
3. [Installation](#installation)
4. [weitere Dokumente](#weitere-dokumente)
5. [wichtige Hinweise](#hinweise)

## Allgemeine Information
<a name="allgemeine-information"></a>
***
Bei RingABell handelt es sich um ein Alexa-Skill, welches im Fach Softwareentwicklung WS20/21 erarbeitet wurde. Dabei soll der Benutzer an verschiedene Erinnerungen erinnert werden, welche er zuvor RingABell mitgeteilt hat. Diese werden von RingABell wieder ausgegeben, sobald der Benutzer das Haus verlässt. Somit unterstützt RingABell das Gedächtnis des Anwenders.

## Technologie
<a name="technologie"></a>
***
Liste der verwendeten Technologien im Projekt:
* [Amazon Developer Service](https://developer.amazon.com/)
* [Amazon Web Services AWS](https://aws.amazon.com/de/)
* [Java](https://www.oracle.com/de/java/technologies/)

## Installation
<a name="installation"></a>
***
Um RingABell zu nutzen wird ein Developer Account von Alexa benötigt, sowie ein AWS Account. Über die [ASK CLI](https://developer.amazon.com/en-US/docs/alexa/smapi/quick-start-alexa-skills-kit-command-line-interface.html) kann mit dem Skill-Manifest (skill.json) der Skill erzeugt werden. Über die ASK CLi oder direkt im Developer (Intents -> JSON Editor) kann das Interaktions-Modell angepasst werden. Das Skill-Manifest sowie die Interaktions-Modell-JSON sind unter dem Ordner [skil-config](https://gitlab.lrz.de/robinjaegers/sweng1-20-team3-ringabell/-/tree/master/skill-config) zu finden. Als nächstes kopiert der Nutzer die Skill-ID im Endpoint Bereich und speichert den Skill. 
Nun muss der Nutzer auf AWS in Services auf Lambda klicken. Wichtig dabei, die Region in welche sie erstellt wird (Frankfurt). Hier muss der Nutzer wieder die Option „von Anfang“ auswählen und einen Funktionsnamen erstellen, sowie bei „Runtime“ Java 11 auswählen. „Auslöser hinzufügen“ klicken und „Alexa Skills Kit“ ausgeählt werden. Die Skill-ID einfügen um das „Alexa Skills Kit“ hinzuzufügen. Weiter muss die ARN kopiert und auf der Developer Konsole, unter Endpoint „Default Region“ einfügen. Als letzter Schritt wird der Quellcode benötigt, die als ZIP-Datei per Gradle erstellt wird.<br>

Für die Wetterfunktion von RingABell braucht der Benutzer einen Account auf der Website 
[Open Weather](https://openweathermap.org). Oder sonst kann der Nutzer eine eigene Wetter-API verwenden.

## weitere Dokumente
<a name="weitere-dokumente"></a>
***
Link zum Dokumenten Ordner:<br>

[Dokumente](https://gitlab.lrz.de/robinjaegers/sweng1-20-team3-ringabell/-/tree/master/documents)
## wichtige Hinweise
<a name="hinweise"></a>
***
Um die Wetterfunktion von RingABell zu nutzen, muss der Anwender in den Einstellungen seinen Standort freigeben. <br>
Für den Einsatz einer graphischen Darstellung muss das Gerät ein Display besitzen, sowie die APL-Erlaubnis in den Einstellungen gesetzt sein.<br>
Bei RingABell handelt es sich um einen Prototyp.<br>

