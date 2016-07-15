package testpackage;

class TryCatchTest{
  static FunctionExample example;
  public static void main(String args[]){
    int b;
    int a=b=0;
    String nullPointer = null;
    b = example.subtractTwo(1, 2);
    try{
      a = nullPointer.length();
      return;
    } catch(NullPointerException e) {} catch(Exception e){}
  }
}
