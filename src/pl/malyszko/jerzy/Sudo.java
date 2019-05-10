package pl.malyszko.jerzy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Sudo {

	private static Map<Coord, Set<SudokuElement>> uncharted;

	public static void ku(Sudoku s) {
		uncharted = collectUncharted(s);
		int unchartedSize = uncharted.size();
		do {
			ListIterator<Entry<Coord, Set<SudokuElement>>> iterator = uncharted.entrySet().stream()
					.sorted((ent, ent2) -> Integer.compare(ent2.getValue().size(), ent.getValue().size()))
					.collect(Collectors.toList()).listIterator();

			breakpoint: while (iterator.hasNext()) {
				Entry<Coord, Set<SudokuElement>> next = iterator.next();
				boolean assigned = attemptToAssign(next, s);
				if (assigned) {
					uncharted.remove(next.getKey());
					break breakpoint;
				}
			}
			if (unchartedSize == uncharted.size()) {
				System.err.println("Unable to resolve");
				break;
			} else {
				unchartedSize = uncharted.size();
			}
		} while (!uncharted.isEmpty());
	}

	private static boolean attemptToAssign(Entry<Coord, Set<SudokuElement>> ent, Sudoku s) {
		Set<Integer> alreadyThere = ent.getValue().stream().map(se -> se.ordinal()).collect(Collectors.toSet());
		Set<Integer> missing = IntStream.range(1, 10).filter(i -> !alreadyThere.contains(i)).boxed()
				.collect(Collectors.toSet());
		if (missing.size() == 1) {
			s.setElement(ent.getKey().xcoo, ent.getKey().ycoo, SudokuElement.translate(missing.iterator().next()));
			return true;
		}
		Set<Coord> othersMissing = getOtherPeersMissing(ent.getKey());
		for (Integer miss : missing) {
			Stream<Set<Integer>> stream = othersMissing.stream()
					.map(coo -> uncharted.get(coo).stream().map(se -> se.ordinal()).collect(Collectors.toSet()));
			boolean allMatch = stream.allMatch(set -> set.contains(miss));
			if (allMatch) {
				s.setElement(ent.getKey().xcoo, ent.getKey().ycoo, SudokuElement.translate(miss));
				return true;
			}
		}
		for (Integer miss : missing) {
			Set<Coord> forbidden = new HashSet<>();
			for (int i = 0; i < 9; i++) {
				for (int y = 0; y < 9; y++) {
					int ordinal = s.getElement(i, y).ordinal();
					if (ordinal == miss) {

					}
				}
			}
		}

		return false;
	}

	private static Set<Coord> getOtherPeersMissing(Coord coord) {
		Set<Coord> groupMembers = getGroupMembers(coord);
		return groupMembers.stream().filter(gm -> uncharted.containsKey(gm)).collect(Collectors.toSet());
	}

	private static Set<Coord> getGroupMembers(Coord coord) {
		Stream<Coord> concatEins = IntStream.range(0, 9).filter(i -> i != coord.xcoo)
				.mapToObj(i -> Coord.get(i, coord.ycoo));
		Stream<Coord> concatZwei = IntStream.range(0, 9).filter(i -> i != coord.ycoo)
				.mapToObj(i -> Coord.get(coord.xcoo, i));
		int iStartInclusive = coord.xcoo - (coord.xcoo % 3);
		int yStartInclusive = coord.ycoo - (coord.ycoo % 3);
		Stream<Coord> concatDrei = IntStream.range(iStartInclusive, iStartInclusive + 3)
				.mapToObj(iStart -> IntStream.range(yStartInclusive, yStartInclusive + 3)
						.mapToObj(yStart -> new int[] { iStart, yStart }))
				.flatMap(Function.identity()).map(tab -> Coord.get(tab[0], tab[1]));
		return Stream.concat(concatEins, Stream.concat(concatZwei, concatDrei)).collect(Collectors.toSet());
	}

	private static Map<Coord, Set<SudokuElement>> collectUncharted(final Sudoku s) {
		Map<Coord, Set<SudokuElement>> retVal = new HashMap<>();
		List<Coord> collect = IntStream.range(0, 9).mapToObj(i -> IntStream.range(0, 9).mapToObj(y -> Coord.get(i, y)))
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

	static final Coord[][] cache = new Coord[9][9];

	static {
		IntStream.range(0, 9).mapToObj(x -> IntStream.range(0, 9).mapToObj(y -> new Coord(x, y)))
				.flatMap(Function.identity()).forEach(c -> cache[c.xcoo][c.ycoo] = c);
	}

	static Coord get(int xaxis, int yaxis) {
		return cache[xaxis][yaxis];
	}

	final int xcoo;
	final int ycoo;

	private Coord(int xcoo, int ycoo) {
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
