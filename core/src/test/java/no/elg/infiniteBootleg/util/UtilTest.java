package no.elg.infiniteBootleg.util;

import no.elg.infiniteBootleg.TestGraphic;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Elg
 */
public class UtilTest extends TestGraphic {

    @Test
    public void isBetweenFloat() {
        assertTrue(Util.isBetween(0, 0, 1));
        assertFalse(Util.isBetween(0, 1, 1));
        assertFalse(Util.isBetween(1010, -1, 9999));
        assertFalse(Util.isBetween(1010, 99999, 9999));
        assertTrue(Util.isBetween(1010, 2000, 9999));
    }

    @Test
    public void hasSuperClass() {
        assertTrue(Util.hasSuperClass(Object.class, Object.class));
        assertFalse(Util.hasSuperClass(Object.class, String.class));
        assertTrue(Util.hasSuperClass(String.class, Object.class));
    }

    @Test
    public void interpretArgsDiscardsLeadingDashesWhenNoVal() {
        Map<Pair<String, Boolean>, String> actual = Util.interpreterArgs(new String[] {"--test"});
        Map<Pair<String, Boolean>, String> expected = new HashMap<>();
        expected.put(new ImmutablePair<>("test", true), null);
        assertEquals(expected, actual);
    }

    @Test
    public void interpretArgsDiscardsLeadingDashesWhenVal() {
        Map<Pair<String, Boolean>, String> actual = Util.interpreterArgs(new String[] {"--test=aaa"});
        Map<Pair<String, Boolean>, String> expected = new HashMap<>();
        expected.put(new ImmutablePair<>("test", true), "aaa");
        assertEquals(expected, actual);
    }

    @Test
    public void interpretArgsDiscardsLeadingDashesWhenAltWithoutVal() {
        Map<Pair<String, Boolean>, String> actual = Util.interpreterArgs(new String[] {"-t"});
        Map<Pair<String, Boolean>, String> expected = new HashMap<>();
        expected.put(new ImmutablePair<>("t", false), null);
        assertEquals(expected, actual);
    }

    @Test
    public void interpretArgsDiscardsLeadingDashesWhenAltWithVal() {
        Map<Pair<String, Boolean>, String> actual = Util.interpreterArgs(new String[] {"-t=2ad"});
        Map<Pair<String, Boolean>, String> expected = new HashMap<>();
        expected.put(new ImmutablePair<>("t", false), "2ad");
        assertEquals(expected, actual);
    }

    @Test
    public void interpretArgsGroupsSingleArgsRemovesDuplicates() {
        Map<Pair<String, Boolean>, String> actual = Util.interpreterArgs(new String[] {"-tt"});
        Map<Pair<String, Boolean>, String> expected = new HashMap<>();
        expected.put(new ImmutablePair<>("t", false), null);
        assertEquals(expected, actual);
    }

    @Test
    public void interpretArgsGroupsSingleArgsHandleDuplicatesWithArgs() {
        Map<Pair<String, Boolean>, String> actual = Util.interpreterArgs(new String[] {"-tt=2ad"});
        Map<Pair<String, Boolean>, String> expected = new HashMap<>();
        expected.put(new ImmutablePair<>("t", false), "2ad");
        assertEquals(expected, actual);
    }


    @Test
    public void interpretArgsGroupsSingleArgsNoVals() {
        Map<Pair<String, Boolean>, String> actual = Util.interpreterArgs(new String[] {"-Tt"});
        Map<Pair<String, Boolean>, String> expected = new HashMap<>();
        expected.put(new ImmutablePair<>("T", false), null);
        expected.put(new ImmutablePair<>("t", false), null);
        assertEquals(expected, actual);
    }

    @Test
    public void interpretArgsGroupsSingleArgs() {
        Map<Pair<String, Boolean>, String> actual = Util.interpreterArgs(new String[] {"-Tt=2ad"});
        Map<Pair<String, Boolean>, String> expected = new HashMap<>();
        expected.put(new ImmutablePair<>("T", false), null);
        expected.put(new ImmutablePair<>("t", false), "2ad");
        assertEquals(expected, actual);
    }
}
