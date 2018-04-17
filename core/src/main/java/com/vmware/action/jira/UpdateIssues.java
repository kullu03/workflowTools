package com.vmware.action.jira;

import java.util.List;

import com.vmware.action.base.BaseBatchJiraAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;
import com.vmware.http.exception.NotFoundException;
import com.vmware.jira.domain.Issue;

@ActionDescription("Bulk updates Jira issues.")
public class UpdateIssues extends BaseBatchJiraAction {

    public UpdateIssues(WorkflowConfig config) {
        super(config);
    }

    @Override
    public String cannotRunAction() {
        if (projectIssues.getIssuesFromJira().isEmpty()) {
            return "there are no issues loaded from Jira";
        }
        return super.cannotRunAction();
    }

    @Override
    public void process() {
        List<Issue> issuesFromJira = projectIssues.getIssuesFromJira();
        log.info("Updating {} issues", issuesFromJira.size());

        for (Issue issueToUpdate : issuesFromJira) {
            try {
                jira.updateIssue(issueToUpdate);
                log.debug("Updated issue {}", issueToUpdate.getKey());
            } catch (NotFoundException e) {
                // ignore if the issue does not exist anymore in JIRA
                log.info("Ignoring missing issue '{}'", issueToUpdate.getKey());
            }
        }
    }

}
