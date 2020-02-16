package micro;

public interface FSideEffect extends FAtom {
    @Override
    default boolean isSideEffect() {
        return true;
    }

}
