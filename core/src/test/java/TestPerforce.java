import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.vmware.util.scm.FileChange;
import com.vmware.util.scm.Perforce;

public class TestPerforce {

    private Perforce perforce = new Perforce("dbiggs-vcloud-sp-main", null);

    @Test
    public void canDetermineRootDirectory() {
        File clientDirectory = perforce.getWorkingDirectory();
        assertNotNull(clientDirectory);
        assertEquals("/Users/dbiggs/p4-sp-main", clientDirectory.getPath());
    }

    @Test
    public void canDetermineClientName() {
        perforce = new Perforce("/Users/dbiggs/p4-sp-main/");
        assertTrue(perforce.isLoggedIn());
        assertEquals("dbiggs", perforce.getUsername());
        assertEquals("dbiggs-vcloud-sp-main", perforce.getClientName());
    }

    @Test
    public void changelistIsSubmitted() {
        assertEquals("submitted", perforce.getChangelistStatus("448453"));
    }

    @Test
    public void canGetFileChangesInChangelist() {
        List<String> openChangelists = perforce.getPendingChangelists();
        assertFalse("Need a pending changelist to run this test", openChangelists.isEmpty());
        List<FileChange> changes = perforce.getFileChangesForPendingChangelist(openChangelists.get(0));
        assertFalse("Should not be empty", changes.isEmpty());
    }
}
