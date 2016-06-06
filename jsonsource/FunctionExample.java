package test;

public class FunctionExample {
	public static int main(String args[]){
		int val1 = 10;
		int val2 = 8;

		int res = subtractTwo(val1, val2);

		return res;
	}

	private static int subtractTwo(int a, int b){
		if(a > b)
			return (a - b);
		else
			return (b - a);
	}
}
