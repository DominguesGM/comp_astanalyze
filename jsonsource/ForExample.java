package test;

public class ForExample {
	public static void main(String args[]){
		int[] values = { 1, 2, 3, -1 };
		int size = values.length;


		for (int i = 0; i < size; i++) {
			values[i] = 1;
			i++;
		}
	}
}
