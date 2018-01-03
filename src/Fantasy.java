

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import org.apache.commons.math3.distribution.GammaDistribution;


public class Fantasy {
	static ArrayList<ArrayList<String>> chance;
	static int players;

	/*createList(String list)
	 * 
	 * string list is file name
	 * 
	 * converts file to 2d string array where file has different values separated by tabs and lines
	 * every array in 2d array is each line in file
	 * values in each array based on separation of tabs in each line
	 * (see any of the QB/RB/...Projected files for sample format)
	 * 
	 * return 2d string array created
	 */
	public static ArrayList<ArrayList<String>> createList(String list) throws FileNotFoundException{
	    InputStream file = Fantasy.class.getResourceAsStream(list);
	   
		Scanner scan = new Scanner(file);
	    
	    ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>();
	    while(scan.hasNextLine()) {
	        String line = scan.nextLine().trim();
	        String[] splitted = line.split("	");
	        ArrayList<String> arraySplitted = new ArrayList<String>(Arrays.asList(splitted));
	        lines.add(arraySplitted);
	    }
	    
	   scan.close();
		return lines;
	}
	
	/*creatListData(String list)
	 * 
	 * string list is file name - file has the mean and standard deviation of each player in draft and their name see file used for 
	 * format (2017FantasyStats)
	 * 
	 * from mean and standard deviation use gamma distribution to compute likelihood player is picked after x amount of 
	 * picks(i.e. CDF), where each line of the 2d array is the chance on the x'th pick
	 * gamma distribution values 
	 * 	alpha/shape = mean/standard deviation
	 * 	beta/scale = standard deviation
	 * 
	 * 	pretty much a Erlang distribution
	 * 		an Erlang distribution is used to compute waiting times between k occurrences of an event
	 * 		this is very similar to how a player is picked in a draft, so this seemed like the best distribution to use
	 * 		to model the data.
	 * 
	 * 	all values of mean and standard deviation are from data collected by footballcalculator.com
	 * 	
	 *	once 2d array holding values of gamma distributions of each player is made adjust values so that the sum of each 
	 *	line equals the number of players picked, which will be the x'th round, so i.e. the x'th line. This adjustment is needed
	 *	because the gamma distributions are only models and therefore some error can be seen when the sum of the values of 
	 *	a row is greater than or less than the x'th round.
	 *
	 *	(a sample data table is provided in folder called (FantasyFootballStatsSample.txt))
	 *	
	 *	values are adjusted by raising every number in a row to a power n until the sum of the values equals the row
	 *	this method of adjustment was chosen because it keeps the values between 0 and 1, additionally its a fairly straight 
	 *	forward method of adjustments.
	 *
	 *	(see method "arrayAdjust" for how values are adjusted in O(log2(n)) time)
	 * 
	 * return 2d string array created
	 */
	public static ArrayList<ArrayList<String>> createListData(String fileName) throws FileNotFoundException{
	    InputStream file = Fantasy.class.getResourceAsStream(fileName);

		Scanner scan = new Scanner(file);
	    
	    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
	    
	    //Add Player Names
	    String line = scan.nextLine().trim();
        String[] splitted = line.split("	");
        ArrayList<String> arraySplitted = new ArrayList<String>(Arrays.asList(splitted));
        list.add(arraySplitted);
        
        //Add positions of players
        line = scan.nextLine().trim();
        splitted = line.split("	");
        arraySplitted = new ArrayList<String>(Arrays.asList(splitted));
        list.add(arraySplitted);
	    
        //Put values of mean in array
        line = scan.nextLine().trim();
        splitted = line.split("	");
        ArrayList<Double> meanValues = new ArrayList<Double>();
        arraySplitted = new ArrayList<String>(Arrays.asList(splitted));
        
        for (Object str : arraySplitted) {
        	   meanValues.add(Double.parseDouble((String)str));
        }
        
        //Put values of SD in array
        line = scan.nextLine().trim();
        splitted = line.split("	");
        ArrayList<Double> SDValues = new ArrayList<Double>();
        arraySplitted = new ArrayList<String>(Arrays.asList(splitted));
        
        for (Object str : arraySplitted) {
        	SDValues.add(Double.parseDouble((String)str));
        }
        
        //computed values of gamma distributions for each player
        double shape;
        double scale;
        double chanceOfPick;
        GammaDistribution distribution;
	    for(int i = 0; i < list.get(0).size(); i++){
	    	
	    	shape = meanValues.get(i)/SDValues.get(i);
    		scale = SDValues.get(i);
	    	distribution = new GammaDistribution(shape,scale);
	 	       
	    	for(int j = 0; j < list.get(0).size(); j++){
	    		chanceOfPick = distribution.cumulativeProbability(j+1);
	    		
	    		if(i == 0){
	    			list.add(new ArrayList<String>());
	    		}
	    		
	    			list.get(j + 2).add(Double.toString(chanceOfPick));
	    	
		    }
	    }
	    
	   scan.close();
		return list;
	}
	
