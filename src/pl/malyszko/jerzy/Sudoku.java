package pl.malyszko.jerzy;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sudoku {

	private SudokuElement[][] elements = new SudokuElement[9][9];

	private Sudoku() {
		for (int i = 0; i < elements.length; i++) {
			for (int y = 0; y < elements[i].length; y++) {
				elements[i][y] = SudokuElement.EMPTY;
			}
		}
	}

	public Sudoku(String input) {
		this(input.split(";"));
	}

	Sudoku(SudokuElement[][] el) {
		this.elements = el;
	}

	public Sudoku(String... chunks) {
		this();
		for (String string : chunks) {
			String[] split2 = string.split("=");
			SudokuElement element = SudokuElement.translate(Integer.valueOf(split2[1]));
			String[] split3 = split2[0].split(":");
			Integer yaxis = Integer.valueOf(split3[0]) - 1;
			Integer xaxis = Integer.valueOf(split3[1]) - 1;
			if (!elements[xaxis][yaxis].equals(SudokuElement.EMPTY))
				throw new IllegalStateException(string);
			setElement(xaxis, yaxis, element);

		}
	}

	public void setElement(int xaxis, int yaxis, SudokuElement sel) {
		elements[xaxis][yaxis] = sel;
		boolean validate = Validator.validate(this);
		if (!validate)
			throw new SudokuValidationException(this,
					String.valueOf(yaxis + 1) + ':' + String.valueOf(xaxis + 1) + '=' + sel.toString());
	}

	public SudokuElement getElement(int xaxis, int yaxis) {
		return elements[xaxis][yaxis];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (SudokuElement[] elm : elements) {
			for (SudokuElement e : elm) {
				sb.append(e.toString()).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public SudokuElement[][] cloneElements() {
		return Stream.of(elements).map(sel -> Arrays.copyOf(sel, sel.length)).collect(Collectors.toList())
				.toArray(new SudokuElement[0][]);
	}

}

enum SudokuElement {

	EMPTY, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGTH, NINE;

	static SudokuElement translate(int i) {
		switch (i) {
		case 1:
			return ONE;
		case 9:
			return NINE;
		case 2:
			return TWO;
		case 3:
			return THREE;
		case 4:
			return FOUR;
		case 5:
			return FIVE;
		case 6:
			return SIX;
		case 7:
			return SEVEN;
		case 8:
			return EIGTH;
		default:
			throw new IllegalArgumentException(String.valueOf(i));
		}
	}

	@Override
	public String toString() {
		return EMPTY.equals(this) ? "?" : String.valueOf(this.ordinal());
	}

}
