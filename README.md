# Chess-Rating

The goal of project is to make a suitable clojure application that can keep track of rating between people with glicko.

A front end for it can be found at [rating-frontend](https://github.com/Quist/rating-frontend)

The project s documented through swagger which can be found at the base url of the api. (If you run it locally for example it will be at http://localhost:3000/)

## Requirements

The project requires leinigen and mongo to run.


## Running the program

To run the program you'll run it through Leinigen. To start it type 

```
lein ring server-headless
```

Will start the server
