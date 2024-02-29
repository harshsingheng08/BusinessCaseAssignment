# Project Name

Brief description of the project.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Dependencies](#dependencies)

## Introduction

This project is basic business case assignment for code review. This is implemented using firebase as backend and kotlin in android development

## Features

- Feature 1: Login 
             - Login is implemented using firebase authentication and Realtime database of firebase
- Feature 2: Register
           - Registration implemented using firebase authentication and Realtime database.
- Feature 3: Home
          - After login, it’s redirected to the home screen and will ask for required location permissions, allowing it so the map screen will give the current location.
         -  Home screen contains horizontal slider as below screenshots and all data are fetching from firebase realtime database.
            1. Profile
            2. Map
            3. Data

## Installation

Provide step-by-step instructions on how to install or set up the project. Include any prerequisites, such as software dependencies or system requirements.

Clone the repository: git clone https://github.com/harshsingheng08/BusinessCaseAssignment.git
Navigate to the project directory: cd project
Install dependencies: ./gradlew clean assembleDebug

## Usage
Sample Usage:
1. Open the project in Android Studio.
2. Build and run the project on an Android device or emulator.

## Dependencies
- Firebase: Firebase used for Authentication and Realtime database
- Glide: Image loading and caching library for Android
- Gson: JSON parsing library for Java and Android
- Map and Location : For loading Map and get current location