	/*arrayAdjust(ArrayList<ArrayList<String>> list)
	 * 
	 * list is 2d array to adjust values of 
	 * 
	 * basically uses binary search to find value of exponent, if sum within .0001 of correct value stop search
	 * 
	 * return 2d string array created
	 */
	public static ArrayList<ArrayList<String>> arrayAdjust(ArrayList<ArrayList<String>> list) throws FileNotFoundException{
			ArrayList<String> line;
			
			double sum = 0;
			double exp = 1;
			
			line = list.get(2); //line to be adjusted
			
			double expLow = 0; //lower boundary
			double expHigh = 3; //higher boundary
			double temp;
			
			for(int i = 0; i < list.get(0).size(); i++){
				exp=1;
				sum=0;
				expLow = 0;
				expHigh = 3;
				
			while(Math.abs((i+1)-sum) > 0.0001*sum){
				
			line = list.get(i+2); //get i'th line, top two rows of list have values of player and positions
			sum = sumWithExponant(line, exp); //calculate sum of values raised to exp
			
			//sets values of new exp and adjusts range
			if(sum > (i+1)){
				temp = exp;
				exp = (expHigh+expLow)/2;
				expLow = temp;
				
			}
			if(sum < (i+1)){
				temp = exp;
				exp = (expHigh+expLow)/2;
				expHigh = temp;
				
			}
	    	
			}

			//adjust values of line based on found value of exp
			for(int j = 0; j < list.get(0).size(); j++){
				list.get(i+2).set(j,Double.toString(Math.pow(Double.parseDouble(list.get(i+2).get(j)),exp)));
			}
			
	    }
			
			return list;
	
	}
	
	/*arrayAdjust(ArrayList<ArrayList<String>> list)
	 * 
	 * list is 1d array
	 * 
	 * raises values in array to exp and sums them
	 * 
	 * return sum calculated
	 */
	public static double sumWithExponant(ArrayList<String> line, double exponent) throws FileNotFoundException{
		double sum = 0;
		for(int i = 0; i < line.size(); i++){
			sum = sum + Math.pow((Double.parseDouble(line.get(i))),exponent);
			
		}

		return sum;
	}
	
	/*deleteColumn(ArrayList<ArrayList<String>> list, String footBallPlayer)
	 * 
	 * list is 2d array with gamma distributions, string is name of football player picked
	 * 
	 * deletes column with player it 
	 * 
	 * return list
	 */
	public static ArrayList<ArrayList<String>> deleteColumn(ArrayList<ArrayList<String>> list, String footBallPlayer){
		int target = -1;

		for(int i=0; i<list.get(0).size(); i++){
			//find player in list and converts name to lower-case and removes . and '
			if(list.get(0).get(i).toLowerCase().replaceAll("[\\.']", "").equals(footBallPlayer.toLowerCase().replaceAll("[\\.']", ""))){
				target=i;
			}
		}
		if(target==-1){
		
		}
		else{
			
			for(int j=0; j<list.size(); j++){
				
					list.get(j).remove(target);
				
			}
		}
		return list;
	}
	
