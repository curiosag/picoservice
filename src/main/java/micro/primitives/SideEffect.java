package micro.primitives;

public interface SideEffect extends Primitive {

    @Override
    default boolean isSideEffect() {
        return true;
    }

}
