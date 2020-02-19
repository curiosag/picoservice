package micro.atoms;

public interface SideEffect extends Atom {

    @Override
    default boolean isSideEffect() {
        return true;
    }

}