	/*deleteColumnTrue(ArrayList<ArrayList<String>> list, String footBallPlayer)
	 * 
	 * used to see if deleteColumn should be called, i.e. is player in list
	 *
	 */
	public static Boolean deleteColumnTrue(ArrayList<ArrayList<String>> list, String footBallPlayer){
		int target = -1;
		for(int i=0; i<list.get(0).size(); i++){
			//test to see if player is in list and converts name to lower-case and removes . and '
			if(list.get(0).get(i).toLowerCase().replaceAll("[\\.']", "").equals(footBallPlayer.toLowerCase().replaceAll("[\\.']", ""))){
				target=i;
			}
		}
		if(target==-1){
			return false;
		}
		else{
			
			return true;
		}
		
	}
	
	/*deleteRow(ArrayList<ArrayList<String>> list, String footBallPlayer)
	 * 
	 * list is 2d array with projected values of players, string is name of football player picked
	 * 
	 * deletes row with player it
	 * 
	 * return list
	 */
	public static ArrayList<ArrayList<String>> deleteRow(ArrayList<ArrayList<String>> list, String footBallPlayer){
		int target = -1;
		for(int i=0; i<list.size(); i++){
			//find player in list and converts name to lowercase and removes . and '
			if(list.get(i).get(0).toLowerCase().replaceAll("[\\.']", "").equals(footBallPlayer.toLowerCase().replaceAll("[\\.']", ""))){
				target=i;
			}
		}
		if(target==-1){
			
		}
		else{
	
					list.remove(target);
				
		}
		return list;
	}
	
	/*deleteRowTrue(ArrayList<ArrayList<String>> list, String footBallPlayer)
	 * 
	 * used to see if deleteRow should be called, i.e. is player in list
	 *
	 */
	public static Boolean deleteRowTrue(ArrayList<ArrayList<String>> list, String footBallPlayer){
		int target = -1;
		for(int i=0; i<list.size(); i++){
			//test to see if player is in list and converts name to lower-case and removes . and '
			if(list.get(i).get(0).toLowerCase().replaceAll("[\\.']", "").equals(footBallPlayer.toLowerCase().replaceAll("[\\.']", ""))){
				target=i;
			}
		}
		if(target==-1){
			return false;
		}
		else{
		
			return true;
		}
		
	}
	
	/*MVP(int count, int picksToGo, ArrayList<ArrayList<String>> list)
	 * 
	 * count is number of players picked, used to determine which row in list to access
	 * players is how many picks the current player picking will have to wait until they
	 * cant pick again
	 * list is the 2d array with gamma distributions
	 * 
	 * each value in double array being computed is the expected number of players picked 
	 * by position in next (picksToGo) picks
	 * 
	 * this expected value is found by finding the expected probably that each player will be picked
	 * in the next (picksToGo) picks by finding the change in the cdf of the gamma distributions
	 * for each player. These values are then summed for each position.
	 * 
	 * return double array computed
	 */
	public static double[] MVP(int count, int picksToGo, ArrayList<ArrayList<String>> list){
		
		double RB=0;
		double WR=0;
		double QB=0;
		double TE=0;
		double PK=0;
		double DEF=0;
		
		
		
		for(int i=0; i<list.get(0).size(); i++){
			if(list.get(1).get(i).equals("RB")){
				RB = RB + Double.parseDouble(list.get(count+picksToGo+1).get(i))-Double.parseDouble(list.get(count+1).get(i));
			}
			if(list.get(1).get(i).equals("WR")){
				WR = WR + Double.parseDouble(list.get(count+picksToGo+1).get(i))-Double.parseDouble(list.get(count+1).get(i));
			}
			if(list.get(1).get(i).equals("QB")){
				QB = QB + Double.parseDouble(list.get(count+picksToGo+1).get(i))-Double.parseDouble(list.get(count+1).get(i));
			}
			if(list.get(1).get(i).equals("TE")){
				TE = TE + Double.parseDouble(list.get(count+picksToGo+1).get(i))-Double.parseDouble(list.get(count+1).get(i));
			}
			if(list.get(1).get(i).equals("PK")){
				PK = PK + Double.parseDouble(list.get(count+picksToGo+1).get(i))-Double.parseDouble(list.get(count+1).get(i));
			}
			if(list.get(1).get(i).equals("DEF")){
				DEF = DEF + Double.parseDouble(list.get(count+picksToGo+1).get(i))-Double.parseDouble(list.get(count+1).get(i));
			}
		}
	
		double[] mvpList = {RB,WR,QB,TE,PK,DEF};
		
		return mvpList;
	}
	
