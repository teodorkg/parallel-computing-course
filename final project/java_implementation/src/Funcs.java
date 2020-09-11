import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class Funcs {
    static Apfloat fallingPower(int number, int power) {
        Apfloat result = new Apfloat(1, Consts.PRECISION);
        for (int i = 0; i < power; i++) {
            result = result.multiply(new Apfloat(number - i, Consts.PRECISION));
        }
        return result;
    }

    static Apfloat calculateA1(int ind) {
        Apfloat a1 = fallingPower(6 * ind, 3 * ind)
                .divide(ApfloatMath.pow(fallingPower(ind, ind), 3));
        return ind % 2 == 0 ? a1 : a1.negate();
    }

    static Apfloat calculateA21(int ind) {
        Apfloat add = new Apfloat(Consts.A21_CONST_1, Consts.PRECISION).add(new Apfloat(Consts.A21_CONST_2, Consts.PRECISION).multiply(new Apfloat(ind, Consts.PRECISION)));
        return add;
    }

    static Apfloat calculateA22(int ind) {
        Apfloat pow = ApfloatMath.pow(new Apfloat(Consts.A22_CONST_1, Consts.PRECISION), new Apfloat(3 * (ind + 0.5), Consts.PRECISION));
        return pow;
    }
}