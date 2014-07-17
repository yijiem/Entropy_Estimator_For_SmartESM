package model;

import java.util.ArrayList;

public class TimeToCoordinate {
	
	private ArrayList<Double> x_coordinate;
	private ArrayList<Double> y_coordinate;
	private ArrayList<Double> z;
	
	/**
	 * constructor, radius and step can be configured by user
	 * @param radius
	 * @param step
	 */
	public TimeToCoordinate(double radius, double step) {
		x_coordinate = new ArrayList<Double>();
		y_coordinate = new ArrayList<Double>();
		z = new ArrayList<Double>();
		for(double i = 0.0; i < 2 * Math.PI; i = i + step) {
			double x = -radius * Math.cos(i);
			double y = radius * Math.sin(i);
			x_coordinate.add(x);
			y_coordinate.add(y);
		}
		
		double step2 = 24.00 / (double) x_coordinate.size();
		System.out.println("step2 = " + step2); // validate the value of step2
		for(double i = 0.0; i < 24; i = i + step2) {
			z.add(i);
		}
	}
	
	/**
	 * print the <x, y> coordinates for validation
	 */
	public void print() {
		for(int i = 0; i < x_coordinate.size(); i++) {
			System.out.println("<" + x_coordinate.get(i) + ", " + y_coordinate.get(i) + ">");
		}
		
		for(int i = 0; i < z.size(); i++) {
			System.out.println(z.get(i));
		}
	}
	
	/**
	 * time-convert-to-coordinate function
	 * @param currentTimeMillis
	 */
	public double[] timeToCoordinate_main(long currentTimeMillis) {
		java.util.Date time=new java.util.Date(currentTimeMillis);
		int hour = time.getHours();
		double minute = time.getMinutes();
		double minutePercentage = minute / 60.00;
		double convertedTime = hour + minutePercentage; // actual time after converted
		
		System.out.println(convertedTime);
		
		double leastDistance = 0;
		int leastDistanceIndex = 0;
		
		// match the convertedTime with the closest one in z, 
		// get the index, 
		// and derive the x and y-coordinate
		for(int i = 0; i < z.size(); i++) {
			if(i == 0) {
				leastDistance = Math.abs(convertedTime - z.get(i));
				leastDistanceIndex = i;
			} else {
				double uncheckedDistance = Math.abs(convertedTime - z.get(i));
				if(uncheckedDistance < leastDistance) {
					leastDistance = uncheckedDistance;
					leastDistanceIndex = i;
				}
			}
		}
		
		double[] coordinate = {x_coordinate.get(leastDistanceIndex), y_coordinate.get(leastDistanceIndex)};
		return coordinate;
	}
	
	/**
	 * a simple test case
	 * @param args
	 */
	public static void main(String[] args) {
		TimeToCoordinate ttc = new TimeToCoordinate(5, 0.1);
		ttc.print();
		double[] coordinate = ttc.timeToCoordinate_main(System.currentTimeMillis());
		System.out.println("<" + coordinate[0] + ", " + coordinate[1] + ">");
	}
}
