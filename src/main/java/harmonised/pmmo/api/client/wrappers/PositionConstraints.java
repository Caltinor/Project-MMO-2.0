package harmonised.pmmo.api.client.wrappers;

import harmonised.pmmo.api.client.types.PositionType;

public record PositionConstraints(
        PositionType type,
        int gridRow,
        int gridColumn
) {
    public static PositionConstraints grid(int row, int col) {return new PositionConstraints(PositionType.GRID, row, col);}

    public boolean gridMatches(int row, int col) {return gridRow == row && gridColumn == col;}
}
