package com.talkingdata.sdk.myna.tools;

public class Complex {

    /**
     * real part
     */
    private final double re;

    /**
     * imaginary part
     */
    private final double im;

    /**
     * Create a new object with the given real and imaginary parts
     */
    public Complex(double real, double img) {
        re = real;
        im = img;
    }

    /**
     * @return a string representation of the invoking Complex object
     */
    @Override
    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im <  0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    /**
     * Get Math.sqrt(re*re + im*im)
     * @return abs/modulus/magnitude and angle/phase/argument
     */
    public double abs()   { return Math.hypot(re, im); }

    /**
     * Get (this + b)
     * @return a new Complex object whose value is (this + b)
     */
    Complex plus(Complex b) {
        Complex a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex(real, imag);
    }

    /**
     * Get (this - b)
     * @return a new Complex object whose value is (this - b)
     */
    Complex minus(Complex b) {
        Complex a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new Complex(real, imag);
    }

    /**
     * Get (this * b)
     * @return a new object whose value is  (this * b)
     */
    Complex times(Complex b) {
        Complex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new Complex(real, imag);
    }

    /**
     * Get (this * alpha)
     * @return a new object whose value is (this * alpha)
     */
    Complex times(double alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    /**
     * Get conjugate of the context
     * @return a new Complex object whose value is the conjugate of this
     */
    Complex conjugate() {  return new Complex(re, -im); }

    /**
     * Get reciprocal of the context
     * @return a new Complex object whose value is the reciprocal of this
     */
    private Complex reciprocal() {
        double scale = re*re + im*im;
        return new Complex(re / scale, -im / scale);
    }

    /**
     * Get real part of the context
     * @return real part
     */
    private double re() { return re; }

    /**
     * Get imaginary part of the context
     * @return imaginary part
     */
    private double im() { return im; }

    /**
     * Get the result of being divided by b
     * @return a / b
     */
    private Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    /**
     * Get sine
     * @return a new Complex object whose value is the complex sine of this
     */
    private Complex sin() {
        return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    /**
     * Get cosine
     * @return a new Complex object whose value is the complex cosine of this
     */
    private Complex cos() {
        return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    /**
     * Get tangent
     * @return a new Complex object whose value is the complex tangent of this
     */
    private Complex tan() {
        return sin().divides(cos());
    }

    /**
     * sample client for testing
     * @param args testing arguments
     */
    static void main(String[] args) {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(-3.0, 4.0);

        System.out.println("a            = " + a);
        System.out.println("b            = " + b);
        System.out.println("Re(a)        = " + a.re());
        System.out.println("Im(a)        = " + a.im());
        System.out.println("b + a        = " + b.plus(a));
        System.out.println("a - b        = " + a.minus(b));
        System.out.println("a * b        = " + a.times(b));
        System.out.println("b * a        = " + b.times(a));
        System.out.println("a / b        = " + a.divides(b));
        System.out.println("(a / b) * b  = " + a.divides(b).times(b));
        System.out.println("conj(a)      = " + a.conjugate());
        System.out.println("|a|          = " + a.abs());
        System.out.println("tan(a)       = " + a.tan());
    }

}
