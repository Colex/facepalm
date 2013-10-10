Facepalm
========

Project developed for the Databases and Distributed Systems courses.

Facepalm is divided in 2 parts and can be used in two different ways:

	Client --> Web server --> Facepalm Server

or

	Client --> Facepalm Server

The web page was written in **JSP** (using **Web sockets** for real-time updates and chat), while the **server** was fully written in **Java**. They connect and communicate using the **RMI** middleware framework. It is also possible for the client to directly connect to the Facepalm Server using the **RMI** client.

The **database** was built in **PostgreSQL**. The database **PL/SQL** for creating tables, triggers, and functions is available in **data.sql**.

