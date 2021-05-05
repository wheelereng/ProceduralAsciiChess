package chess;
import java.util.Scanner;

public class chess {
	
	public static chessEngine game;
	
	
	public static void render() {
		
		System.out.println("|/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\|/\\/|");
		
		//parity Y alternates every three lines to change colour going down
		boolean parityY = true;
		
		//parity x alternates every 6 spaces to alternate colour going across
		boolean parityX = !parityY;
		
		//Variables for checking position inside each square
		int squareYPos = 0;
		
		//Counter for the row numbers
		int rowNum = 8;
		
		//current square on the board
		//ngl, the -1 value was a quickfix
		int row = -1;
		int col = 0;
		
		//Loop for lines
		for (int j = 0; j < 24; j++) {
			
			System.out.print("|");
			
			//if in middle of square print the number

			if (j % 3 == 0) {
				parityY = !parityY;
				//end of line so restart square Y position
				squareYPos = 0;
				row += 1;
			}
			
			parityX = !parityY;
			
			//Loop for characters on line
			for (int i = 0; i < 8; i++) {
				if (parityX) {
					
					//If center of the square and piece on square print piece position
					if (squareYPos == 1 && game.gameState[row][col] != "x") {
						System.out.print("   " + game.gameState[row][col] + "   ");
						parityX = false;
						col += 1;
					} else {
						System.out.print("       ");
						parityX = false;
						col += 1;
					}
				}
				else {
					if (squareYPos == 1 && game.gameState[row][col] != "x") {
						System.out.print("## " + game.gameState[row][col] +" ##");
						parityX = true;
						col += 1;
					} else {
						System.out.print("#######");
						parityX = true;
						col += 1;
					}
				}
			}
			
			//end of line so increment Y position
			squareYPos += 1;
			
			//Reset the column counter
			col = 0;
			
			//if in middle of square print the number
			if (squareYPos == 2) {
				System.out.print("| " + rowNum + " |");
				System.out.println("");
				rowNum -= 1;
			} else {
				System.out.print("|   |");
				System.out.println("");
			}
		}
		System.out.println("|````````````````````````````````````````````````````````|```|");
		System.out.println("|   A      B      C      D      E      F      G      H   |   |");
		System.out.println("|________________________________________________________|___|");
		System.out.println("|/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\|/\\/|");
	}

	public static void main(String[] args) {
		
		
		
		// Print Banner, mostly stolen from asciiart.eu as I have the artistic skill of a donkey, I did attempt at **REDACTED** (due to
		//catastrophic failure).
		
		//Chess horse credit: jgs
		System.out.println("		      ,....,");
		System.out.println("		     ,::::::<");
		System.out.println("		    ,::/^\\\"``.");
		System.out.println("	A	   ,::/, `   e`.");						
		System.out.println("	S	  ,::; |        '.");						
		System.out.println("	C	  ,::|  \\___,-.  c)");                        
		System.out.println("	I	  ;::|     \\   \'-\'");                         
		System.out.println("	I	  ;::|      \\         ");                     
		System.out.println("		  ;::|   _.=`\\         ");                   
		System.out.println("	C	  `;:|.=` _.=`\\         ");                  
		System.out.println("	H	    \'|_.=`   __\\");
		System.out.println("	E	    `\\_..==`` /");
		System.out.println("	S	     .\'.___.-\'.");
		System.out.println("	S	    /          \\");
		System.out.println("		   (\'--......--\')");
		System.out.println("		   /\'--......--\'\\");
		System.out.println("		   `\"--......--\"");
		System.out.println("\nKnight image credit to jgs - asciiart.com\n");
		game = new chessEngine(true);
		
		Scanner inputObj = new Scanner(System.in);
		render();
		System.out.println("");
		String userInput = "";
		int color = 0;
		boolean success = true;
		
		while (!(userInput.equals("q"))) {
			
			if (color==1) {
				System.out.print("Black! Make a move: ");
			} else {
				System.out.print("White! Make a move: ");
			}
			userInput = inputObj.next();
			
			success = game.makeMove(userInput, color);
			
			

			
			if (success) {
				
				render();
				
				if (game.isCheckMate(color)) {
					System.out.println("\n\nCheckmate!\n\n");
					break;
				}
				
				color = (color + 1) % 2;
				
				System.out.println("");
			}
		}
		
		inputObj.close();


	}

}
