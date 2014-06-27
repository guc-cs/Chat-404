Chat-404
========

In the 5th Semester - Winter Fall 2012, Team: "The 404" won the best project award for developing the most impressive java-based chat application. It supports TCP, UDP, Multicast, Group/Private Multi-tab Chat and features very basic encryption. This project statement was developed by Amr Tj. Wallas who was the TA for the course beside Assoc. Prof. Dr. Amal El-Nahas

Introduction
------------
This project is a simple client-server many-to-many chat application. The purpose of this project is to demonstrate how socket programming is used to create network applications and relay messages back and forth through the network on top of various transport layer protocols in java. The complexity arises not only from designing a custom chat protocol but also from correctly parsing the chat protocol messages.

Features
--------
* Chat over TCP as well as UDP
* Sending and receiving files over TCP and UDP
* Public and Private Chat rooms
* Chat over Multicast groups
* Listing of connected users and Public chat rooms
* Tab-based UI for simlataneous chat in multiple rooms
* Mute and Kick Users inside chat rooms
* Simple AES/CBC/PKCS5 Encryption
* Simple Compression

How To
------
1. Compile and run `engine.server.ServerManager.java`
2. Compile then run as many instances of `ui.GUIManager.java` as you like
3. Start chatting from one client instance to the other using the UI


<a href="https://i.imgur.com/jKiu7DC.png">
  <img src="https://i.imgur.com/jKiu7DC.png" />
</a>


Bugs
----
There are a few bugs in cases when a client disconnects. This raises a server side exception which sometimes leads to the server going down.

Contributors
------------
* Nourhan Zakaria
* Nourhan Mohamed
* Samy Saad
