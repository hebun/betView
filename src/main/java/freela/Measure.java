package freela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;


public class Measure {

	static long timePassed = 0;
	static long calls = 0;
	static double ret;
	static Map<String, MeasuredMethod> meths = new HashMap<String, MeasuredMethod>();
	private static boolean isMeasure = true;

	static {

	}



	public static void callAndMeasure(Callable callable, final String namep) {
		long start = System.currentTimeMillis();

		callable.run();
		if (isMeasure) {
			long end = System.currentTimeMillis();

			if (meths.containsKey(namep)) {

				MeasuredMethod value = meths.get(namep);
				value.calls++;
				value.timeTaken += (end - start);
				meths.put(namep, value);
			} else {
				meths.put(namep, new MeasuredMethod(namep, end - start, 1));

			}

			// name, long1 + (end - start));
			// }
			timePassed += (end - start);
			calls++;
		}
	}

	public static void main(String[] args) {
		isMeasure=true;

	//	dump();

	}

	private static void stringConcat() {
		StringBuilder str =new StringBuilder();
		for (int i = 0; i < 1000000; i++) {
			str.append("abc");
		}
	}

	public static void dump() {

		List<MeasuredMethod> values = new ArrayList<MeasuredMethod>(
				meths.values());
		long sum = values.stream().mapToLong(new ToLongFunction<MeasuredMethod>() {
			@Override
			public long applyAsLong(MeasuredMethod e) {
				return e.timeTaken;
			}
		}).sum();
		
		values.add(new MeasuredMethod("total", sum, 1));
		
		values.sort(new Comparator<MeasuredMethod>() {
			@Override
			public int compare(MeasuredMethod o1, MeasuredMethod o2) {

				if (o1.timeTaken > o2.timeTaken)
					if (o1.timeTaken == o2.timeTaken)
						return 0;
					else
						return -1;
				else
					return 1;
			}
		});

		new ASCIITable().printTable(values);
		for (MeasuredMethod measuredMethod : values) {

		}
		// ApiNGDemo.log.info(out);
	}

}
