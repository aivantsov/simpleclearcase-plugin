package jenkins.plugins.simpleclearcase.util;

import static org.junit.Assert.*;

import java.util.*;

import jenkins.plugins.simpleclearcase.*;

import org.junit.*;

public class ListUtilTest {

    @Test
    public void testRemoveEntriesChangeLogEmpty() {
        List<String> loadRules = Arrays.asList("/vobs/source/apps/java");
        LoadRuleDateMap loadRuleMap = new LoadRuleDateMap();
        List<SimpleClearCaseChangeLogEntry> entries = Collections.emptyList();
        boolean removed = ListUtil.removeEntries(entries, loadRuleMap, loadRules);
        assertFalse("change log is empty, nothing to remove", removed);
    }

    @Test
    public void testRemoveEntriesLoadRuleDateMapEmptyPathMatchesLoadRule() {
        String loadRule = "/vobs/source/apps/java";
        List<String> loadRules = Arrays.asList(loadRule);
        LoadRuleDateMap loadRuleMap = new LoadRuleDateMap();
        String comment = "improved build performance";
        String operation = "checkin";
        String eventDescription = "create version";
        String version = "/main/3";
        String user = "fubar";
        Date date = new Date();
        String path = "/vobs/source/apps/java/project/build.gradle";
        SimpleClearCaseChangeLogEntry entry = new SimpleClearCaseChangeLogEntry(date,
                user, path, version, eventDescription, operation, comment);
        assertTrue(entry.containsPathWithPrefix(loadRule));
        List<SimpleClearCaseChangeLogEntry> entries = Arrays.asList(entry);
        boolean removed = ListUtil.removeEntries(entries, loadRuleMap, loadRules);
        assertFalse("load rule map is empty, nothing to remove", removed);
    }

    @Test
    public void testRemoveEntriesLoadRuleDateMapEmptyPathDoesNotMatchLoadRule() {
        String loadRule = "/vobs/repo";
        List<String> loadRules = Arrays.asList(loadRule);
        LoadRuleDateMap loadRuleMap = new LoadRuleDateMap();
        String comment = "improved build performance";
        String operation = "checkin";
        String eventDescription = "create version";
        String version = "/main/3";
        String user = "fubar";
        Date date = new GregorianCalendar(2013, Calendar.JUNE, 01).getTime();
        String path = "/vobs/source/libs/common/common.properties";
        SimpleClearCaseChangeLogEntry entry = new SimpleClearCaseChangeLogEntry(date,
                user, path, version, eventDescription, operation, comment);
        assertFalse(entry.containsPathWithPrefix(loadRule));
        List<SimpleClearCaseChangeLogEntry> entries = Arrays.asList(entry);
        boolean removed = ListUtil.removeEntries(entries, loadRuleMap, loadRules);
        assertFalse("path does not match load rule, nothing to remove", removed);
    }

    @Test
    public void testRemoveEntriesMatchFoundSameBuildDate() {
        String lr1 = "/vobs/source/apps/java";
        String lr2 = "/vobs/source/apps/repo";
        List<String> loadRules = Arrays.asList(lr1, lr2);
        Date date = new GregorianCalendar(2013, Calendar.JUNE, 01).getTime();
        LoadRuleDateMap loadRuleMap = new LoadRuleDateMap();
        loadRuleMap.setBuildTime(lr1, date);
        String comment = "improved build performance";
        String operation = "checkin";
        String eventDescription = "create version";
        String version = "/main/3";
        String user = "fubar";
        String path = "/vobs/source/apps/java/project/build.gradle";
        List<SimpleClearCaseChangeLogEntry> entries = new ArrayList<SimpleClearCaseChangeLogEntry>(
                Arrays.asList(new SimpleClearCaseChangeLogEntry(date, user, path, version, eventDescription,
                        operation, comment)));
        boolean removed = ListUtil.removeEntries(entries, loadRuleMap, loadRules);
        assertTrue(removed);
    }

