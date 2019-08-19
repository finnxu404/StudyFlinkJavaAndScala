package com.finnxu.timewindows;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * PackageName : com.finnxu.timewindows
 * ProjectName : StudyFlinkJavaAndScala
 * Author : finnxu
 * Date : 2019-08-19 22:40
 * Description : TODO
 */
public class JavaWindowsProcessApp {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        timeWindows(env);
        env.execute("JavaWindowsApp");
    }

    private static void timeWindows(StreamExecutionEnvironment env) {
        DataStreamSource<String> data = env.socketTextStream("localhost", 9999);
        data.flatMap(new FlatMapFunction<String, Tuple2<Integer, Integer>>() {
            @Override
            public void flatMap(String value, Collector<Tuple2<Integer, Integer>> out) throws Exception {
                String[] split = value.split(" ");
                for (String elem : split) {
                    if (elem.length() > 0) {
                        out.collect(new Tuple2<>(1, Integer.parseInt(elem)));
                    }
                }
            }
        }).keyBy(0)
                .timeWindow(Time.seconds(5))
                .process(new ProcessWindowFunction<Tuple2<Integer, Integer>, Object, Tuple, TimeWindow>() {
                    @Override
                    public void process(Tuple tuple, Context context, Iterable<Tuple2<Integer, Integer>> elements, Collector<Object> out) throws Exception {
                        System.out.println("------------------------------");
                        int count = 0;
                        for (Tuple2<Integer, Integer> element : elements) {
                            count++;
                        }
                        out.collect("Windows: " + context.window() + "Windows state: " + context.windowState() + " Count: " + count);
                    }
                })
                .setParallelism(1)
                .print();
    }
}
