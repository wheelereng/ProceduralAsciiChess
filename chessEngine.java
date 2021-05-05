package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Jordan Wheeler
 * 
 * Note, uses numbers for positions and we can use normal arithmetic on them, no issue of wrap-around as we consider a single 
 * move at a time, no single move can increment  or decrement the number enough to show up on the other side of the board.
 * e.g. knight in position 07, move forward and right (add 12) is position 19, as 9 > 7 we can't move there.
 * 
 * STILL NEED TO DO: Caslting, En-Passant, Pawn Promotion, isStalemate(), isDraw(). 
 *
 */
public class chessEngine {
	//Hash to store the pieces each colour has on the board, they're positions and where they
	//can move to
	public HashMap<String, HashMap<Integer, List<Integer>>> blackPieces;
	public HashMap<String, HashMap<Integer, List<Integer>>> whitePieces;
	public String[][] gameState;
	
	public boolean checkmateEval;
	
	/**
	 * Initialises the game board positions
	 * @param blackNotWhite - true if black is the main focus. So it will flip the board
	 */
	public chessEngine(Boolean blackNotWhite) {
		
		//Do board flipping at a later stage
		
		//Board layout used for rendering, lowercase is black, uppercase is white, x is an empty square.

		gameState = new String[][] {{"r", "n", "b", "q", "k", "b", "n", "r"},
				 {"p", "p", "p", "p", "p", "p", "p", "p"},
				 {"x", "x", "x", "x", "x", "x", "x", "x"},
				 {"x", "x", "x", "x", "x", "x", "x", "x"},
				 {"x", "x", "x", "x", "x", "x", "x", "x"},
				 {"x", "x", "x", "x", "x", "x", "x", "x"},
				 {"P", "P", "P", "P", "P", "P", "P", "P"},
				 {"R", "N", "B", "Q", "K", "B", "N", "R"}
               };
		
        //Initiates black's pieces on the board and where they can move to 
        whitePieces = new HashMap<>(); 
        whitePieces.put("p", new HashMap<>(Map.of(60, Arrays.asList(50, 40), 61, Arrays.asList(51, 61),
													62, Arrays.asList(52, 42), 63, Arrays.asList(53, 43),
													64, Arrays.asList(54, 44), 65, Arrays.asList(55, 45),
													66, Arrays.asList(56, 46), 67, Arrays.asList(57, 47))));
		
        whitePieces.put("b", new HashMap<>(Map.of(72, Arrays.asList(), 75, Arrays.asList())));
        whitePieces.put("r", new HashMap<>(Map.of(70, Arrays.asList(), 77, Arrays.asList())));
        whitePieces.put("n", new HashMap<>(Map.of(71, Arrays.asList(50, 52), 76, Arrays.asList(57, 55))));
        whitePieces.put("q", new HashMap<>(Map.of(73, Arrays.asList())));
        whitePieces.put("k", new HashMap<>(Map.of(74, Arrays.asList())));
		
		//Initiates white's pieces on the board and where they can move to 
		blackPieces = new HashMap<>();
		blackPieces.put("p", new HashMap<>(Map.of(10, Arrays.asList(20, 30), 11, Arrays.asList(21, 31),
				12, Arrays.asList(22, 32), 13, Arrays.asList(23, 33),
				14, Arrays.asList(24, 34), 15, Arrays.asList(25, 35),
				16, Arrays.asList(26, 36), 17, Arrays.asList(27, 37))));
		
		blackPieces.put("b", new HashMap<>(Map.of(02, Arrays.asList(), 05, Arrays.asList())));
		blackPieces.put("r", new HashMap<>(Map.of(00, Arrays.asList(), 07, Arrays.asList())));
		blackPieces.put("n", new HashMap<>(Map.of(01, Arrays.asList(20, 22), 06, Arrays.asList(25, 27))));
		blackPieces.put("q", new HashMap<>(Map.of(03, Arrays.asList())));
		blackPieces.put("k", new HashMap<>(Map.of(04, Arrays.asList())));
		
		checkmateEval = false;
	}
	
