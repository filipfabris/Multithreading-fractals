package hr.fer.zemris.fractals;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.*;

import hr.fer.zemris.commons.InitComonsCli;
import hr.fer.zemris.java.fractals.viewer.FractalViewer;
import hr.fer.zemris.java.fractals.viewer.IFractalProducer;
import hr.fer.zemris.java.fractals.viewer.IFractalResultObserver;
import hr.fer.zemris.math.Complex;
import hr.fer.zemris.math.ComplexPolynomial;
import hr.fer.zemris.math.ComplexRootedPolynomial;

//1
//-1 +i0
// i
//0 -i1

// --workers=2 --tracks=10
public class NewtonParallel {

	// static da ne treba izradivat objekt u mainu
	private static double CONVERGENCE_THRESHOLD = 0.001;

	private static double ROOT_THRESHOLD = 0.002;

	private static int MAX_ITERATIONS = 16 * 16;

	private static String TERMINATOR = "done";

	// main metoda
	public static void main(String[] args) {

		Options options = InitComonsCli.init();

		CommandLine cmd;
		CommandLineParser parser = new DefaultParser();
		HelpFormatter helper = new HelpFormatter();
		
		int noWorkers = 4;
		int noTracks = 16;

		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("w")) {
				String opt_config = cmd.getOptionValue("w");
				System.out.println("Number of worker threads: " + opt_config);
			
				noWorkers = Integer.parseInt(opt_config);
			}

