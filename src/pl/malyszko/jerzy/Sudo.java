package pl.malyszko.jerzy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	static enum Association {
		VERTICAL, HORIZONTAL, SUBREGION;
	}

	private static Map<Coord, Set<SudokuElement>> uncharted;

	public static void ku(Sudoku s) {
		uncharted = collectUncharted(s);

		continuepoint: do {
			int unchartedSize = uncharted.size();
			try {

				List<Entry<Coord, Set<SudokuElement>>> ls = uncharted.entrySet().stream()
						.sorted((ent, ent2) -> Integer.compare(ent2.getValue().size(), ent.getValue().size()))
						.collect(Collectors.toList());

				ListIterator<Entry<Coord, Set<SudokuElement>>> iterator = ls.listIterator();

				while (iterator.hasNext()) {
					Entry<Coord, Set<SudokuElement>> next = iterator.next();
					boolean assigned = attemptToAssign(next, s);
					if (assigned) {
						uncharted.remove(next.getKey());
						continue continuepoint;
					}
				}

				if (unchartedSize == uncharted.size()) {
//					Coord avoidAxing = tryToAvoidAxing(s);
//					uncharted.remove(avoidAxing);
//					if (avoidAxing == null) {
					System.err.println("Unable to resolve");
					uncharted.entrySet().stream()
							.sorted((ent, ent2) -> Integer.compare(ent2.getValue().size(), ent.getValue().size()))
							.collect(Collectors.toList()).forEach(System.err::println);
					break;
//					}

				}
			} finally {
				actualizeUncharted(s);
			}

		} while (!uncharted.isEmpty());

	}