	/**
	 * Re-calculates all the moves that can be played after a move is played
	 * @param color - the players color to consider.
	 */
	public void calculateAllMoves(int color) {
		HashMap<String, HashMap<Integer, List<Integer>>> pieceSet;
		
		// Get the correct colours pieces
		if (color == 0) {
			pieceSet = whitePieces;
		} else {
			pieceSet = blackPieces;
		}
		
		for (String pieceType: pieceSet.keySet()) {
			HashMap<Integer, List<Integer>> subPieces = pieceSet.get(pieceType);
			for (Integer piecePos: subPieces.keySet()) {
				
				//If it's a pawn and it is its first move then pass through the true parameter.
				if (pieceType.equals("p") && ((color == 0 && (piecePos / 10) == 6) || (color == 1 && (piecePos / 10) == 1))) {
					subPieces.put(piecePos, moveTo(pieceType, piecePos, color, true));
				}
				else {
					subPieces.put(piecePos, moveTo(pieceType, piecePos, color, false));
				}
				
				pieceSet.put(pieceType, subPieces);
					
			}
			
		}
	}
		
	public String decodeAN(String AN) {
		
		HashMap<String, Integer> letterTranslate = new HashMap<>();
		String[] ANChar;
		Integer newPos;
		Integer column;
		
		letterTranslate.put("a", 0);
		letterTranslate.put("b", 1);
		letterTranslate.put("c", 2);
		letterTranslate.put("d", 3);
		letterTranslate.put("e", 4);
		letterTranslate.put("f", 5);
		letterTranslate.put("g", 6);
		letterTranslate.put("h", 7);
		
		//List of available pieces for more helpful error messages
		List<String> allowedPieces = Arrays.asList("p", "b", "n", "r", "q", "k");
		
		AN = AN.toLowerCase();
		ANChar = AN.split("");
		
		//ParseInt method can throw error if it's incorrect, handle this.
		try {
		
			if (ANChar.length!=3) {
				throw new NumberFormatException();
			}
			
			String piece = ANChar[0];
			
			if (letterTranslate.containsKey(ANChar[1])) {
				column = letterTranslate.get(ANChar[1]);
				newPos = ((8 - Integer.parseInt(ANChar[2])) * 10) + column;
			} else {
				throw new NumberFormatException();
			}
			
			//Check to see if it's an actual piece
			if (allowedPieces.contains(piece)) {
				return piece + newPos.toString();
			} else {
				System.out.println(":O !!!!!!!!!!!!!!!!!!!!!!!!!!!!! :O");
				System.out.println("Sorry, that piece  isn't recognised");
				System.out.println("```````````````````````````````````");
				return "e";
			}
			
		} catch (NumberFormatException e) {
			System.out.println("O.o !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! o.O");
			System.out.println("Sorry, but that's not a position we can understand.");
			System.out.println("```````````````````````````````````````````````````");
			return "e";
		}
	}
	
