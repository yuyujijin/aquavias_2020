import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Level implements Cloneable {

	Piece[][] pieces;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLUE = "\u001b[36m";
	public static final String ANSI_BOLD = "\u001B[1m";
	public static final String ANSI_SELECTED = "\u001b[48;5;240m";
	public static final String ANSI_RED = "\u001b[31m";
	public View View;
	public int ID;
	public final int WIDTH;
	public final int HEIGHT;
	public int END_X;
	public int END_Y;
	int selected_x;
	int selected_y;

	char type;
	//c pour le compteur, f pour fuite, n pour 'normal' c'est à dire sans restriction
	int compteur;
	Instant start = Instant.now(); //Commence le décompte dès le début du niveau
	Instant end; //Se mettra à jour à chaque action de la part des utilisateurs
	int gametime;

	public Level(int w, int h) {

		/*
		 * Cette fonction créée seulement un tableau vide, avec une entrée et une sortie
		 */

		WIDTH = w;
		HEIGHT = h;
		END_X = WIDTH + 1;
		END_Y = HEIGHT - 1;

		setTab(w, h);

		/*
		 * On ne créer pas d'ID ici, l'id est créé seulement au moment de la sauvegarde
		 * d'un niveau
		 */
	}
	
	public Level(int w, int h, char t, int compt){
        t = Character.toLowerCase(t);
        if (t == 'f' || t == 'c' || t == 'n') type = t;
        else type = 'n';
        if(type == 'f' || type == 'n') compteur = 0;
        compteur = compt;
        WIDTH = w;
        HEIGHT = h;

        setTab(w, h);
        /*
         * On ne créer pas d'ID ici, l'id est créé seulement au moment de la sauvegarde
         * d'un niveau
         */
    }


	@SuppressWarnings("unchecked")
	public Level(int id) throws Exception {

		System.out.println("\nChargement du niveau...\n");
		FileReader reader;

		/* on récupère le fichier contenant le lvl */

		reader = new FileReader("levels/level" + id + ".json");

		/* on le parse */

		JSONParser jsonParser = new JSONParser();
		JSONObject obj = (JSONObject) jsonParser.parse(reader);

		/* on récupère les données de la taille et de l'id, et les initialise */

		int w = Math.toIntExact((long) obj.get("WIDTH"));
		int h = Math.toIntExact((long) obj.get("HEIGHT"));
		ID = Math.toIntExact((long) obj.get("ID"));
		WIDTH = w;
		HEIGHT = h;
		compteur = Math.toIntExact((long) obj.get("compteur"));
		END_X = WIDTH + 1;
		END_Y = HEIGHT - 1;
		setTab(w, h);
		String t = obj.get("type").toString();
		type = t.charAt(0);



		/* on récupère l'array Y (vertical) contenant les array X (horizontaux) */

		JSONArray y = (JSONArray) obj.get("Pieces");

		/* on créer un itérateur pour y */

		Iterator<JSONArray> iterator = y.iterator();

		/* et des indexs */

		int i = 0;
		int j = 0;

		/* on parcourt le tableau en récuperant chaque JSONObject piece */

		while (iterator.hasNext()) {
			JSONArray x = iterator.next();

			/* Ici on passe aux sous tableaux */

			for (JSONObject p : (Iterable<JSONObject>) x) {
				/* On vérifie si le type de la pièce, pour voir si elle est null ou non */

				String type = (String) p.get("TYPE");

				/* puis si c'est une pièce non nul on l'initalise */

				if (!type.equals("NONE")) {
					int rotation = Math.toIntExact((long) p.get("ROTATION"));
					pieces[j][i] = getPiece(type, rotation);
					if ((boolean) p.get("FULL"))
						pieces[j][i].setFull(true);
				}
				i++;
			}
			i = 0;
			j++;
		}

		/* Tout s'est bien déroulé */

		System.out.println("Niveau chargé.");
	}

	void setTab(int w, int h) {
		pieces = new Piece[h][w + 2]; /*
										 * La première et la dernière colonne sont presque vides, elles ne contiennent
										 * que les pièces de début et de fin
										 */
		pieces[0][0] = new PieceI(); /* On place la première pièce à 0, 0 */
		pieces[0][0].rotate();
		pieces[0][0].setFull(true);
		pieces[h - 1][w + 1] = new PieceI();
		pieces[h - 1][w + 1].rotate(); /* Et la dernière pièce au dernier indice du tableau */
	}

	Piece getPiece(String s, int i) {
		Piece pc;
		switch (s) {
		case ("I"):
			pc = new PieceI();
			break;
		case ("T"):
			pc = new PieceT();
			break;
		case ("L"):
			pc = new PieceL();
			break;
		default:
			pc = new PieceX();
			break;
		}
		for (int j = 0; j < i; j++) {
			pc.rotate();
		}
		return pc;
	}

	void affiche() { /* Affiche l'état du jeu */
		clearScreen();
		if(type == 'c') System.out.println(ANSI_BOLD+"              ["+compteur+"]"+ANSI_RESET);
		if (type =='f'){
			gametime = howLongSinceStart(); //La variable mesure la différence entre l'instant de début et l'instant présent
			if(gametime<=compteur-10) System.out.println(ANSI_BOLD+"              [" + (compteur-gametime) + " secondes restantes !]"+ANSI_RESET);
			else if(gametime >= compteur-10) System.out.println(ANSI_BOLD+"              [Vite, plus que " + (compteur-gametime) + " secondes !!]"+ANSI_RESET);
		}
		for (int i = 0; i < pieces.length; i++) {

			for (int j = 0; j < pieces[i].length; j++) {

				if (pieces[i][j] != null) {
					if (i == selected_y && j == selected_x)
						System.out.print(ANSI_SELECTED);
					if (pieces[i][j].isFull())
						System.out.print(ANSI_BLUE); /* Si la pièce contient de l'eau elle s'affiche en bleu */
					System.out.print(pieces[i][j].toString());
					if (pieces[i][j].isFull() || (i == selected_y && j == selected_x))
						System.out.print(ANSI_RESET); /* Et on arrête le bleu */
				} else {
					if (i == selected_y && j == selected_x)
						System.out.print(ANSI_SELECTED);
					System.out.print(" ");
					if (i == selected_y && j == selected_x)
						System.out.print(ANSI_RESET);
				}
			}
			System.out.println();
		}
	}


	public static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	void rotate(int i, int j) { /*
								 * i = y, j = x Fait tourner la pièces de coordonnées "i" et "j" mais reset
								 * l'eau qu'elle contient avant
								 */
		if(i==0 && j==0 || i==HEIGHT-1 && j==WIDTH+1) return;
		if (i < pieces.length && j < pieces[i].length && pieces[i][j] != null) {
			pieces[i][j].setFull(false);
			pieces[i][j].rotate();
        	if(type == 'c') compteur--;
		}
		update();
	}

	boolean isLeaking() {
		return(nbLeak()!=0);
	}
	
	public int nbLeak() {
		int leak=0;
		for (int i=0;i<HEIGHT;i++) {
			for(int j=1;j<WIDTH+1;j++) {
				if (pieces[i][j]!=null && pieces[i][j].isFull()) {
					if (pieces[i][j].DOWN && (!isInTab(i + 1, j) || 
									(isInTab(i + 1, j) && (pieces[i + 1][j]==null || 
									(pieces[i + 1][j]!=null && !pieces[i + 1][j].UP))))) {
						if((i+1!=0 || j!=0) && (i+1!=HEIGHT-1 || j!=WIDTH+1))
							leak++;			
					}
					if (pieces[i][j].UP && (!isInTab(i - 1, j) ||
							(isInTab(i - 1, j) && (pieces[i - 1][j]==null||
							(pieces[i-1][j]!=null && !pieces[i - 1][j].DOWN))))) {
						if((i-1!=0 || j!=0) && (i-1!=HEIGHT-1 || j!=WIDTH+1))
							leak++;
					}
					if (pieces[i][j].RIGHT && (!isInTab(i, j + 1) ||
							(isInTab(i, j + 1) && (pieces[i][j + 1]==null ||
							(pieces[i][j + 1]!=null && !pieces[i][j + 1].LEFT))))) {
						if((i!=0 || j+1!=0) && (i!=HEIGHT-1 || j+1!=WIDTH+1))
							leak++;
					}
					if (pieces[i][j].LEFT && (!isInTab(i, j - 1) ||
							(isInTab(i, j - 1) && (pieces[i][j - 1]==null ||
							(pieces[i][j - 1]!=null && !pieces[i][j - 1].RIGHT))))) {
						if((i!=0 || j-1!=0) && (i!=HEIGHT-1 || j-1!=WIDTH+1))
							leak++;
					}
				}
			}
		}
		return leak;
	}
	
	boolean estFinie(boolean affichage) { 
		//retourne faux tant que le niveau n'est pas fini, appelle Victory() sinon 
		//elle affiche le message de victoire ou de defaite si affichage est true
		if (type == 'c'){
			if(compteur<=0 || Victory()) {
				if(affichage) {
					if(Victory()) {
					clearScreen();
					affiche();
					System.out.println("\nBravo, la ville est desservie en eau !\n\n"
							+ "Pressez une touche pour passer au niveau suivant !");
					} else {
						clearScreen();
						affiche();
						System.out.println("\nOups, trop de deplacements, les habitants sont partis ailleurs chercher de l'eau :(\n\n"
								+ "Pressez une touche pour retenter d'aider la ville !");
					}
				}
				return true;
			}
			return false;
		} else if (type == 'f'){
			if (/*gametime >= compteur ||*/ Victory()){
				if(affichage){
					if (Victory()) {
						clearScreen();
						affiche();
						System.out.println("\nBravo, la ville est desservie en eau !\n\n" +
								"Pressez une touche pour passer au niveau suivant !");
					} else {
							clearScreen();
							affiche();
							System.out.println("\nAh c'est malin, il n'y a plus d'eau pour alimenter la ville !\n\n"
									+ "Pressez une touche pour retenter de l'aider!");
					}
				}
				return true;
			}
			return false;
		} else {
			if(Victory()) {
				if (affichage) {
					clearScreen();
					affiche();
					System.out.println("Bravo, la ville est desservie en eau !\n\n" +
							"Pressez une touche pour passer au niveau suivant !");
				}
				return true;
			}
			return false;
		}
	}
	
	void newLevel(int id) throws Exception{
		Level lvl = new Level(id);
		InputsWindow iw = new InputsWindow(lvl);
	}
	
	boolean Victory() { //retourne si la partie est finie ou non
		if(type == 'c' && compteur > 0 && !isLeaking())
        	return (pieces[HEIGHT - 1][WIDTH + 1].isFull());
		else if (type == 'f' /*&& gametime < compteur*/ && !isLeaking())
			return pieces[HEIGHT - 1][WIDTH + 1].isFull();
		else
			return pieces[HEIGHT - 1][WIDTH + 1].isFull() && !isLeaking();
    }


	void update() {
		// vide d'abord entièrement l'eau du circuit
		// puis appelle update dès la source
		voidAll();
		update(0,0);
		estFinie(true);
	}

	boolean isEnd(int x, int y) {
		return x == END_X && y == END_Y;
	}

	private void voidAll() { // vide l'eau de tout le circuit sauf de la source
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH + 2; j++) {
				if(i == 0 && j == 0) continue;
				if (pieces[i][j] != null) {
					pieces[i][j].setFull(false);
				}
			}
		}
	}

	void update(int i, int j) {
		// vérifie que les pièces limitrophes existent, qu'elles sont connectées à
		// l'actuelle et qu'elles ne sont pas déjà remplies
		if (i == HEIGHT - 1 && j == WIDTH + 1) return;

		if (isInTab(i + 1, j) && connected(pieces[i][j], pieces[i + 1][j], "DOWN") && !pieces[i + 1][j].isFull()) {
			setFull(i + 1, j);
			update(i + 1, j);
		}
		if (isInTab(i - 1, j) && connected(pieces[i][j], pieces[i - 1][j], "UP") && !pieces[i - 1][j].isFull()) {
			setFull(i - 1, j);
			update(i - 1, j);
		}
		if (isInTab(i, j + 1) && connected(pieces[i][j], pieces[i][j + 1], "RIGHT") && !pieces[i][j + 1].isFull()) {
			setFull(i, j + 1);
			update(i, j + 1);

		}
		if (isInTab(i, j - 1) && connected(pieces[i][j], pieces[i][j - 1], "LEFT") && !pieces[i][j - 1].isFull()) {
			setFull(i, j - 1);
			update(i, j - 1);
		}
	}

	int howLongSinceStart() {
		end = Instant.now();
		Duration timePassed = Duration.between(start, end);
		return (int) timePassed.toMillis() / 1000;
	}

	void setFull(int i, int j) { /* Pour remplir la pièce de coordonnées i et j */
		pieces[i][j].setFull(true);
	}

	boolean isInTab(int i, int j) { /* Vérifie que la pièce de coordonnées i et j est dans le tableau */
		return (i < HEIGHT && j < WIDTH + 2 && i >= 0 && j >= 0);
	}
	// i = y j = x

	boolean isVerticalyOk(int i) {
		return (i < HEIGHT && i >= 0);
	}

	boolean isHorizontalyOk(int j) {
		return (j <= WIDTH && j > 0);
	}

	boolean isFull(int i, int j) { /* Vérifie que le pièce de coordonnées i et j est pleine */
		return pieces[i][j] != null && pieces[i][j].isFull(); // i = y, j = x
	}

	boolean connected(Piece p1, Piece p2,
			String direction) { /* Vérifie le connection des pièces étant donnée une direction */
		if (p1 == null || p2 == null)
			return false;
		switch (direction) {
		case "UP":
			return (p1.isUp() && p2.isDown());
		case "RIGHT":
			return (p1.isRight() && p2.isLeft());
		case "DOWN":
			return (p1.isDown() && p2.isUp());
		case "LEFT":
			return (p1.isLeft() && p2.isRight());
		}
		return false;
	}

	/*void getPiecePos() {
		System.out.println("\nPlease select X position.");
		int scannerX = getInputInt();
		while (!isHorizontalyOk(scannerX)) {
			System.out.println("Position is invalid.");
			scannerX = getInputInt();
		}
		System.out.println("Please select Y position.");
		int scannerY = getInputInt();
		while (!isVerticalyOk(scannerY)) {
			System.out.println("Position is invalid.");
			scannerY = getInputInt();
		}
		if (isInTab(scannerY, scannerX) && pieces[scannerY][scannerX] != null) {
			System.out.println("Rotating piece [" + scannerX + ";" + scannerY + "]");
			selected_x = scannerX;
			selected_y = scannerY;
		} else {
			System.out.println("There is no piece here!");
			getPiecePos();
		}
	}

	int getInputInt() {
		Scanner scX = new Scanner(System.in);
		while (!scX.hasNextInt()) {
			System.out.println("This is not a number.");
			scX.next();
		}
		return scX.nextInt();
	}

	void printBlocker() {
		System.out.print("\n" + ANSI_BOLD + "#");
		for (int i = 0; i < WIDTH; i++)
			System.out.print("=");
		System.out.println("#" + ANSI_RESET + "\n");
	}*/


	//méthode qui permet de créer un niveau
	  void createLevel(){

		Scanner sc= new Scanner(System.in);
		int h, w;
		String type;
		int rotation;

		char c;


		System.out.println("** Bienvenue dans l'atelier de création de niveaux! **");
		System.out.println("Choisis une taille : ");
		System.out.println("Longueur = ");
		h = sc.nextInt();
		sc.nextLine();
		System.out.print("Largeur= ");
		w= sc.nextInt();
		sc.nextLine();
		Level level= new Level(h, w);
		System.out.println("Yay! Tu as créé le plateau! Maintenant, remplie le avec les pièces de ton choix: ");
		for(int i=0; i< h; i++){
			for(int j=1; j< w-1; j++) {
				System.out.println("Quel type? { I, L, T, X} ");  //instruction back
				type = sc.nextLine();
				while (!type.equals("L") && !type.equals("T") && !type.equals("I") && !type.equals("X")) {
					System.out.println("Mauvaise pioche, choisis à nouveau:  {I, L, T, X}: ");
					type = sc.nextLine();
				}

				System.out.println("Nombre de rotation? ");           //on peut utiliser un bloc try catch ou une assertion 
				rotation = sc.nextInt();
				sc.nextLine();
				while (rotation < 0) {
					System.out.println("Choisis à nouveau: ");
					rotation = sc.nextInt();
					sc.nextLine();
				}
				level.pieces[i][j] = level.getPiece(type, rotation);

				level.affiche();
			}
		}


		System.out.println("Level créé avec succès! Veux-tu le sauvegarder ? O/N");
		c = sc.nextLine().charAt(0);
		if(c=='O'){
			level.saveLevel();
			System.out.println("Sauvegargé, à la prochaine! ");
		}else{
			System.out.println();
		}



	}


	@SuppressWarnings("unchecked")
	void saveLevel() { /* pour sauvegarder le niveau */

		System.out.println("Sauvegarde du niveau...\n");
		JSONObject obj = new JSONObject();

		/* on créer un array, qui sera l'array vertical (y) */

		JSONArray y = new JSONArray();
		for (int i = 0; i < HEIGHT; i++) {

			/* ici on créer repetitivement des array horizontaux */

			JSONArray x = new JSONArray();
			for (int j = 0; j < WIDTH + 2; j++) {

				/* On créer un JSONObject contenant les attributs d'une pièce */
				/* son type, sa rotation et si elle est pleine ou non */

				JSONObject p = new JSONObject();
				if (pieces[i][j] != null) {
					p.put("TYPE", pieces[i][j].getType());
					p.put("ROTATION", pieces[i][j].getRotation());
					if (pieces[i][j].isFull()) {
						p.put("FULL", true);
					} else {
						p.put("FULL", false);
					}
				} else {
					p.put("TYPE", "NONE");
				}
				x.add(p);
			}
			y.add(x);
		}

		/* puis on rajoute qques infos utiles */

		obj.put("HEIGHT", HEIGHT);
		obj.put("WIDTH", WIDTH);
		obj.put("Pieces", y);
		try {

			/* on récupère l'id dans id.json, puis on l'incrémente de 1 */

			FileReader reader = new FileReader("levels/id.json");
			JSONParser jsonParser = new JSONParser();
			JSONObject JSONId = (JSONObject) jsonParser.parse(reader); /* on le parse */
			int id = Math.toIntExact((long) JSONId.get("ID"));
			ID = id;

			/* on rajoute l'id dans le json du niveau */

			obj.put("ID", ID);

			/* l'id est stocké, on l'incrémente */

			JSONObject JSONnewId = new JSONObject();
			JSONnewId.put("ID", id + 1);
			FileWriter IDfile = new FileWriter("levels/id.json"); /* enfin ici, on sauvegarde le fichier */
			IDfile.write(JSONnewId.toString());
			IDfile.close();

			/* l'id a été incrementé, on sauvegarde maintenant le niveau avec l'ancien id */

			FileWriter file = new FileWriter("levels/level" + id + ".json"); /* enfin ici, on sauvegarde le fichier */
			file.write(obj.toString());
			System.out.println("Niveau sauvegardé.");
			file.close();
		} catch (IOException | ParseException e) {
			System.out.println(e + "Impossible de sauvegarder le niveau.");
		}
	}

	void movePointer(String dir) {
		int x = selected_x;
		int y = selected_y;
		switch (dir) {
		case "UP":
			y--;
			break;
		case "DOWN":
			y++;
			break;
		case "RIGHT":
			x++;
			break;
		case "LEFT":
			x--;
			break;
		}
		if (isInTab(y, x)) {
			selected_x = x;
			selected_y = y;
			if(!estFinie(false))
				affiche();
		}
		estFinie(true);
	}

	void rotatePointer() {
		if(!estFinie(false)) {
			if (pieces[selected_y][selected_x] != null) {
				rotate(selected_y, selected_x);
				update();
			}
			if(!estFinie(false))
				affiche();
		}
		estFinie(true);
	}

	/* utilisée pour cloner un niveau */

	public Level clone() {
		Level cloned = new Level(WIDTH, HEIGHT);
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH + 2; j++) {
				if (pieces[i][j] != null)
					cloned.pieces[i][j] = pieces[i][j].clone();
			}
		}
		cloned.selected_x = selected_x;
		cloned.selected_y = selected_y;
		cloned.compteur = compteur;
		return cloned;
	}

	/*
	 * rajoute des pièces aléatoirement essentiellement utilisée pour tester les
	 * fonctions de pathfinding (le plus important c'est l'essentielle)
	 * 
	 */

	void randomizeLevel(int n) {
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 1; j < WIDTH + 1; j++) {
				if (pieces[i][j] == null) {
					Random rm = new Random();
					int x = rm.nextInt(n);
					Piece p = null;
					switch (x) {
					case 0:
						p = new PieceT();
						break;
					case 1:
						p = new PieceL();
						break;
					case 2:
						p = new PieceI();
						break;
					}
					pieces[i][j] = p;
				}
			}
		}
	}

	/*
	 * Affiche l'état du jeu pour le level checker. Unique différence? c'est rouge.
	 */

	void afficheChemin() {
		clearScreen();
		System.out.println(ANSI_BOLD + "              [" + compteur + "]" + ANSI_RESET);
		for (int i = 0; i < pieces.length; i++) {

			for (int j = 0; j < pieces[i].length; j++) {

				if (pieces[i][j] != null) {
					if (i == selected_y && j == selected_x)
						System.out.print(ANSI_SELECTED);
					if (pieces[i][j].isFull())
						System.out.print(ANSI_RED); /* Si la pièce contient de l'eau elle s'affiche en rouge */
					System.out.print(pieces[i][j].toString());
					if (pieces[i][j].isFull() || (i == selected_y && j == selected_x))
						System.out.print(ANSI_RESET); /* Et on arrête le bleu */
				} else {
					if (i == selected_y && j == selected_x)
						System.out.print(ANSI_SELECTED);
					System.out.print(" ");
					if (i == selected_y && j == selected_x)
						System.out.print(ANSI_RESET);
				}
			}
			System.out.println();
		}
	}

	Piece[][] getPieces(){ return pieces; }

	void setOverviewer(View po){ View = po ; }

	private class Coordonnes{
		int i;
		int j;

		public Coordonnes(int i, int j){
			this.i = i;
			this.j = j;
		}

		int getI(){ return i;}
		int getJ(){ return j;}

		Piece getPiece(){ return pieces[i][j]; }
	}

	String compteurToString(){
		return "["+Integer.toString(compteur)+"]";
	}
}
