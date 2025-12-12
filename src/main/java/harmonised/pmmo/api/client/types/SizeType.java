package harmonised.pmmo.api.client.types;

public record SizeType(Type type, double value) {
    public enum Type {
        /**Size will be exactly the value defined*/
        ABSOLUTE,
        /**Size will be a percent of the parent dimension*/
        PERCENT,
        /**Size is defined by the child through internal mechanisms.*/
        INTERNAL}

    /**Helper method for obtaining an {@link Type.ABSOLUTE} {@link SizeType}
     *
     * @param value the constant value to apply to the sizing function
     * @return a new {@link SizeType}
     */
    public static SizeType absolute(double value) {return new SizeType(Type.ABSOLUTE, value);}

    /**Helper method for obtaining an {@link Type.PERCENT} {@link SizeType}
     *
     * @param value a multiplier on the parent dimension value to define the child dimension
     * @return a new {@link SizeType}
     */
    public static SizeType percent(double value) {return new SizeType(Type.PERCENT, value);}

    /**Helper variable for obtaining an {@link Type.INTERNAL} {@link SizeType}.  Since no value is used
     * a helper method taking a value is not necessary.*/
    public static final SizeType INTERNAL = new SizeType(Type.INTERNAL, 0d);

    /**Helper method to translate the integer based dimensions of widgets to the double-based values
     * used to calculate size constraints.
     *
     * @param parent the dimension of the parent
     * @param child the dimension of the child
     * @return the constrained dimension
     */
    public int get(int parent, int child) {
        return get((double)parent, (double)child);
    }

    /**Method to obtain the calculated dimension for this child element based on the {@link Type}
     *
     * @param parent the parent container dimension limit
     * @param child the child's self-defined dimension value
     * @return the constrained dimension value
     */
    public int get(double parent, double child) {
        return switch (type()) {
            case ABSOLUTE -> (int)value;
            case PERCENT -> (int)(parent * value);
            case INTERNAL -> (int)child;
        };
    }
}
