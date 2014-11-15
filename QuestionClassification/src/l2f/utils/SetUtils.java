package l2f.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set utilities, namely, powerset and permutations.
 * 
 * @author Joao
 */
public class SetUtils {

	/**
	 * Returns the powerset of <code>features</code>.
	 * @param <T> the type of the objects in the set
	 * @param features base set
	 * @param includeEmptySet whether to include the empty set or not
	 * @return powerset of <code>features</code>
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<List<T>> powerSet(List<T> features, boolean includeEmptySet) {
		List<List<T>> sets = new ArrayList<List<T>>();
		if (features.isEmpty()) {
			if (includeEmptySet) {
				sets.add(new ArrayList<T>());
			}
			return sets;
		}
		T head = features.remove(0);
		if (!includeEmptySet) {
			sets.add(Arrays.asList(head));
		}
		for (List<T> set : powerSet(features, includeEmptySet)) {
			List<T> newSet = new ArrayList<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	public static <T> List<List<T>> permute(List<T> list) {
		List<List<T>> permutations = new ArrayList<List<T>>();
		if (list.isEmpty()) {
			permutations.add(new ArrayList<T>());
		} else {
			T head = list.get(0);
			List<T> rest = list.subList(1, list.size());
			for (List<T> permutation : permute(rest)) {
				for (int i = 0; i < permutation.size() + 1; i++) {
					List<T> newPermutation = new ArrayList<T>(permutation);
					newPermutation.add(i, head);
					permutations.add(newPermutation);
				}
			}
		}
		return permutations;
	}

	public static <T> List<List<T>> combine(List<T> elements, int k) {
		List<List<T>> combinations = new ArrayList<List<T>>();
		combinations = combine(new ArrayList<T>(), elements, k);
		return combinations;
	}

	private static <T> List<List<T>> combine(List<T> prefix, List<T> elements, int k) {
		List<List<T>> combinations = new ArrayList<List<T>>();
		if (k == 0) {
			//System.out.println(prefix);
			List<T> newCombination = new ArrayList<T>(prefix);
			combinations.add(newCombination);
			return combinations;
		}
		for (int i = 0; i < elements.size(); i++) {
			List<T> v = new ArrayList<T>();
			v.addAll(prefix);
			v.add(elements.get(i));
			combinations.addAll(combine(v, elements.subList(i + 1, elements.size()), k - 1));
		}
		return combinations;
	}

	public static void main(String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(Arrays.asList("A", "B", "C", "D", "E"));
		for (List<String> permutation : SetUtils.combine(list,2)) {
			System.out.println(permutation);
		}

		//System.out.println(powerSet(list, false));
	}
}
