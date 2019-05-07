package pl.malyszko.jerzy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Sudo {

	public static void ku(Sudoku s) {
		Map<Coord, Set<SudokuElement>> uncharted = collectUncharted(s);
		do {
			Map<Coord, Set<SudokuElement>> sorted = uncharted.entrySet().stream()
					.sorted((ent, ent2) -> Integer.compare(ent2.getValue().size(), ent.getValue().size()))
					.collect(Collectors.toMap(ent -> ent.getKey(), ent -> ent.getValue()));
		} while (!uncharted.isEmpty());
	}

	private static Map<Coord, Set<SudokuElement>> collectUncharted(final Sudoku s) {
		Map<Coord, Set<SudokuElement>> retVal = new HashMap<>();
		List<Coord> collect = IntStream.range(0, 9).mapToObj(i -> IntStream.range(0, 9).mapToObj(y -> new Coord(i, y)))
				.flatMap(Function.identity())
				.filter(coo -> SudokuElement.EMPTY.equals(s.getElement(coo.xcoo, coo.ycoo)))
				.collect(Collectors.toList());

		for (Coord coord : collect) {
			Stream<SudokuElement> concatEins = IntStream.range(0, 9).filter(i -> i != coord.xcoo)
					.mapToObj(i -> s.getElement(i, coord.ycoo));
			Stream<SudokuElement> concatZwei = IntStream.range(0, 9).filter(i -> i != coord.ycoo)
					.mapToObj(i -> s.getElement(coord.xcoo, i));
			int iStartInclusive = coord.xcoo - (coord.xcoo % 3);
			int yStartInclusive = coord.ycoo - (coord.ycoo % 3);
			Stream<SudokuElement> concatDrei = IntStream.range(iStartInclusive, iStartInclusive + 3)
					.mapToObj(iStart -> IntStream.range(yStartInclusive, yStartInclusive + 3)
							.mapToObj(yStart -> new int[] { iStart, yStart }))
					.flatMap(Function.identity()).map(tab -> s.getElement(tab[0], tab[1]));

			Set<SudokuElement> collected = Stream.concat(concatEins, Stream.concat(concatZwei, concatDrei))
					.filter(se -> !SudokuElement.EMPTY.equals(se)).collect(Collectors.toSet());
			retVal.put(coord, collected);
		}

		return retVal;
	}

}

class Coord {

	final int xcoo;
	final int ycoo;

	Coord(int xcoo, int ycoo) {
		super();
		if (xcoo > 8 || xcoo < 0)
			throw new IllegalArgumentException(String.valueOf(xcoo));
		if (ycoo > 8 || ycoo < 0)
			throw new IllegalArgumentException(String.valueOf(ycoo));
		this.xcoo = xcoo;
		this.ycoo = ycoo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + xcoo;
		result = prime * result + ycoo;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coord other = (Coord) obj;
		if (xcoo != other.xcoo)
			return false;
		if (ycoo != other.ycoo)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Coord [xcoo=" + xcoo + ", ycoo=" + ycoo + "]";
	}

}
