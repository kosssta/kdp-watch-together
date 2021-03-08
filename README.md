# kdp-watch-together

A distributed application for watching videos together over the network. <br/><br/>
Clients need to create accounts first, then to upload videos they want to watch, and finally to create a room and invite other clients into it. Java SWING is used for GUI, JavaFX for MediaPlayer and Java Sockets for connecting client programs and servers.
The application consists of a central server, subservers and clients. <br/><br/>
The central server is used for communication between subservers and for failure detection of some of them, as well. <br/>
Subservers are used to store clients' accounts, rooms and videos. Upon registration, clients get a subserver that is "responsible for them". If a subserver crashes, all of its clients will be distributed to the other subservers. <br/>
The client part of the application has GUI with the options of registration, login, video upload, create a room and watch the video (on their own or in the room). <br/>
