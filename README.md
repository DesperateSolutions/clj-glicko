# Chess-Rating

The goal of project is to make a suitable clojure application that can keep track of rating between people with glicko.

## Requirements

The project requires leinigen and mongo to run. To build the frontend you'll need node and npm as well.

## Installing frontend dependencies

Install frontend dependencies with:
```
npm install
```
## Building frontend
Build the frontend(combine all the js files into one javascript bundle):
```
gulp build
```

## Running the program

To run the program you'll run it through Leinigen. To start it type 

```
lein ring server
```

Will start the server

## TODO

Improve to have different games in regards to time limits.
Make a single runable script so you can do both frot and backend 
