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

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

public class DynlistListenerTest {
    @Test
    public void testIsBlocked() {
        DynlistModule module = mock(DynlistModule.class);
        Mockito.when(module.getMatches(eq("no"))).thenReturn(new ArrayList<DynlistEntry>());
        Mockito.when(module.getMatches(eq("yes"))).thenReturn(Arrays.asList(new DynlistEntry("blocking", Pattern.compile(".*"))));
        Mockito.when(module.getMatches(eq("permitted"))).thenReturn(Arrays.asList(new DynlistEntry("blocking", Pattern.compile(".*"))));
        DynlistListener listener = new DynlistListener(module);
        ProxiedPlayer plr = mock(ProxiedPlayer.class);
        Mockito.when(plr.hasPermission(any(String.class))).thenReturn(false);

        assertThat(listener.isBlocked("no", plr), is(false));
        assertThat(plr.hasPermission("xlogin.dlby.blocking"), is(false));
        assertThat(listener.isBlocked("yes", plr), is(true));

        Mockito.when(plr.hasPermission(any(String.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0].equals("xlogin.dlby.blocking");
            }
        });
        assertThat(listener.isBlocked("permitted", plr), is(false));
    }
}
