package test;

public class ForAssignementExample {
	public static void main(String args[]){
		int testing = 0;

		int i = 0;
		
		for (i++;;) {
			testing++;
			if(testing > 2)
				break;
		}
		
		for (;;i++) {
			testing++;
			if(testing > 5)
				break;
		}
	}
}
