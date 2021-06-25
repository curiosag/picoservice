package micro.compiler.sources;

import micro.compiler.Undermine;

@Undermine
public class Functions {

    /*     private static final int cnst = 0;

        public int literalsAndConst(){
            return cnst + Constants.a + 1;
        }
      */
    public static int pong(int v) {
        return v;
    }

    @Undermine
    public static void assignments() {
        int v0;
        int v1;
        v0 = 1;
        int v2 = v0;
        if (true)
            v1 = pong(1);
        else {
            v1 = 0;
        }

    }

/*
    @Undermine
    public static int max(int a, long b, int huhu) {
        var v1 = 1;
        var v2 = a;
        var v3 = micro.compiler.sources.sub.Sub.sub(a, Math.toIntExact(b));
        var v4 = a + b;

        if (a > b)
            return huhu;
        else {
            return Math.toIntExact(b - pong(a) + pong(Math.toIntExact(a + b)));
        }
    }

*/

}