	/**
	 * This function will deal the logic
	 * of moving the piece and its relevant checks.
	 * @param AN - Algebraic notation that was entered.
	 * @param color - the color of the player making the move
	 * @return true if the move is succesful and has been made, false otherwise.
	 */
	public boolean makeMove(String AN, int color) {
		//variables for which pieces to use depending on the colour
		HashMap<String, HashMap<Integer, List<Integer>>> pieceSet;
		HashMap<String, HashMap<Integer, List<Integer>>> enemyPieceSet;
		String decodedAN;
		String piece;
		Integer newPos;
		List<Integer> originalPos;
		
		//Iterator to get the position of the king in the structure as we won't know its
		//exact position.
		Iterator<Map.Entry<Integer, List<Integer>>> iterator;
		
		//Variables to deal with the roll back in the event that a piece was captured
		//during the calculation
		boolean captureEvent = false;
		String capturedPiece = null;
		Integer capturedPos = null;
		

		
		//if white's move look through their pieces and check to see if they still have it.
		if (color == 0) {
			pieceSet = whitePieces;
			enemyPieceSet = blackPieces;
		} else {
			pieceSet = blackPieces;
			enemyPieceSet = whitePieces;
		}
		
		//decode the user given input.
		if (!checkmateEval) {
			decodedAN = decodeAN(AN);
		} else {
			decodedAN = AN;
		}
		
		//if there's an error in the decoding then return false
		if (decodedAN.equals("e")) {
			return false;
		//else we set up the required variables.
		} else {
			String[] ANChar = decodedAN.split("");
			piece = ANChar[0];
			newPos = Integer.parseInt((ANChar[1] + ANChar[2]));
		}

		Set<String> availablePieces = pieceSet.keySet();
		if (availablePieces.contains(piece)) {
			
			//Check to see if they can make the move or not
			
			HashMap<Integer, List<Integer>> pieceData =  pieceSet.get(piece);
			
			for (Integer pos : pieceData.keySet()) {
				
				if (pieceData.get(pos).contains(newPos)) {

					//They might be able to make the move, so we make the move
					//update piece data, and then check for checks.
					//We do it this way in order to avoid making deep copies of the
					//pieceSets and to avoid over-complicating it, as it's not very 
					//computationally or memory intensive.
											
				    
					//updates the pieces and their positions in the game 
					originalPos = pieceData.get(pos);
					
					pieceData.remove(pos);
					pieceData.put(newPos, moveTo(piece, newPos, color, false));
					
					pieceSet.put(piece, pieceData);
					
					//Calculate all moves for the current colour that's moved.
					calculateAllMoves(color);
					
					//Calculate for the opposing colour.
					calculateAllMoves((color + 1) % 2);	
					
					//Get the first entry of the king in the HashMap.
					iterator = pieceSet.get("k").entrySet().iterator();
		
					//If capture then remove from opposing colours pieces.
					if (enemyOccupied(newPos, color)) {
						for (String pieceType: enemyPieceSet.keySet()) {
							HashMap<Integer, List<Integer>> piecePos = enemyPieceSet.get(pieceType);
							if (piecePos.containsKey(newPos)) {
								//remove piece, update and exit loop
								capturedPos =  newPos;
								capturedPiece = pieceType;
								
								piecePos.remove(newPos);
								enemyPieceSet.put(pieceType, piecePos);
								
								captureEvent = true;
								break;
							}
						}
					}
					
					//If we are in check we roll back to previous state and return false.
					if (inCheck(iterator.next().getKey(), color)) {
						
						//Roll-back to previous state.
						
						//first roll back to players current move previous state
						pieceData.remove(newPos);
						pieceData.put(pos, originalPos);
						pieceSet.put(piece, pieceData);
						
						//if a capture has been made during the calculation then add the piece 
						//back into the enemies collection.
						if(captureEvent) {
							HashMap<Integer, List<Integer>> enemyPositions = enemyPieceSet.get(capturedPiece);
							enemyPositions.put(capturedPos, Arrays.asList());
							enemyPieceSet.put(capturedPiece, enemyPositions);
						}
						
						//Calculate all moves for the current colour that's moved.
						calculateAllMoves(color);
						
						//Calculate for the opposing colour.
						calculateAllMoves((color + 1) % 2);	
						
						if (!checkmateEval) {
							//friendly message to signify a failed move.
							System.out.println(":( !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! :(");
							System.out.println("............You are in check!.............");
						}

						return false;
					}
					
					//If we are evaluating the board for a checkmate and we get to here,
					//then we have passed all checks and we can roll back and return true.
					if (checkmateEval) {
						
						//Roll-back to previous state.
						
						//first roll back to players current move previous state
						pieceData.remove(newPos);
						pieceData.put(pos, originalPos);
						pieceSet.put(piece, pieceData);
						
						//if a capture has been made during the calculation then add the piece 
						//back into the enemies collection.
						if(captureEvent) {
							HashMap<Integer, List<Integer>> enemyPositions = enemyPieceSet.get(capturedPiece);
							enemyPositions.put(capturedPos, Arrays.asList());
							enemyPieceSet.put(capturedPiece, enemyPositions);
						}
						
						//Calculate all moves for the current colour that's moved.
						calculateAllMoves(color);
						
						//Calculate for the opposing colour.
						calculateAllMoves((color + 1) % 2);	

						return true;
					}

					//If white then make it upper case.
					if (color==0) {
						gameState[newPos/10][newPos%10] = piece.toUpperCase();
					} else {
						gameState[newPos/10][newPos%10] = piece;
					}
					
					//Clear the piece from the old position
					gameState[pos/10][pos%10] = "x";

					return true;
				}

			}
			
			//If we finished the loop and we haven't found a space then they can't move there!
			System.out.println(":O !!!!!!!!!!!!!!!!!!!!!!!!!!! :O");
			System.out.println("Sorry, but that's an illegal move");
			System.out.println("`````````````````````````````````");
			return false;
			
		} else {
			System.out.println(":( !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! :(");
			System.out.println("Sorry buddy, you lost that piece earler...");
			System.out.println("``````````````````````````````````````````");
			return false;	
		}

	}
	
	/**
	 * Checks to see if the square in the given position is free or not.
	 * @param pos - position to be checked.
	 * @return - return true if it's free, false otherwise.
	 */
	public boolean squareFree(Integer pos) {
		Integer row = pos / 10;
		
		Integer col = pos % 10;
		
		if (col > 7 || row > 7 || col < 0 || row < 0) {
			return false;
		}
		
		//Check to see if a white piece occupies the square.
		for (String piece: whitePieces.keySet()) {
			if (whitePieces.get(piece).containsKey(pos)) {
				return false;
			}
		}
		
		//Check to see if a black piece occupies the square
		for (String piece: blackPieces.keySet()) {
			if (blackPieces.get(piece).containsKey(pos)) {
				return false;
			}
		}
		
		//If passed all tests then return true
		return true;
	}
	
