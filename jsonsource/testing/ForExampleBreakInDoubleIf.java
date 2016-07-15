package testpackage;

public class ForExampleBreakInDoubleIf {
	public static void main(String args[]){
		int[] values = { 1, 2, 3, -1 };
		int size = values.length;


		for (int i = 0; i < size ; i++) {
			values[i] = 1;
			if(i == 1){
				if(size == 4)
					break;
				i++;
			}
			i++;
		}
	}
}