//	private static Coord tryToAvoidAxing(Sudoku s) {
//		Map<SudokuElement, Integer> occur = IntStream.range(1, 10).mapToObj(SudokuElement::translate)
//				.collect(Collectors.toMap(Function.identity(), se -> countOccurences(s, se.ordinal())));
//		List<Entry<SudokuElement, Integer>> collect = occur.entrySet().stream()
//				.sorted((ent1, ent2) -> ent2.getValue().compareTo(ent1.getValue())).collect(Collectors.toList());
//		ListIterator<Entry<SudokuElement, Integer>> listIterator = collect.listIterator();
//		while (listIterator.hasNext()) {
//			Entry<SudokuElement, Integer> next = listIterator.next();
//			SudokuElement key = next.getKey();
//			Integer occurence = next.getValue();
//			if (occurence == 9)
//				continue;
//			Set<Coord> locations = getPossibleLocations(s, key);
//			for (Coord loc : locations) {
//				Map<Association, Set<Coord>> groupMembers = getGroupMembers(loc);
//				Map<Association, Set<Coord>> otherPeersMissing = getOtherPeersMissing(loc);
//				Map<Association, Set<SudokuElement>> nonEmptyPeers = gatherNonEmptyPeers(s, loc);
//				Set<Map<Coord, SudokuElement>> subregions = getPeerSubregions(s, loc);
//				for (Association ass : Association.values()) {
//					Set<Coord> set = otherPeersMissing.get(ass);
//					Set<SudokuElement> set2 = nonEmptyPeers.get(ass);
//					if (set.size() == 2) {
//						Set<SudokuElement> alternatives = Stream.of(SudokuElement.values())
//								.filter(se -> !SudokuElement.EMPTY.equals(se)).filter(se -> !set2.contains(se))
//								.filter(se -> !key.equals(se)).collect(Collectors.toSet());
//						if (alternatives.size() < 3) {
//							for (Map<Coord, SudokuElement> subr : subregions) {
//								Set<SudokuElement> collect2 = IntStream.range(1, 10).mapToObj(SudokuElement::translate)
//										.filter(e -> !subr.values().contains(e)).collect(Collectors.toSet());
//								if (collect2.size() == alternatives.size() && collect2.containsAll(alternatives)) {
//									Set<Coord> cset = subr.entrySet().stream()
//											.filter(ent -> SudokuElement.EMPTY.equals(ent.getValue()))
//											.map(ent -> ent.getKey()).collect(Collectors.toSet());
//									if (groupMembers.entrySet().stream().flatMap(ent -> ent.getValue().stream())
//											.noneMatch(cset::contains)) {
//										if (occurence < 5)
//											continue;
//										doAssign(s, key.ordinal(), loc);
//										return loc;
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		return null;
//	}

	private static Set<Map<Coord, SudokuElement>> getPeerSubregions(Sudoku s, Coord loc) {
		Set<Map<Coord, SudokuElement>> retVal = new HashSet<>();
		int xstart = (loc.xcoo / 3) * 3;
		int ystart = (loc.ycoo / 3) * 3;
		Set<int[]> xpeers = IntStream.of(0, 3, 6).filter(i -> i != xstart)
				.mapToObj(i -> IntStream.range(i, i + 3).toArray()).collect(Collectors.toSet());
		Set<int[]> ypeers = IntStream.of(0, 3, 6).filter(i -> i != ystart)
				.mapToObj(i -> IntStream.range(i, i + 3).toArray()).collect(Collectors.toSet());
		for (int[] xp : xpeers) {
			int[] array = IntStream.range(ystart, ystart + 3).toArray();
			Map<Coord, SudokuElement> collect = IntStream.of(xp)
					.mapToObj(ar -> IntStream.of(array).mapToObj(y -> new int[] { ar, y })).flatMap(Function.identity())
					.map(ar -> Coord.get(ar[0], ar[1]))
					.collect(Collectors.toMap(Function.identity(), coo -> s.getElement(coo.xcoo, coo.ycoo)));
			retVal.add(collect);
		}
		for (int[] yp : ypeers) {
			int[] array = IntStream.range(xstart, xstart + 3).toArray();
			Map<Coord, SudokuElement> collect = IntStream.of(yp)
					.mapToObj(ar -> IntStream.of(array).mapToObj(x -> new int[] { x, ar })).flatMap(Function.identity())
					.map(ar -> Coord.get(ar[0], ar[1]))
					.collect(Collectors.toMap(Function.identity(), coo -> s.getElement(coo.xcoo, coo.ycoo)));
			retVal.add(collect);
		}

		return retVal;
	}

	private static boolean isOtherLocPossible(Sudoku s, Set<Coord> possibleLocations, Coord coo, SudokuElement alt) {
		Set<Coord> groupMembers = getGroupMembersFlat(coo);
		Set<Set<SudokuElement>> collect = possibleLocations.stream().filter(c -> !c.equals(coo))
				.filter(groupMembers::contains).map(c -> gatherNonEmptyPeersFlat(s, c)).filter(set -> set != null)
				.collect(Collectors.toSet());
		boolean otherLocationPossible = collect.stream().anyMatch(set -> !set.contains(alt));
		return otherLocationPossible;
	}

	private static Set<SudokuElement> gatherNonEmptyPeersFlat(Sudoku s, Coord c) {
		return gatherNonEmptyPeers(s, c).values().stream().flatMap(se -> se.stream()).collect(Collectors.toSet());
	}

	private static void actualizeUncharted(Sudoku s) {
		uncharted.entrySet().stream()
				.forEach(ent -> uncharted.compute(ent.getKey(), (coo, old) -> gatherNonEmptyPeersFlat(s, coo)));
	}

	static Set<Coord> getPossibleLocations(Sudoku s, SudokuElement el) {
		Set<Coord> retVal = IntStream.range(0, 9).mapToObj(i -> IntStream.range(0, 9).mapToObj(y -> Coord.get(i, y)))
				.flatMap(Function.identity())
				.filter(coo -> getGroupMembersFlat(coo).stream().map(c -> s.getElement(c.xcoo, c.ycoo))
						.filter(sel -> !sel.equals(SudokuElement.EMPTY)).noneMatch(sel -> sel.equals(el)))
				.filter(coo -> s.getElement(coo.xcoo, coo.ycoo).equals(SudokuElement.EMPTY))
				.collect(Collectors.toSet());
		return retVal;
	}

	private static boolean attemptToAssign(Entry<Coord, Set<SudokuElement>> ent, Sudoku s) {
		Set<Integer> alreadyThere = ent.getValue().stream().map(se -> se.ordinal()).collect(Collectors.toSet());
		Set<Integer> missing = IntStream.range(1, 10).filter(i -> !alreadyThere.contains(i)).boxed()
				.collect(Collectors.toSet());
		if (missing.size() == 1) {
			Integer next = missing.iterator().next();
			doAssign(s, next, ent.getKey());
			return true;
		}

		Map<Association, Set<Coord>> otherPeersMissing = getOtherPeersMissing(ent.getKey());

		for (SudokuElement se : missing.stream().map(i -> SudokuElement.translate(i)).collect(Collectors.toList())) {
			for (Association ass : otherPeersMissing.keySet()) {
				Set<Coord> set = otherPeersMissing.get(ass);
				boolean onlyOption = set.stream().map(c -> uncharted.get(c)).allMatch(col -> col.contains(se));
				if (onlyOption) {
					doAssign(s, se.ordinal(), ent.getKey());
					return true;
				}
			}
			Set<Coord> collect = otherPeersMissing.values().stream().flatMap(col -> col.stream())
					.collect(Collectors.toSet());
			boolean onlyOption = collect.stream().map(c -> uncharted.get(c)).allMatch(col -> col.contains(se));
			if (onlyOption) {
				doAssign(s, se.ordinal(), ent.getKey());
				return true;
			}
		}

		return false;
	}

	private static int countOccurences(Sudoku s, Integer otherMiss) {
		return (int) Stream.of(s.cloneElements()).flatMap(arr -> Stream.of(arr)).filter(se -> se.ordinal() == otherMiss)
				.count();
	}

	private static void doAssign(Sudoku s, Integer miss, Coord coord) {
		System.out.println(coord + " === " + miss);
		s.setElement(coord.xcoo, coord.ycoo, SudokuElement.translate(miss));
	}

	private static Map<Association, Set<Coord>> getOtherPeersMissing(Coord coord) {
		Map<Association, Set<Coord>> groupMembers = getGroupMembers(coord);
		groupMembers.entrySet().stream().forEach(ent -> ent
				.setValue(ent.getValue().stream().filter(c -> uncharted.containsKey(c)).collect(Collectors.toSet())));
		return groupMembers;
	}

	private static Set<Coord> getGroupMembersFlat(Coord coord) {
		return getGroupMembers(coord).values().stream().flatMap(col -> col.stream()).collect(Collectors.toSet());
	}

	private static Map<Association, Set<Coord>> getGroupMembers(Coord coord) {
		Map<Association, Set<Coord>> retVal = new HashMap<>();
		Stream<Coord> concatEins = IntStream.range(0, 9).filter(i -> i != coord.xcoo)
				.mapToObj(i -> Coord.get(i, coord.ycoo));
		Set<Coord> vertical = concatEins.collect(Collectors.toSet());
		retVal.put(Association.VERTICAL, vertical);
		Stream<Coord> concatZwei = IntStream.range(0, 9).filter(i -> i != coord.ycoo)
				.mapToObj(i -> Coord.get(coord.xcoo, i));
		Set<Coord> horizontal = concatZwei.collect(Collectors.toSet());
		retVal.put(Association.HORIZONTAL, horizontal);
		int iStartInclusive = coord.xcoo - (coord.xcoo % 3);
		int yStartInclusive = coord.ycoo - (coord.ycoo % 3);
		Stream<Coord> concatDrei = IntStream.range(iStartInclusive, iStartInclusive + 3)
				.mapToObj(iStart -> IntStream.range(yStartInclusive, yStartInclusive + 3)
						.mapToObj(yStart -> new int[] { iStart, yStart }))
				.flatMap(Function.identity()).map(tab -> Coord.get(tab[0], tab[1])).filter(c -> !c.equals(coord));
		Set<Coord> subr = concatDrei.collect(Collectors.toSet());
		retVal.put(Association.SUBREGION, subr);
		return retVal;
	}

	private static Map<Coord, Set<SudokuElement>> collectUncharted(final Sudoku s) {
		Map<Coord, Set<SudokuElement>> retVal = new HashMap<>();
		List<Coord> collect = IntStream.range(0, 9).mapToObj(i -> IntStream.range(0, 9).mapToObj(y -> Coord.get(i, y)))
				.flatMap(Function.identity())
				.filter(coo -> SudokuElement.EMPTY.equals(s.getElement(coo.xcoo, coo.ycoo)))
				.collect(Collectors.toList());

		for (Coord coord : collect) {
			Set<SudokuElement> collected = gatherNonEmptyPeersFlat(s, coord);
			retVal.put(coord, collected);
		}

		return retVal;
	}

	private static Map<Association, Set<SudokuElement>> gatherNonEmptyPeers(final Sudoku s, Coord coord) {
		Map<Association, Set<SudokuElement>> retVal = getGroupMembers(coord).entrySet().stream()
				.map(ent -> new Object[] { ent.getKey(),
						ent.getValue().stream().map(gm -> s.getElement(gm.xcoo, gm.ycoo))
								.filter(se -> !SudokuElement.EMPTY.equals(se)).collect(Collectors.toSet()) })
				.collect(Collectors.toMap(tab -> (Association) tab[0], tab -> (Set<SudokuElement>) tab[1]));
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
