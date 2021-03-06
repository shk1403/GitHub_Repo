/**
 * Date :-  02/06/16.
 * Author :- Saddam
 */
 
 	import java.util.Iterator;
	import java.util.StringTokenizer;
	import org.apache.hadoop.fs.Path;
	import org.apache.hadoop.io.IntWritable;
	import org.apache.hadoop.io.LongWritable;
	import org.apache.hadoop.io.Text;
	import org.apache.hadoop.mapred.FileInputFormat;
	import org.apache.hadoop.mapred.FileOutputFormat;
	import org.apache.hadoop.mapred.JobClient;
	import org.apache.hadoop.mapred.JobConf;
	import org.apache.hadoop.mapred.MapReduceBase;
	import org.apache.hadoop.mapred.Mapper;
	import org.apache.hadoop.mapred.OutputCollector;
	import org.apache.hadoop.mapred.Partitioner;
	import org.apache.hadoop.mapred.Reducer;
	import org.apache.hadoop.mapred.Reporter;
	import org.apache.hadoop.mapred.TextInputFormat;
	import org.apache.hadoop.mapred.TextOutputFormat;

	// Mapper

	public class WordCounMapper{

	    public static class Map extends MapReduceBase implements
	            Mapper<LongWritable, Text, Text, IntWritable> {

	        @Override
	        public void map(LongWritable key, Text value,
	                OutputCollector<Text, IntWritable> output, Reporter reporter)
	                throws IOException {

	            String line = value.toString();
	            StringTokenizer tokenizer = new StringTokenizer(line);

	            while (tokenizer.hasMoreTokens()) {
	                value.set(tokenizer.nextToken());
	                output.collect(value, new IntWritable(1));
	              
	                System.out.println("@Mapping" + value);

	            }

	        }
	    }

	// Combiner

	    public static class IntPairPartialSumCombiner
	        extends Reducer<Text,IntPair,Text,IntPair> {
	        private IntPair result = new IntPair();

	        public void reduce(Text key, Iterable<IntPair> values,
	                           Context context
	                           ) throws IOException, InterruptedException {
	            int sum = 0;
	            int total = 0;
	            for (IntPair val : values) {
	                sum += val.getFirst();
	                total += val.getSecond();
	            }
	            result.set(sum, total);
	            context.write(key, result);
	        }
	    }

	// Partitioner

	    public static class MyPartitioner implements Partitioner<Text, IntWritable> {

	        @Override
	        public int getPartition(Text key, IntWritable value, int numPartitions) {

	            String myKey = key.toString().toLowerCase();
	            //the word hadoop will go to partition 1
	            if (myKey.equals("hadoop")) {
	                return 0;
	            }//the word data will go to partition 2
	            if (myKey.equals("data")) {
	                return 1;
	            } else {//all other words will go to partition 3
	                return 2;
	            }
	        }

	        @Override
	        public void configure(JobConf arg0) {

	            // Gives you a new instance of JobConf if you want to change Job
	            // Configurations

	        }
	    }

	// Reducer

	    public static class Reduce extends MapReduceBase implements
	            Reducer<Text, IntWritable, Text, IntWritable> {

	        @Override
	        public void reduce(Text key, Iterator<IntWritable> values,
	                OutputCollector<Text, IntWritable> output, Reporter reporter)
	                throws IOException {

	            int sum = 0;
	            while (values.hasNext()) {
	                sum += values.next().get();
	                // sum = sum + 1;
	            }

	            // beer,3

	            output.collect(key, new IntWritable(sum));
	            System.out.println("Reducing" + "key:" + key + " value:" + sum);
	        }
	    }

	// Main Driver Class

	    public static void main(String[] args) throws Exception {

	        JobConf conf = new JobConf(WordCountCombiner.class);
	        conf.setJobName("wordcount");

	        // Forcing program to run 3 reducers
	        conf.setNumReduceTasks(3);
	        conf.setPartitionerClass(MyPartitioner.class);
	        conf.setMapperClass(Map.class);
	        conf.setReducerClass(Reduce.class);
	      
	        conf.setCombinerClass(Reduce.class);
	      

	        conf.setOutputKeyClass(Text.class);
	        conf.setOutputValueClass(IntWritable.class);

	        //how the data will be read
	        conf.setInputFormat(TextInputFormat.class);
	        conf.setOutputFormat(TextOutputFormat.class);

	        FileInputFormat.setInputPaths(conf, new Path(args[0]));
	        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

	        JobClient.runJob(conf);

	    }
	}
