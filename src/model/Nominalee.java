package model;

import java.util.HashMap;
import java.util.Map;

/*
 * Estimate entropy for nominal data
 * Use 10 as the base number of the log operator
 * Design in generic form
 */
public class Nominalee<T> {
	private HashMap<T, Double> data; // store nominal data and its counts
	private double totalCount; // total amount of the data
	
	public Nominalee() {
		data = new HashMap<T, Double>();
		totalCount = 0.0;
	}
	
	/**
	 * Add nominal data into hashmap, automatically record its count
	 * @param content
	 */
	public void add(T content) {
		totalCount++;
		if(data.get(content) != null) {
			// same key occurs in the map, increase the count
			double currentCount = data.get(content);
			currentCount++;
			data.put(content, currentCount);
		} else {
			// first time content, count is 1
			data.put(content, 1.0);
		}
	}
	
	/**
	 * Main function to calculate nominal entropy
	 * @return
	 */
	public double nominalee_main() {
		double entropy = 0.0;
		for(Map.Entry<T, Double> entry : data.entrySet()) {
			double possibility = entry.getValue() / totalCount;
			entropy += -(possibility * Math.log10(possibility));
		}
		return entropy;
	}
	
	/**
	 * A simple test
	 * @param args
	 */
	public static void main(String[] args) {
		Nominalee<String> testData = new Nominalee<String>();
		testData.add("AA-walking");
		testData.add("AA-running");
		testData.add("BB-sitting");
		testData.add("BB-walking");
		System.out.println("1st entropy = " + testData.nominalee_main());
		
		testData.add("AA-walking");
		System.out.println("2nd entropy = " + testData.nominalee_main());
	}
}