    @Test
    public void testRemoveEntriesMatchFoundDifferentBuildDate() {
        String lr1 = "/vobs/source/apps/java";
        String lr2 = "/vobs/source/apps/repo";
        List<String> loadRules = Arrays.asList(lr1, lr2);
        LoadRuleDateMap loadRuleMap = new LoadRuleDateMap();
        Date buildTime = new GregorianCalendar(2013, Calendar.JUNE, 01).getTime();
        loadRuleMap.setBuildTime(lr1, buildTime);
        String comment = "improved build performance";
        String operation = "checkin";
        String eventDescription = "create version";
        String version = "/main/3";
        String user = "fubar";
        String path = "/vobs/source/apps/java/project/build.gradle";
        Date eventDate = new GregorianCalendar(2013, Calendar.JUNE, 12).getTime();
        List<SimpleClearCaseChangeLogEntry> entries = new ArrayList<SimpleClearCaseChangeLogEntry>(
                Arrays.asList(new SimpleClearCaseChangeLogEntry(eventDate, user, path, version,
                        eventDescription, operation, comment)));
        boolean removed = ListUtil.removeEntries(entries, loadRuleMap, loadRules);
        assertFalse(removed);
    }

    @Test
    public void testRemoveEntriesNullEntryDate() {
        List<String> loadRules = Arrays.asList("/vobs/source/apps/java");
        LoadRuleDateMap loadRuleMap = new LoadRuleDateMap();
        String comment = "improved code coverage";
        String operation = "checkin";
        String eventDescription = "create version";
        String version = "/main/3";
        String user = "fubar";
        Date date = null;
        List<SimpleClearCaseChangeLogEntry> entries = Arrays.asList(new SimpleClearCaseChangeLogEntry(date,
                user, version, eventDescription, operation, comment));
        boolean removed = ListUtil.removeEntries(entries, loadRuleMap, loadRules);
        assertFalse(removed);
    }

    @Test
    public void testGetLatestCommitDatesEmpty() {
        List<String> loadRules = Collections.emptyList();
        List<SimpleClearCaseChangeLogEntry> entries = Collections.emptyList();
        LoadRuleDateMap actual = ListUtil.getLatestCommitDates(entries, loadRules);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetLatestCommitDates() {
        String javaRule = "/vobs/source/apps/java";
        String commonRule = "/vobs/source/libs/common";
        List<String> loadRules = Arrays.asList(javaRule, commonRule);
        String comment = "clean-up";
        String operation = "checkin";
        String eventDescription = "create version";
        String version = "/main/15";
        String user = "fubar";
        Date firstCommit = new GregorianCalendar(2013, Calendar.JUNE, 01).getTime();
        Date nextCommit = new GregorianCalendar(2013, Calendar.JUNE, 07).getTime();
        Date lastCommit = new GregorianCalendar(2013, Calendar.JUNE, 11).getTime();
        List<SimpleClearCaseChangeLogEntry> entries = Arrays.asList(
                    new SimpleClearCaseChangeLogEntry(firstCommit,
                                                      user, 
                                                      "/vobs/source/apps/java/packaging.properties", 
                                                      version, eventDescription, operation, comment), 
                    new SimpleClearCaseChangeLogEntry(lastCommit,
                                                      user, 
                                                      "/vobs/source/apps/java/project/build.gradle", 
                                                      version, eventDescription, operation, comment), 
                    new SimpleClearCaseChangeLogEntry(nextCommit,
                                                      user, 
                                                      "/vobs/source/apps/java/project", 
                                                      version, eventDescription, operation, comment) 
                );
        LoadRuleDateMap actual = ListUtil.getLatestCommitDates(entries, loadRules);
        assertFalse(actual.isEmpty());
        assertNull(actual.getBuiltTime(commonRule));
        assertEquals(lastCommit, actual.getBuiltTime(javaRule));
    }

}