	/**
	 * Checks to see if the square in the given position is occupied by the enemy square or not.
	 * @param pos - the position on the board to be checked.
	 * @param color - the color of the player in question.
	 * @return
	 */
	public boolean enemyOccupied(Integer pos, int color) {
		HashMap<String, HashMap<Integer, List<Integer>>> pieceSet;
		
		//Make sure we're on the board.
		Integer row = pos / 10;
		Integer col = pos % 10;
		
		if (col > 7 || row > 7 || col < 0 || row < 0) {
			return false;
		}
		
		//loop through the enemies pieces and check to see if they occupy the square.
		
		// Get the correct enemy pieces
		if (color == 1) {
			pieceSet = whitePieces;
		} else {
			pieceSet = blackPieces;
		}
		
		for (String pieceType: pieceSet.keySet()) {
			
			HashMap<Integer, List<Integer>> subPieces = pieceSet.get(pieceType);
			
			if (subPieces.containsKey(pos)) {
				return true;
			}
		}
		
		//If we have checked all the pieces and we haven't found a position then there's nothing to capture
		return false;
	}
	
	/**
	 * Checks to see if the King is in check or not.
	 * 
	 * @param kingPos - the king's position on the board
	 * @param color - the colour of the king, i.e. the color of who to check is in check.
	 * @return true is the king is in check, false otherwise.
	 */
	public boolean inCheck(Integer kingPos, int color) {
		HashMap<String, HashMap<Integer, List<Integer>>> enemyPieceSet;
		if (color == 0) {
			enemyPieceSet = blackPieces;
		} else {
			enemyPieceSet = whitePieces;
		}
		
		//check to see if the enemy pieces can see the king, if so return true.
		for (String pieceType : enemyPieceSet.keySet()) {
			HashMap<Integer, List<Integer>> subPieces = enemyPieceSet.get(pieceType);
			for (Integer piecePos: subPieces.keySet()) {
				if (subPieces.get(piecePos).contains(kingPos)) {
					return true;
				}
			}
		}
		
		//If no pieces can see the king return false
		return false;
	}
	
	/**
	 * Checks the entire board and all the moves to see if the king is still in check after any move,
	 * if so then it's check mate.
	 * @param color - the color of the player who is in check.
	 * @return true is it's checkmate, false otherwise.
	 */
	public boolean isCheckMate(int color) {
		//Loop through the players pieces and make all the moves,
		//if all the moves leave them in check then it's checkmate.
		
		checkmateEval = true;
		String AN;
		int enemyColor;
		List<String> moves =  new ArrayList<String>();
		
		HashMap<String, HashMap<Integer, List<Integer>>> enemyPieceSet;
		if (color == 0) {
			enemyPieceSet = blackPieces;
			enemyColor = 1;
		} else {
			enemyPieceSet = whitePieces;
			enemyColor = 0;
		}
		//We make a list of all the available moves, then iterate through that list using
		//the makeMove function, as the makeMove function changes the contents of the pieceSet
		
		//Iterate over the piece types
		for (String piece : enemyPieceSet.keySet()) {
			HashMap<Integer, List<Integer>> piecePos = enemyPieceSet.get(piece);
			
			//Iterate over the current positions of the pieces.
			for (Integer pos: piecePos.keySet()) {
				
				List<Integer> moveList = piecePos.get(pos);
				//Iterate over the future moves of the pieces.
				for (Integer futurePos : moveList) {
					//Build the string to pass into the makeMove function.
					if (futurePos < 10) {
						AN = piece + "0" + futurePos.toString();
					} else {
						AN = piece + futurePos.toString();
					}
						moves.add(AN);
				}
				
			}
			
			for (String move: moves) {
				if (makeMove(move, enemyColor)) {
					checkmateEval = false;
					return false;
				}
			}
		}
		checkmateEval = false;
		return true;
	}
	
