package harmonised.pmmo.api.client.types;

import harmonised.pmmo.api.client.wrappers.PositionConstraints;

public enum PositionType {
    STATIC(),     //normal flow
    ABSOLUTE(),   //specific screen position
    GRID();

    public final PositionConstraints constraint;
    PositionType() {this(0, 0);}
    PositionType(int row, int col) {
        constraint = new PositionConstraints(this, row, col);
    }
}