			if (cmd.hasOption("t")) {
				String opt_config = cmd.getOptionValue("t");
				System.out.println("Number of tracks: " + opt_config);
				
				noTracks = Integer.parseInt(opt_config);
			}

		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("Input error");
			helper.printHelp("Usage:", options);
			System.exit(0);
		}catch(NumberFormatException e) {
			System.out.println("Please enter integers for worker and tracks options");
		}

		int rootsCounter = 0;

		List<Complex> rootsArray = new ArrayList<>();

		System.out.println("Welcome to Newton-Raphson iteration-based fractal viewer.\n"
				+ "Please enter at least two roots, one root per line. Enter done when done");

		// Try with resources
		try (Scanner sc = new Scanner(System.in)) {

			while (true) {
				System.out.print("Root " + (rootsCounter + 1) + "> ");
				String input = sc.nextLine().trim();

				// Minimalno dvije točke
				// Terminacija
				if (input.equalsIgnoreCase(TERMINATOR)) {

					if (rootsCounter < 2) {
						System.out.println("Please enter at least two roots");
						continue;
					} else {
						System.out.println("Image of fractal will appear shortly. Thank you");
						break;
					}
				}

				if (input.length() == 0) {
					System.out.println("Please enter complex root properly");
					continue;
				}

				try {
					if (Complex.parseAndAddToList(input, rootsArray) == false) {
						System.out.println("Invalid complex root syntax");
						continue;
					}
				} catch (NullPointerException ignore) {
					System.err.println("Error has appeared druring adding complex root to array");
					continue;
				}

				rootsCounter++;
			}
		}

		// Uspjesan unos
		// lista korijena
		ComplexRootedPolynomial rootedPolynomial = new ComplexRootedPolynomial(Complex.ONE, rootsArray);

		// Newton fractal
		NewtonFractalProducerParallel producer = new NewtonFractalProducerParallel(rootedPolynomial, noWorkers, noTracks);

		// Prikaz
		FractalViewer.show(producer);
	}

	// Invoker pararelizacije
	private static class NewtonFractalProducerParallel implements IFractalProducer {

		ComplexRootedPolynomial rootedPolynomial;

		int noTracks;
		int noWorkers;
		ComplexPolynomial polynomial;

		private NewtonFractalProducerParallel(ComplexRootedPolynomial rootedPolynomial, int noWorkers, int noTracks) {
			this.rootedPolynomial = rootedPolynomial;
			this.polynomial = rootedPolynomial.toComplexPolynom();
			this.noTracks = noTracks;
			this.noWorkers = noWorkers;
		}

		@Override
		public void produce(double reMin, double reMax, double imMin, double imMax, int width, int height,
				long requestNo, IFractalResultObserver observer, AtomicBoolean cancel) {

			short[] data = new short[width * height];
			int brojYPoTraci = height / noTracks;

			final BlockingQueue<PosaoIzracuna> queue = new LinkedBlockingQueue<>();
			Thread[] radnici = new Thread[noWorkers];

			for (int i = 0; i < radnici.length; i++) {
				radnici[i] = new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							PosaoIzracuna p = null;
							try {
								p = queue.take();
								if (p == PosaoIzracuna.NO_JOB)
									break;
							} catch (InterruptedException ignorable) {
								continue;
							}
							p.run();
						}
					}
				});
			}
			for (int i = 0; i < radnici.length; i++) {
				radnici[i].start();
			}

			for (int i = 0; i < noTracks; i++) {
				int yMin = i * brojYPoTraci;
				int yMax = (i + 1) * brojYPoTraci - 1;
				if (i == noTracks - 1) {
					yMax = height - 1;
				}
				PosaoIzracuna posao = new PosaoIzracuna(reMin, reMax, imMin, imMax, width, height, yMin, yMax,
						MAX_ITERATIONS, data, cancel, this.rootedPolynomial);
				while (true) {
					try {
						queue.put(posao);
						break;
					} catch (InterruptedException ignorable) {
					}
				}
			}
			for (int i = 0; i < radnici.length; i++) {
				while (true) {
					try {
						queue.put(PosaoIzracuna.NO_JOB);
						break;
					} catch (InterruptedException ignorable) {
					}
				}
			}

			for (int i = 0; i < radnici.length; i++) {
				while (true) {
					try {
						radnici[i].join();
						break;
					} catch (InterruptedException ignorable) {
					}
				}
			}

			System.out.println("Racunanje gotovo. Idem obavijestiti promatraca tj. GUI!");
			observer.acceptResult(data, (short) (polynomial.order() + 1), requestNo);
		}

	}

	// Pararelizacija posla
	public static class PosaoIzracuna implements Runnable {
		double reMin;
		double reMax;
		double imMin;
		double imMax;
		int width;
		int height;
		int yMin;
		int yMax;
		int m;
		short[] data;
		AtomicBoolean cancel;
		public static PosaoIzracuna NO_JOB = new PosaoIzracuna();

		ComplexRootedPolynomial rootedPolynomial;
		ComplexPolynomial polynomial;
		ComplexPolynomial derived;

		private PosaoIzracuna() {
		}

		public PosaoIzracuna(double reMin, double reMax, double imMin, double imMax, int width, int height, int yMin,
				int yMax, int m, short[] data, AtomicBoolean cancel, ComplexRootedPolynomial rootedPolynomial) {
			super();
			this.reMin = reMin;
			this.reMax = reMax;
			this.imMin = imMin;
			this.imMax = imMax;
			this.width = width;
			this.height = height;
			this.yMin = yMin;
			this.yMax = yMax;
			this.m = m;
			this.data = data;
			this.cancel = cancel;
			this.rootedPolynomial = rootedPolynomial;
			this.polynomial = rootedPolynomial.toComplexPolynom();
			this.derived = this.polynomial.derive();
		}

		@Override
		public void run() {

			int offset = this.yMin * this.width; // public class Mandelbrot

			// ymin to ymax
			for (int y = this.yMin; y <= this.yMax && !cancel.get(); ++y) {

				if (cancel.get()) {
					break;
				}
				// xmin to xmax
				for (int x = 0; x < width; x++) {

					// odredivanje tocke, map_to_complex_plain, FraktalSlijednoProsireno.java
					double cre = (double) x / ((double) width - 1.0) * (reMax - reMin) + reMin;
					double cim = (double) (height - 1 - y) / ((double) height - 1.0) * (imMax - imMin) + imMin;

					// zn = c
					Complex zn = new Complex(cre, cim);

					int iter = 0;
					Complex znold = new Complex();

					while (true) {

						Complex numerator = polynomial.apply(zn);
						Complex denominator = derived.apply(zn);
						znold = zn;
						Complex fraction = numerator.divide(denominator);
						zn = zn.sub(fraction);

						double module = znold.sub(zn).module();
						iter++;

						// Ako je modul manji od konvergencijskog trasholda stani,
						// ili ako smo prosli max broj iteracija
						if (module < CONVERGENCE_THRESHOLD || iter > MAX_ITERATIONS) {
							break;
						}

					}
					int index = this.rootedPolynomial.indexOfClosestRootFor(zn, ROOT_THRESHOLD);
					data[offset++] = (short) (index + 1);
				}
			}
		}
	}

}