	/**
	 * Calculates all the moves a particular piece can make.
	 * @param piece - the piece to calculate the moves for.
	 * @param pos - the position / the square it is on.
	 * @param color - the color of the piece.
	 * @param firstMove - whether or not it is the first move or not (Pawns)
	 * @return a list of available moves.
	 */
	public List<Integer> moveTo(String piece, Integer pos, Integer color, Boolean firstMove) {
		//comments of where the piece is looking is from black's perspective. They're just to illustrate 
		//the thought process.
		
		List<Integer> positions = new ArrayList<Integer>();
		Integer newPos;
		
		//Variable to add or subtract for pawn moves
		int moveDirection = -1;
		
		//if from blacks view we want to be adding for the pawns
		if (color.equals(1)) {
			moveDirection = 1;
		}
		
		//Pawns
		if (piece.equals("p")) {
				newPos = pos + (moveDirection * 10);
				if (squareFree(newPos)) { 
					positions.add(newPos);
				}
				
				//If first move and square isn't occupied
				if (firstMove && squareFree(pos + (moveDirection * 20))) {
					positions.add(pos + (moveDirection * 20));
				}
				
				//Pawn captures
				
				//Left diagonal
				newPos =  pos + (moveDirection * 11);
				if (enemyOccupied(newPos, color)) {
					positions.add(newPos);
				}
				
				//right diagonal
				newPos = pos + (moveDirection * 9);
				if (enemyOccupied(newPos, color)) {
					positions.add(newPos);
				}
		}
		
		//Rooks and QUEENS
		if (piece.equals("r") || piece.equals("q")) {
			
			//first look ahead
			newPos = pos + 10;
			while (squareFree(newPos)) {
				positions.add(newPos);
				newPos += 10;
			}
			
			//ahead capture, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//Now look 'backwards'
			newPos = pos - 10;
			while (squareFree(newPos)) {
				positions.add(newPos);
				newPos -= 10;
			}
			
			//'backwards' capture, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//Now Look left
			
			newPos = pos - 1;
			while (squareFree(newPos)) {
				positions.add(newPos);
				newPos -= 1;
			}
			
			//left capture, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//Now look right
			newPos = pos + 1;
			while (squareFree(newPos)) {
				positions.add(newPos);
				newPos += 1;
			}
			
			//right capture, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
		} 
		
		//Knights
		if (piece.equals("n")) {
		
			//2 right 1 'forward'
			newPos = pos + 12;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//2 right 1 'forward', newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//2 'forward' 1 left
			newPos = pos - 8;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//2 'forward' 1 left, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//2 'backward' 1 right 
			newPos = pos + 8;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//2 'backward' 1 right, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//2 'backward' 1 left
			newPos = pos - 12;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//2 'backward' 1 left, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//2 right 1 'forward'
			newPos = pos + 21;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//2 right 1 'forward', newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//2 left 1 'forward'
			newPos = pos - 19;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//2 left 1 'forward', newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//2 right 1 'backward'
			newPos = pos + 19;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//2 right 1 'forward', newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//2 left 1 'backward'
			newPos = pos - 21;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//2 left 1 'backward', newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
		} 
		
		//Bishops and QUEEN
		if (piece.equals("b") || piece.equals("q")) {
			//'Forward' right diagonal
			newPos = pos + 11;
			while (squareFree(newPos)) {
				positions.add(newPos);
				newPos += 11;
			}
			
			//forward right diagonal, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//Backwards left diagonal
			newPos = pos - 11;
			while (squareFree(newPos)) {
				positions.add(newPos);
				newPos -= 11;
			}
			
			//backwards left diagonal, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//Forwards left Diagonal
			newPos = pos + 9;
			while(squareFree(newPos)) {
				positions.add(newPos);
				newPos += 9;
			}
			
			//forwards left diagonal, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//Backward right diagonal
			newPos = pos - 9;
			while(squareFree(newPos)) {
				positions.add(newPos);
				newPos -= 9;
			}
			
			//Backwards right diagonal, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
		}
		
		//King
		
		if (piece.equals("k")) {
			//Up one square
			newPos = pos + 10;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//Up one square, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//Down one square
			newPos = pos - 10;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//Down one square, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//right one square
			newPos = pos + 1;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//right one square, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//Left one square
			newPos = pos - 1;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//left one square, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//right up
			newPos = pos + 11;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//right up square, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//left down
			newPos = pos - 11;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//left down, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//right down
			newPos = pos - 9;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//right down square, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
			//left up
			newPos = pos + 9;
			if (squareFree(newPos)) {
				positions.add(newPos);
			}
			
			//Left up, newPos last position is on occupied square
			if (enemyOccupied(newPos, color)) {
				positions.add(newPos);
			}
			
		}
		
		return positions;
		
	}

}
