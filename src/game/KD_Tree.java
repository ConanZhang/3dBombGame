package game;

import game.Game.Bomb;
import game.Game.Target;
import java.util.*;

/**
 * @author conanz and georbeca
 *
 * KD Tree class that creates a tree based on x and y coordinates. The root will begin by x coordinate, then the children will be by y coordinates, then x coordinate, ext...
 * 
 * It takes in an ArrayList then implements:
 * 
 *      1.buildTreeX
 *      2.buildTreeY
 *      3.buildTreeZ
 *      
 *      To build the tree.
 * 
 * An outer class can then call on:
 * 
 *      1. neighbors
 * 
 *      Which will return the neighbors of the current Target in a radius the user specifies.
 * 
 * Miscellaneous:
 * 
 * Debugging Tools:
 *      
 *      1. iterativeLevelOrderTraversal
 *      2. preOrder Traversal
 * 
 *      To see the structure of the tree
 * 
 * Comparators:
 * 
 *      1. XComparator
 *      2. YComparator
 *      3. ZComparator
 * 
 *      For sorting the array list passed into the KD Tree
 * 
 * Inner Class:
 * 
 *      1. TreeNode
 *         
 *      To hold Targets   
 * 
 */
public class KD_Tree
{ 
    /**Class Member Variables**/
    //arrays to sort points by
    Target[] TargetsSortedByX;
    Target[] TargetsSortedByY;
    Target[] TargetsSortedByZ;
    
    TreeNode root;
    
    /**KD TREE CONSTRUCTOR**/
    public KD_Tree(ArrayList<Target> points){
        
        //initiate class member variables
        TargetsSortedByX = new Target[points.size()];
        TargetsSortedByY = new Target[points.size()];
        TargetsSortedByZ = new Target[points.size()];
        
        //copy points into x and y arrays
        for(int i = 0; i < points.size(); i++)
        {
            TargetsSortedByX[i] = points.get(i);
            TargetsSortedByY[i] = points.get(i);
            TargetsSortedByZ[i] = points.get(i);
        }
        
        //sort arrays
        Arrays.sort(TargetsSortedByX, new XComparator());
        Arrays.sort(TargetsSortedByY, new YComparator());
        Arrays.sort(TargetsSortedByZ, new ZComparator());
        
        root = buildTreeX(TargetsSortedByX, TargetsSortedByY, TargetsSortedByZ, 0, points.size() );
    }
    
    /**FUNCTION TO CREATE TREE BASED ON X COORDINATES**/
    TreeNode buildTreeX(Target[] TargetsSortedByX1, Target[] TargetsSortedByY1, Target[] TargetsSortedByZ1, int start, int end){
        /**BASE CASE**/
        if(start >= end){
            return null;
        }
        
        //find median
        int median = start+ (end-start)/2;
        
        //root of tree sorted by X
        TreeNode n = new TreeNode(TargetsSortedByX1[median], null, null,0);

        //rework TargetsSortedByY because the Y median will be different than the X median
        Target[] temp = new Target[end-start];
        
        //start at beginning of temp
        int j = 0;
        
        //fill in temp with values less than the x median
        for(int i = start; i < end; i++){
            if(TargetsSortedByY1[i].posx <= TargetsSortedByX1[median].posx){
                //check to see if the median of the y values is the same as the x values
                if (TargetsSortedByY1[i] == TargetsSortedByX1[median]) {
                    continue;
                }
                temp[j++] = TargetsSortedByY1[i];
            }
        }
        
        //fill in temp with values greater than x median
        for(int k = start; k < end; k++){
            if(TargetsSortedByY1[k].posx > TargetsSortedByX1[median].posx) {
                temp[j++] = TargetsSortedByY1[k];
            }
        }
        
        //copy temp into array sorted by y coordinate
        for (int l=start; l<end; l++) {
            //values less than median
            if (l < median) {
                TargetsSortedByY1[l] = temp[l-start];
            } 
            //equal to median
            else if (l == median) {
                TargetsSortedByY1[l] = TargetsSortedByX1[median];
            }
            //greater than median
            else if (l > median) {
                TargetsSortedByY1[l] = temp[l-start-1];
            }
        }
        
        //recursive call to switch nodes to hold y coordinates
        n.left = buildTreeY(TargetsSortedByX1, TargetsSortedByY1, TargetsSortedByZ1, start, median);
        n.right = buildTreeY(TargetsSortedByX1, TargetsSortedByY1, TargetsSortedByZ1, median+1, end);
        
        return n;
    }