	/*adjustMVP(double[] mvp, ArrayList<ArrayList<Integer>> playerPicks)
	 * 	 
	 * mvp is the previous array calculated above
	 * playerPicks is a 2d array holding the number of players by position each person in the draft has choosen
	 * 
	 * this method just re-adjusts the values in mvp based on previous player picks. For example if a person picks
	 * 2 QB's he's very unlikely to pick another, this method re-adjusts the values accordingly, as the gamma 
	 * distribution calculation method alone is not enough since these gamma distributions can't be re-adjusted for
	 * every possible situation as there isn't enough data. 
	 * 
	 * data used for this calculation is in the global 2d array chance which is calculated from the Chance file which
	 * holds values determining how likely it is another player in a certain position will be picked based on how many 
	 * players in that position someone already has. 
	 *
	 * return double array computed
	 */
	public static double[] adjustMVP(double[] mvp, ArrayList<ArrayList<Integer>> playerPicks){
		double count[] = new double[6];

		for(int i = 0; i< players ;i++){
			count[0] = count[0] + 1 - Double.parseDouble(chance.get(playerPicks.get(i).get(0)).get(0));
			count[1] = count[1] + 1 - Double.parseDouble(chance.get(playerPicks.get(i).get(1)).get(1));
			count[2] = count[2] + 1 - Double.parseDouble(chance.get(playerPicks.get(i).get(2)).get(2));
			count[3] = count[3] + 1 - Double.parseDouble(chance.get(playerPicks.get(i).get(3)).get(3));
			count[4] = count[4] + 1 - Double.parseDouble(chance.get(playerPicks.get(i).get(4)).get(4));
			count[5] = count[5] + 1 - Double.parseDouble(chance.get(playerPicks.get(i).get(5)).get(5));
		}
		double sum1 = mvp[0]+mvp[1]+mvp[2]+mvp[3]+mvp[4]+mvp[5];
		
		mvp[0]=mvp[0]*(players-count[0])/players;
		mvp[1]=mvp[1]*(players-count[1])/players;
		mvp[2]=mvp[2]*(players-count[2])/players;
		mvp[3]=mvp[3]*(players-count[3])/players;
		mvp[4]=mvp[4]*(players-count[4])/players;
		mvp[5]=mvp[5]*(players-count[5])/players;
		
		double sum2 = mvp[0]+mvp[1]+mvp[2]+mvp[3]+mvp[4]+mvp[5];
		
		mvp[0]=mvp[0]*sum1/sum2;
		mvp[1]=mvp[1]*sum1/sum2;
		mvp[2]=mvp[2]*sum1/sum2;
		mvp[3]=mvp[3]*sum1/sum2;
		mvp[4]=mvp[4]*sum1/sum2;
		mvp[5]=mvp[5]*sum1/sum2;
		
		return mvp;
		
	}
	
