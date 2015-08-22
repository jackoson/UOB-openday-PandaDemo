##Running##
To run, just use `ant client` from the root directory.

##Usage##
If you run the program using `ant client` it will open in a presentation mode where it will endlessly plays games with Mr X as the only AI. As it does so it displays the game board on the left and a representation of the AI on the right, along with some information about what is happening. The sequence it follows is:

* Begin Mr X's move.
    * Build the game tree for Mr X.
  * Wait for the tooltips to finish cycling through once.
  * Play Mr X's move.
* Begin Blue players move.
  * Wait for 20s or for someone to make a move.
  * If no move has been played, play an AI move.

## TODO: ##

+ AI GUI
+ Cheat codes
+ Meteor strike

![Concept.jpg](https://bitbucket.org/repo/RygA6p/images/12134649-Concept.jpg)
![AIGUI.jpg](https://bitbucket.org/ashestoashes/panda-cwk6/downloads/IMG_20150414_224816.jpg "AI GUI")