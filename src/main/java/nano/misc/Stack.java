package nano.misc;

import nano.ingredients.Address;

public interface Stack {
    Stack copyFrom(Stack source);
    Stack push(Address a);
    Address pop();
    Address peek(int idx);
    int depth();
}
