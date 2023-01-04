package hr.fer.zemris.math;

import java.util.Arrays;
import java.util.List;

public class ComplexRootedPolynomial {

	private Complex constant;

	private Complex[] roots;

	// constructor public
	public ComplexRootedPolynomial(Complex constant, Complex... roots) {
		this.constant = constant;
		this.roots = Arrays.copyOf(roots, roots.length);
	}
	
	public ComplexRootedPolynomial(Complex constant, List<Complex> roots) {
		this(constant, roots.stream().toArray(Complex[] ::new));
	}

	// computes polynomial value at given point z
	public Complex apply(Complex z) {
		Complex output = this.constant;

		for (Complex element : this.roots) {
			output = output.multiply(z.sub(element));
		}

		return output;
	}

	// converts this representation to ComplexPolynomial type
	public ComplexPolynomial toComplexPolynom() {
		ComplexPolynomial outputProduct = new ComplexPolynomial(this.constant);

		for (Complex element : this.roots) {
			outputProduct = outputProduct.multiply(new ComplexPolynomial(element.negate(), Complex.ONE));
		}

		return outputProduct;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("(").append(this.constant.toString()).append(")");
		
		for(Complex element: this.roots) {
			sb.append("*(z-(").append(element.toString()).append("))");
		}
		
		return sb.toString();

	}

	// finds index of closest root for given complex number z that is within
	// treshold; if there is no such root, returns -1
	// first root has index 0, second index 1, etc
	public int indexOfClosestRootFor(Complex z, double treshold) {

		// Prvi je pretpostavka min
		double minDistance = z.sub(this.roots[0]).module();
		int index = 0;

		double length = this.roots.length;

		for (int i = 0; i < length; i++) {
			double calculateDistance = z.sub(this.roots[i]).module();
			if (calculateDistance < minDistance) {
				index = i;
				minDistance = calculateDistance;
			}
		}

		if (minDistance < treshold) {
			return index;
		}

		return -1;

	}

}
