package hr.fer.zemris.math;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Complex {

	public static final Complex ZERO = new Complex(0, 0);
	public static final Complex ONE = new Complex(1, 0);
	public static final Complex ONE_NEG = new Complex(-1, 0);
	public static final Complex IM = new Complex(0, 1);
	public static final Complex IM_NEG = new Complex(0, -1);

	private double re;

	private double im;

	public Complex() {

	}

	public Complex(double re, double im) {
		this.re = re;
		this.im = im;
	}

	// returns module of complex number
	public double module() {
		return Math.sqrt(Math.pow(re, 2) + Math.pow(im, 2));
	}

	public double angle() {
		double angle = Math.atan2(im, re);
		return angle;
	}

	// returns this*c
	public Complex multiply(Complex c) {
		double re = this.re * c.re - this.im * c.im;
		double im = this.re * c.im + this.im * c.re;

//		this.re = re;
//		this.im = im;
//		
//		return this;
		return new Complex(re, im);
	}

	// returns this/c
	public Complex divide(Complex c) {
		double divide = c.re * c.re + c.im * c.im;

		double re = this.re * c.re + this.im * c.im;
		double im = this.im * c.re - this.re * c.im;

//		this.re = re / divide;
//		this.im = im / divide;
//		
//		return this;

		return new Complex(re / divide, im / divide);
	}

	// returns this+c
	public Complex add(Complex c) {
		double re = this.re + c.re;
		double im = this.im + c.im;

//		return this;
		return new Complex(re, im);
	}

	// returns this-c
	public Complex sub(Complex c) {
		double re = this.re - c.re;
		double im = this.im - c.im;

//		return this;
		return new Complex(re, im);
	}

	// returns -this
	public Complex negate() {
		double re = -this.re;
		double im = -this.im;

//		return this;
		return new Complex(re, im);
	}

	// returns this^n, n is non-negative integer
	public Complex power(int n) {
		double modulePowN = Math.pow(this.module(), n);
		double angle = this.angle();
		double re = modulePowN * Math.cos(n * angle);
		double im = modulePowN * Math.sin(n * angle);

//		return this;
		return new Complex(re, im);
	}

	// returns n-th root of this, n is positive integer
	public List<Complex> root(int n) {
		List<Complex> rootList = new ArrayList<>();

		double moduleRoot = Math.pow(this.module(), 1 / n);
		double angle = this.angle();

		for (int i = 0; i < n; i++) {
			double iAngle = (angle + 2 * Math.PI * i) / n;
			Complex tmp = new Complex(moduleRoot * Math.cos(iAngle), moduleRoot * Math.sin(iAngle));
			rootList.add(tmp);
		}

		return rootList;
	}

	@Override
	public String toString() {
		return String.format("%.1f+%.1fi", this.re, this.im);
	}

	public static boolean parseAndAddToList(String input, List<Complex> roots) {

		// Oba dvoje
		Pattern realImagPattern = Pattern.compile("([-+])?(\\d+\\.?\\d*)\\s*([-+])\\s*i(\\d*\\.?\\d*)");
		Matcher realImgMatcher = realImagPattern.matcher(input);

		if (realImgMatcher.find()) {
			// Mora imat match 4 grupe

			String realOperator = "";
			double real = 0;
			String imaginaryOperator = "";
			double imaginary = 1;
			String tmp;

			// Match preskocen
			int i = 1;

			// Prvi predznak
			tmp = realImgMatcher.group(i);
			if (tmp != null) {
				realOperator = tmp;
			}
			i++;

			// Real broj
			tmp = realImgMatcher.group(i);
			real = Double.parseDouble(tmp);
			i++;

			// imaginaryOperator
			tmp = realImgMatcher.group(i);
			imaginaryOperator = tmp;
			i++;

			// Imaginary broj
			tmp = realImgMatcher.group(i);
			if (tmp != null &&  tmp.length() != 0) {
				imaginary = Double.parseDouble(tmp);
			}
			i++;

			String format = realOperator + real;
			real = Double.parseDouble(format);

			format = imaginaryOperator + imaginary;
			imaginary = Double.parseDouble(format);

			Complex dot = new Complex(real, imaginary);
			roots.add(dot);
			return true;
		}

		// samo realni dio
		Pattern realPattern = Pattern.compile("^([-+])?(\\d+\\.?\\d*)\\s*$");
		Matcher realMatcher = realPattern.matcher(input);
			
		if (realMatcher.find()) {
			
			// Mora imat match 2 grupe

			String realOperator = "";
			double real = 0;
			String tmp;

			// Match preskocen
			int i = 1;

			// Prvi predznak
			tmp = realMatcher.group(i);
			if (tmp != null) {
				realOperator = tmp;
			}
			i++;

			// Real broj
			tmp = realMatcher.group(i);
			real = Double.parseDouble(tmp);
			i++;

			String format = realOperator + real;
			real = Double.parseDouble(format);

			Complex dot = new Complex(real, 0);
			roots.add(dot);
			return true;
		}

		Pattern imagPattern = Pattern.compile("^([-+])?i(\\d*\\.?\\d*)$");
		Matcher imagMatcher = imagPattern.matcher(input);

		if (imagMatcher.find()) {
			// Mora imat match 2 grupe

			String imaginaryOperator = "";
			double imaginary = 1;
			String tmp;

			// Match preskocen
			int i = 1;

			// imaginaryOperator
			tmp = imagMatcher.group(i);
			if (tmp != null) {
				imaginaryOperator = tmp;
			}
			i++;

			// Imaginary broj
			tmp = imagMatcher.group(i);
			if (tmp != null && tmp.length() != 0) {
				imaginary = Double.parseDouble(tmp);
			}
			i++;

			String format = imaginaryOperator + imaginary;
			imaginary = Double.parseDouble(format);

			Complex dot = new Complex(0, imaginary);
			roots.add(dot);
			return true;
		}

		return false;
	}

}
