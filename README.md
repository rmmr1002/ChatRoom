# ChatRoom

This is a Chat Server written in Java. It alows users to communicate with one another by connecting to the server and interacting with it in accordance to an application protocol. <br> Through this protocol, the server allows clients to engage in group chats in chat rooms and send private messages to one another. <br>
The Server uses Socket programming to handle client communication and multi threading to handle concurrent client connections. 

## Prerequisites
<ol>
<li> Unix based system </li>
<li > Java installed </li> 
</ol>

## How to run 
<ol> 
<li> Clone the repo </li>
<li> Run <b> make </b> in the given directory </li>
<li> This will compile the given Server.java file into an executable </li>
<li> To run the Server, type <b> ./server -p PortNumber </b> to bind the server executable to a port </li>
<li> For users, use the given executable client file by running <b> ./client </b>. You can then connect the client to a server and run commands </li>
</ol>

## Features supported by client
While running, the client takes commands directly from the user. All commands are preceded by a backslash. Not every command is available in every context. The client supports the following commands:
<ol>
<li> <b> \connect IP Address:Port </b> = Instruct the client to connect to a new chat server,specified by the IP address and port. </li>
<li> <b> \disconnect </b> = If connected to a server, disconnect from that server.</li>
<li> <b> \join Room Password </b> =  Join the specified  chatroom, creating it if it does not already exist. The Password is optional, with the default being the empty string. Users may only join rooms for which they know the password.  Both Room and Password must be less than 256 characters in length. </li>
<li> <b> \leave </b> = If in a room, this exits the room. Otherwise, it leaves the server. </li>
<li> <b> \list users </b> = List all users. If in a room, it lists all users in that room. Otherwise, it lists all users connected to the server. </li>
<li> <b> \list rooms </b> = List all rooms that currently exist on the server. </b>
<li> <b> \msg User Message </b> = Send a private message to the specified user. User must beless than 256 characters in length and the Message must be less than 65536 characters in length.</li>
<li> <b> \nick Name </b> = Set your nickname to the specified name. Name must be less than 256 characters in length. </li>
<li> <b> \quit </b> = Disconnect the session and exit the program. </li>
</ol>