	/*WhichList(String player...)
	 * 	 
	 * determines which position a inputed player
	 * 
	 * returns a integer based on position found, 0 returned if player not found
	 */
	public static int WhichList(String player,ArrayList<ArrayList<String>> RB,
										   ArrayList<ArrayList<String>> WR,
										   ArrayList<ArrayList<String>> QB,
										   ArrayList<ArrayList<String>> TE,
										   ArrayList<ArrayList<String>> PK,
										   ArrayList<ArrayList<String>> DEF){
		
		if(deleteRowTrue(RB,player)==true){
			return 1;
		}
		if(deleteRowTrue(WR,player)==true){
			return 2;
		}
		if(deleteRowTrue(QB,player)==true){
			return 3;
		}
		if(deleteRowTrue(TE,player)==true){
			return 4;
		}
		if(deleteRowTrue(PK,player)==true){
			return 5;
		}
		if(deleteRowTrue(DEF,player)==true){
			return 6;
		}
      return 0;
				
	}
			
			
			
//0-RB
//1-WR
//2-QB
//3-TE
//4-PK
//5-DEF

			
public static void main(String[] args) throws FileNotFoundException {
	//calculate gamma distributions
	ArrayList<ArrayList<String>> listStats =createListData("2017FantasyStats");

	System.out.println("Loading...");
	System.out.println("********************************************************");
	System.out.println("	Data only for standard NPPR leagues...");
	System.out.println("********************************************************");
	System.out.println("");
	
	//adjust gamma distributions
	listStats = arrayAdjust(listStats);
	
	//gets values of projected points by position and player
	ArrayList<ArrayList<String>> listRB =createList("RBProjected");
	ArrayList<ArrayList<String>> listWR =createList("WRProjected");
	ArrayList<ArrayList<String>> listQB =createList("QBProjected");
	ArrayList<ArrayList<String>> listTE =createList("TEProjected");
	ArrayList<ArrayList<String>> listPK =createList("PKProjected");
	ArrayList<ArrayList<String>> listDEF =createList("DEFProjected");
	
	//sets chance based on values in chance file
	chance = createList("Chance");

	System.out.println("How many players are there?:");
	int count = 1;
	int where =0;
	int whereMax = 0;
	Scanner kc = new Scanner(System.in);
	players = kc.nextInt();
	
	//data is best used for 8-12 player leagues
	if(players < 8 || players > 12){
		System.out.println("********************************************************");
		System.out.println("	Data may not be completely accurate as there");
		System.out.println("	is only specific data for 8-12 player leagues...");
		System.out.println("********************************************************");
		System.out.println("");
	}
	
	//player picks keeps track of number of players picked by position for each person in draft
	ArrayList<ArrayList<Integer>> playerPicks = new ArrayList<ArrayList<Integer>>();
	for(int i = 0; i< players;i++){
		playerPicks.add(new ArrayList<Integer>());
		playerPicks.get(i).add(0);
		playerPicks.get(i).add(0);
		playerPicks.get(i).add(0);
		playerPicks.get(i).add(0);
		playerPicks.get(i).add(0);
		playerPicks.get(i).add(0);
	}
	
	//determine which type of draft style
	//this will effect method "mvp" calculation
	System.out.println("What type of draft method?: 1-snake, 2-linear");
	int draft = kc.nextInt();
	
	while(draft != 1 && draft != 2){
		System.out.println("Type in a option:");
		draft = kc.nextInt();	
	}
	
	
	int currentPlayer=1;
	boolean forward = true;
	int nextPick = 0;

	//linear draft
	while(draft == 2){
	    
		double max=0;
		whereMax=0;
		where=0;
		double[] mvpList = MVP(count,players,listStats);

		mvpList = adjustMVP(mvpList,playerPicks);
		
		//this determines the best player to pick based on the expected number of players by position that will be picked 
		//and based on the point differential between the current top player at a position vs the expected top player in that position
		//after all the other people in the draft have chosen
		//the max value by position is chosen then the top player in that position is chosen as the next player to pick
		if((Double.parseDouble(listRB.get(0).get(1))-Double.parseDouble(listRB.get((int) (mvpList[0] + .6)).get(1))) > max){
				whereMax=0;
				max=(Double.parseDouble(listRB.get(0).get(1))-Double.parseDouble(listRB.get((int) (mvpList[0] + .6)).get(1)));
			}
			if((Double.parseDouble(listWR.get(0).get(1))-Double.parseDouble(listWR.get((int) (mvpList[1] + .6)).get(1))) > max){
				whereMax=1;
				max=(Double.parseDouble(listWR.get(0).get(1))-Double.parseDouble(listWR.get((int) (mvpList[1] + .6)).get(1)));
			}
			if((Double.parseDouble(listQB.get(0).get(1))-Double.parseDouble(listQB.get((int) (mvpList[2] + .6)).get(1))) > max){
				whereMax=2;
				max=(Double.parseDouble(listQB.get(0).get(1))-Double.parseDouble(listQB.get((int) (mvpList[2] + .6)).get(1)));
			}
			if((Double.parseDouble(listTE.get(0).get(1))-Double.parseDouble(listTE.get((int) (mvpList[3] + .6)).get(1))) > max){
				whereMax=3;
				max=(Double.parseDouble(listTE.get(0).get(1))-Double.parseDouble(listTE.get((int) (mvpList[3] + .6)).get(1)));
			}
			if((Double.parseDouble(listPK.get(0).get(1))-Double.parseDouble(listPK.get((int) (mvpList[4] + .6)).get(1))) > max){
				whereMax=4;
				max=(Double.parseDouble(listPK.get(0).get(1))-Double.parseDouble(listPK.get((int) (mvpList[4] + .6)).get(1)));
			}
			if((Double.parseDouble(listDEF.get(0).get(1))-Double.parseDouble(listDEF.get((int) (mvpList[5] + .6)).get(1))) > max){
				whereMax=5;
				max=(Double.parseDouble(listDEF.get(0).get(1))-Double.parseDouble(listDEF.get((int) (mvpList[5] + .6)).get(1)));
			}
			
		System.out.println("");
		if(whereMax == 0){
			System.out.println("*********************************");
			System.out.println("Select: " + listRB.get(0).get(0) + " (RB)");
			System.out.println("*********************************");
		}
		if(whereMax == 1){
			System.out.println("*********************************");
			System.out.println("Select: " + listWR.get(0).get(0) + " (WR)");
			System.out.println("*********************************");
		}
		if(whereMax == 2){
			System.out.println("*********************************");
			System.out.println("Select: " + listQB.get(0).get(0) + " (QB)");
			System.out.println("*********************************");
		}
		if(whereMax == 3){
			System.out.println("*********************************");
			System.out.println("Select: " + listTE.get(0).get(0) + " (TE)");
			System.out.println("*********************************");
		}
		if(whereMax == 4){
			System.out.println("*********************************");
			System.out.println("Select: " + listPK.get(0).get(0) + " (PK)");
			System.out.println("*********************************");
		}
		if(whereMax == 5){
			System.out.println("*********************************");
			System.out.println("Select: " + listDEF.get(0).get(0) + " (DEF)");
			System.out.println("*********************************");
		}
		System.out.println("  (Insert Player Picked)...");

		
		//find the player that just picked from the used input
		Scanner sc = new Scanner(System.in);
		String i = sc.nextLine();
		where =WhichList(i,listRB,listWR,listQB,listTE,listPK,listDEF);
		
		//delete player from lists
		if(where == 1){
			listRB=deleteRow(listRB,i);
			playerPicks.get(currentPlayer-1).set(0,playerPicks.get(currentPlayer-1).get(0) + 1);
		}
		if(where == 2){
			listWR=deleteRow(listWR,i);
			playerPicks.get(currentPlayer-1).set(1,playerPicks.get(currentPlayer-1).get(1) + 1);
		}
		if(where == 3){
			listQB=deleteRow(listQB,i);
			playerPicks.get(currentPlayer-1).set(2,playerPicks.get(currentPlayer-1).get(2) + 1);
		}
		if(where == 4){
			listTE=deleteRow(listTE,i);
			playerPicks.get(currentPlayer-1).set(3,playerPicks.get(currentPlayer-1).get(3) + 1);
		}
		if(where == 5){
			listPK=deleteRow(listPK,i);
			playerPicks.get(currentPlayer-1).set(4,playerPicks.get(currentPlayer-1).get(4) + 1);
		}
		if(where == 6){
			listDEF=deleteRow(listDEF,i);
			playerPicks.get(currentPlayer-1).set(5,playerPicks.get(currentPlayer-1).get(5) + 1);
		}
		if(where == 0){
			System.out.println("Player Not Found");
		}
		if(where!=0){
			count++;
			listStats = deleteColumn(listStats,i);
			listStats = arrayAdjust(listStats);
			
		//stop after every person has max players
		if(16*players == count){
			draft = 0;
			sc.close();
		}
		
		//since draft is linear calculation of the next player is simple
		currentPlayer++;
		
		if(currentPlayer == players + 1){
			currentPlayer = 1;
		}
		}

	}
	
//snake draft
while(draft == 1){
	
	    
	    //determines next picked based on current player and direction of picking 
		//this is only because snakes draft go back and forth in a non-circular way
	    if(forward == true){
	    	nextPick = 2*(players-currentPlayer) + 1;
	    }
	    if(forward == false){
	    	nextPick = 2*currentPlayer - 1;
	    }
	    
		double max=0;
		where=0;
		whereMax=0;
		double[] mvpList = MVP(count,nextPick,listStats);
		mvpList = adjustMVP(mvpList,playerPicks);
		
		//this determines the best player to pick based on the expected number of players by position that will be picked 
		//and based on the point differential between the current top player at a position vs the expected top player in that position
		//after all the other people in the draft have chosen
		//the max value by position is chosen then the top player in that position is chosen as the next player to pick
			
		if((Double.parseDouble(listRB.get(0).get(1))-Double.parseDouble(listRB.get((int) (mvpList[0] + .6)).get(1))) > max){
			whereMax=0;
			max=(Double.parseDouble(listRB.get(0).get(1))-Double.parseDouble(listRB.get((int) (mvpList[0] + .6)).get(1)));
			
		}
		if((Double.parseDouble(listWR.get(0).get(1))-Double.parseDouble(listWR.get((int) (mvpList[1] + .6)).get(1))) > max){
			whereMax=1;
			max=(Double.parseDouble(listWR.get(0).get(1))-Double.parseDouble(listWR.get((int) (mvpList[1] + .6)).get(1)));
		}
		if((Double.parseDouble(listQB.get(0).get(1))-Double.parseDouble(listQB.get((int) (mvpList[2] + .6)).get(1))) > max){
			whereMax=2;
			max=(Double.parseDouble(listQB.get(0).get(1))-Double.parseDouble(listQB.get((int) (mvpList[2] + .6)).get(1)));
		}
		if((Double.parseDouble(listTE.get(0).get(1))-Double.parseDouble(listTE.get((int) (mvpList[3] + .6)).get(1))) > max){
			whereMax=3;
			max=(Double.parseDouble(listTE.get(0).get(1))-Double.parseDouble(listTE.get((int) (mvpList[3] + .6)).get(1)));
		}
		if((Double.parseDouble(listPK.get(0).get(1))-Double.parseDouble(listPK.get((int) (mvpList[4] + .6)).get(1))) > max){
			whereMax=4;
			max=(Double.parseDouble(listPK.get(0).get(1))-Double.parseDouble(listPK.get((int) (mvpList[4] + .6)).get(1)));
		}
		if((Double.parseDouble(listDEF.get(0).get(1))-Double.parseDouble(listDEF.get((int) (mvpList[5] + .6)).get(1))) > max){
			whereMax=5;
			max=(Double.parseDouble(listDEF.get(0).get(1))-Double.parseDouble(listDEF.get((int) (mvpList[5] + .6)).get(1)));
		}
		
		System.out.println("");
		if(whereMax == 0){
			System.out.println("*********************************");
			System.out.println("Select: " + listRB.get(0).get(0) + " (RB)");
			System.out.println("*********************************");
		}
		if(whereMax == 1){
			System.out.println("*********************************");
			System.out.println("Select: " + listWR.get(0).get(0) + " (WR)");
			System.out.println("*********************************");
		}
		if(whereMax == 2){
			System.out.println("*********************************");
			System.out.println("Select: " + listQB.get(0).get(0) + " (QB)");
			System.out.println("*********************************");
		}
		if(whereMax == 3){
			System.out.println("*********************************");
			System.out.println("Select: " + listTE.get(0).get(0) + " (TE)");
			System.out.println("*********************************");
		}
		if(whereMax == 4){
			System.out.println("*********************************");
			System.out.println("Select: " + listPK.get(0).get(0) + " (PK)");
			System.out.println("*********************************");
		}
		if(whereMax == 5){
			System.out.println("*********************************");
			System.out.println("Select: " + listDEF.get(0).get(0) + " (DEF)");
			System.out.println("*********************************");
		}
		
		System.out.println("  (Insert Player Picked)...");
		
		//find the player that just picked from the used input
		Scanner sc = new Scanner(System.in);
		String i = sc.nextLine();
		where =WhichList(i,listRB,listWR,listQB,listTE,listPK,listDEF);
		
		//delete player from lists
		if(where == 1){
			listRB=deleteRow(listRB,i);
			playerPicks.get(currentPlayer-1).set(0,playerPicks.get(currentPlayer-1).get(0) + 1);
		}
		if(where == 2){
			listWR=deleteRow(listWR,i);
			playerPicks.get(currentPlayer-1).set(1,playerPicks.get(currentPlayer-1).get(1) + 1);
		}
		if(where == 3){
			listQB=deleteRow(listQB,i);
			playerPicks.get(currentPlayer-1).set(2,playerPicks.get(currentPlayer-1).get(2) + 1);
		}
		if(where == 4){
			listTE=deleteRow(listTE,i);
			playerPicks.get(currentPlayer-1).set(3,playerPicks.get(currentPlayer-1).get(3) + 1);
		}
		if(where == 5){
			listPK=deleteRow(listPK,i);
			playerPicks.get(currentPlayer-1).set(4,playerPicks.get(currentPlayer-1).get(4) + 1);
		}
		if(where == 6){
			listDEF=deleteRow(listDEF,i);
			playerPicks.get(currentPlayer-1).set(5,playerPicks.get(currentPlayer-1).get(5) + 1);
		}
		if(where == 0){
			System.out.println("Player Not Found");
		}
		if(where!=0){
			count++;
			listStats = deleteColumn(listStats,i);
			listStats = arrayAdjust(listStats);
			
		
		//calculate next player and direction after next player
		if(forward == true){
	    	currentPlayer++;
		if(currentPlayer == players + 1){
			currentPlayer--;
			forward = false;
			
		}
	    }
	    
	    if(forward == false){
	    	currentPlayer--;
		if(currentPlayer == 0){
			currentPlayer++;
			forward = true;
			
		}
	    }
	    
	    //stop after every person has max players
	    if(16*players == count){
			draft = 0;
			sc.close();
		}
	    
		}
	}

kc.close();

	
	
}


}
	

