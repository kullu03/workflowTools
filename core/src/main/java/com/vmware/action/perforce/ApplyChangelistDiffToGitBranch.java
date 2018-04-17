package com.vmware.action.perforce;

import java.io.File;
import java.io.IOException;

import com.vmware.action.base.BasePerforceCommitUsingGitAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;
import com.vmware.util.FileUtils;
import com.vmware.util.StringUtils;
import com.vmware.util.exception.FatalException;
import com.vmware.util.exception.RuntimeIOException;
import com.vmware.util.logging.LogLevel;

@ActionDescription("Applies a diff for the selected changelist to the current git branch. Can be used to apply shelved changelists.")
public class ApplyChangelistDiffToGitBranch extends BasePerforceCommitUsingGitAction {

    public ApplyChangelistDiffToGitBranch(WorkflowConfig config) {
        super(config);
        super.failIfCannotBeRun = true;
    }

    @Override
    public void process() {
        String changelistIdToUse = draft.perforceChangelistId;
        if (StringUtils.isBlank(changelistIdToUse)) {
            changelistIdToUse = perforce.selectPendingChangelist();
        }

        log.info("Generating git compatible diff for perforce changelist {}", changelistIdToUse);
        String diffData = perforce.diffChangelistInGitFormat(changelistIdToUse, LogLevel.TRACE);

        String checkOutput = git.applyPatch(diffData, true);
        if (StringUtils.isNotBlank(checkOutput)) {
            log.debug("Failed diff\n{}" + diffData);
            throw new FatalException("Check of git diff failed!\n" + checkOutput);
        }
        log.info("Check of diff succeeded, applying diff");
        String applyOutput = git.applyPatch(diffData, false);
        if (StringUtils.isNotBlank(applyOutput)) {
            saveDiffAndThrowException(applyOutput, diffData);
        }
        log.info("Successfully applied diff");
    }

    private void saveDiffAndThrowException(String applyOutput, String diffData) {
        try {
            File tempPatchFile = File.createTempFile("failedDiff", ".patch");
            FileUtils.saveToFile(tempPatchFile, diffData);
            throw new FatalException("Failed to apply diff cleanly, saved diff to temp file {}!\n{}",
                    tempPatchFile.getPath(), applyOutput);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }
}