    /**FUNCTION TO CREATE TREE BASED ON Y COORDINATES**/
    TreeNode buildTreeY(Target[] TargetsSortedByX1, Target[] TargetsSortedByY1, Target[] TargetsSortedByZ1, int start, int end){
        /**BASE CASE**/
        if(start >= end){
            return null;
        }
        
        //find median
        int median = start+ (end-start)/2;
        
        TreeNode n = new TreeNode(TargetsSortedByY1[median], null, null,1);

        //rework TargetsSortedByZ because the Z median will be different than the Y median
        Target[]temp = new Target[end-start];
        
        //start at beginning of temp
        int j = 0;
        
        //fill in temp with values less than the y median
        for(int i = start; i < end; i++){
            if(TargetsSortedByZ1[i].posy <= TargetsSortedByY1[median].posy){
                //check to see if the median of the y values is the same as the x values
                if (TargetsSortedByZ1[i] == TargetsSortedByY1[median]) {
                    continue;
                }
                temp[j++] = TargetsSortedByZ1[i];
            }
        }
        
        //fill in temp with values greater than y median
        for(int k = start; k < end; k++){
            if(TargetsSortedByZ1[k].posy > TargetsSortedByY1[median].posy) {
                temp[j++] = TargetsSortedByZ1[k];
            }
        }
        
      //copy temp into array sorted by x coordinate
        for (int l=start; l<end; l++) {
            //values less than median
            if (l < median) {
                TargetsSortedByZ1[l] = temp[l-start];
            } 
            //equal to median
            else if (l == median) {
                TargetsSortedByZ1[l] = TargetsSortedByY1[median];
            }
            //greater than median
            else if (l > median) {
                TargetsSortedByZ1[l] = temp[l-start-1];
            }
        }
        
        //recursive call to switch nodes to hold x coordinates
        n.left = buildTreeZ(TargetsSortedByX1, TargetsSortedByY1 ,  TargetsSortedByZ1, start, median);
        n.right = buildTreeZ(TargetsSortedByX1, TargetsSortedByY1, TargetsSortedByZ1, median+1, end);
        
        return n;
    }
    
    /**FUNCTION TO CREATE TREE BASED ON Z COORDINATES**/
    TreeNode buildTreeZ(Target[] TargetsSortedByX1, Target[] TargetsSortedByY1, Target[] TargetsSortedByZ1, int start, int end){
        /**BASE CASE**/
        if(start >= end){
            return null;
        }
        
        //find median
        int median = start+ (end-start)/2;
        
        TreeNode n = new TreeNode(TargetsSortedByZ1[median], null, null,2);

        //rework TargetsSortedByZ because the Z median will be different than the Y median
        Target[]temp = new Target[end-start];
        
        //start at beginning of temp
        int j = 0;
        
        //fill in temp with values less than the y median
        for(int i = start; i < end; i++){
            if(TargetsSortedByX1[i].posz <= TargetsSortedByZ1[median].posz){
                //check to see if the median of the y values is the same as the x values
                if (TargetsSortedByX1[i] == TargetsSortedByZ1[median]) {
                    continue;
                }
                temp[j++] = TargetsSortedByX1[i];
            }
        }
        
        //fill in temp with values greater than y median
        for(int k = start; k < end; k++){
            if(TargetsSortedByX1[k].posz > TargetsSortedByZ1[median].posz) {
                temp[j++] = TargetsSortedByX1[k];
            }
        }
        
      //copy temp into array sorted by x coordinate
        for (int l=start; l<end; l++) {
            //values less than median
            if (l < median) {
                TargetsSortedByX1[l] = temp[l-start];
            } 
            //equal to median
            else if (l == median) {
                TargetsSortedByX1[l] = TargetsSortedByZ1[median];
            }
            //greater than median
            else if (l > median) {
                TargetsSortedByX1[l] = temp[l-start-1];
            }
        }
        
        //recursive call to switch nodes to hold x coordinates
        n.left = buildTreeX(TargetsSortedByX1, TargetsSortedByY1 ,  TargetsSortedByZ1, start, median);
        n.right = buildTreeX(TargetsSortedByX1, TargetsSortedByY1, TargetsSortedByZ1, median+1, end);
        
        return n;
    }
    
    /**DRIVER FUNCTION NEIGHBORS**/
    public LinkedList<Target> neighbors(Bomb point, double squareRadius){
        return neighbors(point, squareRadius, root);
    }
    
