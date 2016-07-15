
class TryCatchTest{
  public static void main(String args[]){
    int b;
    int a=b=0;
    String nullPointer = null;
    try{
      a = nullPointer.length();
    } catch(NullPointerException e) {
      b++;
    }
  }
}
