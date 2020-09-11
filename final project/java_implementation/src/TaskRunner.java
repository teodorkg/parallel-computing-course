import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import java.io.*;

public class TaskRunner {

	public static void main(String[] args) {
		//testThreadBenefit(100000, 22);
		//verifyCalculations(41);

		parseArgumentsAndRun(args);
	}

	private static void parseArgumentsAndRun(String[] args) {
		boolean isQuiet = false;
		boolean isTest = false;
		String outputFileName = "";
		int iterations = 0;
		int threadCount = 0;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-p")) {
				iterations = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-t")) {
				threadCount = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-o")) {
				outputFileName = args[++i];
			} else if (args[i].equals("-q")) {
				isQuiet = true;
			} else if (args[i].equals("test")) {
				isTest = true;
			} else if (args[i].equals("-apfloatPrecision")) {
				Consts.PRECISION = Integer.parseInt(args[++i]);
			} else {
				System.out.println("Wrong arguments !!!");
			}
		}

		if (isTest) {
			outputFileName = "";
			isQuiet = false;
			for (int i = 1; i <= threadCount; i++) {
				run(isQuiet, outputFileName, iterations, i);
			}
		} else run(isQuiet, outputFileName, iterations, threadCount);
	}

	private static void run(boolean quiet, String outputFileName, int iterations, int threadCount) {
		if (!quiet) System.out.println("Iterations: " + iterations);
		if (!quiet) System.out.println("Threads: " + threadCount);
		long startTime = System.nanoTime();
		Apfloat result = calculate(iterations, threadCount);
		long endTime = System.nanoTime();
		if (!quiet) System.out.println("Time: " + ((double)(endTime - startTime)/1000000000) + " sec");

		Apfloat error = getError(result);
		if (!quiet) System.out.println("Error: " + error);

		if (outputFileName.equals("")) return;

		File file = new File(outputFileName);
		try(FileOutputStream fos = new FileOutputStream(file)) {
			String content = "1/(pi*12) = " + result + "\n" + "Error = " + error + "\n";
			fos.write(content.getBytes());
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static Apfloat calculate(int iterations, int thread_count) {
		double chunk_size = (double)iterations / thread_count;
		Apfloat returns[] = new Apfloat[thread_count];
		Thread tr[] = new Thread[thread_count];

		if (thread_count == 1) {
			// Special case for 1 thread in order not to calculate the biggest factorial

			Apfloat a1 = Funcs.calculateA1(0);
			Apfloat a21 = Funcs.calculateA21(0);
			Apfloat a22 = Funcs.calculateA22(0);

			SumRunnable r1 = new SumRunnable(a1, a21, a22, returns, 0, iterations - 1, 0, true);
			Thread t1 = new Thread(r1);
			tr[0] = t1;
			t1.start();
		} else {
			for (int i = 1; i <= thread_count; i = i + 2) {
				// Current chunk sum indexes
				int sumStartInd = (int) ((i - 1) * chunk_size);
				int sumMiddleInd = (int) (i * chunk_size);
				int sumEndInd = (int) ((i + 1) * chunk_size) - 1;

				Apfloat a1 = Funcs.calculateA1(sumMiddleInd);
				Apfloat a21 = Funcs.calculateA21(sumMiddleInd);
				Apfloat a22 = Funcs.calculateA22(sumMiddleInd);


				// Thread i - 1
				SumRunnable r1 = new SumRunnable(a1, a21, a22, returns, sumStartInd, sumMiddleInd - 1, i - 1, false);
				Thread t1 = new Thread(r1);
				tr[i - 1] = t1;
				t1.start();

				// Thread i
				if (i != thread_count) {
					SumRunnable r2 = new SumRunnable(a1, a21, a22, returns, sumMiddleInd, sumEndInd, i, true);
					Thread t2 = new Thread(r2);
					tr[i] = t2;
					t2.start();
				}
			}
		}
		
		Apfloat sum = Apfloat.ZERO;
		for(int i = 0; i < thread_count; i++) {
			try {
				tr[i].join();
				sum = sum.add(returns[i]);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return sum;
	}

	private static Apfloat calculateDummy(int sumEnd) {
		long startTime = System.nanoTime();
		Apfloat c = Apfloat.ZERO;
		for (int i = 0; i < sumEnd; i++) {
			c = c.add(Funcs.calculateA1(i).multiply(Funcs.calculateA21(i)).divide(Funcs.calculateA22(i)));
		}
		long endTime = System.nanoTime();
		System.out.println("Dummy");
		System.out.println("Time: " + ((double)(endTime - startTime)/1000000000) + " sec");
		return c;
	}

	private static void verifyCalculations(int sumEnd) {
		System.out.println("Sum end index: " + sumEnd);
		Apfloat a = calculate(sumEnd, 1);
		Apfloat b = calculate(sumEnd, 6);

		Apfloat c = calculateDummy(sumEnd);

		Apfloat realPi = Apfloat.ONE.divide(ApfloatMath.pi(Consts.PRECISION).multiply(new Apfloat(12)));
		System.out.println(ApfloatMath.abs(a.subtract(realPi).precision(20)));
		System.out.println(ApfloatMath.abs(b.subtract(realPi).precision(20)));
		System.out.println(ApfloatMath.abs(c.subtract(realPi).precision(20)));
	}

	private static Apfloat getError(Apfloat result) {
		Apfloat realOneOverPi = Apfloat.ONE.divide(ApfloatMath.pi(Consts.PRECISION).multiply(new Apfloat(12)));
		return ApfloatMath.abs(result.subtract(realOneOverPi).precision(20));
	}
}
