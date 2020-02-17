package micro.atomicFunctions;

public interface SideEffect extends Atom {

    @Override
    default boolean isSideEffect() {
        return true;
    }

}
