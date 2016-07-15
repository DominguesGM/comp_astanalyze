
public class DoWhileExample {
	public static String main(String args[]){
		String finalString = "";
		int i = 10;

		do{
			finalString += "a";
			i--;
		}
		while(i > 0);
		return finalString;
	}
}
