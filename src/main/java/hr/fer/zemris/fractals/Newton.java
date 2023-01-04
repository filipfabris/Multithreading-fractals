package hr.fer.zemris.fractals;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import hr.fer.zemris.java.fractals.viewer.FractalViewer;
import hr.fer.zemris.java.fractals.viewer.IFractalProducer;
import hr.fer.zemris.java.fractals.viewer.IFractalResultObserver;
import hr.fer.zemris.math.Complex;
import hr.fer.zemris.math.ComplexPolynomial;
import hr.fer.zemris.math.ComplexRootedPolynomial;

public class Newton {
	
	//1
	//-1 +i0
	// i
	//0 -i1

	//static da ne treba izradivat objekt u mainu
	private static double CONVERGENCE_THRESHOLD = 0.001;
	
	private static double ROOT_THRESHOLD = 0.002;
	
	private static int MAX_ITERATIONS = 16*16;
	
	private static String TERMINATOR = "done";
	
	public static void main(String[] args) {
		System.out.println("Welcome to Newton-Raphson iteration-based fractal viewer.\n"
				+ "Please enter at least two roots, one root per line. Enter done when done");

		int rootsCounter = 0;
				
		List<Complex> rootsArray = new ArrayList<>();

		// Try with resources
		try (Scanner sc = new Scanner(System.in)) {
			
			while (true) {
				System.out.print("Root " + (rootsCounter + 1) + "> ");
				String input = sc.nextLine().trim();

				//Minimalno dvije točke
				//Terminacija
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
		//lista korijena
		ComplexRootedPolynomial rootedPolynomial = new ComplexRootedPolynomial(Complex.ONE, rootsArray);
		//Newton fractal
		NewtonFractalProducer producer = new NewtonFractalProducer(rootedPolynomial);
		//Prikaz
		FractalViewer.show(producer);
	}

    private static class NewtonFractalProducer implements IFractalProducer {

		ComplexRootedPolynomial rootedPolynomial;
		ComplexPolynomial polynomial;
		ComplexPolynomial derived;

		private NewtonFractalProducer(ComplexRootedPolynomial rootedPolynomial) {
			this.rootedPolynomial = rootedPolynomial;
			this.polynomial = rootedPolynomial.toComplexPolynom();
			this.derived = this.polynomial.derive();
		}

		@Override
		public void produce(double reMin, double reMax, double imMin, double imMax, int width, int height,
				long requestNo, IFractalResultObserver observer, AtomicBoolean cancel) {
			int offset = 0;
			short[] data = new short[width * height];

			//ymin to ymax
			for (int y = 0; y < height; y++) {
				
				if (cancel.get()) {
					break;					
				}
				//xmin to xmax
				for (int x = 0; x < width; x++) {
					
					//odredivanje tocke, map_to_complex_plain, FraktalSlijednoProsireno.java
					double cre = x / (width-1.0) * (reMax - reMin) + reMin;
					double cim = (height-1.0-y) / (height-1) * (imMax - imMin) + imMin;
					
					//zn = c
					Complex zn = new Complex(cre, cim);
					

					int iter = 0;
					Complex znold = new Complex();
					
					while(true) {                       
                        Complex numerator = polynomial.apply(zn);
                        Complex denominator = derived.apply(zn);
                        znold = zn;
                        Complex fraction = numerator.divide(denominator);
                        zn = zn.sub(fraction);
                        
                        double module = znold.sub(zn).module();
                        iter++;
                        
                        //Ako je modul manji od konvergencijskog trasholda stani,
                        //ili ako smo prosli max broj iteracija
                        if(module < CONVERGENCE_THRESHOLD || iter > MAX_ITERATIONS) {
                        	break;
                        }
						
					}
					int index = this.rootedPolynomial.indexOfClosestRootFor(zn, ROOT_THRESHOLD);
					data[offset++] = (short) (index + 1);
				}
			}
			observer.acceptResult(data, (short) (polynomial.order() + 1), requestNo);
		}

	}
}
