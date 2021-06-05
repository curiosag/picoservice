package micro;

public interface _FlowDirections {

    default boolean isUpstream(String valName)
    {
        return Names.result.equals(valName) || Names.exception.equals(valName);
    }

    default boolean isDownstream(String valName)
    {
        return ! isUpstream(valName);
    }
}
