import java.io.*;
//import java.lang.reflect.Type;
import java.util.*;
import javax.swing.JOptionPane;

import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;

/**
 * @author William Jiang
 *
 */
public class Hearthstone {
	
	static ArrayList<Card> hand = new ArrayList<Card>();
	static ArrayList<CardInPlay> yourBoard = new ArrayList<CardInPlay>();
	static ArrayList<CardInPlay> opponentBoard = new ArrayList<CardInPlay>();
	static ArrayList<Card> cardList;
	static CardInPlay player, enemy;

	static int mana = 1;
	static Node currMax;	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		FileReader f = new FileReader("cards.json");
		BufferedReader bufferedR = new BufferedReader(f);
		
		Gson conv = new Gson();
		//Object json = conv.fromJson(bufferedR, Object.class);
		
		//System.out.println(json.toString());
		
		//Type listType = new TypeToken<ArrayList<Card>>(){}.getType();
		
		Card[] cardArray = conv.fromJson(bufferedR, Card[].class);
		
		cardList = new ArrayList<Card>(Arrays.asList(cardArray));
		
		
		bufferedR.close();
			
		startGame();
		
	}
	
	public static void startGame() {
		
		player = new CardInPlay(null, 30, 0);
		enemy = new CardInPlay(null, 30, 0);
		
		int start = JOptionPane.showConfirmDialog(null, "Do you have the coin?");
		
		if(start == 0) {
			Card coin = new Card();
			coin.name = "The Coin";
			coin.type = "Spell";
			coin.text = "Gain 1 mana this turn only";
			hand.add(coin);
		}
		
		boolean done = false;
		
		while(!done) {
			String msg = "Add card by name. Cancel when done. \n Current Hand: ";
			for(int i = 0; i < hand.size(); i++) {
				msg = msg.concat(hand.get(i).name);
				if(i != hand.size() - 1) {
					msg = msg.concat(", ");
				}
			}
			
			String in = JOptionPane.showInputDialog(msg);
			
			if(in == null) {
				done = true;
				continue;
			}
			else {
				int index = cardList.indexOf(new Card(in));
				if(index == -1) {
					JOptionPane.showMessageDialog(null, "Card not found, try again", "Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					hand.add(cardList.get(index));
				}
			}
			
			
		}
		
		done = false;
		
		while(!done) {
			String msg = "Add minion by name in the format Name/Atk/Health. Cancel when done. \n Current Opponent Board: ";
			for(int i = 0; i < opponentBoard.size(); i++) {
				msg = msg.concat(opponentBoard.get(i).baseCard.name);
				if(i != opponentBoard.size() - 1) {
					msg = msg.concat(", ");
				}
			}
			
			String in = JOptionPane.showInputDialog(msg);
			
			if(in == null) {
				done = true;
				continue;
			}
			else {
				String[] splitInput = in.split("/");
				int index = cardList.indexOf(new Card(splitInput[0]));
				if(index == -1) {
					JOptionPane.showMessageDialog(null, "Card not found, try again", "Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					opponentBoard.add(new CardInPlay(cardList.get(index), Integer.parseInt(splitInput[1]), Integer.parseInt(splitInput[2])));
				}
			}
		}
		
		Node def = new Node(new ArrayList<Action>(), 0, 0, hand, yourBoard, opponentBoard, enemy, player);
		def.value = calculateValue(def);
		currMax = def;
		DFSRecursive(def);
		printActionSet();
		
		for(int i = 0; i < currMax.moveSet.size(); i++ ) {
			Action act = currMax.moveSet.get(i);
			if(act.actionType == 0) {
				
				CardInPlay newInPlay = new CardInPlay(act.tbPlayed, act.tbPlayed.health, act.tbPlayed.attack);
				newInPlay.canAttack = false;
				
				hand.remove(act.tbPlayed);
				yourBoard.add(newInPlay);
				
			}
			else if(act.actionType == 1) {
				
				CardInPlay tbAttacked = opponentBoard.get(opponentBoard.indexOf(act.target));
				CardInPlay atker = yourBoard.get(yourBoard.indexOf(act.attacker));
				
				if(tbAttacked.health - atker.attack <= 0) {
					opponentBoard.remove(tbAttacked);
				}
				else {
					tbAttacked.health -= atker.attack;
				}
				
				if(atker.health - tbAttacked.attack <= 0) {
					yourBoard.remove(atker);
				}
				else {
					atker.health -= tbAttacked.attack;
					atker.canAttack = false;
				}
				
				
			}
			else if(act.actionType == 5) {
				CardInPlay atker = yourBoard.get(yourBoard.indexOf(act.attacker));
				
				enemy.health -= act.attacker.attack;
				
				atker.canAttack = false;
			}
		}
		
		while(true) {
			UpdateGameState();
			
			
			def = new Node(new ArrayList<Action>(), 0, 0, hand, yourBoard, opponentBoard, enemy, player);
			currMax = def;
			DFSRecursive(def);
			printActionSet();
			
			for(int i = 0; i < currMax.moveSet.size(); i++ ) {
				Action act = currMax.moveSet.get(i);
				if(act.actionType == 0) {
					
					CardInPlay newInPlay = new CardInPlay(act.tbPlayed, act.tbPlayed.health, act.tbPlayed.attack);
					newInPlay.canAttack = false;
					
					hand.remove(act.tbPlayed);
					yourBoard.add(newInPlay);
					
				}
				else if(act.actionType == 1) {
					
					CardInPlay tbAttacked = opponentBoard.get(opponentBoard.indexOf(act.target));
					CardInPlay atker = yourBoard.get(yourBoard.indexOf(act.attacker));
					
					if(tbAttacked.health - atker.attack <= 0) {
						opponentBoard.remove(tbAttacked);
					}
					else {
						tbAttacked.health -= atker.attack;
					}
					
					if(atker.health - tbAttacked.attack <= 0) {
						yourBoard.remove(atker);
					}
					else {
						atker.health -= tbAttacked.attack;
						atker.canAttack = false;
					}
					
					
				}
				else if(act.actionType == 5) {
					CardInPlay atker = yourBoard.get(yourBoard.indexOf(act.attacker));
					
					enemy.health -= act.attacker.attack;
					
					atker.canAttack = false;
				}
			}
			
			mana++;
			
		}
		
		
	}
	
	public static void printActionSet() {
		for(int i = 0; i < currMax.moveSet.size(); i++) {
			Action move = currMax.moveSet.get(i);
			if(move.actionType == 0) {
				System.out.println("Play " + move.tbPlayed.name);
			}
			else if(move.actionType == 1) {
				System.out.println("Attack " + move.target.baseCard.name + " with " + move.attacker.baseCard.name);
			}
			else if(move.actionType == 5) {
				System.out.println("Attack face with " + move.attacker.baseCard.name);
			}
		}
	}
	
	public static void UpdateGameState() {
		
		String msg = "Previous Opponent Board: ";
		for(int i = 0; i < opponentBoard.size(); i++) {
			msg = msg.concat(opponentBoard.get(i).baseCard.name);
			if(i != opponentBoard.size() - 1) {
				msg = msg.concat(", ");
			}
		}
		
		msg = msg.concat("\n Were minions destroyed?");
		
		int ik = JOptionPane.showConfirmDialog(null, msg);
		
		if(ik != 1) {
			opponentMinionDestroyed();
		}
		
		
		
		ik = JOptionPane.showConfirmDialog(null, "Have existing minion stats changed?");
		
		if(ik != 1) {
			opponentMinionModified();
		}
		
		
		
		ik = JOptionPane.showConfirmDialog(null, "Have new enemy minions been played?");
		
		if(ik != 1) {
			opponentMinionPlayed();
		}
		
		msg = "Previous Your Board: ";
		for(int i = 0; i < yourBoard.size(); i++) {
			msg = msg.concat(yourBoard.get(i).baseCard.name);
			if(i != yourBoard.size() - 1) {
				msg = msg.concat(", ");
			}
		}
		
		msg = msg.concat("\n Were your minions destroyed?");
		
		ik = JOptionPane.showConfirmDialog(null, msg);
		
		if(ik != 1) {
			yourMinionDestroyed();
		}
		
		
		
		ik = JOptionPane.showConfirmDialog(null, "Have your existing minion stats changed?");
		
		if(ik != 1) {
			yourMinionModified();
		}
		
		for(int i = 0; i < yourBoard.size(); i++) {
			yourBoard.get(i).canAttack = true;
		}
		
		ik = JOptionPane.showConfirmDialog(null, "Have new enemy minions been played on your side?");
		
		if(ik != 1) {
			yourMinionPlayed();
		}
		
		String input = JOptionPane.showInputDialog("Enter health totals in format YourHealth/EnemyHealth: ");
		String[] inputSplit = input.split("/");
		player.health = Integer.parseInt(inputSplit[0]);
		enemy.health = Integer.parseInt(inputSplit[1]);
		
		
	}
	
	public static void opponentMinionDestroyed() {
		
		boolean done = false;
		
		while(!done) {		
			if(opponentBoard.size() == 0) {
				break;
			}
			CardInPlay[] oB = new CardInPlay[opponentBoard.size()];
			oB = opponentBoard.toArray(oB);
			Object choiceObject = JOptionPane.showInputDialog(null, "Choose minion destroyed", "Board Updating", JOptionPane.QUESTION_MESSAGE, null, oB, oB[0]);
			if(choiceObject != null) {
				CardInPlay destroyTarget = (CardInPlay) choiceObject;
				opponentBoard.remove(destroyTarget);
			}
			else {
				done = true;
			}
		}
	}
	
	public static void opponentMinionModified() {
		
		boolean done = false;
		
		while(!done) {
			CardInPlay[] oB = new CardInPlay[opponentBoard.size()];
			oB = opponentBoard.toArray(oB);
			Object choiceObject = JOptionPane.showInputDialog(null, "Choose minion to update stats for", "Board Updating", JOptionPane.QUESTION_MESSAGE, null, oB, oB[0]);
			if(choiceObject != null) {
				CardInPlay modifyTarget = (CardInPlay) choiceObject;
				String statInput = JOptionPane.showInputDialog("Enter stats in form 'Attack/Health' : ");
				String[] newStats = statInput.split("/");
				CardInPlay mod = opponentBoard.get(opponentBoard.indexOf(modifyTarget));
				mod.attack = Integer.parseInt(newStats[0]);
				mod.health = Integer.parseInt(newStats[1]);
			}
			else {
				done = true;
			}
		}
	}
	
	public static void opponentMinionPlayed() {
		
		boolean done = false;
		
		while(!done) {
			String in = JOptionPane.showInputDialog("Add new enemy minion in the form 'Name/Attack/Health' :");
			
			if(in == null) {
				done = true;
				continue;
			}
			else {
				String[] splitInput = in.split("/");
				int index = cardList.indexOf(new Card(splitInput[0]));
				if(index == -1) {
					JOptionPane.showMessageDialog(null, "Card not found, try again", "Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					opponentBoard.add(new CardInPlay(cardList.get(index), Integer.parseInt(splitInput[1]), Integer.parseInt(splitInput[2])));
				}
			}
		}
	}
	
	public static void yourMinionDestroyed() {
		
		boolean done = false;
		
		while(!done) {		
			CardInPlay[] oB = new CardInPlay[yourBoard.size()];
			oB = yourBoard.toArray(oB);
			Object choiceObject = JOptionPane.showInputDialog(null, "Choose minion destroyed", "Board Updating", JOptionPane.QUESTION_MESSAGE, null, oB, oB[0]);
			if(choiceObject != null) {
				CardInPlay destroyTarget = (CardInPlay) choiceObject;
				yourBoard.remove(destroyTarget);
			}
			else {
				done = true;
			}
		}
	}
	
	public static void yourMinionModified() {
		
		boolean done = false;
		
		while(!done) {
			CardInPlay[] oB = new CardInPlay[yourBoard.size()];
			oB = yourBoard.toArray(oB);
			Object choiceObject = JOptionPane.showInputDialog(null, "Choose minion to update stats for", "Board Updating", JOptionPane.QUESTION_MESSAGE, null, oB, oB[0]);
			if(choiceObject != null) {
				CardInPlay modifyTarget = (CardInPlay) choiceObject;
				String statInput = JOptionPane.showInputDialog("Enter stats in form 'Attack/Health' : ");
				String[] newStats = statInput.split("/");
				CardInPlay mod = yourBoard.get(yourBoard.indexOf(modifyTarget));
				mod.attack = Integer.parseInt(newStats[0]);
				mod.health = Integer.parseInt(newStats[1]);
			}
			else {
				done = true;
			}
		}
	}
	
	public static void yourMinionPlayed() {
		
		boolean done = false;
		
		while(!done) {
			String in = JOptionPane.showInputDialog("Add new enemy minion in the form 'Name/Attack/Health' :");
			
			if(in == null) {
				done = true;
				continue;
			}
			else {
				String[] splitInput = in.split("/");
				int index = cardList.indexOf(new Card(splitInput[0]));
				if(index == -1) {
					JOptionPane.showMessageDialog(null, "Card not found, try again", "Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					yourBoard.add(new CardInPlay(cardList.get(index), Integer.parseInt(splitInput[1]), Integer.parseInt(splitInput[2])));
				}
			}
		}
	}
	
	public static Node DFSRecursive(Node curr) {
		
		if(curr == null) {
			return null;
		}
		
		
		if(curr.value >= currMax.value) {
			currMax = curr;
		}
		
		ArrayList<Action> pA = possibleActions(curr);
		
		for(int i = 0; i < pA.size(); i++) {
			Node child = createChild(curr, pA.get(i));
			
			DFSRecursive(child);
		}
		
		return null;
		
	}
	
	//function that creates a child from a parent node, copying data and then adding new values to the node
	public static Node createChild(Node parent, Action act) {
		Node sol = new Node(new ArrayList<Action>(parent.moveSet), parent.value, parent.manaCost, new ArrayList<Card>(parent.currHand), new ArrayList<CardInPlay>(parent.currPBoard), new ArrayList<CardInPlay>(parent.currEBoard), parent.currEnemy, parent.currPlayer);
		sol.moveSet.add(act);
		
		if(act.actionType == 0) {
			
			CardInPlay newInPlay = new CardInPlay(act.tbPlayed, act.tbPlayed.health, act.tbPlayed.attack);
			newInPlay.canAttack = false;
			
			sol.currHand.remove(act.tbPlayed);
			sol.currPBoard.add(newInPlay);
			sol.manaCost += act.tbPlayed.cost;
			
		}
		else if(act.actionType == 1) {
			
			CardInPlay tbAttacked = sol.currEBoard.get(sol.currEBoard.indexOf(act.target));
			CardInPlay atker = sol.currPBoard.get(sol.currPBoard.indexOf(act.attacker));
			
			if(tbAttacked.health - atker.attack <= 0) {
				sol.currEBoard.remove(tbAttacked);
			}
			else {
				tbAttacked.health -= atker.attack;
			}
			
			if(atker.health - tbAttacked.attack <= 0) {
				sol.currPBoard.remove(atker);
			}
			else {
				atker.health -= tbAttacked.attack;
				atker.canAttack = false;
			}
			
			
		}
		else if(act.actionType == 5) {
			CardInPlay atker = sol.currPBoard.get(sol.currPBoard.indexOf(act.attacker));
			
			sol.currEnemy.health -= act.attacker.attack;
			
			atker.canAttack = false;
		}
		
		
		sol.value = calculateValue(sol);
		
		
		return sol;
	}
	
	public static float calculateValue(Node n) {
		
		float enemyMinions = 0;
		for(int i = 0; i < n.currEBoard.size(); i++) {
			enemyMinions += n.currEBoard.get(i).attack;
			enemyMinions += (n.currEBoard.get(i).health * 1.5);
		}
		
		float yourMinions = 0;
		for(int i = 0; i < n.currPBoard.size(); i++) {
			yourMinions += n.currPBoard.get(i).attack;
			yourMinions += (n.currPBoard.get(i).health * 1.5);
		}
		
		float healthValues = (n.currPlayer.health - n.currEnemy.health) * 0.5f; 
		
		if(n.currEnemy.health == 0) {
			return Float.MAX_VALUE;
		}
		else {
			return yourMinions - enemyMinions + healthValues;
		}
	}
	
	//function to find all possible actions from a state by using for loop to get list of all objects not in state (which can then be added)
	public static ArrayList<Action> possibleActions(Node n) {
		ArrayList<Action> sol = new ArrayList<Action>();
		
		//All possible cards that can be played
		
		for(int i = 0; i < n.currHand.size(); i++) {
			if(n.currHand.get(i).cost + n.manaCost <= mana) {
				if(n.currHand.get(i).type.equalsIgnoreCase("Minion")) {
					sol.add(new Action(0, null, null, n.currHand.get(i)));
				}
			}
		}
		
		//All possible attacks that can be made
		
		for(int i = 0; i < n.currPBoard.size(); i++) {
			if(n.currPBoard.get(i).canAttack == false) {
				continue;
			}
			for(int j = 0; j < n.currEBoard.size(); j++) {
				sol.add(new Action(1, n.currEBoard.get(j), n.currPBoard.get(i), null));
			}
			sol.add(new Action(5, n.currEnemy, n.currPBoard.get(i), null));
		}
		
		
		
		
		return sol;
	}

}



class Card {
	String id, name, text, flavor, artist, cardClass, faction, rarity, set, type;
	int attack, cost, health, dbfId, durability;
	boolean collectible, elite;
	String[] mechanics;
	
	public Card(String n) {
		this.name = n;
	}
	
	public Card() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean equals(Object o) {
		if(o.getClass() != this.getClass()) {
			return false;
		}
		
		Card otherCard = (Card) o;
		
		if(this.name.equals(otherCard.name)) {
			return true;
		}
		
		return false;
		
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}

class CardInPlay {
	Card baseCard;
	int health, attack;
	boolean canAttack, damaged;
	
	public CardInPlay(Card c, int h, int a) {
		this.baseCard = c;
		this.health = h;
		this.attack = a;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o.getClass() != this.getClass()) {
			return false;
		}
		
		CardInPlay otherCard = (CardInPlay) o;
		
		if(this.baseCard.name.equals(otherCard.baseCard.name) && this.health == otherCard.health && this.attack == otherCard.attack) {
			return true;
		}
		
		return false;
	}
	
}

class Node {
	float manaCost, value;
	ArrayList<Action> moveSet = new ArrayList<Action>();
	ArrayList<Card> currHand;
	ArrayList<CardInPlay> currPBoard, currEBoard;
	CardInPlay currEnemy, currPlayer;
	
	public Node(ArrayList<Action> n, float v, float mC, ArrayList<Card> cH, ArrayList<CardInPlay> PB, ArrayList<CardInPlay> EB, CardInPlay en, CardInPlay play) {
		this.manaCost = mC;
		this.value = v;
		this.moveSet = n;
		this.currHand = cH;
		this.currPBoard = PB;
		this.currEBoard = EB;
		this.currEnemy = en;
		this.currPlayer = play;
	}
	
	@Override
	public String toString() {
		return this.moveSet.toString();
	}
	
}

class Action {
	CardInPlay target, attacker;
	Card tbPlayed;
	int actionType;
	
	public Action(int at, CardInPlay t, CardInPlay a, Card tbp) {
		this.actionType = at;
		this.target = t;
		this.attacker = a;
		this.tbPlayed = tbp;
	}
	
	@Override
	public String toString() {
		if(this.actionType == 0) {
			return "Play " + this.tbPlayed.name;
		}
		else if(this.actionType == 1) {
			return "Attack " + this.target.baseCard.name + " with " + this.attacker.baseCard.name;
		}
		else if(this.actionType == 5) {
			return "Attack face with " + this.attacker.baseCard.name;
		}
		return null;
	}
}
