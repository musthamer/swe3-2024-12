
public class neu{
  public static int gemeinsam;
  public static void main (String...args){
    Thread t = new MyThread();
    t.run();
    System.out.println("nach start");

  }
}
/*
 System.out.println("nach Start");
t.start(); 
  while(true){
    try{
      Thread.sleep(900);
      System.out.println("moin");
    }catch (Exception e) {}
   */

class MyThread extends Thread {
//man darf kein public benutzen wenn er zwei klassen in demselben klasse hat
  public void run(){
  while(true){
    try{
      Thread.sleep(1000);
    }catch (Exception e) {}
    System.out.println("running" + neu.gemeinsam );
// 
}

}
}










