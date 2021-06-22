package micro.compiler.sources;

import java.util.ArrayList;
import java.util.List;

public class Functions {

    public void varDeclarationsAssignments(){
        int a = 1;
        int b = a;
        int c = a + b + 1;
        int f = Math.max(a, 1);
        a = a + 1; // illegal, reassignment
        int d;
        d = 0; // ok
        List<String> l1 = new ArrayList<>();
    }

 /*   public static int bong(int v) {
        return v;
    }


    @Churn
    public static void ifStuff() {
        {
            int v1;

            if (true)
                v1 = 1;
            else {
                v1 = 0;
            }
        }
    }

    @Churn
    public static int max(int a, long b) {
        {
            var v1 = 1;
            var v2 = a;
            var v3 = micro.compiler.sources.sub.Sub.sub(a, Math.toIntExact(b));
            var v4 = a + b;

            if (a > b)
                return a;
            else {
                return Math.toIntExact(b - bong(a) + bong(Math.toIntExact(a + b)));
            }
        }
    }
*/

}
