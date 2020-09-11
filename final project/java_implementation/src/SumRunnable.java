import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class SumRunnable implements Runnable {

	Apfloat returns[];
	Apfloat a1;
	Apfloat a21;
	Apfloat a22;
	int startInd;
	int endInd;
	int threadInd;
	boolean goUp;

	public SumRunnable(Apfloat a1, Apfloat a21, Apfloat a22, Apfloat returns[], int startInd, int endInd, int threadInd, boolean goUp) {

		this.returns = returns;
		this.startInd = startInd;
		this.endInd = endInd;
		this.threadInd = threadInd;
		this.goUp = goUp;
		if (goUp){
			this.a1 = a1;
			this.a21 = a21;
			this.a22 = a22;
		} else if (endInd != -1) {
			this.a1 = a1.divide(Funcs.fallingPower(6*endInd + 6, 6).negate())
					.multiply(Funcs.fallingPower(3*endInd + 3, 3).multiply(ApfloatMath.pow(new Apfloat(endInd + 1), 3)));
			this.a21 = a21.subtract(new Apfloat(Consts.A21_CONST_2, Consts.PRECISION));
			this.a22 = a22.divide(ApfloatMath.pow(new Apfloat(Consts.A22_CONST_1, Consts.PRECISION), 3));
		}
		
	}
	
	public void run() {

		Apfloat result = Apfloat.ZERO;

		int i = goUp ? startInd : endInd;
		while(startInd <= i && i <= endInd) {
			result = result.add(a1.multiply(a21.divide(a22)));
			if (goUp) {
				if (i < endInd) {
					a1 = a1.multiply(Funcs.fallingPower(6 * i + 6, 6).negate())
							.divide(Funcs.fallingPower(3 * i + 3, 3).multiply(ApfloatMath.pow(new Apfloat(i + 1, Consts.PRECISION), 3)));
					a21 = a21.add(new Apfloat(Consts.A21_CONST_2, Consts.PRECISION));
					a22 = a22.multiply(ApfloatMath.pow(new Apfloat(Consts.A22_CONST_1, Consts.PRECISION), 3));
				}
				i++;
			}
			else {
				if (startInd < i){
					a1 = a1.divide(Funcs.fallingPower(6 * (i - 1) + 6, 6).negate())
							.multiply(Funcs.fallingPower(3 * (i - 1) + 3, 3).multiply(ApfloatMath.pow(new Apfloat((i - 1) + 1, Consts.PRECISION), 3)));
					a21 = a21.subtract(new Apfloat(Consts.A21_CONST_2, Consts.PRECISION));
					a22 = a22.divide(ApfloatMath.pow(new Apfloat(Consts.A22_CONST_1, Consts.PRECISION), 3));
				}
				i--;
			}
			
		}

		returns[threadInd] = result;
	}

}
