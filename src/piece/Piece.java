package piece;

import main.Board;
import main.GamePanel;
import main.Type;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import static main.GamePanel.simPieces;

public class Piece {

    public Type type;
    public BufferedImage image;
    public int x, y;
    public int col, row, preCol, preRow;
    public int color;
    public Piece hittingP;
    public boolean moved, twoStepped;

    public Piece(int col, int row, int color) {
        this.col = col;
        this.row = row;
        this.color = color;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;
    }
    
    public Piece(Piece original) {
    this.type = original.type;
    this.col = original.col;
    this.row = original.row;
    this.color = original.color;
    this.x = original.x;
    this.y = original.y;
    this.preCol = original.preCol;
    this.preRow = original.preRow;
    this.hittingP = original.hittingP; // Optional: Copy hitting state if needed
    this.moved = original.moved;
    this.twoStepped = original.twoStepped;
}


    public BufferedImage getImage(String imagePath) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(
                    Piece.class.getResourceAsStream(imagePath + ".png"));
        }catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }

    public int getX(int col) {
        return col* Board.SQUARE_SIZE;
    }

    public int getY(int row) {
        return row* Board.SQUARE_SIZE;
    }

    public int getCol(int x) {return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;}

    public int getRow(int y) {return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;}

    public Piece getHittingP(int targetCol, int targetRow){
        for (Piece piece : GamePanel.simPieces){
            if (piece.col == targetCol && piece.row == targetRow && piece != this){
                return piece;
            }
        }
        return null;
    }

    public int getIndex(){
        for (int index = 0; index < GamePanel.simPieces.size(); index++) {
            if(GamePanel.simPieces.get(index) == this){
                return index;
            }
        }
        return 0;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }

    public void updatePosition() {

        //To check En Passant
        if (type == Type.PAWN){
            if (Math.abs(row - preRow) == 2){
                twoStepped = true;
            }
        }


        x = getX(col);
        y = getY(row);
        preCol = getCol(x);
        preRow = getRow(y);
        moved = true;
    }

    public boolean canMove(int targetCol,int targetRow){
        return false;
        
    }

    public boolean isWithThinBoard(int targetCol, int targetRow){
        if (targetCol >= 0 && targetRow <= 7 && targetRow >= 0 &&
        targetCol <= 7){
            return true;
        }
        return false;
    }

    public void resetPosition() {
        col = preCol;
        row = preRow;
        x = getX(col);
        y = getY(row);
    }

    public  boolean isValidSquare(int targetCol, int targetRow){
        hittingP = getHittingP(targetCol, targetRow);
        if (hittingP == null){ // This square is VACANT
            return true;
        }else { // this square is OCCUPIED
            if (hittingP.color != this.color){ // If the color is different, it can be captured
                return true;
            }else {
                hittingP = null;
            }
        }
        return false;
    }

    public boolean isSameSquare(int targetCol, int targerRow){
        if (targetCol == preCol && targerRow == preRow){
            return true;
        }
        return false;
    }

    public boolean pieceIsOnStraightLine(int targetCol, int targetRow){
        //When this piece is moving to the left
        for (int c = preCol - 1; c > targetCol ; c--) {
            for (Piece piece : GamePanel.simPieces){
                if (piece.col == c && piece.row == targetRow){
                    hittingP = piece;
                    return true;
                }
            }
        }
        //When this piece is moving to the right
        for (int c = preCol + 1; c < targetCol ; c++) {
            for (Piece piece : GamePanel.simPieces){
                if (piece.col == c && piece.row == targetRow){
                    hittingP = piece;
                    return true;
                }
            }
        }
        //When this piece is moving up
        for (int r = preRow - 1; r > targetRow ; r--) {
            for (Piece piece : GamePanel.simPieces){
                if (piece.col == targetCol && piece.row == r){
                    hittingP = piece;
                    return true;
                }
            }
        }
        //When this piece is moving down
        for (int r = preRow + 1; r < targetRow ; r++) {
            for (Piece piece : GamePanel.simPieces){
                if (piece.col == targetCol && piece.row == r){
                    hittingP = piece;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean pieceIsOnDiagonalLine(int targetCol, int targetRow) {

        if (targetRow < preRow) {
            //Up left
            for (int c = preCol - 1; c > targetCol; c--) {
                int diff = Math.abs(c - preCol);
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
            //Up right
            for (int c = preCol + 1; c < targetCol; c++) {
                int diff = Math.abs(c - preCol);
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }

        if (targetRow > preRow) {
            //Down left
            for (int c = preCol - 1; c > targetCol; c--) {
                int diff = Math.abs(c - preCol);
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
            //Down right
            for (int c = preCol + 1; c < targetCol ; c++) {
                int diff = Math.abs(c - preCol);
                for (Piece piece : GamePanel.simPieces){
                    if (piece.col == c && piece.row == preRow + diff){
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    

    
    private void handleLinearMoves(ArrayList<int[]> validMoves, int[][] directions) {
        for (int[] dir : directions) {
            int targetCol = col;
            int targetRow = row;

            while (true) {
                // Increment the target position in the given direction
                targetCol += dir[0];
                targetRow += dir[1];

                // Stop if out of bounds
                if (!isWithThinBoard(targetCol, targetRow)) break;

                // Check for pieces at the target position
                Piece piece = getPieceAt(targetCol, targetRow);

                if (piece != null) {
                    // If the piece is an opponent's, it's a valid capture
                    if (piece.color != this.color) {
                        validMoves.add(new int[]{targetCol, targetRow});
                    }
                    // Stop after encountering any piece (opponent or friendly)
                    break;
                }

                // Add the empty square as a valid move
                validMoves.add(new int[]{targetCol, targetRow});
            }
        }
    }

        
    public ArrayList<int[]> getAllValidMoves() {
            ArrayList<int[]> validMoves = new ArrayList<>();

            switch (type) {
                case PAWN:
                    handlePawnMoves(validMoves);
                    break;
                case KNIGHT:
                    handleKnightMoves(validMoves);
                    break;
                case ROOK:
                    handleRookMoves(validMoves);
                    break;
                case BISHOP:
                    handleBishopMoves(validMoves);
                    break;
                case QUEEN:
                    handleQueenMoves(validMoves);
                    break;
                case KING:
                    handleKingMoves(validMoves);
                    break;
                default:
                    break;
            }

            return validMoves;
        }

        private void handlePawnMoves(ArrayList<int[]> validMoves) {
            int direction = (color == GamePanel.WHITE) ? -1 : 1; // Direction of pawn movement based on color

            // Forward move (1 square)
            int forwardRow = row + direction;
            if (isWithThinBoard(col, forwardRow) && getPieceAt(col, forwardRow) == null) {
                validMoves.add(new int[]{col, forwardRow});
                
            if (color == GamePanel.BLACK && forwardRow == 7) {
                GamePanel.simPieces.remove(this);
                GamePanel.simPieces.add(new Queen(col, forwardRow, GamePanel.BLACK)); // Auto-promote to Queen
                return; // Stop further move generation for this pawn
            }
            
                // Double forward move (2 squares)
                if (!moved) {
                    int doubleForwardRow = row + (2 * direction);
                    if (isWithThinBoard(col, doubleForwardRow) && getPieceAt(col, doubleForwardRow) == null) {
                        validMoves.add(new int[]{col, doubleForwardRow});
                    }
                }
            }

            // Diagonal capture moves
            int leftDiagCol = col - 1;
            int rightDiagCol = col + 1;

            // Left diagonal capture
            if (isWithThinBoard(leftDiagCol, forwardRow)) {
                Piece targetPiece = getPieceAt(leftDiagCol, forwardRow);
                if (targetPiece != null && targetPiece.color != color) {
                    validMoves.add(new int[]{leftDiagCol, forwardRow});
                }
            }

            // Right diagonal capture
            if (isWithThinBoard(rightDiagCol, forwardRow)) {
                Piece targetPiece = getPieceAt(rightDiagCol, forwardRow);
                if (targetPiece != null && targetPiece.color != color) {
                    validMoves.add(new int[]{rightDiagCol, forwardRow});
                }
            }

            // En Passant
            if (row == (color == GamePanel.WHITE ? 3 : 4)) { // Check if the pawn is in the correct row for en passant
                // Left diagonal en passant
                if (isWithThinBoard(leftDiagCol, forwardRow)) {
                    Piece adjacentPiece = getPieceAt(leftDiagCol, row);
                    if (adjacentPiece != null && adjacentPiece.type == Type.PAWN &&
                        adjacentPiece.color != color && adjacentPiece.twoStepped) {
                        validMoves.add(new int[]{leftDiagCol, forwardRow});
                    }
                }

                // Right diagonal en passant
                if (isWithThinBoard(rightDiagCol, forwardRow)) {
                    Piece adjacentPiece = getPieceAt(rightDiagCol, row);
                    if (adjacentPiece != null && adjacentPiece.type == Type.PAWN &&
                        adjacentPiece.color != color && adjacentPiece.twoStepped) {
                        validMoves.add(new int[]{rightDiagCol, forwardRow});
                    }
                }
            }
        }


        private void handleKnightMoves(ArrayList<int[]> validMoves) {
            int[][] deltas = {
                {-2, -1}, {-1, -2}, {1, -2}, {2, -1},
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1}
            };

            for (int[] delta : deltas) {
                int targetCol = col + delta[0];
                int targetRow = row + delta[1];
                if (isWithThinBoard(targetCol, targetRow) && isValidSquare(targetCol, targetRow)) {
                    validMoves.add(new int[]{targetCol, targetRow});
                }
            }
        }

        private void handleRookMoves(ArrayList<int[]> validMoves) {
            // Horizontal and vertical moves
            handleLinearMoves(validMoves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
        }

        private void handleBishopMoves(ArrayList<int[]> validMoves) {
            // Diagonal moves
            handleLinearMoves(validMoves, new int[][]{{1, 1}, {-1, 1}, {1, -1}, {-1, -1}});
        }

        private void handleQueenMoves(ArrayList<int[]> validMoves) {
            // Queen combines Rook and Bishop moves
            handleLinearMoves(validMoves, new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, 1}, {1, -1}, {-1, -1}
            });
        }

        private void handleKingMoves(ArrayList<int[]> validMoves) {
            int[][] deltas = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
            };

            for (int[] delta : deltas) {
                int targetCol = col + delta[0];
                int targetRow = row + delta[1];
                if (isWithThinBoard(targetCol, targetRow) && isValidSquare(targetCol, targetRow)) {
                    validMoves.add(new int[]{targetCol, targetRow});
                }
            }
        }
        
            // Supporting Methods
    public Piece getPieceAt(int targetCol, int targetRow) {
        for (Piece piece : simPieces) {
            if (piece.col == targetCol && piece.row == targetRow) {
                return piece; // Return the piece at the target position
            }
        }
        return null; // No piece found at the specified position
    }
}
