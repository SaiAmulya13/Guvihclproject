Hardware Inventory Management
============================================
A java-based application for managing Hardware Inventory using Mongodb.

Prerequisites:
-java JDK 8 or Higher
-MongoDB installed and running on localhost:2701

Setup Instructions:
1.Ensure MongoDB is running
2.Compile the project using provided commands
3.Run Main.java to start the application

Compilation:
javac -cp "lib/*" -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })

Execution:
java -cp "out;lib/*" com.inventory.Main

Features:
-Add new hardware assets with details like name, category, model, quantity, and status
-View complete hardware inventory with real-time data from MongoDB
-Update hardware information such as quantity, condition, or assignment
-Delete obsolete or damaged hardware records
-Search and filter hardware by category, model, or availability
-Track hardware status (available, in-use, under maintenance)
-MongoDB NoSQL database for fast, scalable, and flexible data storage
-Prevent data duplication using unique hardware identifiers
-User-friendly and structured inventory management workflow
