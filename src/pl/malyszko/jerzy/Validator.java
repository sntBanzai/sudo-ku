package pl.malyszko.jerzy;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Validator {

	private static final Collector<SudokuElement, ?, Map<SudokuElement, Long>> GROUPING_BY = Collectors
			.groupingBy(Function.identity(), Collectors.counting());
	private static final Predicate<SudokuElement> FILTER_EMPTY = se -> !se.equals(SudokuElement.EMPTY);
	private static final Predicate<? super Entry<SudokuElement, Long>> DUPLICATIONS_PREDICATE = ent -> ent
			.getValue() > 1;

	public static boolean validate(Sudoku s) {
		SudokuElement[][] sel = s.cloneElements();
		for (int i = 0; i < sel.length; i++) {
			SudokuElement[] line = sel[i];

			Set<SudokuElement> duplicates = Stream.of(line).filter(FILTER_EMPTY).collect(GROUPING_BY).entrySet()
					.stream().filter(DUPLICATIONS_PREDICATE).map(ent -> ent.getKey()).collect(Collectors.toSet());
			if (!duplicates.isEmpty()) {
				return false;
			}
		}
		for (int y = 0; y < 9; y++) {
			final int curry = y;
			Set<SudokuElement> dups = IntStream.range(0, 9).mapToObj(i -> sel[i][curry]).filter(FILTER_EMPTY)
					.collect(GROUPING_BY).entrySet().stream().filter(DUPLICATIONS_PREDICATE).map(ent -> ent.getKey())
					.collect(Collectors.toSet());
			if (!dups.isEmpty())
				return false;
		}
		for (int i = 0; i < 3; i++) {
			for (int y = 0; y < 3; y++) {
				int iStartInclusive = i == 0 ? 0 : (i * 3);
				int yStartInclusive = y == 0 ? 0 : (y * 3);
				
				Set<SudokuElement> collect = IntStream.range(iStartInclusive, iStartInclusive + 3)
						.mapToObj(iStart -> IntStream.range(yStartInclusive, yStartInclusive + 3)
								.mapToObj(yStart -> new int[] { iStart, yStart }))
						.flatMap(Function.identity()).map(arr -> sel[arr[0]][arr[1]]).filter(FILTER_EMPTY)
						.collect(GROUPING_BY).entrySet().stream().filter(DUPLICATIONS_PREDICATE)
						.map(ent -> ent.getKey()).collect(Collectors.toSet());
				if (!collect.isEmpty())
					return false;

			}
		}
		return true;
	}
}
