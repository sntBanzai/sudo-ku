package pl.malyszko.jerzy;

public class SudokuValidationException extends RuntimeException {

	public SudokuValidationException(Sudoku sudoku, String message) {
		super(message);
		System.err.println(sudoku);
	}

	
	
}
