package dogstatsd;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;

public class stasd {

	public static void main(String[] args) throws Exception {
		System.out.println("dogstatsid");
		int sampleRate;
		
		// TODO Auto-generated method stub
		StatsDClient statsd = new NonBlockingStatsDClientBuilder()
	            .prefix("statsd")
	            .hostname("localhost")
	            .port(8125)
	            .build();
		for (int i = 0; i < 10; i++) {
           // statsd.incrementCounter("example_metric.increment", new String[]{"environment:dev"});
		//	statsd.incrementCounter("example_metric.increment", sampleRate=1);
          // statsd.decrementCounter("example_metric.decrement", new String[]{"environment:dev"});
           //  statsd.count("example_metric.count", 2, new String[]{"environment:dev"});
     //        statsd.recordGaugeValue("example_metric.gauge", i, new String[]{"environment:dev"});
       //    statsd.recordSetValue("example_metric.set", i, new String[]{"environment:dev"});

         //   statsd.recordHistogramValue("example_metric.histogram", i, new String[]{"environment:dev"});
         //   statsd.recordDistributionValue("example_metric.distribution",i,new String[]{"environment:dev"});
		statsd.recordExecutionTime("example_metric.timer",1000,new String[]{"environment:dev"});
             

            Thread.sleep(1000);
            System.out.println(i);
		}


	}

}
