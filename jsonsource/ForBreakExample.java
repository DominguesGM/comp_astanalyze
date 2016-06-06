package test;

public class ForBreakExample {
	public static void main(String args[]){

		for (int i = 0; i < 10; i++) {
			if(i == 6) break;
			else continue;
		}

		int a = 2;
		int b = 4;

		while(b > a){
			a++;
		}
	}
}