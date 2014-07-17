package model;

public class Kdpee {
	
	private final static double loge_2 =  0.693147181; //natural logarithm of 2
	// Calculates log2 of a number.
	// You need this function in case of using Microsoft (Microsoft does not provide log2...).
	
	private static double log2(double n){
	    return Math.log(n) / loge_2; // log(n)/log(2) is log2(n)
	}
	
	/**
	 * 
	 * @param dimrefs
	 * @param n
	 * @param d
	 * @param mins
	 * @param maxs
	 * @param zcut -----> 1.96 for 95% confidence threshold for a standard normal distribution
	 * @return
	 */
	public static double kdpee_main(double[][] dimrefs, int n, int d, double[] mins, 
			double[] maxs, double zcut){	
		int[] keys = new int[n]; // do not need key[] as parameter
		int minlev = (int) Math.ceil(0.5 * log2(n)); // Min partitioning level, one of the two stop criteria
		int i;
		double result;
		
		for(i = 0; i < n; ++i){
			keys[i] = i; // initialise keys
		}
		result = kdpee_recurse(dimrefs, n, d, mins, maxs, zcut, keys,
			false, 0, 1./n, 0, n-1, minlev);
		return result;
	}
	
	private static double kdpee_recurse(double[][] dimrefs, int n, int d, double[] mins, 
			double[] maxs, double zcut, int[] keys,
			boolean mayTerminate, int curlev, double n_rec,
			int minindex, int maxindex, int minlev
			){

		int dimno = curlev % d; // The dimension along which we're intending to split
		int thesize = 1+maxindex-minindex; // No of points in this subset
		
		// As well as returning the median, this PARTITIONS the data (keys) in-place
		double median = kdpee_hoareMedian(dimrefs[dimno], keys, minindex, maxindex);
		double zscore = 0;
		
		if(curlev == minlev){
			mayTerminate = true; // We have passed the lower termination depth
		}
		
		if(mayTerminate){
			zscore = (Math.sqrt(thesize) * (median+median-mins[dimno]-maxs[dimno]) 
					/ 
					(maxs[dimno]-mins[dimno]));
			if(zscore < 0.){
				zscore = 0. - zscore;
			}
		}
		
		if(thesize==1 || (mayTerminate && (zscore < zcut))){ // needs to be reviewed
			// allowed to terminate, and z-score doesn't disprove uniformity, 
			// so let's calc the negsurprisal!
			double frac = thesize * n_rec; // this part is weird
			double volume = maxs[0] - mins[0];
			for(int i = 1; i < d; ++i){
				volume *= maxs[i] - mins[i];
			}
			
			if(volume == 0.){
				return 0.;
			}else{
				return Math.log(volume / frac) * frac; // this part is also weird
			}
		
		}else{
			// We need to partition and recurse
			double oldextremum;
			double left,right;
			
			int newmaxindex, newminindex;
			if((thesize & 1) == 0){ // even # points
				newmaxindex = minindex + thesize/2 - 1;
				newminindex = minindex + thesize/2;
			}else{ // odd # points
				newmaxindex = maxindex - (thesize+1)/2;
				newminindex = minindex + (thesize-1)/2;
			}
			
			// Remember the outer extremum, replace with median, then recurse
			oldextremum = maxs[dimno];
			maxs[dimno] = median;
			left = kdpee_recurse(dimrefs, n, d, mins, maxs, zcut, keys,
					mayTerminate, curlev+1, n_rec,
					minindex, newmaxindex, minlev
					);
			// put the extremum back in place
			maxs[dimno] = oldextremum;
			
			// Remember the outer extremum, replace with median, then recurse
			oldextremum = mins[dimno];
			mins[dimno] = median;
			right = kdpee_recurse(dimrefs, n, d, mins, maxs, zcut, keys,
					mayTerminate, curlev+1, n_rec,
					newminindex, maxindex, minlev
					);
			// put the extremum back in place
			mins[dimno] = oldextremum;
			
			return left + right;
		}
	}
	
	// by rearranging the keys between keys[minindex] & keys[maxindex] (inclusive),
	// find the median value in that section of oneRow
	private static double kdpee_hoareMedian(double[] oneRow, int[] keys, int minindex, int maxindex){
		int num = 1 + maxindex - minindex;
	
		if((num & 1) == 1){ // odd
			return kdpee_hoareFind(oneRow, keys, minindex, maxindex, (num - 1) / 2);
		}else{ // even
			return (
				kdpee_hoareFind(oneRow, keys, minindex, maxindex, (num / 2))
					+
				kdpee_hoareFind(oneRow, keys, minindex, maxindex, (num / 2) - 1)
					) * 0.5;
		}
	}
	
	private static double kdpee_hoareFind(double[] oneRow, int[] keys, int minindex, int maxindex, int findThis){
		// a pretty hack way to pass by reference for primitive type
		int[] l = new int[1];
		int[] i = new int[1];
		int[] j = new int[1];
		int[] r = new int[1];
		
		i[0] = minindex;
		j[0] = maxindex;
		findThis += minindex; // offset so that we're actually in the desired section
		while(i[0] < j[0]){
			kdpee_hoarePartition(oneRow, keys, minindex, maxindex, i[0], j[0], oneRow[keys[findThis]], l, r);
			if(r[0] < findThis){
				// kth smallest is in right split
				i[0] = l[0];
			}
			if(findThis < l[0]){
				// kth smallest is in left split
				j[0] = r[0];
			}
		}
	
		// The desired element is now in desired position, so return its value
		return oneRow[keys[findThis]];
	}
	
	private static void kdpee_hoarePartition(double[] oneRow, int[] keys, int minindex, int maxindex, 
												int l0, int r0, double fulcrum, int[] l, int[] r){
		int tmp;
		l[0] = l0;
		r[0] = r0;
		while(l[0] <= r[0]){
			// left_scan
			while(l[0] <= maxindex && oneRow[keys[l[0]]] < fulcrum){
				++l[0];
			}
			// right_scan
			while(r[0] >= minindex && oneRow[keys[r[0]]] > fulcrum){
				--r[0];
			}
			// check and exchange (keys)
			if(l[0] <= r[0]){
				tmp = keys[l[0]];
				keys[l[0]] = keys[r[0]];
				keys[r[0]] = tmp;
				// then
				++l[0];
				--r[0];
			}
		}
	}
	
	/**
	 * A simple test case
	 * @param args
	 */
	public static void main(String[] args) {
		double[][] dimrefs = {{1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 3}, {1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 3}, {1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 3}, {1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 3}};
		int n = 11;
		int d = 4;
		double[] mins = {1, 1, 1, 1};
		double[] maxs = {5, 5, 5, 5};
		double zcut = 1.96;
		
		System.out.println(kdpee_main(dimrefs, n, d, mins, maxs, zcut));
	}
	
}
