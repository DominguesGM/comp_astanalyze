package test;

class TryCatchTest{
  public static void main(String args[]){
    int b;
    int a=b=0;
    String nullPointer = null;
    try{
      a = nullPointer.length();
    } catch(NullPointerException e) {
		  a=3;
  	} catch(Exception e){
  		a=4;
  	}
  	finally{
  		a=5;
  	}
    a=6;
  }
}
