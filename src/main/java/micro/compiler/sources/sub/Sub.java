package micro.compiler.sources.sub;

import micro.compiler.Undermine;

public class Sub {

    @Undermine
    public static int sub(int left, int right) {
        {
            return left - right;
        }
    }

}
