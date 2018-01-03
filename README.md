This is a project which uses gamma distributions to model the expected draft order in a Fantasy Football draft for the 2017 NFL season (however by changing the data files this can be used for any normal style draft). The gamma distributions are created from the mean (i.e. average draft pick) and standard deviation of when a player is picked, in order to compute the likelihood a player is picked after x amount of rounds(i.e. CDF), this data can be found at https://fantasyfootballcalculator.com/adp.

Information about gamma distribution: (alpha or shape = mean/standard deviation) (beta or scale = standard deviation)

Based on the alpha and beta values this gamma distribution is actually a Erlang distribution (see wikipedia.org/wiki/Erlang_distribution). An Erlang distribution is used to compute waiting times between k occurrences of an event this is very similar to how a player is picked in a draft, so this seemed like the best distribution to use to model the data.

From this the program calculates the expected number of positions that will be drafted in the next t picks, where t is the number of picks the current player has till their next pick. After computing this data the program determines which position is best to pick by looking at the difference in projected points, from the top player in that position vs the expected top player after t picks (projected points can be found at http://games.espn.com/ffl/tools/projections?). After determining the best position to pick from the program chooses the highest projected player in that position.

To run the project from an IDE download project in an IDE and run Fantasy.java in the src file. 

To run the project with jar file download FantasyFootball.jar and at command prompt use "java -cp FantasyFootball.jar Fantasy" 

After running the project, you'll be prompted to input information about you league. Once you do this it will give the best option for which player to choose (note you don't have to put what order you are in, in the draft it will automatically output the best option during every turn). Keep track of the players chosen by typing their names in the command prompt (upper case, period, and apostrophes don't matter), once its your turn just pick the player most recently outputted. Also defensive names are as follows "(city) defense", however if a city has multiple teams the format is "(city abbreviation i.e. NY or LA) (team name) defense"
