package test;

public class ForExample {
	public static void main(String args[]){
		int[] values = { 1, 2, 3, -1 };
		int size = values.length;

		int i = 0;

		for (;;) {
			values[i] = 1;
			i++;
		}
	}
}
