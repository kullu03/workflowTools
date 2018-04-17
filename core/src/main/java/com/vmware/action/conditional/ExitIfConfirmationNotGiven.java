package com.vmware.action.conditional;

import com.vmware.action.BaseAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;
import com.vmware.util.input.InputUtils;

@ActionDescription("Exit if the user does not confirm, can be used for confirming commit changes.")
public class ExitIfConfirmationNotGiven extends BaseAction {

    public ExitIfConfirmationNotGiven(WorkflowConfig config) {
        super(config);
    }

    @Override
    public void process() {
        log.info("");
        String confirmation = InputUtils.readValue("Confirm (press ENTER to confirm or type NO to cancel)");
        if (confirmation.equalsIgnoreCase("NO")) {
            log.info("");
            log.info("Canceling!");
            System.exit(0);
        }
    }
}
