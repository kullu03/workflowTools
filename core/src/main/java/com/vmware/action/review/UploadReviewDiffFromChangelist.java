package com.vmware.action.review;

import static java.lang.String.format;

import java.io.File;

import com.vmware.action.base.BaseLinkedPerforceCommitAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;
import com.vmware.util.CommandLineUtils;
import com.vmware.util.logging.LogLevel;

@ActionDescription("Uses rbt post to upload a changelist as a diff to reviewboard, only for perforce.")
public class UploadReviewDiffFromChangelist extends BaseLinkedPerforceCommitAction {

    public UploadReviewDiffFromChangelist(WorkflowConfig config) {
        super(config);
        super.setExpectedCommandsToBeAvailable("rbt");
        super.failIfCannotBeRun = true;
    }

    @Override
    public void process() {
        File clientDirectory = serviceLocator.getPerforce().getWorkingDirectory();
        String command = format("rbt post -r %s %s", draft.id, draft.perforceChangelistId);
        String output = CommandLineUtils.executeCommand(clientDirectory, command, null, LogLevel.INFO);
        if (!output.contains("Review request #" + draft.id + " posted")) {
            log.error("Failed to upload diff successfully\n{}", output);
        }
    }
}
