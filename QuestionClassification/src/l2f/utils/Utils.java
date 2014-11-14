package l2f.utils;

import edu.berkeley.nlp.ling.HeadFinder;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {
    }
    /**
     *
     * String Utils
     *
     */
    public static final String EMPTY = "";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Joins each elem in the {@code Collection} with the given glue.
     * For example, given a list of {@code Integers}, you can create
     * a comma-separated list by calling {@code join(numbers, ", ")}.
     */
    public static <X> String join(Iterable<X> l, String glue) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (X o : l) {
            if (!first) {
                sb.append(glue);
            } else {
                first = false;
            }
            sb.append(o);
        }
        return sb.toString();
    }

    /**
     * Computes the WordNet 2.0 POS tag corresponding to the PTB POS tag s.
     *
     * @param s a Penn TreeBank POS tag.
     */
    public static String pennPOSToWordnetPOS(String s) {
        if (s.matches("NN|NNP|NNS|NNPS")) {
            return "noun";
        }
        if (s.matches("VB|VBD|VBG|VBN|VBZ|VBP|MD")) {
            return "verb";
        }
        if (s.matches("JJ|JJR|JJS|CD")) {
            return "adjective";
        }
        if (s.matches("RB|RBR|RBS|RP|WRB")) {
            return "adverb";
        }
        return null;
    }

    /**
     * Given a String, return the number of whitespaces in it.
     *
     * @param s String to check
     * @return number of whitespaces contained in the String
     */
    public static int countWhitespaces(String s) {
        int total = 0;
        for (int j = 0; j < s.length(); j++) {
            if (s.charAt(j) == ' ') {
                total++;
            }
        }
        return total;
    }

    /**
     * Joins each elem in the {@code Collection} with the given glue.
     * For example, given a list of {@code Integers}, you can create
     * a comma-separated list by calling {@code join(numbers, ", ")}.
     */
    public static <X> String join(Iterable<X> l, char glue) {
        StringBuilder sb = new StringBuilder(32);
        boolean first = true;
        for (X o : l) {
            if (!first) {
                sb.append(glue);
            } else {
                first = false;
            }
            sb.append(o);
        }
        return sb.toString();
    }

    /**
     * Checks if every character in the string is uppercased.
     */
    public static boolean isAllCaps(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isUpperCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * File Utils
     *
     */
    /**
     * Check existence, permissions on index directory.
     * Index must be directory, with read and write permission.
     * Creates index dir, if doesn't exist.
     *
     * @param dirName directory pathname
     * @throws IOException
     */
    public static File checkIndex(String dirName) throws IOException {
        return checkIndex(dirName, true);
    }

    /**
     * Check existence, permissions on index directory.
     *
     * @param dirName directory pathname
     * @param createIfNotExists whether or not to create new directory for pathname
     * @throws IOException
     */
    public static File checkIndex(String dirName, boolean createIfNotExists) throws IOException {
        File dir = new File(dirName);
        if (!dir.exists() && !createIfNotExists) {
            String msg = "Error, no such index: " + dir.getAbsolutePath();
            throw new IOException(msg);
        }
        if (!dir.exists()) {
            dir.mkdirs();
            return checkIndex(dirName, false);
        } else {
            if (!dir.isDirectory()) {
                String msg = "Error, not a directory: " + dir.getAbsolutePath();
                throw new IOException(msg);
            }
            if (!dir.canRead()) {
                String msg = "Error, cannot read index file: " + dir.getAbsolutePath();
                throw new IOException(msg);
            }
            if (!dir.canWrite()) {
                String msg = "Error, cannot write to index file: " + dir.getAbsolutePath();
                throw new IOException(msg);
            }
        }
        return dir;
    }

    /**
     * Check existence, permissions on input file.
     *
     * @param name file name
     * @throws IOException
     */
    public static File checkInputFile(String name) {
        File file = new File(name);
        if (!(file.exists() && file.isFile() && file.canRead())) {
            String msg = "File missing or incorrect: " + name;
            throw new IllegalArgumentException(msg);
        }
        return file;
    }

    /**
     * Check existence, permissions on input file.
     *
     * @param file input file
     * @throws IOException
     */
    public static boolean checkInputFile(File file) {
        if (!(file.exists() && file.isFile() && file.canRead())) {
            return false;
        }
        return true;
    }

    /**
     * Check existence, permissions on output file.
     * Create if not exists
     *
     * @param name file name
     * @throws IOException
     */
    public static File checkOutputFile(String name) throws IOException {
        File file = new File(name);
        if (!file.exists()) {
            file.createNewFile();
        }
        if (!(file.isFile() && file.canWrite())) {
            String msg = "File missing or incorrect: " + name;
            throw new IllegalArgumentException(msg);
        }
        return file;
    }

    /**
     * Check if dir exists
     *
     * @param name directory name
     * @throws IOException
     */
    public static File checkDir(String name) throws IOException {
        File file = new File(name);
        if (!(file.exists() && file.isDirectory())) {
            String msg = "No such directory: " + name;
            throw new IllegalArgumentException(msg);
        }
        return file;
    }

    /**
     * Check that existing file is directory, create dir if not exists.
     *
     * @param dir directory
     * @throws IOException
     */
    public static void ensureDirExists(File dir) throws IOException {
        if (dir.isDirectory()) {
            return;
        }
        if (dir.exists()) {
            String msg = "Existing file must be directory."
                    + " Found file=" + dir;
            throw new IOException(msg);
        }
        dir.mkdirs();
    }

    @SuppressWarnings("unchecked")
    public static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    @SuppressWarnings("unchecked")
    public static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
    /**
     *
     * Tree Utils
     *
     */
    public final static String PLURAL_SUFFIX = "S";
    public final static String CARDINAL = "CD";

    /**
     * Returns the label of the tree leaf that is the head of the tree.
     *
     * @param hf The headfinding algorithm to use
     * @param parent  The parent of this tree
     * @return The head tree leaf if any, else <code>null</code>
     */
    public static String headTerminalLabel(HeadFinder hf, Tree<String> tree) {
        Tree<String> head = headTerminal(hf, tree);
        return head == null ? null : head.getLabel();
    }

    /**
     * Returns the tree leaf that is the head of the tree.
     *
     * @param hf The headfinding algorithm to use
     * @param parent  The parent of this tree
     * @return The head tree leaf if any, else <code>null</code>
     */
    public static Tree<String> headTerminal(HeadFinder hf, Tree<String> tree) {
        if (tree.isLeaf()) {
            return tree;
        }
        Tree<String> head = hf.determineHead(tree);
        if (head != null) {
            return headTerminal(hf, head);
        }
        System.err.println("Head is null: " + tree);
        return null;
    }

    /**
     * Returns the preterminal tree that is the head of the tree.
     * See {@link #isPreTerminal()} for
     * the definition of a preterminal node. Beware that some tree nodes may
     * have no preterminal head.
     *
     * @param hf The headfinding algorithm to use
     * @return The head preterminal tree, if any, else <code>null</code>
     * @throws IllegalArgumentException if called on a leaf node
     */
    public static Tree<String> headPreTerminal(HeadFinder hf, Tree<String> tree) {
        if (tree.isPreTerminal()) {
            return tree;
        } else if (tree.isLeaf()) {
            throw new IllegalArgumentException("Called headPreTerminal on a leaf: " + tree);
        } else {
            Tree<String> head = hf.determineHead(tree);
            if (head != null) {
                return headPreTerminal(hf, head);
            }
            System.err.println("Head preterminal is null: " + tree);
            return null;
        }
    }

    public static Tree<String> getFirstPreTerminal(Tree<String> tree, String preterminal) {
        //tree.getPreTerminalYield()
        return null;
    }

    /**
     * Return whether this node is a phrasal node or not.  A phrasal node
     * is defined to be a node which is not a leaf or a preterminal.
     * Worded positively, this means that it must have two or more children,
     * or one child that is not a leaf.
     *
     * @return <code>true</code> if the node is phrasal;
     *         <code>false</code> otherwise
     */
    public static boolean isPhrasal(Tree<String> t) {
        List<Tree<String>> kids = t.getChildren();
        return !(kids == null || kids.size() == 0 || (kids.size() == 1 && kids.get(0).isLeaf()));
    }
    private static final Pattern PUNCTUATION = Pattern.compile("[.?!]");

    public static List<Tree<String>> getPhrasal(Tree<String> t) {
        List<Tree<String>> phrasal = new ArrayList<Tree<String>>();

        List<Tree<String>> children = t.getChildren();
        for (Tree<String> child : children) {
            if (isPhrasal(child) && child.getLabel().endsWith("P") && !child.getLabel().equals("VP")) {
                phrasal.add(child);
            } else if (child.isPreTerminal() && isPhrasal(t)
                    && !PUNCTUATION.matcher(child.getLabel()).matches()) {
                phrasal.add(child); // (SQ (VBZ is))
            } else {
                phrasal.addAll(getPhrasal(child));
            }
        }
        return phrasal;
    }

    public static List<Tree<String>> getLabeledNodes(Tree<String> tree, String label) {
        List<Tree<String>> labelednodes = new ArrayList<Tree<String>>();

        List<Tree<String>> children = tree.getChildren();
        for (Tree<String> child : children) {
            if (child.getLabel().equals(label)) {
                labelednodes.add(child);
            }
            labelednodes.addAll(getLabeledNodes(child, label));
        }
        return labelednodes;
    }

    public static List<String> getLabels(List<Tree<String>> trees) {
        List<String> labels = new ArrayList<String>();
        for (Tree<String> t : trees) {
            labels.add(t.getLabel());
        }
        return labels;
    }

    public static List<String> getTerminals(List<Tree<String>> trees) {
        List<String> labels = new ArrayList<String>();
        for (Tree<String> t : trees) {
            //I have changed this, so ponctuation like : Who sang 'Everybody wants to Rule the World'?
            //won't appear as terminal
            labels.add(Utils.join(t.getYield(), " ").replaceAll("(''|\"|``)", "").trim());
        }
        return labels;
    }

    public static String flatten(Tree<String> t) {
        return Utils.join(t.getYield(), " ");
    }

    public static List<Tree<String>> valueOf(String str) {
        List<Tree<String>> tree = new ArrayList<Tree<String>>();
        Iterator<Tree<String>> iterator = new Trees.PennTreeReader(new StringReader(str));
        while (iterator.hasNext()) {
            tree.add(iterator.next());
        }
        return tree;
    }
    
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
    

}
