public class BubbleSort{
  public static void main (String... args){
    int[] b = {1,3,9,4,2,1,5,8};
   for (int j=0 ; i < b.length ; ++J){
     for (int i=0 ; j < b.length-1 ; ++i){
      if(b[i]>b[i+1]){
        int swap = b[i] ; 
        b[i] = b[i+1];
        b[i+1] =  swap;
      }
    }
   }
   for (int i = 0 ; i < b.length ; ++i){

     System.out.print(b[i] + " ");
   }
   System.out.println();
}
}