    /**FUNCTION TO RETURN NEIGHBORS OF GIVEN POINT BY SEARCHING THROUGH KD TREE**/
    /**Avoid square root with SQUARE RADIUS whenever possible. It's really expensive.**/
    public LinkedList<Target> neighbors(Bomb point, double squareRadius, TreeNode n){
        LinkedList<Target> neighbors = new LinkedList<Target>();
        
        //fallen off list
        if(n==null)return neighbors;
        
        //distance formula without square root
        double x = (point.posx - n.point.posx)*(point.posx - n.point.posx);
        double y = (point.posy - n.point.posy)*(point.posy - n.point.posy);
        double z = (point.posz - n.point.posz)*(point.posz - n.point.posz);
        double a =  x + y + z;
        
        //case 1 where distance is less than the radius given
        if( a < squareRadius){
            neighbors.add(n.point);
            
            //descend to both children because previous point is in radius
            neighbors.addAll(neighbors(point, squareRadius, n.left));
            neighbors.addAll(neighbors(point, squareRadius, n.right));

            return neighbors;
        }
        
        //case 2 where nodes aren't in radius
        if(n.type == 0 && x<squareRadius || (n.type == 1 && y < squareRadius) || (n.type == 2 && z < squareRadius)) {
            
            //left or right
            if( (n.type == 0 && point.posx <n.point.posx) || (n.type == 1 && point.posy < n.point.posy || (n.type == 2 && point.posz < n.point.posz) ) ) {
                //left
                neighbors.addAll(neighbors(point, squareRadius, n.left));
            }
            else {
                //right
                neighbors.addAll(neighbors(point, squareRadius, n.right));
            }
            return neighbors;
        }
        
        return neighbors;
    }
    
    /**DEBUGGING TOOLS**/
    /**ITERATIVE FUNCTION TO TRAVERSE BREADTH FIRST**/
    public void iterativeLevelOrder()
    {
        System.out.println("Iterative Level-Order");
        
        //create array queue
        queue.ArrayQueue<TreeNode> arrayQueue = new queue.ArrayQueue<TreeNode>();
        
        //start at root
        TreeNode n = root;
        arrayQueue.enqueue(n);
        
        while(!arrayQueue.isEmpty())
        {
            //remove and print out data
            n = arrayQueue.dequeue();
            System.out.println("X:" + n.point.posx + " " + "Y:" + n.point.posy + " " + "Z:" + n.point.posz);
            
            if(n != null)
            {
                //traverse left sub tree
                if(n.left!=null)
                {
                    arrayQueue.enqueue(n.left);
                }
                //traverse right sub tree
                if(n.right!=null)
                {
                    arrayQueue.enqueue(n.right);
                }
            }
        }
        System.out.println();

    }
    
    /**DRIVER METHOD FOR PREORDER TRAVERSAL**/
    public void preOrder()
    {
        System.out.println("Pre-Order");
        preOrder(root);
        System.out.println();
    }
    
    /**FUNCTION TO TRAVERSE BY ROOT, LEFT, RIGHT**/
    private void preOrder(TreeNode n)
    {
        if(n != null)
        {
            System.out.print("X:" + n.point.posx + " " + "Y:" + n.point.posy + " " + "Z:" + n.point.posz);
            preOrder(n.left);
            preOrder(n.right);
        }
    }
    
    /**KD TREE NODE THAT TAKES IN A POINT**/
    private class TreeNode{
        /**Class Member Variables**/
        private Target point;
        private TreeNode left;
        private TreeNode right;
        private int type;

        /**CONSTRUCTOR**/
        public TreeNode(Target pointParameter, TreeNode leftParameter, TreeNode rightParameter, int typeParameter){
            point = pointParameter;
            left = leftParameter;
            right = rightParameter;
            type = typeParameter;
        }
        
    }
    
    /**COMPARATOR TO COMPARE X VALUES**/
    public class XComparator implements Comparator<Target>{
        public int compare(Target p1, Target p2) {
            if(p1.posx < p2.posx) return -1;//p1's x value is less
            if(p2.posx < p1.posx) return 1;//p2's x value is less
            return 0;//same x values
        }
    }
    
    /**COMPARATOR TO COMPARE Y VALUES**/
    public class YComparator implements Comparator<Target>{
        public int compare(Target p1, Target p2) {
            if(p1.posy < p2.posy) return -1;//p1's y value is less
            if(p2.posy < p1.posy) return 1;//p2's y value is less
            return 0;//same y values
        }
    }
    
    /**COMPARATOR TO COMPARE Z VALUES**/
    public class ZComparator implements Comparator<Target>{
        public int compare(Target p1, Target p2) {
            if(p1.posz < p2.posz) return -1;//p1's z value is less
            if(p2.posz < p1.posz) return 1;//p2's z value is less
            return 0;//same z values
        }
    }
}
