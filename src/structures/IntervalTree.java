package structures;

import java.util.ArrayList;

/**
 * Encapsulates an interval tree.
 * 
 * @author runb-cs112
 */
public class IntervalTree {
	
	/**
	 * The root of the interval tree
	 */
	IntervalTreeNode root;
	
	/**
	 * Constructs entire interval tree from set of input intervals. Constructing the tree
	 * means building the interval tree structure and mapping the intervals to the nodes.
	 * 
	 * @param intervals Array list of intervals for which the tree is constructed
	 */
	public IntervalTree(ArrayList<Interval> intervals) {
		
		// make a copy of intervals to use for right sorting
		ArrayList<Interval> intervalsRight = new ArrayList<Interval>(intervals.size());
		for (Interval iv : intervals) {
			intervalsRight.add(iv);
		}
		
		// rename input intervals for left sorting
		ArrayList<Interval> intervalsLeft = intervals;
		
		// sort intervals on left and right end points
		sortIntervals(intervalsLeft, 'l');
		sortIntervals(intervalsRight,'r');
		
		// get sorted list of end points without duplicates
		ArrayList<Integer> sortedEndPoints = 
							getSortedEndPoints(intervalsLeft, intervalsRight);
		
		// build the tree nodes
		root = buildTreeNodes(sortedEndPoints);
		
		// map intervals to the tree nodes
		mapIntervalsToTree(intervalsLeft, intervalsRight);
	}
	
	/**
	 * Returns the root of this interval tree.
	 * 
	 * @return Root of interval tree.
	 */
	public IntervalTreeNode getRoot() {
		return root;
	}
	
	/**
	 * Sorts a set of intervals in place, according to left or right endpoints.  
	 * At the end of the method, the parameter array list is a sorted list. 
	 * 
	 * @param intervals Array list of intervals to be sorted.
	 * @param lr If 'l', then sort is on left endpoints; if 'r', sort is on right endpoints
	 */
	public static void sortIntervals(ArrayList<Interval> intervals, char lr) {

		if (lr == 'l'){
			for (int i = 1; i < intervals.size(); i++){
				for (int j = i; j > 0; j--){
					if (intervals.get(j).leftEndPoint < intervals.get(j-1).leftEndPoint){
						Interval temp = intervals.get(j);
						intervals.set(j, intervals.get(j-1));
						intervals.set(j-1, temp);
					}
				}
			}
		}else{
			for (int i = 1; i < intervals.size(); i++){
				for (int j = i; j > 0; j--){
					if (intervals.get(j).rightEndPoint < intervals.get(j-1).rightEndPoint){
						Interval temp = intervals.get(j);
						intervals.set(j, intervals.get(j-1));
						intervals.set(j-1, temp);
					}
				}
			}
		}
		
		return;
	}
	
	/**
	 * Given a set of intervals (left sorted and right sorted), extracts the left and right end points,
	 * and returns a sorted list of the combined end points without duplicates.
	 * 
	 * @param leftSortedIntervals Array list of intervals sorted according to left endpoints
	 * @param rightSortedIntervals Array list of intervals sorted according to right endpoints
	 * @return Sorted array list of all endpoints without duplicates
	 */
	public static ArrayList<Integer> getSortedEndPoints(ArrayList<Interval> leftSortedIntervals, ArrayList<Interval> rightSortedIntervals) {
		ArrayList<Integer> sortedPoints = new ArrayList<Integer>();
		
		sortedPoints.add(leftSortedIntervals.get(0).leftEndPoint);
		
		for (int i = 1; i < leftSortedIntervals.size(); i++){
			int temp = leftSortedIntervals.get(i).leftEndPoint;
			if (sortedPoints.get(sortedPoints.size() - 1) == temp)
				continue;
			else
				sortedPoints.add(temp);
		}
		
		for (int i = 0; i < rightSortedIntervals.size(); i++){
			int temp = rightSortedIntervals.get(i).rightEndPoint;
			if (!sortedPoints.contains(temp)){
				for (int j = sortedPoints.size() - 1; j >= 0; j--){
					if (temp > sortedPoints.get(j)){
						if (j == sortedPoints.size() - 1)
							sortedPoints.add(temp);
						else
							sortedPoints.set(j+1, temp);
						break;
					}
					
					if (j == sortedPoints.size() - 1){
						sortedPoints.add(sortedPoints.get(j));
						sortedPoints.set(j, temp);
					}else{
						sortedPoints.set(j+1, sortedPoints.get(j));
						sortedPoints.set(j, temp);
					}
				}
			}
		}
		
		return sortedPoints;
	}
	
