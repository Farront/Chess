package main;

import piece.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable{
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    //Pieces
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    public static Piece castlingP;
    Piece activeP, checkingP;


    //Color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    //Booleans
    private boolean playWithAI;
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;

    public GamePanel(boolean playWithAI) {
        this.playWithAI = playWithAI; // Store the game mode
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
        copyPieces(pieces, simPieces);
    }


    public void launchGame() {

        gameThread = new Thread(this);
        gameThread.start();

    }

    public void setPieces() {

        // WHITE TEAM
        pieces.add(new  Pawn(0, 6, WHITE));
        pieces.add(new  Pawn(1, 6, WHITE));
        pieces.add(new  Pawn(2, 6, WHITE));
        pieces.add(new  Pawn(3, 6, WHITE));
        pieces.add(new  Pawn(4, 6, WHITE));
        pieces.add(new  Pawn(5, 6, WHITE));
        pieces.add(new  Pawn(6, 6, WHITE));
        pieces.add(new  Pawn(7, 6, WHITE));
        pieces.add(new  Rook(0,7, WHITE));
        pieces.add(new  Rook(7, 7,WHITE));
        pieces.add(new  Knight(1, 7,WHITE));
        pieces.add(new  Knight(6, 7,WHITE));
        pieces.add(new  Bishop(2, 7,WHITE));
        pieces.add(new  Bishop(5, 7,WHITE));
        pieces.add(new  Queen(3, 7,WHITE));
        pieces.add(new  King(4, 7,WHITE));

        // BLACK TEAM
        pieces.add(new Pawn(0, 1,BLACK));
        pieces.add(new Pawn(1, 1,BLACK));
        pieces.add(new Pawn(2, 1,BLACK));
        pieces.add(new Pawn(3, 1,BLACK));
        pieces.add(new Pawn(4, 1,BLACK));
        pieces.add(new Pawn(5, 1,BLACK));
        pieces.add(new Pawn(6, 1,BLACK));
        pieces.add(new Pawn(7, 1,BLACK));
        pieces.add(new Rook(0, 0,BLACK));
        pieces.add(new Rook(7, 0,BLACK));
        pieces.add(new Knight(1, 0,BLACK));
        pieces.add(new Knight(6, 0,BLACK));
        pieces.add(new Bishop(2, 0,BLACK));
        pieces.add(new Bishop(5, 0,BLACK));
        pieces.add(new Queen(3, 0,BLACK));
        pieces.add(new King(4, 0,BLACK));
    }

    //Test
    public void testPromotion() {
        pieces.add(new Pawn(0,4,WHITE));
        pieces.add(new Pawn(5,6,BLACK));
    }

    public void testIllegal(){
        pieces.add(new Queen(2,2,WHITE));
        pieces.add(new King(3,4,WHITE));
        pieces.add(new King(0,3,BLACK));
        pieces.add(new Bishop(0,4,BLACK));
        pieces.add(new Queen(4,4,BLACK));

    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target){
        target.clear();
        for (int i = 0; i < source.size(); i++) {
            target.add(source.get(i));
        }
    }



    @Override
    public void run() {

        //Game loop
        double drawInterval = (double) 1000000000 /FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null){

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;

            if (delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update(){

        if (promotion){
            promotion();
        }else if (!gameOver && !stalemate){
            // Mouse Button Pressed //
            if (mouse.pressed) {
                if (activeP == null) {
                    // If the activeP is Null, check if you can pick up a piece
                    for (Piece piece : simPieces) {
                        // if the mouse is on an all piece, pick it up as the activeP
                        if (piece.color == currentColor &&
                                piece.col == mouse.x / Board.SQUARE_SIZE &&
                                piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activeP = piece;
                        }
                    }
                }else {
                    // if the player is holding a piece, simulate the move
                    simulate();
                }

            }
            // Mouse Button released
            if (!mouse.pressed){

                if (activeP != null){

                    if (validSquare){
                        // Move confirmed

                        //Update the piece list in case a piece has been captured and removed during the simulation
                        copyPieces(simPieces, pieces);
                        activeP.updatePosition();
                        if (castlingP != null){
                            castlingP.updatePosition();
                        }

                        if (isKingInCheck() && isCheckMate()){
                            gameOver = true;
                        }else if (isStalemate() && !isKingInCheck()){
                            stalemate = true;
                        }
                        else { // The game is still going on
                            if (canPromote()){
                                promotion = true;
                            }else {
                                changePlayer();
                            }
                        }


                    }else {
                        //The move is not valid so reset everything
                        copyPieces(pieces, simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }
    }


    private void checkEndConditions() {
        if (getKing(true) == null) { // Opponent's King is captured
            gameOver = true;
            currentColor = (currentColor == WHITE) ? BLACK : WHITE; // Set the winner
        } else if (getKing(false) == null) { // Current player's King is captured
            gameOver = true;
            currentColor = (currentColor == WHITE) ? BLACK : WHITE; // Set the winner
        }
    }

   
    private void promotion() {

        if (mouse.pressed){
            for(Piece piece : promoPieces){
                if (piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE){
                    switch (piece.type){
                        case ROOK: simPieces.add(new Rook(activeP.col, activeP.row, currentColor)); break;
                        case KNIGHT: simPieces.add(new Knight(activeP.col, activeP.row, currentColor)); break;
                        case BISHOP: simPieces.add(new Bishop(activeP.col, activeP.row, currentColor)); break;
                        case QUEEN: simPieces.add(new Queen(activeP.col, activeP.row, currentColor)); break;
                        default: break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }

    }

    private void simulate() {

        canMove = false;
        validSquare = false;

        // Reset the piece list in every loop
        // This is basically for restoring the removed piece during the simulation
        copyPieces(pieces, simPieces);

        // Reset the castling piece's position
        if (castlingP != null){
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        // if a piece is being hold, update its position
        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        // Check if the piece is hovering over a reachable square
        if (activeP.canMove(activeP.col, activeP.row)){

            canMove = true;

            // if hitting a piece, remove it from the list
            if(activeP.hittingP != null){
                simPieces.remove(activeP.hittingP.getIndex());
            }

            checkCastling();

            if (!isIllegal(activeP) || !opponentCanCaptureKing()){
                validSquare = true;
            }
        }
    }


    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        //Board
        board.draw(g2);

        //Piece
        for(Piece p : simPieces){
            p.draw(g2);
        }

        if (activeP != null){
            if (canMove){
                if (isIllegal(activeP) || opponentCanCaptureKing()){
                    g2.setColor(Color.gray);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.7f));
                    g2.fillRect(activeP.col*Board.SQUARE_SIZE, activeP.row*Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }else {
                    g2.setColor(Color.white);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.7f));
                    g2.fillRect(activeP.col*Board.SQUARE_SIZE, activeP.row*Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }
            // Draw the active piece in the end sp it won't be hidde by the board or the colored square
            activeP.draw(g2);
        }
        //Status Messages
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 25));
        g2.setColor(Color.white);

        if (promotion){
            g2.drawString("Promote to: " , 630, 150);
            for (Piece piece : promoPieces)
                g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
        }else {
            if (currentColor == WHITE){
                g2.drawString("White's turn", 630,490);
                if (checkingP != null && checkingP.color == BLACK){
                    g2.setColor(Color.red);
                    g2.drawString("The King", 630, 530);
                    g2.drawString("is in check!", 630, 560);
                    }
            }else {
                g2.drawString("Black's turn", 630,120);
                if (checkingP != null && checkingP.color == WHITE) {
                    g2.setColor(Color.red);
                    g2.drawString("The King", 630, 50);
                    g2.drawString("is in check!", 630, 80);
                }
            }
        }

        if (gameOver){

            String s = (currentColor == WHITE) ? "White Wins":"Black Wins" ;

            // Draw semi-transparent black rectangle
            g2.setColor(new Color(0, 0, 0, 128)); // 128 is the alpha value for 50% opacity
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw text message on top of the semi-transparent background
            g2.setFont(new Font("Arial", Font.BOLD, 90));
            g2.setColor(Color.green);
            g2.drawString(s, 200, 320);
        }

        if (stalemate){
            // Draw semi-transparent black rectangle
            g2.setColor(new Color(0, 0, 0, 128)); // 128 is the alpha value for 50% opacity
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw text message on top of the semi-transparent background
            g2.setFont(new Font("Arial", Font.BOLD, 90));
            g2.setColor(Color.lightGray);
            g2.drawString("Stalemate", 200, 320);
        }
    }

    private void changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK;
            // Reset Black's two-stepped status
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
            // If playing with AI, let AI make its move
            if (playWithAI) {
                performAIMove(); // AI logic for making a move
            }
        } else {
            currentColor = WHITE;
            // Reset White's two-stepped status
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }

        checkEndConditions();
        activeP = null;
    }

    private void checkCastling(){

        if (castlingP != null){
            if (castlingP.col == 0) {
                castlingP.col += 3;
            }else if (castlingP.col == 7){
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private  boolean canPromote(){

        if (activeP.type == Type.PAWN){
            if (currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7){
                promoPieces.clear();
                promoPieces.add(new Rook(9,2,currentColor));
                promoPieces.add(new Knight(9,3,currentColor));
                promoPieces.add(new Bishop(9,4,currentColor));
                promoPieces.add(new Queen(9,5,currentColor));
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck() {
        Piece king = getKing(true); // Opponent's king
        if (king == null) {
            System.out.println("Error: King not found in simPieces. Cannot check for check.");
            gameOver = true; // Set game over to terminate gameplay
            return false; // No king to check against
        }

        if (activeP != null && activeP.canMove(king.col, king.row)) { // Ensure activeP is not null
            checkingP = activeP;
            return true;
        } else {
            checkingP = null;
        }
        return false;
    }


    private Piece getKing(boolean opponent) {
        for (Piece piece : simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != currentColor) {
                    return piece;
                }
            } else {
                if (piece.type == Type.KING && piece.color == currentColor) {
                    return piece;
                }
            }
        }
        System.out.println("Warning: No king found for " + (opponent ? "opponent" : "current player"));
        return null; // King is missing
    }
    
    private boolean isKingAlive(){
       Piece king = getKing(true);
       if(king==null)
           return false;
       return true;
    }

    private boolean isIllegal(Piece king){

        if (king.type == Type.KING){
             for (Piece piece : simPieces){
                 if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)){
                     return true;
                 }
             }
        }
        return false;
    }

    private boolean opponentCanCaptureKing() {

        Piece king = getKing(false);

        for (Piece piece : simPieces){
            if (piece.color != king.color && piece.canMove(king.col, king.row)){
                return true;
            }
        }

        return false;
    }

    private boolean isCheckMate(){

        Piece king = getKing(true);

        if (kingCanMove(king)) {
            return false;
        } else {
            // The player still had a chance
            // Check if he can block attack with his pieces

            // Check the position of the checking piece and the king in check
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if (colDiff == 0) {
                // The checking piece is attacking vertically
                if (checkingP.row < king.row) {
                    // The checking piece is above the king
                    for (int row = checkingP.row; row < king.row; row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }

                if (checkingP.row > king.row) {
                    // The checking piece is below the king
                    for (int row = checkingP.row; row > king.row; row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }

            } else if (rowDiff == 0) {
                // The checking piece is attacking horizontally
                if (checkingP.col < king.col) {
                    // The checking piece is to the left
                    for (int col = checkingP.col; col < king.row; col++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }

                if (checkingP.col > king.col) {
                    // The checking piece is to the right
                    for (int col = checkingP.col; col > king.row; col--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (colDiff == rowDiff) {
                // The checking piece is attacking diagonally
                if (checkingP.row < king.row) {
                    // The checking piece is above the king
                    if (checkingP.col < king.col) {
                        // The checking piece is in the upper left
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if (checkingP.col > king.col) {
                        // The checking piece is in the upper right
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                if (checkingP.row > king.row) {
                    // The checking piece is below the king
                    if (checkingP.col < king.col) {
                        // The checking piece is in the lower left
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if (checkingP.col > king.col) {
                        // The checking piece is in the lower right
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            } else {
                // The checking place is knight
            }
        }

        return true;

    }

    private boolean kingCanMove(Piece king){

        // Simulate if there is a square where the king can move
        if (isValidMove(king, -1, -1)) {return true;}
        if (isValidMove(king, 0, -1)) {return true;}
        if (isValidMove(king, 1, -1)) {return true;}
        if (isValidMove(king, -1, 0)) {return true;}
        if (isValidMove(king, 1, 0)) {return true;}
        if (isValidMove(king, -1, 1)) {return true;}
        if (isValidMove(king, 0, 1)) {return true;}
        if (isValidMove(king, 1, 1)) {return true;}

        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus){

        boolean isValidMove = false;

        // Update the temporary King position
        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)) {
            if (king.hittingP != null) {
                simPieces.remove(king.hittingP.getIndex());
            }

            if (isIllegal(king) == false) {
                isValidMove = true;
            }
        }

        // Reset the temporary King position
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    private boolean isStalemate(){
        int count = 0;
        // Count the number of piece
        for (Piece piece : simPieces){
            if (piece.color != currentColor){
                count++;
            }
        }
        // If only one piece (the King) is left
        if (count == 1) return !kingCanMove(getKing(true));

        return false;
    }


    private void performAIMove() {
        try {
            synchronizePieces(); // Align `simPieces` with the actual board state

            boolean hasValidMove = false;
            for (Piece piece : simPieces) {
                if (piece.color == BLACK && !piece.getAllValidMoves().isEmpty()) {
                    hasValidMove = true;
                    break;
                }
            }
            if (!hasValidMove) {
                System.out.println("No valid moves for AI! Game over.");
                gameOver = true;
                currentColor = WHITE; // Assuming White wins if AI cannot move
                return;
            }

            int depth = 3; // Depth for Minimax recursion
            Piece bestPiece = null;
            int[] bestMove = null;
            int bestScore = Integer.MIN_VALUE;

            // Iterate over all AI-controlled (BLACK) pieces
            for (Piece piece : new ArrayList<>(simPieces)) {
                if (piece.color == BLACK) {
                    ArrayList<int[]> possibleMoves = piece.getAllValidMoves();
                    for (int[] move : possibleMoves) {
                        int targetCol = move[0];
                        int targetRow = move[1];

                        ArrayList<Piece> boardCopy = deepCopyBoard(simPieces); // Deep copy
                        Piece capturedPiece = simulateMoveOnBoard(boardCopy, piece, targetCol, targetRow);
                        int score = minimax(boardCopy, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                        undoMoveOnBoard(boardCopy, piece, targetCol, targetRow, capturedPiece);

                        if (score > bestScore) {
                            bestScore = score;
                            bestPiece = piece;
                            bestMove = move;
                        }
                    }
                }
            }

            // Apply the best move to the actual game board
            if (bestPiece != null && bestMove != null) {
                int targetCol = bestMove[0];
                int targetRow = bestMove[1];

                Piece capturedPiece = getPieceAt(targetCol, targetRow, pieces); // Use actual board
                if (capturedPiece != null && capturedPiece.color != bestPiece.color) {
                    simPieces.remove(capturedPiece);
                    pieces.remove(capturedPiece);
                }

                bestPiece.preCol = bestPiece.col;
                bestPiece.preRow = bestPiece.row;
                bestPiece.col = targetCol;
                bestPiece.row = targetRow;
                bestPiece.updatePosition();

                synchronizePieces();

                System.out.println("AI move: " + bestPiece.type + " to (" + targetCol + ", " + targetRow + ")");
                changePlayer();
            } else {
                System.out.println("AI could not find a valid move!");
                gameOver = true;
                currentColor = WHITE; // Assuming White wins if AI cannot move
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred during AI move calculation.");
        }
    }


    private int minimax(ArrayList<Piece> boardState, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || gameOver) {
            return evaluateBoard(boardState); // Evaluate the current board state
        }

        if (isMaximizing) { // AI's turn (BLACK pieces)
            int maxEval = Integer.MIN_VALUE;
            for (Piece piece : new ArrayList<>(boardState)) { // Iterate over a copy of the boardState
                if (piece.color == BLACK) {
                    ArrayList<int[]> possibleMoves = piece.getAllValidMoves();
                    for (int[] move : possibleMoves) {
                        int targetCol = move[0];
                        int targetRow = move[1];

                        // Validate move bounds
                        if (targetCol < 0 || targetCol > 7 || targetRow < 0 || targetRow > 7) {
                            System.out.println("Invalid move: Out of bounds (" + targetCol + ", " + targetRow + ")");
                            continue; // Skip this move
                        }

                        // Simulate the move and evaluate it
                        ArrayList<Piece> boardCopy = deepCopyBoard(boardState);
                        Piece capturedPiece = simulateMoveOnBoard(boardCopy, piece, targetCol, targetRow);
                        int eval = minimax(boardCopy, depth - 1, alpha, beta, false);
                        undoMoveOnBoard(boardCopy, piece, targetCol, targetRow, capturedPiece);

                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) break; // Beta cutoff
                    }
                }
            }
            return maxEval;
        } else { // Player's turn (WHITE pieces)
            int minEval = Integer.MAX_VALUE;
            for (Piece piece : new ArrayList<>(boardState)) { // Iterate over a copy of the boardState
                if (piece.color == WHITE) {
                    ArrayList<int[]> possibleMoves = piece.getAllValidMoves();
                    for (int[] move : possibleMoves) {
                        int targetCol = move[0];
                        int targetRow = move[1];

                        // Validate move bounds
                        if (targetCol < 0 || targetCol > 7 || targetRow < 0 || targetRow > 7) {
                            System.out.println("Invalid move: Out of bounds (" + targetCol + ", " + targetRow + ")");
                            continue; // Skip this move
                        }

                        // Simulate the move and evaluate it
                        ArrayList<Piece> boardCopy = deepCopyBoard(boardState);
                        Piece capturedPiece = simulateMoveOnBoard(boardCopy, piece, targetCol, targetRow);
                        int eval = minimax(boardCopy, depth - 1, alpha, beta, true);
                        undoMoveOnBoard(boardCopy, piece, targetCol, targetRow, capturedPiece);

                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) break; // Alpha cutoff
                    }
                }
            }
            return minEval;
        }
    }



    private Piece simulateMoveOnBoard(ArrayList<Piece> board, Piece piece, int targetCol, int targetRow) {
        Piece capturedPiece = getPieceAt(targetCol, targetRow, board); // Get the captured piece
        if (capturedPiece != null) {
            board.remove(capturedPiece); // Remove the captured piece from the board
        }
        
        // Promotion check for AI-controlled pawns
        if (piece.type == Type.PAWN && piece.color == GamePanel.BLACK && targetRow == 7) {
            board.remove(piece); // Remove pawn
            board.add(new Queen(targetCol, targetRow, GamePanel.BLACK)); // Replace with Queen
            return capturedPiece; // Return captured piece for restoration
        }
        
        // Update the piece's position on the copied board
        piece.preCol = piece.col;
        piece.preRow = piece.row;
        piece.col = targetCol;
        piece.row = targetRow;

        return capturedPiece; // Return the captured piece for restoration
    }


    private void undoMoveOnBoard(ArrayList<Piece> board, Piece piece, int targetCol, int targetRow, Piece capturedPiece) {
        piece.col = piece.preCol; // Restore original position
        piece.row = piece.preRow;

        if (capturedPiece != null) {
            board.add(capturedPiece); // Restore the captured piece to the board
        }
    }


    private ArrayList<Piece> deepCopyBoard(ArrayList<Piece> originalBoard) {
        ArrayList<Piece> boardCopy = new ArrayList<>();
        for (Piece piece : originalBoard) {
            boardCopy.add(new Piece(piece)); // Use the copy constructor
        }
        return boardCopy;
    }
    
    private int evaluateBoard(ArrayList<Piece> boardState) {
        int score = 0;
        for (Piece piece : boardState) {
            int value = getPieceValue(piece);
            score += (piece.color == BLACK) ? value : -value; // Positive for AI, negative for opponent
        }
        return score;
    }

    private int getPieceValue(Piece piece) {
        if (piece == null || piece.type == null) return 0; // Handle null pieces
        switch (piece.type) {
            case PAWN: return 1;
            case KNIGHT: return 3;
            case BISHOP: return 3;
            case ROOK: return 5;
            case QUEEN: return 9;
            case KING: return 100;
            default: return 0;
        }
    }


    private void synchronizePieces() {
        ArrayList<Piece> newSimPieces = new ArrayList<>();
        for (Piece piece : pieces) {
            if (piece.col >= 0 && piece.row >= 0) {
                newSimPieces.add(piece); // Add valid pieces to the new list
            }
        }
        simPieces.clear();
        simPieces.addAll(newSimPieces); // Replace simPieces safely
        System.out.println("Synchronized simPieces with pieces");
    }


    private Piece getPieceAt(int targetCol, int targetRow, ArrayList<Piece> board) {
        for (Piece piece : board) { // Iterate over the provided list
            if (piece.col == targetCol && piece.row == targetRow) { // Match position
                return piece; // Return the piece found at the position
            }
        }
        return null; // Return null if no piece is found
    }
}
