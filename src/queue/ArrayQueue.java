/**
 * @author georbeca
 *
 * Class which implements a Queue using a Linked List.
 * 
 * Note that encapsulation is used so a user can't break the structure.
 * 
 * Note that generics are used.
 * 
 * enqueue(T data), dequeue(), doubleSize(), and halfSize() are the functions
 */

package queue;

public class ArrayQueue<T>
{
    /**Class member variables**/
    private T[] a;
    private int front, back, size;
    
    /**Constructor creates front and back,
     * and size and array
     **/
    @SuppressWarnings("unchecked")
    public ArrayQueue()
    {
        //Set front back and size
        front = 0;
        back  = 0;
        size  = 0;
        //Create array
        int initialSize  = 1;
        a = (T[]) new Object[initialSize];
    }
    
    /**Enqueue function adds one at the back**/
    public void enqueue(T data)
    {
        //Double size of array if full
        if(size >= a.length)
            doubleSize();
      
        //Keep going circular
        if(back == a.length)
            back = 0;
        //Add to back
        a[back++] = data;
        
        //Only increase the size after you have put something inside
        size++;
    }
    
    /**Dequeue function removes one from the front**/
    public T dequeue()
    {
        //Half size of array if it's only one quarter full
        if( size <= (a.length/4) )
            halfSize();
        
        //Keep going circular
        if(front == a.length)
            front = 0;
        
        //Minus one from size
        size--;
        
        //Return what you dequeue
        return a[front++];
    }
    
    /**Function to double the size of the array.
     * Called from push(T data).
     **/
    @SuppressWarnings("unchecked")
    private void doubleSize()
    {
        //Create new array that's twice as big as whatever we are using
        T[] b = (T[]) new Object[a.length*2];        
        //Copy all data from old array into new one
        //Note you have to move front each time
        for(int i = 0; i < size; i++)
        {
            //Keep going circular
            if(front == a.length)
                front = 0;
            b[i] = a[front++];
        }
        
        //Make old "a" point to the new array and front to the start
        //and back to the end
        a = b;
        front = 0;
        back = size;
    }
    
    /**Function to half the size of the array.
     * Called from push(T data).
     **/
    @SuppressWarnings("unchecked")
    private void halfSize()
    {
      //Create new array that's half as big as whatever we are using
        T[] b = (T[]) new Object[a.length/2];        
        //Copy all data from old array into new one
        //Note you have to move front each time
        for(int i = 0; i < size; i++)
        {
          //Keep going circular
            if(front == a.length)
                front = 0;
            b[i] = a[front++];
        }
        
        //Make old "a" point to the new array and front to the start
        //and back to the end
        a = b;
        front = 0;
        back = size;
    }
    
    /**Function to make the array not just keep expanding when testing**/
    @SuppressWarnings("unchecked")
    public void resetArraySize()
    {
        front = 0;
        back = size;
        T[] b = (T[]) new Object[100];
        a = b;
    }
    
    /**Function to print the queue**/
    public void print()
    {
        for(int i = front; i < back; i++)
            System.out.print(a[i] + ", ");
        System.out.println();
    }
    
    /**Function to return whether queue is empty**/
    public boolean isEmpty()
    {
        if(size == 0)
            return true;
        return false;
    }
}
