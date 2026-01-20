package harmonised.pmmo.api.client.wrappers;

import harmonised.pmmo.api.client.types.PositionType;

public record PositionConstraints(
        PositionType type,
        int verticalPosition,
        int horizontalPosition
) {
    public static PositionConstraints grid(int row, int col) {return new PositionConstraints(PositionType.GRID, row, col);}
    public static PositionConstraints offset(int x, int y) {return new PositionConstraints(PositionType.OFFSET, y, x);}

    public int row() {return verticalPosition;}
    public int col() {return horizontalPosition;}
    public int xOffset() {return horizontalPosition;}
    public int yOffset() {return verticalPosition;}
}
