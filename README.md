# Java Airport Simulator (3-phase simulation)

This is a Maven-based Java simulation for airport systems, designed to model and analyze airport operations in a controlled, virtual environment using a 3-phase model. It uses a <a href="https://www.dcs.ed.ac.uk/home/simjava/distributions/doc/eduni/distributions/package-summary.html">distribution package from The University of Edinburgh</a> to model events like passenger arrivals, check-ins, security checks, border control and boarding. The project provides a complete framework for setting up, managing, and analyzing the flow of passengers and queues within an airport environment. In addition to improving passenger flow, the simulator also provides useful data on how busy each part of the airport is. By showing which service point are the most crowded, it helps airport management make better decisions on how to allocate resources and improve overall efficiency.

<p align="center">
  <img src="https://github.com/user-attachments/assets/c551f8e5-f465-433b-b37a-314e82e47480" width="700">
</p>

The Airport Simulator is intended to help airport operators simulate the time passengers take during the boarding process and optimize resource allocation to improve efficiency.

:clipboard: JavaDoc documentation: https://viktoriia-code.github.io/Java-Airport-Simulator/src/main/docs/index.html

📅 November - December, 2024

## Technical stack
- **Java**: The main programming language used for building the application.
- **Maven**: A tool that helps manage the project's dependencies and automates the build process.
- **JavaFX**: A framework used to create the user interface (UI) for the application.
- **JavaDoc**: A tool used to generate API documentation in HTML format directly from the Java source code.
- **MariaDB**: A relational database management system used to store and manage the application's data persistently.
- **JUnit5**: A modern testing framework used to write and execute unit tests, ensuring code quality and reliability.
<p align="center">
  <img src="https://github.com/user-attachments/assets/ee2e5b35-7ee6-4689-9abc-540f4443a7ab" width="700">
</p>

## Installation

1. Clone the project repository from GitHub and navigate to the project folder:
```
git clone https://github.com/Viktoriia-code/Java-Airport-Simulator.git
cd ./Java-Airport-Simulator
```

2. Use Maven to build the project:
```
mvn clean install
```

3. Set up MariaDB:
Install MariaDB on your system. If you don't have MariaDB installed, you can follow the installation instructions for your operating system from the official MariaDB website: https://mariadb.org/download/.
Once MariaDB is installed, you will need to set up the database for the simulator application. Execute the `create_simulation_db.sql` script from the `resources` folder.

## Execution
In your IDE (e.g., IntelliJ IDEA or Eclipse), right-click the `Launcher` class and choose Run.

## Simulation Metrics and Observed Data
<p align="center">
  <img src="https://github.com/user-attachments/assets/f2bf7b85-d4a8-4bc0-881b-1b61a5843c83" width="700">
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/c87c71fa-7088-457f-b8ba-7e090b3c4d0f" width="700">
</p>

## Configurable Simulation Settings
<p align="center">
  <img src="https://github.com/user-attachments/assets/c63acc4b-7c76-48b0-8f58-14660bf7dba5" width="700">
</p>

## Authors:
- [Viktoriia-code](https://github.com/Viktoriia-code)
- [Santtu](https://github.com/santten)
- [Jiayue Zheng](https://github.com/JY1Z)
- [farahelbaj](https://github.com/farahelbaj)
