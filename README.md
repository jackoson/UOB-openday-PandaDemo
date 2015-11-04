## Prerequisites ##

You must have [ant](http://ant.apache.org/) installed to run.

## Running ##

To run, use `ant client` from the root directory.

## What to expect ##

If you run the program using `ant client` it will open in a presentation mode where it will endlessly play games with Mr X as the only AI. As it does so, it displays the game board on the left and a representation of the AI on the right, along with some information about what is happening. The sequence it follows is:

* Begin Mr X's move.
    * Build the game tree for Mr X.
    * Wait for the tooltips to finish cycling through once.
    * Play Mr X's move.
* Begin Blue players move.
    * Wait for 20s or for someone to make a move.
    * If no move has been played, play an AI move.