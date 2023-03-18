# Multithreading-fractals
*  based on BlockingQueue

### Example
* java hr.fer.zemris.java.fractals.NewtonParallel --workers=2 --tracks=10
```
Welcome to Newton-Raphson iteration-based fractal viewer.
Please enter at least two roots, one root per line. Enter 'done' when done. 
Root 1> 1
Root 2> -1 + i0 
Root 3> i 
Root 4> 0 - i1 
Root 5> done 
Image of fractal will appear shortly. Thank you.
```

* The number of threads to use for paralellization is controlled with --workers=N; alternatively, shorter form can be used: -w N. If this parameter is not given, use the number of available processors on the computer 
* The number of tracks (i.e. jobs) to be used is specified with --tracks=K (or shorter: -t K). Minimal acceptable K is 1. If user specifies K which is larger than the number of rows in picture, “silently” use number of rows for number of jobs. If this parameter is not present, use 4 * numberOfAvailableProcessors jobs.
