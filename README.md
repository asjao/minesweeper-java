# Minesweeper — Java Application

A Java implementation of the classic Minesweeper game with clearly separated game logic and two user interfaces: a console interface and a graphical interface built with Java Swing.

This project was developed as an individual university project for the **Software Development (Java)** course at the **University of Sarajevo – Faculty of Science**.

## Features

- Randomly generated Minesweeper boards
- Safe first click
- Flagging and unflagging cells
- Flood-fill opening of connected empty regions
- Chord functionality for opening neighboring cells
- Win and loss detection
- Remaining mine counter
- Restart and new game functionality
- Input validation
- Multiple difficulty levels:
  - Beginner: 9×9, 10 mines
  - Intermediate: 16×16, 40 mines
  - Expert: 16×30, 99 mines
- Custom board dimensions and mine count
- Game timer in the graphical version
- Console and graphical user interfaces using the same underlying game logic

## Technologies

- **Java**
- **Java Swing**
- **Object-Oriented Programming**
- **BFS / Flood Fill**
- **Java Collections**
- **Eclipse**

## Architecture

The application separates the game logic from the user interface.

The project is organized into three main packages:

- `logika/` – contains the complete game logic and rules
- `konzola/` – console-based user interface
- `gui/` – graphical user interface implemented with Java Swing

The UI layers do not implement game rules directly. Instead, both interfaces communicate with the same game model.

### Main Components

- **Board** – stores mine positions and neighboring mine counts
- **MinesweeperGame** – manages the game state and implements the game rules
- **CellState** – represents whether a cell is closed, open, or flagged
- **GameState** – represents whether the game is running, won, or lost
- **ConsoleApp** – console interface
- **GuiApp** – Swing graphical interface

## Class Diagram

The class diagram for the project is available here:

[View Class Diagram](class-diagram.pdf)

## Running the Project

### Requirements

- Java 8 or newer
- No external libraries required

### Graphical Version

Run:
gui.GuiApp

The application will open in a Java Swing window.
Console Version
Run:
konzola.ConsoleApp

Then follow the commands displayed in the terminal.
Controls
GUI
- Left click – open a cell
- Right click – place or remove a flag
- Shift + Left click – chord action
- Restart – start a new game using the current settings
- Select parameters – choose a predefined or custom difficulty
Console
- o r c – open a cell
- f r c – place/remove a flag
- c r c – chord action
- r – restart / start a new game
- q – quit

## About
Course: Software Development (Java)

Project type: Individual university project

University: University of Sarajevo – Faculty of Science

Year: 2026
