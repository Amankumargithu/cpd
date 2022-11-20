package ntp.util;

public class BlackScholesCalculator {

    // inputs...
    private double stockPrice;
    private double strikePrice;
    private double timeToExpiration;
    private double rate;
    private double volatility;
    private boolean putFlag;

    // outputs...
    private double optionPrice;
    // the partial derivatives...
    private double delta;
    private double gamma;
    private double theta;

    private double d, d1;
    private int csndRecusionCount = 0;

    public double getOptionPrice() {
       return optionPrice;
    }

    public double getDelta() {
	   return delta;
    }

    public double getGamma() {
	   return gamma;
    }

    public double getTheta() {
	   return theta;
    }

    public BlackScholesCalculator(double stockPrice, double strikePrice,
                                  double timeToExpiration, double rate,
                                  double volatility, boolean putFlag) {
        this.stockPrice = stockPrice;
        this.strikePrice = strikePrice;
        this.timeToExpiration = timeToExpiration;
        this.rate = rate;
        this.volatility = volatility;
        this.putFlag = putFlag;

    }

    private void init() {
        d = (Math.log(stockPrice / strikePrice) +
                   (rate + (volatility * volatility) / 2D) * timeToExpiration) /
                   (volatility * Math.sqrt(timeToExpiration));
        d1 = d - volatility * Math.sqrt(timeToExpiration);
        if(putFlag) {
            d = -d;
            d1 = -d1;
        }
    }

    public void calculatePriceAndPartials() {
        price();

        int phi = putFlag ? -1 : 1;
        csndRecusionCount = 0;
        delta = csnd(d);
        gamma = (Math.exp( -(d*d)/2 ) / Math.sqrt( 2*Math.PI )) / ( stockPrice * volatility * Math.sqrt( timeToExpiration) );
        csndRecusionCount = 0;
        theta = ((-stockPrice * snd(d) * volatility) /
                 (2D * Math.sqrt(timeToExpiration)) -
                 (double)phi * rate * strikePrice * Math.exp(rate * timeToExpiration) *
                 csnd((double)phi * d1)
                )/365;

    }

    private void price() {
        init();
        csndRecusionCount = 0;
        optionPrice = stockPrice * csnd(d) - strikePrice * Math.exp(-rate * timeToExpiration) * csnd(d1);
        if(putFlag) optionPrice = -optionPrice;
    }

    private double snd(double x) {
       return Math.exp((-x * x) / 2D) / Math.sqrt(Math.PI + Math.PI);
    }

    private  double csnd(double x) {
    	try
    	{
    		csndRecusionCount ++;
    		if(csndRecusionCount >= 100)
    		{
//    			System.out.println("Recursion coun is over 100");
    			return 0;
    		}
    		double a[] = { 0.31938153000000002D, -0.356563782D, 1.781477937D, -1.8212559779999999D, 1.3302744289999999D };
    		double gamma = 0.23164190000000001D;
    		double k[] = new double[5];
    		if(x >= 0.0D) {
    			k[0] = 1.0D / (1.0D + gamma * x);
    			double s = a[0] * k[0];
    			for(short i = 1; i < 5; i++) {
    				k[i] = k[i - 1] * k[0];
    				s += a[i] * k[i];
    			}

    			return 1.0D - snd(x) * s;
    		} else {
    			return 1.0D - csnd(-x);
    		}
    	}
    	catch(Exception e){
    		System.out.println("Exception in BSC");
    	}
    	return 0;
    }

    public static double impliedVolatilityNewtonSteps(double stockPrice, double strikePrice,
                             double timeToExpiration, double rate,
                             double volatility, double optionLastPrice, boolean putFlag) {
        double d9 = 0.0D;
        double d10 = volatility;
        double d11;
        do {
            double d8 = d10 + Math.exp(-5D);

            BlackScholesCalculator bsc1 = new BlackScholesCalculator(stockPrice,
                                                                     strikePrice,
                                                                     timeToExpiration,
                                                                     rate,
                                                                     d10+d8 /* volatility */,
                                                                     putFlag);


            bsc1.price();
            double d6 = optionLastPrice - bsc1.optionPrice;

            BlackScholesCalculator bsc2 = new BlackScholesCalculator(stockPrice,
                                                                     strikePrice,
                                                                     timeToExpiration,
                                                                     rate,
                                                                     d10 /* volatility */,
                                                                     putFlag);


            bsc2.price();
            double d7 = optionLastPrice - bsc2.optionPrice;

            d10 -= d8 * (1.0D / ((d6 - 1.0D) / d7));
            d9++;
            d11 = Math.abs(d7);
        } while(d11 > 0.001D && d9 < 1000D);

        return d10;

    }

    public static Double impliedVolatilityBisections(double stockPrice, double strikePrice,
                                                     double timeToExpiration, double rate,
                                                     double volatility, double optionLastPrice, boolean putFlag) {


       BlackScholesCalculator bsc = null;
       double sigma_low=0.0001;

       bsc = new BlackScholesCalculator(stockPrice,
                                        strikePrice,
                                        timeToExpiration,
                                        rate,
                                        sigma_low /* volatility */,
                                        putFlag);

       bsc.price();

       double price = bsc.optionPrice;
       if (price>optionLastPrice) return 0.0;

       // simple binomial search for the implied volatility.
       // relies on the value of the option increasing in volatility
       double ACCURACY = 1.0e-5; // make this smaller for higher accuracy
       int MAX_ITERATIONS = 1000;
       double HIGH_VALUE = 1e10;
//       double ERROR = -1e40;
       Double ERROR = null;

       // want to bracket sigma. first find a maximum sigma by finding a sigma
       // with an estimated price higher than the actual price.
       double sigma_high=0.3;


       bsc = new BlackScholesCalculator(stockPrice,
                                        strikePrice,
                                        timeToExpiration,
                                        rate,
                                        sigma_high /* volatility */,
                                        putFlag);

       bsc.price();

       price = bsc.optionPrice;

       while (price < optionLastPrice) {

          sigma_high = 2.0 * sigma_high; // keep doubling.

          bsc = new BlackScholesCalculator(stockPrice,
                                           strikePrice,
                                           timeToExpiration,
                                           rate,
                                           sigma_high /* volatility */,
                                           putFlag);

          bsc.price();

          price = bsc.optionPrice;

          if (sigma_high>HIGH_VALUE)
          {
             // panic, something wrong.
        	  System.out.println("ERROR - " + sigma_high + " " + HIGH_VALUE);
             return ERROR;
          }
       }

       for (int i=0; i<MAX_ITERATIONS; i++) {

          double sigma = (sigma_low+sigma_high)*0.5;

          bsc = new BlackScholesCalculator(stockPrice,
                                           strikePrice,
                                           timeToExpiration,
                                           rate,
                                           sigma /* volatility */,
                                           putFlag);

          bsc.price();

          price = bsc.optionPrice;

          double test =  (price-optionLastPrice);

	  if (Math.abs(test)<ACCURACY) {
             return sigma;
          }

	  if (test < 0.0) {
             sigma_low = sigma;
          }
	  else {
             sigma_high = sigma;
          }
       }
       System.out.println("Max terations achieved");
       return ERROR;

    }
}
