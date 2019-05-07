package pl.malyszko.jerzy;

public class Runner {

	public static void main(String[] args) {
		Sudoku s = new Sudoku("7:1=6", "8:1=8", "5:2=7", "6:2=3", "9:2=9", "1:3=3", "3:3=9", "8:3=4", "9:3=5", "1:4=4",
				"2:4=9", "1:5=8", "3:5=3", "5:5=5", "7:5=9", "9:5=2", "8:6=3", "9:6=6", "1:7=9", "2:7=6", "7:7=3",
				"9:7=8", "1:8=7", "4:8=6", "5:8=8", "2:9=2", "3:9=8");
		System.out.print(s.toString());
		Sudo.ku(s);
		System.out.println();
		System.out.print(s.toString());

	}

}
