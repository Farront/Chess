package piece;

import main.GamePanel;
import main.Type;

public class Bishop extends Piece{
    public Bishop(int col, int row, int color) {
        super(col, row, color);

        type = Type.BISHOP;

        if (color == GamePanel.WHITE) {
            image = getImage("/res/piece/w-bishop");
        }else {
            image = getImage("/res/piece/b-bishop");
        }
    }

    public boolean canMove(int targetCol, int targetRow){

        if (isWithThinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){

            if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) {
                if (isValidSquare(targetCol,targetRow) && pieceIsOnDiagonalLine(targetCol,targetRow) == false) {
                    return true;
                }
            }
        }

        return false;
    }
}
