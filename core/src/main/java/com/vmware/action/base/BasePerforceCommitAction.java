package com.vmware.action.base;

import com.vmware.config.WorkflowConfig;
import com.vmware.util.scm.Perforce;

public abstract class BasePerforceCommitAction extends BaseCommitAction {

    protected Perforce perforce;

    public BasePerforceCommitAction(WorkflowConfig config) {
        super(config);
        super.setExpectedCommandsToBeAvailable("p4");
        super.failIfCannotBeRun = true;
    }

    @Override
    public String failWorkflowIfConditionNotMet() {
        String reasonForFailing = perforceClientCannotBeUsed();
        if (reasonForFailing != null) {
            return reasonForFailing;
        }
        return super.failWorkflowIfConditionNotMet();
    }

    @Override
    public void preprocess() {
        this.perforce = serviceLocator.getPerforce();
    }
}