	/**
	 * Builds the interval tree structure given a sorted array list of end points
	 * without duplicates.
	 * 
	 * @param endPoints Sorted array list of end points
	 * @return Root of the tree structure
	 */
	public static IntervalTreeNode buildTreeNodes(ArrayList<Integer> endPoints) {
		
		Queue<IntervalTreeNode> treeQueue = new Queue<IntervalTreeNode>();
		
		for (int i : endPoints){
			float j = (float) i;
			IntervalTreeNode t = new IntervalTreeNode(j, j, j);
			t.leftIntervals = new ArrayList<Interval>();
			t.rightIntervals = new ArrayList<Interval>();
			treeQueue.enqueue(t);
		}
		
		int s = treeQueue.size();
		
		while (s > 0){
			
			if (s == 1)
				return treeQueue.dequeue();
		
			int temp = s;
			
			while (temp > 1){
				IntervalTreeNode T1 = treeQueue.dequeue();
				IntervalTreeNode T2 = treeQueue.dequeue();
				float v1 = T1.maxSplitValue, v2 = T2.minSplitValue, v3 = T1.minSplitValue, v4 = T2.maxSplitValue;
				IntervalTreeNode N = new IntervalTreeNode((v1+v2)/2, v3, v4);
				N.leftChild = T1;
				N.rightChild = T2;
				N.leftIntervals = new ArrayList<Interval>();
				N.rightIntervals = new ArrayList<Interval>();
				treeQueue.enqueue(N);
				temp = temp - 2;
			}
			
			if (temp == 1)
				treeQueue.enqueue(treeQueue.dequeue());
			
			s = treeQueue.size();
		}	
		return null;
	}
	
	/**
	 * Maps a set of intervals to the nodes of this interval tree. 
	 * 
	 * @param leftSortedIntervals Array list of intervals sorted according to left endpoints
	 * @param rightSortedIntervals Array list of intervals sorted according to right endpoints
	 */
	public void mapIntervalsToTree(ArrayList<Interval> leftSortedIntervals, ArrayList<Interval> rightSortedIntervals) {

		for (Interval o : leftSortedIntervals){
			findHighest(o, root).leftIntervals.add(o);
		}
		
		for (Interval o: rightSortedIntervals){
			findHighest(o, root).rightIntervals.add(o);
		}
	}
	
	/**
	 * private recursive method to find first instance of the interval
	 * intersecting in the tree
	 */
	
	private IntervalTreeNode findHighest(Interval o, IntervalTreeNode N){
		if (o.leftEndPoint <= N.splitValue && N.splitValue <= o.rightEndPoint)
			return N;
		else if (o.leftEndPoint >= N.splitValue)
			return findHighest(o, N.rightChild);
		return findHighest(o, N.leftChild);
	}
	
	/**
	 * Gets all intervals in this interval tree that intersect with a given interval.
	 * 
	 * @param q The query interval for which intersections are to be found
	 * @return Array list of all intersecting intervals; size is 0 if there are no intersections
	 */
	public ArrayList<Interval> findIntersectingIntervals(Interval q) {
		
		return findIntersectingIntervals(q, root);
	}
	
	private ArrayList<Interval> findIntersectingIntervals(Interval o, IntervalTreeNode N){
		
		ArrayList<Interval> result = new ArrayList<Interval>();
		
		if (N.leftChild == null && N.rightChild == null)
			return result;
		
		if (o.leftEndPoint <= N.splitValue && N.splitValue <= o.rightEndPoint){
			result.addAll(N.leftIntervals);
			result.addAll(findIntersectingIntervals(o, N.rightChild));
			result.addAll(findIntersectingIntervals(o, N.leftChild));
			
		}else if (N.splitValue <= o.leftEndPoint){
			int i = N.rightIntervals.size() - 1;
			while (i >= 0 && N.rightIntervals.get(i).intersects(o)){
				result.add(N.rightIntervals.get(i));
				i--;
			}
			
			result.addAll(findIntersectingIntervals(o, N.rightChild));
			
		}else if(N.splitValue >= o.rightEndPoint){
			int i = 0;
			while (i < N.leftIntervals.size() && N.leftIntervals.get(i).intersects(o)){
				result.add(N.leftIntervals.get(i));
				i++;
			}
			
			result.addAll(findIntersectingIntervals(o, N.leftChild));
		}
		
		return result;
	}

}
