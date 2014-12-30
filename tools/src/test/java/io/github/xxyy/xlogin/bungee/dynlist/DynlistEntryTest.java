package io.github.xxyy.xlogin.bungee.dynlist;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DynlistEntryTest {

    @Test
    public void testMatches() throws Exception {
        Pattern regex = Pattern.compile("testing ?scienc[eE]+");
        DynlistEntry entry = new DynlistEntry("test", regex);
        assertTrue("Regex doesn't even match", regex.matcher("testingscience").matches());
        assertTrue(entry.matches("testingscience"));
    }

    @Test
    public void testSerialize() throws Exception {
        Pattern regex = Pattern.compile("testing ?scienc[eE]+");
        DynlistEntry entry = new DynlistEntry("test", regex);
        assertThat(entry.serialize(), is(equalTo("test/testing ?scienc[eE]+")));
    }

    @Test
    public void testDeserialize() throws Exception {
        DynlistEntry entry = DynlistEntry.deserialize("test/testing ?scienc[eE]+");
        assertThat(entry.getRegex().pattern(), is(equalTo("testing ?scienc[eE]+")));
        assertThat(entry.getName(), is(equalTo("test")));
    }
}
