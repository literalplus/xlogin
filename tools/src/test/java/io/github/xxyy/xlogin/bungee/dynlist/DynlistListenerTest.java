package io.github.xxyy.xlogin.bungee.dynlist;

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
