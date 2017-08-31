/*
 * xLogin - An advanced authentication application and awesome punishment management thing
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.l1t.xlogin.bungee.dynlist;

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
