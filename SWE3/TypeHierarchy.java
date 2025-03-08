public class TypeHierarchy {
  //varargs
  public static void main(String[] args) {

 A a = new B();   //richtig
// B b = new A();geht auch nicht 
// b.specificBMethod();  geht nicht 

 doSomethingWithAnA(a);
 doSomethingwithAnA(new A());

 B b = new B();
 doSomethingWithAnA(b);  // hier erwartet die Methode ein referez von Typ A
 doSomethingWithAnA(15);
 
}

}

static void doSomethingWithAnA(A anA){ // das A hier ist kein Klasse sondern ein Typ 
  anA.specificMethod();

}

}

class A {

}

class B extends A {

}
