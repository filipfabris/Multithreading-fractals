package hr.fer.zemris.math;

import java.util.Arrays;

public class ComplexPolynomial {
	
	private Complex[] polinoms;
	
	// constructor public ComplexPolynomial(Complex ...factors) {...}
	ComplexPolynomial(Complex ... polinoms){
		this.polinoms = Arrays.copyOf(polinoms, polinoms.length);
	}
	
	
	// returns order of this polynom; eg. For (7+2i)z^3+2z^2+5z+1 returns 3 
	public short order() {
		return (short) (polinoms.length - 1);
		
	}
	
	// computes a new polynomial this*p 
	public ComplexPolynomial multiply(ComplexPolynomial p) {
		
		Complex[] outputPolinom = new Complex[this.order() + p.order() + 1];
		
		int thisLength = this.polinoms.length;
		int otherLength = p.polinoms.length;
		
        for (int i = 0; i < thisLength; i++) {
            for (int j = 0; j < otherLength; j++) {
                if (outputPolinom[i + j] == null) {
                	outputPolinom[i + j] = this.polinoms[i].multiply(p.polinoms[j]);                	
                }else {
                	outputPolinom[i + j] = outputPolinom[i + j].add(this.polinoms[i].multiply(p.polinoms[j]));
                	}
            }
        }
		
		return new ComplexPolynomial(outputPolinom);
		
	}
	
	
	// computes first derivative of this polynomial; for example, for 
	//  (7+2i)z^3+2z^2+5z+1 returns (21+6i)z^2+4z+5 
	public ComplexPolynomial derive() {
		
        Complex[] outputPolinomial = new Complex[this.polinoms.length - 1];
        int length = this.polinoms.length;
        
        for(int i = 1; i <length; i++) {
        	//Spusti dolje i pomnozi sa eksponentom (i)
        	outputPolinomial[i-1] = this.polinoms[i].multiply(new Complex(i, 0));
        }

		return new ComplexPolynomial(outputPolinomial);
		
	} 
	
	
	// computes polynomial value at given point z 
	public Complex apply(Complex z) {
		//Uvrstavamo z u polinom
        Complex value = this.polinoms[0];
        int length = this.polinoms.length;
        
        //Prvi je uzet vec
        for (int i = 1; i<length ; i++) {        	
        	//(5-4i)*x^3
        	value = value.add(z.power(i).multiply(this.polinoms[i]));
        	//vuce multiply iz ove klase
//        	value = value.add(multiply(this.polinoms[i].multiply(z.power(i))));
        }
        
        return value;
		
	} 
	
	
	@Override 
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(int i = polinoms.length - 1; i >= 0; i--) {
            sb.append("(").append(this.polinoms[i].toString()).append(")");
            if(i != 0) {
            	sb.append("*z^").append(i).append("+");
            }
        }
        
        return sb.toString();
    }

}
