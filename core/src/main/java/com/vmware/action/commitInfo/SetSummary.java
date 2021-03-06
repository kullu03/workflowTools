package com.vmware.action.commitInfo;

import com.vmware.action.base.BaseCommitAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;
import com.vmware.util.input.InputUtils;
import com.vmware.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;

@ActionDescription("Sets the summary field. Replaces existing value if there is one.")
public class SetSummary extends BaseCommitAction {

    public SetSummary(WorkflowConfig config) {
        super(config);
    }

    @Override
    public String cannotRunAction() {
        if (!commitConfig.setEmptyPropertiesOnly || StringUtils.isBlank(draft.summary)) {
            return super.cannotRunAction();
        }

        return "setEmptyPropertiesOnly is set to true and Summary has a value";
    }

    @Override
    public void process() {
        if (StringUtils.isNotBlank(draft.summary)) {
            log.info("Existing Summary\n" + draft.summary);
        }

        String topic = InputUtils.readData("Topic (defaults to " + commitConfig.defaultTopic + " if none set)",
                true, 20, commitConfig.topicTemplates);
        topic = topic.isEmpty() ? commitConfig.defaultTopic : topic;
        draft.summary = topic + ": " + InputUtils.readData("Enter Summary", true, commitConfig.maxSummaryLength - (topic.length() + 2));
    }
}
