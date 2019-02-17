package com.footballresult;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MatchPlayed {

	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private Text team1 = new Text();
		private Text team2 = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String line = value.toString();
			String values[] = line.split(",");
			if(!(values[1]).equals("home_team")) {
				team1.set(values[1]);
				context.write(team1, one);
			}
			
			if(!(values[2]).equals("away_team")) {
				team2.set(values[2]);
				context.write(team2, one);
			}
		}

	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private Text result = new Text("");
		private int freq = 0;

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}

			context.write(key, new IntWritable(sum));

			/*
			 * if(sum > freq){ freq = sum; result.set(key); } // context.write(key,new
			 * IntWritable(sum)) ;
			 */
		}
	}

	/*
	 * @Override public void cleanup(Context context) throws
	 * IOException,InterruptedException{ context.write(result,new
	 * IntWritable(freq)); }
	 */

	/*
	 * public static class KeyValueSwapper extends
	 * Mapper<Text,Text,Text,IntWritable>{ Text output = new Text(""); int wordFreq
	 * = 0;
	 * 
	 * @Override public void map(Text key, Text val, Context context){
	 * context.write(key, new IntWritable(Integer.parseInt(val.toString()));
	 * if(Integer.parseInt(val.toString())>wordFreq){ output.set(key); wordFreq =
	 * Integer.parseInt(val.toString()); }
	 * 
	 * }
	 * 
	 * 
	 * }
	 * 
	 */

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "WordCount");
		job.setJarByClass(MatchPlayed.class);
		job.setMapperClass(TokenizerMapper.class);
		// job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		Path inp = new Path(args[0]);
		Path out = new Path(args[1]);

		FileInputFormat.addInputPath(job, inp);
		FileOutputFormat.setOutputPath(job, out);

		/*
		 * job.waitForCompletion(true); Job job1 = Job.getInstance(conf,
		 * "highest word"); job1.setJarByClass(HighFreqWord.class);
		 * //job1.setMapperClass(KeyValueSwapper.class);
		 * //job1.setCombinerClass(IntSumReducer.class);
		 * //job1.setReducerClass(IntSumReducer.class);
		 * job1.setOutputKeyClass(Text.class);
		 * job1.setOutputValueClass(IntWritable.class);
		 * job1.setInputFormatClass(KeyValueTextInputFormat.class);
		 * FileInputFormat.addInputPath(job1, new Path("output3"));
		 * FileOutputFormat.setOutputPath(job1, out);
		 */
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}