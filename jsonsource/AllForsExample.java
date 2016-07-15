
public class AllForsExample {
	public static void main(String args[]){
		int testing = 0;

		for (int i = 0; i < 10; i++) {
		testing++;
		}

		for (int i = 0; ; i++) {
		testing++;
		if(i < 10)
			break;
		}

		for (int i = 0; i < 10;) {
		testing++;
		i++;
		}

		for (int i = 0;;) {
		testing++;
		i++;
		testing++;
		if(i < 10)
			break;
		}

		int i = 0;
		for (; i < 10; i++) {
		testing++;
		}

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

		for (;;) {
			testing++;
			if(testing > 10)
				break;
		}
	}
}
