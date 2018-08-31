# jkomickeeper

A comic book manager for Linux, written in Java and using JavaFX for the GUI

## Building
To build run `mvn clean compile assembly:single`

## Usage

### Making a new database
To make a new database start the program, click the settings tab, type the desired name of the
database in the field labeled "DB Name" and then click make DB

### Indexing comics
To index comics click the settings tab, click "Select comic dir", select the dir which
contains the comics you want to index and finally click the index button. The indexing may
take a few seconds.

### Changing the current database
To change the current database click the settings tab, click "Select database" and then selected
the desired database.

### Searching tags
To search for a tag enter "tag:TAG_VALUE" into the search bar and press enter

### Screenshot
![screenshot](media/screenshot.png)