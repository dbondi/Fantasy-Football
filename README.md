# Fantasy-Football
This is a project which uses gamma distribution to model the expected draft order of the players in the for drafts in 2017 NFL season. The gamma distrubtuions are created from the mean (i.e. average draft pick) and standard deviation of when a player is picked, in order to compute the likelihood a player is picked after x amount of picks(i.e. CDF), this data can be found at https://fantasyfootballcalculator.com/adp. 

Information about gamma distributon:
      _alpha/shape = mean/standard deviation
      _beta/scale = standard deviation

Based on the alpha and beta values this gamma distribtuin is actaully a Erlang distribution (see wikipedia.org/wiki/Erlang_distribution). An Erlang distribution is used to compute waiting times between k occurrences of an event this is very similar to how a player is picked in a draft, so this seemed like the best distribution to use to model the data.

From this the program calcuates the expected number of postion that will be drafted in the next x picks, where x is the number of picks the current draft picker has till their next pick. After computing this data the program determines which position is best to pick by looking at the difference in project points from the top player in that positon vs the expected top  player after x picks (project points can be found at http://games.espn.com/ffl/tools/projections?). After determining the best postion to pick from the program choose the highest project player in that position.

To run the project run Fantasy.java in the src file. You'll be propted to input information about you league. Once you do this it will
give the best option for which player to choose (note you dont have to put what order you are in, in the draft it will automatically output
best option during every turn). Keep track of the players choosen by typing their names in the command prompt (upper case, period, and 
apostrophes dont matter), once its your turn just pick the player most recently outputed. Also defensive names are as follows 
(city) (defense), however if a city has multiple teams the format is (city abbreviation i.e. NY or LA) (team name) (defense)
