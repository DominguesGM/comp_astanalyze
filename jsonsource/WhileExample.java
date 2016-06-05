package test;

public class WhileExample {
	public static void main(String args[]){
		int i = 10;
		int multiplier = 1;

		while(i > 0){
			multiplier *= i;
			i--;
		}
	}
}